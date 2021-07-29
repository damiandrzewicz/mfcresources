package de.atos.solumversion.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CommitInfoDTO {

    private long newRevision;

    private String commitAuthor;
}
