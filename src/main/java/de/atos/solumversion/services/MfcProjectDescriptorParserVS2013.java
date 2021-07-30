package de.atos.solumversion.services;

import de.atos.solumversion.domain.MfcProjectDescriptor;
import de.atos.solumversion.utils.LineFileReader;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MfcProjectDescriptorParserVS2013 implements MfcProjectDescriptorParser{

    private MfcProjectDescriptor mfcProjectDescriptor = new MfcProjectDescriptor();

    public MfcProjectDescriptorParserVS2013(MfcProjectDescriptor mfcProjectDescriptor) {
        this.mfcProjectDescriptor = mfcProjectDescriptor;
    }

    @Override
    public MfcProjectDescriptor parseDescriptor(File descriptor) {


        LineFileReader.read(descriptor, line -> {
            String pattern = "Project\\(\\\".+\\\"\\) = \\\"(\\w+)\\\", \\\"(\\w+\\\\\\w+\\.\\w+)\\\".+";
            Pattern p = Pattern.compile(pattern);
            Matcher m = p.matcher(line);
            if(m.find()){
                String projectName = m.group(1);
                String projectPathRelativeToRoot = m.group(2);
                mfcProjectDescriptor.getProjects().put(projectName, projectPathRelativeToRoot);
            }
            return true;
        });

        return mfcProjectDescriptor;

    }
}
