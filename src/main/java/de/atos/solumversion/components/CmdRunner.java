package de.atos.solumversion.components;

import de.atos.solumversion.configuration.WorkingCopyDirectoryConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
@Slf4j
public class CmdRunner implements CommandLineRunner {

    private WorkingCopyDirectoryConfig workingCopyDirectoryConfig;

    public CmdRunner(WorkingCopyDirectoryConfig workingCopyDirectoryConfig) {
        this.workingCopyDirectoryConfig = workingCopyDirectoryConfig;
    }

    @Override
    public void run(String... args) throws Exception {
        String rootDirectory = workingCopyDirectoryConfig.getRootDirectory();

        log.debug("Checking 'wc.rootDirectory' property...");
        if(rootDirectory.isEmpty() || rootDirectory.isBlank()){
            throw new RuntimeException("Missing root working copy directory configuration! Please set 'wc.rootDirectory' property.");
        }

        File wcDir = new File(rootDirectory);
        log.debug("Creating 'wc.rootDirectory'={}", wcDir.getAbsolutePath());
        if(!wcDir.exists()){
            if(wcDir.mkdirs()){
                log.debug("Created 'wc.rootDirectory'={}", wcDir.getAbsolutePath());
            }
            else{

                throw new RuntimeException(String.format("Cannot create 'wc.rootDirectory'={}", wcDir.getAbsolutePath()));
            }
        }
    }
}
