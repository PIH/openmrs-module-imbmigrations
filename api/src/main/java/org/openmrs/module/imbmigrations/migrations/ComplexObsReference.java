package org.openmrs.module.imbmigrations.migrations;

import java.io.File;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.openmrs.util.OpenmrsUtil;

/**
 * Object that can hold a reference to a particular Obs uuid and value_complex
 * as well as a pointer to the file containing the complex data that needs to be migrated
 */
public class ComplexObsReference {

	private String obsUuid;
	private Integer patientId;
	private Date encounterDate;
	private String initialValueComplex;

	public ComplexObsReference(String obsUuid, Integer patientId, Date encounterDate, String initialValueComplex) {
		this.obsUuid = obsUuid;
		this.patientId = patientId;
		this.encounterDate = encounterDate;
		this.initialValueComplex = initialValueComplex;
	}

	public String getInitialFileName() {
		String[] names = initialValueComplex.split("\\|");
		return names.length == 1 ? names[0] : names[names.length - 1];
	}

	public String getFileExtension() {
		String[] filenameParts = getInitialFileName().split("\\.");
		String fileExtension = (filenameParts.length < 2) ? filenameParts[0] : filenameParts[filenameParts.length - 1];
		fileExtension = StringUtils.isNotBlank(fileExtension) ? fileExtension : "raw";
		return fileExtension;
	}

	public String getFileNameBase() {
		String fileNameBase = getInitialFileName().replace("." + getFileExtension(), "");
		fileNameBase = fileNameBase.replace("'", "");
		fileNameBase = fileNameBase.replace("\"", "");
		return fileNameBase;
	}

	public String getTargetFileName() {
		if (getInitialFileName().contains(obsUuid)) {
			return getInitialFileName();
		}
		String base = (StringUtils.isNotBlank(getFileNameBase()) ? getFileNameBase() + "_" : "");
		return base + obsUuid + "." + getFileExtension();
	}

	public String getTargetValueComplex() {
		String targetFileName = getTargetFileName();
		return targetFileName + " file |" + targetFileName;
	}

	public File getInitialFile() {
		File dir = OpenmrsUtil.getDirectoryInApplicationDataDirectory("complex_obs");
		return new File(dir, getInitialFileName());
	}

	public File getTargetFile() {
		File dir = OpenmrsUtil.getDirectoryInApplicationDataDirectory("complex_obs");
		return new File(dir, getTargetFileName());
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("uuid: ").append(obsUuid).append(", ");
		sb.append("initialValueComplex: ").append(initialValueComplex).append(", ");
		sb.append("initialFileName: ").append(getInitialFileName()).append(", ");
		sb.append("fileNameBase: ").append(getFileNameBase()).append(", ");
		sb.append("fileExtension: ").append(getFileExtension()).append(", ");
		sb.append("targetValueComplex: ").append(getTargetValueComplex()).append(", ");
		sb.append("targetFileName: ").append(getTargetFileName());
		return sb.toString();
	}

	public String getObsUuid() {
		return obsUuid;
	}

	public void setObsUuid(String obsUuid) {
		this.obsUuid = obsUuid;
	}

	public Integer getPatientId() {
		return patientId;
	}

	public void setPatientId(Integer patientId) {
		this.patientId = patientId;
	}

	public Date getEncounterDate() {
		return encounterDate;
	}

	public void setEncounterDate(Date encounterDate) {
		this.encounterDate = encounterDate;
	}

	public String getInitialValueComplex() {
		return initialValueComplex;
	}

	public void setInitialValueComplex(String initialValueComplex) {
		this.initialValueComplex = initialValueComplex;
	}
}
