package de.atos.solumversion.services;

import de.atos.solumversion.domain.MfcProjectDescriptor;
import lombok.Getter;

import java.io.File;

@Getter
public class MfcProjectDescriptorParserVS600 implements MfcProjectDescriptorParser{

    private MfcProjectDescriptor mfcProjectDescriptor = new MfcProjectDescriptor();

    public MfcProjectDescriptorParserVS600(MfcProjectDescriptor mfcProjectDescriptor) {
        this.mfcProjectDescriptor = mfcProjectDescriptor;
    }

    @Override
    public MfcProjectDescriptor parseDescriptor(File descriptor) {

         // Project: \"\w*\"=.(.*) -
        return null;
    }
}
