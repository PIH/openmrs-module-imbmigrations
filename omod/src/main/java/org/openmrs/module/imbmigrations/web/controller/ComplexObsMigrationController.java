package org.openmrs.module.imbmigrations.web.controller;

import java.util.List;

import org.openmrs.module.imbmigrations.migrations.ComplexObsMigrator;
import org.openmrs.module.imbmigrations.migrations.ComplexObsReference;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ComplexObsMigrationController {

	@ModelAttribute("migrator")
	public ComplexObsMigrator getComplexObsMigrator() {
		return new ComplexObsMigrator();
	}

	@RequestMapping(value = "/module/imbmigrations/migrateComplexObs", method = RequestMethod.GET)
	public void listMigrations(ModelMap model) {
	}

	@RequestMapping(value = "/module/imbmigrations/migrateAllObs", method = RequestMethod.POST)
	public String migrateAllObs() throws Exception {
		getComplexObsMigrator().migrate();
		return "redirect:/module/imbmigrations/migrateComplexObs.list";
	}

	@RequestMapping(value = "/module/imbmigrations/migrateComplexObsWithSameValueComplex", method = RequestMethod.GET)
	public void listComplexObsWithSameValueComplex(ModelMap model) {
	}

	@RequestMapping(value = "/module/imbmigrations/migrateComplexObsWithSameValueComplex", method = RequestMethod.POST)
	public String migrateComplexObsWithSameValueComplex(@RequestParam(value="uuid") String uuid) throws Exception {
		for (List<ComplexObsReference> refs : getComplexObsMigrator().getObsWithSameValueComplex().values()) {
			for (ComplexObsReference ref : refs) {
				if (ref.getObsUuid().equalsIgnoreCase(uuid)) {
					getComplexObsMigrator().updateComplexObs(ref);
				}
			}
		}
		return "redirect:/module/imbmigrations/migrateComplexObsWithSameValueComplex.list";
	}
}
