package de.atos.solumversion.services;

import de.atos.solumversion.dto.SvnItemDTO;

public interface MfcProjectService {

    SvnItemDTO fetch(SvnItemDTO svnItemDTO) throws MfcProjectServiceException;

//    void remove(SvnItemDTO svnItemDTO);
//
//    SvnItemDTO update(SvnItemDTO svnItemDTO);
//
//    CommitDTO commit(CommitDTO commitDTO);

}
