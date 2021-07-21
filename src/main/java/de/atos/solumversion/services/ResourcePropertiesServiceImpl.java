package de.atos.solumversion.services;

import de.atos.solumversion.domain.ResourceProperties;

import java.io.File;

public class ResourcePropertiesServiceImpl implements ResourcePropertiesService{
    @Override
    public ResourceProperties parseResourceProperties(File resourceFile) throws ResourcePropertiesServiceException {
        if(!resourceFile.exists()){
            throw new ResourcePropertiesServiceException(String.format("Resource file not exists: [%s]", resourceFile.getPath()));
        }

        return null;
    }
}
