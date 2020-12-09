package org.openmrs.module.imbmigrations.web.controller;

import org.openmrs.module.imbmigrations.migrations.ComplexObsMigrator;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

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

	@RequestMapping(value = "/module/imbmigrations/migrateComplexObs", method = RequestMethod.POST)
	public String migrateComplexObs() throws Exception {

		return "redirect:/module/imbmigrations/migrateComplexObs.list";
	}
}
