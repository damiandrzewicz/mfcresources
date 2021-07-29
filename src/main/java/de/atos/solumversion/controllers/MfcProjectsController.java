package de.atos.solumversion.controllers;

import de.atos.solumversion.dto.MfcProjectDTO;
import de.atos.solumversion.services.MfcProjectService;
import de.atos.solumversion.services.MfcProjectServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("api/v1/projects")
public class MfcProjectsController {

    private MfcProjectService mfcProjectService;

    public MfcProjectsController(MfcProjectService mfcProjectService) {
        this.mfcProjectService = mfcProjectService;
    }


    @PostMapping
    public MfcProjectDTO init(@RequestBody MfcProjectDTO mfcProjectDTO) throws MfcProjectServiceException {
        return mfcProjectService.init(mfcProjectDTO);
    }

    @GetMapping
    public List<MfcProjectDTO> getProjects() throws MfcProjectServiceException {
        return mfcProjectService.getProjects();
    }

    @DeleteMapping
    public void remove(@RequestBody MfcProjectDTO mfcProjectDTO) {
        mfcProjectService.remove(mfcProjectDTO);
        //mfcProjectService.removeWC(mfcProjectDTO);
    }


}
