package de.atos.solumversion.services;

import de.atos.solumversion.domain.MfcProjectDescriptor;

import java.io.File;

public interface MfcProjectDescriptorParser {

    MfcProjectDescriptor parseDescriptor(File descriptor);
}
