package de.atos.solumversion.services;

import de.atos.solumversion.domain.MfcResourceProperties;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public interface MfcResourceParserService {

    MfcResourceProperties parseResourceProperties(File resourceFile) throws MfcResourceParserServiceException;

    void updateResourceProperties(File resourceFile, MfcResourceProperties mfcResourceProperties) throws MfcResourceParserServiceException;
}
