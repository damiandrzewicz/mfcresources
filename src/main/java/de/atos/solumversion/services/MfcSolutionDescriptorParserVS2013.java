package de.atos.solumversion.services;

import de.atos.solumversion.domain.MfcSolutionDescriptor;
import de.atos.solumversion.utils.LineFileReader;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MfcSolutionDescriptorParserVS2013 implements MfcSolutionDescriptorParser {

    private MfcSolutionDescriptor mfcSolutionDescriptor = new MfcSolutionDescriptor();

    public MfcSolutionDescriptorParserVS2013(MfcSolutionDescriptor mfcSolutionDescriptor) {
        this.mfcSolutionDescriptor = mfcSolutionDescriptor;
    }

    @Override
    public MfcSolutionDescriptor parseDescriptor(File descriptor) {


        LineFileReader.read(descriptor, line -> {
            String pattern = "Project\\(\\\".+\\\"\\) = \\\"(\\w+)\\\", \\\"(.*)\\\",.*";
            Pattern p = Pattern.compile(pattern);
            Matcher m = p.matcher(line);
            if(m.find()){
                String projectName = m.group(1);
                String projectPathRelativeToRoot = m.group(2);
                mfcSolutionDescriptor.getProjects().put(projectName, projectPathRelativeToRoot);
            }
            return true;
        });

        return mfcSolutionDescriptor;

    }

    @Override
    public MfcProjectDescriptorParser createProjectDescriptorParser() {
        return new MfcProjectDescriptorParserVS2013();
    }
}
