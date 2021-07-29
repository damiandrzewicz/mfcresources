package de.atos.solumversion.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.atos.solumversion.domain.MfcResourceProperties;
import de.atos.solumversion.domain.SvnInfo;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class MfcResourceDTO {

    private SvnInfo svnInfo = new SvnInfo();

    private String projectName;

    private String resourceNameWithExtension;

    private MfcResourceProperties mfcResourceProperties = new MfcResourceProperties();

    @JsonIgnore
    public String getResourceName(){
        String[] split = this.resourceNameWithExtension.split(".");
        if(split.length == 2){
            return split[0];
        } else {
            throw new IllegalArgumentException(String.format("Wrong resource name: %s", this.resourceNameWithExtension));
        }
    }
}
