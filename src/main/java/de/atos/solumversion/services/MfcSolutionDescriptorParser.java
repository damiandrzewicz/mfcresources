package de.atos.solumversion.services;

import de.atos.solumversion.domain.MfcSolutionDescriptor;

import java.io.File;

public interface MfcSolutionDescriptorParser {

    MfcSolutionDescriptor parseDescriptor(File descriptor);

    MfcProjectDescriptorParser createProjectDescriptorParser();
}
