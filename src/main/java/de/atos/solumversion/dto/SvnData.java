package de.atos.solumversion.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class SvnData {

    private String target;

    private String name;

    private long revision;

    public boolean validateAllowedTarget(){
        if(target.startsWith("http")){
            return true;
        } else{
            return false;
        }
    }
}
