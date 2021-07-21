package de.atos.solumversion.controllers;

import de.atos.solumversion.dto.ProjectDTO;
import de.atos.solumversion.dto.ResourceDTO;
import de.atos.solumversion.exceptions.AuthException;
import de.atos.solumversion.services.MfcProjectService;
import de.atos.solumversion.services.MfcProjectServiceException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/projects")
public class ProjectsController {

    private MfcProjectService mfcProjectService;

    public ProjectsController(MfcProjectService mfcProjectService) {
        this.mfcProjectService = mfcProjectService;
    }

    @PostMapping
    public ProjectDTO fetchProject(@RequestBody ProjectDTO projectDTO) throws AuthException, MfcProjectServiceException {
        return mfcProjectService.fetchProject(projectDTO);
    }

    @PutMapping
    public ProjectDTO updateWholeWorkingCopyProject(@RequestBody ProjectDTO projectDTO) throws MfcProjectServiceException {
        return mfcProjectService.updateWholeWorkingCopyProject(projectDTO);
    }

    @GetMapping
    public List<ProjectDTO> getWorkingCopyProjects() throws MfcProjectServiceException {
        return mfcProjectService.getWorkingCopyProjects();
    }

    @GetMapping("/resources")
    public List<ResourceDTO> getProjectResources(ProjectDTO projectDTO) throws MfcProjectServiceException {
        return mfcProjectService.getProjectResources(projectDTO);
    }
}
