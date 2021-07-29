package de.atos.solumversion.services;

import de.atos.solumversion.dto.MfcProjectDTO;

import java.util.List;

public interface MfcProjectService {

    MfcProjectDTO init(MfcProjectDTO mfcProjectDTO) throws MfcProjectServiceException;

    List<MfcProjectDTO> getProjects() throws MfcProjectServiceException;

    void remove(MfcProjectDTO mfcProjectDTO);

}
