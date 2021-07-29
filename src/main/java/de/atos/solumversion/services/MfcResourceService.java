package de.atos.solumversion.services;

import de.atos.solumversion.dto.CommitDTO;
import de.atos.solumversion.dto.CommitInfoDTO;
import de.atos.solumversion.dto.MfcProjectDTO;
import de.atos.solumversion.dto.MfcResourceDTO;

import java.util.List;

public interface MfcResourceService {

    List<MfcResourceDTO> getResources(MfcProjectDTO mfcProjectDTO) throws MfcResourceServiceException;

    void updateResources(List<MfcResourceDTO> mfcResourceDTO);

    CommitInfoDTO commit(CommitDTO commitDTO);

    void update(List<String> urls) throws MfcResourceServiceException;

    void revert(List<String> urls);
}
