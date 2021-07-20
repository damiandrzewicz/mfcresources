package de.atos.solumversion.controllers;

import de.atos.solumversion.dto.SvnTargetDTO;
import de.atos.solumversion.exceptions.AuthException;
import de.atos.solumversion.services.MfcProjectService;
import de.atos.solumversion.services.MfcProjectServiceException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/projects")
public class ProjectsController {

    private MfcProjectService mfcProjectService;

    public ProjectsController(MfcProjectService mfcProjectService) {
        this.mfcProjectService = mfcProjectService;
    }

    @PostMapping
    public SvnTargetDTO fetch(@RequestBody SvnTargetDTO svnItemDTO) throws AuthException, MfcProjectServiceException {
        mfcProjectService.fetch(svnItemDTO);
        throw new AuthException("auth exception");
    }
}
