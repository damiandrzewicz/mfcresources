package de.atos.solumversion.services;

import de.atos.solumversion.domain.MfcSolutionDescriptor;
import lombok.Getter;

import java.io.File;

@Getter
public class MfcSolutionDescriptorParserVS600 implements MfcSolutionDescriptorParser {

    private MfcSolutionDescriptor mfcSolutionDescriptor = new MfcSolutionDescriptor();

    public MfcSolutionDescriptorParserVS600(MfcSolutionDescriptor mfcSolutionDescriptor) {
        this.mfcSolutionDescriptor = mfcSolutionDescriptor;
    }

    @Override
    public MfcSolutionDescriptor parseDescriptor(File descriptor) {

         // Project: \"\w*\"=.(.*) -
        return null;
    }

    @Override
    public MfcProjectDescriptorParser createProjectDescriptorParser() {
        return null;
    }
}
