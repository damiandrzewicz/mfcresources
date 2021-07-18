package de.atos.solumversion.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "wc")
@Setter
@Getter
public class WorkingCopyDirectoryConfig {

    private String rootDirectory;

}
