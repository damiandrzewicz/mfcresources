package de.atos.solumversion.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ResourceDTO {

    private SvnData svnData = new SvnData();

    private String productVersion;

    private String fileVersion;

    private String companyName;

    private String fileDescription;

    private String internalName;

    private String legalCopyright;

    private String originalFilename;

    private String productName;

    private String specialBuild;
}
