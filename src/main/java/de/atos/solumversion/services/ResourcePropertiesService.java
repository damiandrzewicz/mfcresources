package de.atos.solumversion.services;

import de.atos.solumversion.domain.ResourceProperties;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public interface ResourcePropertiesService {

    ResourceProperties parseResourceProperties(File resourceFile) throws ResourcePropertiesServiceException;
}
