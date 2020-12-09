package org.openmrs.module.imbmigrations.migrations;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This migration exists to update all complex obs file names such that the naming is consistent with what
 * is done in the most recent versions of OpenMRS (2.3.x) and which does not have limitations around multiple obs
 * given the same titles on either the same server or across a network of sync'd servers.
 */
public class ComplexObsMigrator {

	private static final Logger log = LoggerFactory.getLogger(ComplexObsMigrator.class);

	private Map<String, List<ComplexObsReference>> complexObsValueMap = new HashMap<String, List<ComplexObsReference>>();
	private Set<String> unmatchedComplexObsFiles = new HashSet<String>();
	private List<ComplexObsReference> alreadyMigrated = new ArrayList<ComplexObsReference>();
	private List<ComplexObsReference> bulkMigration = new ArrayList<ComplexObsReference>();
	private Map<String, List<ComplexObsReference>> multipleObsWithSameValue = new LinkedHashMap<String, List<ComplexObsReference>>();
	private List<ComplexObsReference> obsMissingDataFiles = new ArrayList<ComplexObsReference>();

	public ComplexObsMigrator() {

		// First get all of the complex_obs from the file system in order to track which are not found in value_complex
		File[] complexObsFiles = getComplexObsDir().listFiles();
		if (complexObsFiles != null) {
			for (File f : complexObsFiles) {
				unmatchedComplexObsFiles.add(f.getName());
			}
		}

		// Next get all of the complex obs from the DB
		AdministrationService as = Context.getAdministrationService();
		StringBuilder query = new StringBuilder();
		query.append("select o.uuid, o.obs_id, o.person_id, e.encounter_datetime, o.value_complex ");
		query.append("from   obs o left join encounter e on o.encounter_id = e.encounter_id ");
		query.append("where  o.value_complex is not null ");
		List<List<Object>> rows = as.executeSQL(query.toString(), true);
		for (List<Object> row : rows) {
			String uuid = (String) row.get(0);
			Integer obsId = (Integer) row.get(1);
			Integer patientId = (Integer) row.get(2);
			Date encounterDate = (Date) row.get(3);
			String valueComplex = (String) row.get(4);
			ComplexObsReference reference = new ComplexObsReference(uuid, obsId, patientId, encounterDate, valueComplex);
			List<ComplexObsReference> byValue = complexObsValueMap.get(valueComplex);
			if (byValue == null) {
				byValue = new ArrayList<ComplexObsReference>();
				complexObsValueMap.put(valueComplex, byValue);
			}
			byValue.add(reference);
			unmatchedComplexObsFiles.remove(reference.getInitialFileName());
		}

		for (String valueComplex : complexObsValueMap.keySet()) {
			List<ComplexObsReference> refs = complexObsValueMap.get(valueComplex);
			if (refs.size() > 1) {
				ComplexObsReference firstRef = refs.get(0);
				if (!firstRef.getInitialFile().exists()) {
					obsMissingDataFiles.addAll(refs);
				}
				else {
					multipleObsWithSameValue.put(valueComplex, refs);
				}
			}
			else {
				ComplexObsReference ref = refs.get(0);
				if (ref.getTargetFile().exists() && ref.getTargetFileName().contains(ref.getObsUuid())) {
					alreadyMigrated.add(ref);
				} else {
					if (ref.getInitialFile().exists()) {
						bulkMigration.add(ref);
					}
					else {
						obsMissingDataFiles.add(ref);
					}
				}
			}
		}
	}

	public List<ComplexObsReference> getValuesAlreadyMigrated() {
		return alreadyMigrated;
	}

	public List<ComplexObsReference> getValuesForBulkMigration() {
		return bulkMigration;
	}

	public Map<String, List<ComplexObsReference>> getObsWithSameValueComplex() {
		return multipleObsWithSameValue;
	}

	public Map<String, Integer> getMigrationStatus() {
		Map<String, Integer> m = new LinkedHashMap<String, Integer>();
		m.put("Already Migrated", alreadyMigrated.size());
		m.put("Can Migrate in Bulk", bulkMigration.size());
		m.put("Value Complex shared by multiple obs", multipleObsWithSameValue.size());
		m.put("Obs with no data file", obsMissingDataFiles.size());
		m.put("Data file with no obs", unmatchedComplexObsFiles.size());
		return m;
	}

	public void migrate() {
		log.warn("Bulk Migration started.  There are " + bulkMigration.size() + " complex obs to migrate");
		int numMigrated = 0;
		for (ComplexObsReference ref : bulkMigration) {
			updateComplexObs(ref);
			numMigrated++;
			if (numMigrated % 10 == 0) {
				log.warn("Migrated " + numMigrated);
			}
		}
		log.warn("Migration completed for " + numMigrated + " complex obs");
	}

	public void updateComplexObs(ComplexObsReference ref) {
		boolean initialFileExists = false;
		boolean fileNameUpdated = false;
		boolean valueComplexUpdated = false;
		boolean fileNameUpdateReverted = false;
		String errorMessage = "";

		// First, ensure the existing complex obs file exists
		File initialFile = ref.getInitialFile();
		initialFileExists = initialFile.exists();

		if (initialFileExists) {

			// First, move/rename the file
			File targetFile = ref.getTargetFile();
			try {
				FileUtils.moveFile(initialFile, targetFile);
				fileNameUpdated = targetFile.exists();
			}
			catch (Exception e) {
				errorMessage = e.getMessage();
			}

			// If the file name has been updated, update the obs value complex
			if (fileNameUpdated) {
				StringBuilder updateStmt = new StringBuilder();
				updateStmt.append("update obs ");
				updateStmt.append("set value_complex = '" + ref.getTargetValueComplex() + "' ");
				updateStmt.append("where uuid = '" + ref.getObsUuid() + "'");
				try {
					Context.getAdministrationService().executeSQL(updateStmt.toString(), false);
					valueComplexUpdated = true;
				}
				catch (Exception e) {
					errorMessage = e.getMessage();
				}

				// If there was an error updating the value complex, then revert the file name change
				if (!valueComplexUpdated) {
					try {
						FileUtils.moveFile(targetFile, initialFile);
						fileNameUpdateReverted = initialFile.exists();
					}
					catch (Exception e) {
						if (StringUtils.isBlank(errorMessage)) {
							errorMessage = e.getMessage();
						}
					}
				}
			}
		}
		StringBuilder line = new StringBuilder();
		line.append("\"").append(ref.getObsUuid()).append("\"").append(",");
		line.append("\"").append(ref.getInitialValueComplex()).append("\"").append(",");
		line.append("\"").append(ref.getPatientId()).append("\"").append(",");
		line.append("\"").append(ref.getEncounterDate()).append("\"").append(",");
		line.append("\"").append(ref.getInitialFileName()).append("\"").append(",");
		line.append("\"").append(ref.getFileExtension()).append("\"").append(",");
		line.append("\"").append(ref.getFileNameBase()).append("\"").append(",");
		line.append("\"").append(ref.getTargetFileName()).append("\"").append(",");
		line.append("\"").append(ref.getTargetValueComplex()).append("\"").append(",");
		line.append("\"").append(initialFileExists).append("\"").append(",");
		line.append("\"").append(fileNameUpdated).append("\"").append(",");
		line.append("\"").append(valueComplexUpdated).append("\"").append(",");
		line.append("\"").append(fileNameUpdateReverted).append("\"").append(",");
		line.append("\"").append(errorMessage).append("\"");
		writeToLog(line.toString());
	}

	private void writeToLog(String logLine) {
		FileWriter fw = null;
		BufferedWriter bw = null;
		PrintWriter pw = null;
		try {
			File logDir = OpenmrsUtil.getDirectoryInApplicationDataDirectory("complex_obs");
			fw = new FileWriter(new File(logDir, "update-log.csv"), true);
			bw = new BufferedWriter(fw);
			pw = new PrintWriter(bw);
			pw.println(logLine);
		}
		catch (IOException e) {
			log.error("Error writing to log", e);
		}
		finally {
			IOUtils.closeQuietly(pw);
			IOUtils.closeQuietly(bw);
			IOUtils.closeQuietly(fw);
		}
	}

	private File getComplexObsDir() {
		return OpenmrsUtil.getDirectoryInApplicationDataDirectory("complex_obs");
	}
}
