package de.atos.solumversion.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class SvnInfo {

    private String url;

    private long revision;

    private String lastCommitAuthor;

    private boolean isOutdated = false;

    private boolean hasLocalModifications = false;

}
