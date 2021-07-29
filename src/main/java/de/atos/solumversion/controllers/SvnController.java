package de.atos.solumversion.controllers;

import de.atos.solumversion.dto.LoginDTO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController("api/v1/svn")
public class SvnController {

    @PostMapping("/login")
    void login(@RequestBody LoginDTO loginDTO){

    }

    void logout(){

    }
}
