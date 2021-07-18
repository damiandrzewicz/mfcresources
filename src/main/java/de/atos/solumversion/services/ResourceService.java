package de.atos.solumversion.services;

import de.atos.solumversion.dto.CommitDTO;
import de.atos.solumversion.dto.MfcResourceDTO;
import de.atos.solumversion.dto.SvnItemDTO;

import java.util.List;

public interface ResourceService {

    List<MfcResourceDTO> fetch(SvnItemDTO svnItemDTO);

    CommitDTO commit(CommitDTO commitDTO);
}
