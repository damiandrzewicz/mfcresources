package de.atos.solumversion.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Setter
@Getter
public class SubversionFile {

    private String name;

    private String extension;

    private Date created;

    private String url;

    private String repositoryRoot;

    private String workingCopy;

    public String getNameWithExtension(){
        return name + "." + extension;
    }

    public void setNameWithExtension(String nameWithExtension){
        String[] tokens = nameWithExtension.split("\\.(?=[^\\.]+$)");
        if(tokens.length == 1){
            throw new IllegalArgumentException("Missing name or extension");
        }

        String name = tokens[0];
        if(name.isEmpty() || name.isBlank()){
            throw new IllegalArgumentException("Missing name");
        }

        String extension = tokens[1];
        if(extension.isEmpty() || extension.isBlank()){
            throw new IllegalArgumentException("Missing extension");
        }

        this.name = name;
        this.extension = extension;
    }
}
