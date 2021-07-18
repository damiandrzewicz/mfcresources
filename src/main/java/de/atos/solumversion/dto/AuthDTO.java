package de.atos.solumversion.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;


@Setter
@Getter
public class AuthDTO {

    @NotNull
    @NotBlank
    private String user;

    @NotNull
    @NotBlank
    private String password;
}
