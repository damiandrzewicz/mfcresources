package de.atos.solumversion.domain;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Setter
@Getter
public class SubversionDirectory {

    private String name;

    private Date created;

    private String url;

    private String repositoryRoot;

    private String workingCopy;
}
