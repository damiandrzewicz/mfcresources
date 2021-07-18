package de.atos.solumversion.controllers;

import de.atos.solumversion.domain.MfcSubversionProject;
import de.atos.solumversion.domain.SubversionDirectory;
import de.atos.solumversion.dto.AuthDTO;
import de.atos.solumversion.exceptions.WorkingCopyDirectoryException;
import de.atos.solumversion.services.MfcSubversionService;
import de.atos.solumversion.services.MfcSubversionServiceException;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/repo")
public class MfcProjectController {

    private MfcSubversionService subversionService;

    public MfcProjectController(MfcSubversionService subversionService) {
        this.subversionService = subversionService;
    }

    @PostMapping("/auth")
    public void authenticate(@RequestBody @Valid AuthDTO authDTO){
        subversionService.authentiate(authDTO.getUser(), authDTO.getPassword());
    }

    @GetMapping("/projects")
    public List<SubversionDirectory> listSavedProjects() throws WorkingCopyDirectoryException {
        return subversionService.workingCopyProjects();
    }

    @PostMapping("/projects")
    public MfcSubversionProject load(@RequestBody @Valid MfcSubversionProject project) throws MfcSubversionServiceException {
        subversionService.init(project.getProjectDirectory().getUrl());
        return subversionService.loadProject();
    }


}
