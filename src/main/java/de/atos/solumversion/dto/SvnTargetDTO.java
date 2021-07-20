package de.atos.solumversion.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class SvnTargetDTO {

    public enum Type{
        URL, FILE;
    }

    private String target;

    public Type getType(){
        if(target.startsWith("http")){
            return Type.URL;
        } else{
            return Type.FILE;
        }
    }

    public String getTarget(){
        if(getType().equals(Type.URL)){
            return target;
        } else {
            return target.replace("file:", "");
        }

    }
}
