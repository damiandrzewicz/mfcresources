package de.atos.solumversion.domain;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.io.FilenameUtils;

@Getter
@Setter
public class MfcProjectDescriptor {

    public enum ConfigurationType{
        Application("Application"),
        DynamicLibrary("DynamicLibrary"),
        StaticLibrary("StaticLibrary"),
        Makefile("Makefile");

        private String value;

        ConfigurationType(String value){
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    @Setter
    @Getter
    public class Configuration{

        private ConfigurationType configurationType;

        public boolean isApplication(){
            return this.configurationType.equals(ConfigurationType.Application);
        }

        public boolean isStaticLibrary(){
            return this.configurationType.equals(ConfigurationType.StaticLibrary);
        }

        public boolean isDynamicLibrary(){
            return this.configurationType.equals(ConfigurationType.DynamicLibrary);
        }
    }

    private Configuration configurationDebug;
    private Configuration configurationRelease;

    private String nameWithExt;

    private String pathRelativeToRoot;

    public String getName(){
        return FilenameUtils.getName(nameWithExt);
    }

    public String getExtension(){
        return FilenameUtils.getExtension(nameWithExt);
    }
}
