package de.atos.solumversion.domain;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class MfcSubversionProject {

    private SubversionDirectory projectDirectory;

    private List<SubversionFile> resources;
}
