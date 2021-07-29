package de.atos.solumversion.controllers;

import de.atos.solumversion.dto.CommitDTO;
import de.atos.solumversion.dto.MfcProjectDTO;
import de.atos.solumversion.dto.MfcResourceDTO;
import de.atos.solumversion.services.MfcResourceService;
import de.atos.solumversion.services.MfcResourceServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("api/v1/resources")
public class MfcResourcesController {

    private MfcResourceService mfcResourceService;

    public MfcResourcesController(MfcResourceService mfcResourceService) {
        this.mfcResourceService = mfcResourceService;
    }

    @GetMapping
    public List<MfcResourceDTO> getResources(@RequestBody MfcProjectDTO mfcProjectDTO) throws MfcResourceServiceException {
        return mfcResourceService.getResources(mfcProjectDTO);
    }

    @PutMapping
    public List<MfcResourceDTO> updateResources(@RequestBody List<MfcResourceDTO> mfcResourceDTOS){
        return mfcResourceService.updateResources(mfcResourceDTOS);
    }

    @PostMapping("/commit")
    public List<MfcResourceDTO>  commit(@RequestBody CommitDTO commitDTO){
        return mfcResourceService.commit(commitDTO);
    }

    @PostMapping("/update")
    public List<MfcResourceDTO> update(@RequestBody List<MfcResourceDTO> mfcResourceDTOS) throws MfcResourceServiceException {
        return mfcResourceService.update(mfcResourceDTOS);
    }

    @PostMapping("/revert")
    public MfcResourceDTO revert(@RequestBody MfcResourceDTO mfcResourceDTO){
        return mfcResourceService.revert(mfcResourceDTO);
    }


}
