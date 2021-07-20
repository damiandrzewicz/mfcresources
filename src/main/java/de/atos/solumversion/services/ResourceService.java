package de.atos.solumversion.services;

import de.atos.solumversion.dto.CommitDTO;
import de.atos.solumversion.dto.MfcResourceDTO;
import de.atos.solumversion.dto.SvnTargetDTO;

import java.util.List;

public interface ResourceService {

    List<MfcResourceDTO> fetch(SvnTargetDTO svnItemDTO);

    CommitDTO commit(CommitDTO commitDTO);
}
