package de.atos.solumversion.controllers;

import de.atos.solumversion.domain.MfcSubversionProject;
import de.atos.solumversion.services.SavedSubversionProjectsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/projects")
public class MfcProjectController {

    private SavedSubversionProjectsService savedSubversionProjectsService;

    @GetMapping("/")
    public MfcSubversionProject listSavedProjects(){

    }
}
