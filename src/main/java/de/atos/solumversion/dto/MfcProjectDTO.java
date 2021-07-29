package de.atos.solumversion.dto;

import de.atos.solumversion.domain.SvnInfo;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class MfcProjectDTO {

    private SvnInfo svnInfo = new SvnInfo();

    private String name;

    private String mfcVersion;

    private String visualStudioVersion;

}
