package de.atos.solumversion.services;

import de.atos.solumversion.dto.SvnTargetDTO;

public interface MfcProjectService {

    SvnTargetDTO fetch(SvnTargetDTO svnItemDTO) throws MfcProjectServiceException;

//    void remove(SvnItemDTO svnItemDTO);
//
//    SvnItemDTO update(SvnItemDTO svnItemDTO);
//
//    CommitDTO commit(CommitDTO commitDTO);

}
