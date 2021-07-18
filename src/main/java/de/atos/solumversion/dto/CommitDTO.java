package de.atos.solumversion.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class CommitDTO {

    private List<SvnItemDTO> items;

    private String message;
}
