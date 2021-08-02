package de.atos.solumversion.components;

import de.atos.solumversion.domain.MfcSolutionDescriptor;
import de.atos.solumversion.services.MfcSolutionDescriptorParser;
import de.atos.solumversion.services.MfcSolutionDescriptorParserVS2013;
import de.atos.solumversion.services.MfcSolutionDescriptorParserVS600;
import de.atos.solumversion.utils.LineFileReader;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class MfcSolutionDescriptorParserFactory {

    public MfcSolutionDescriptorParser create(File descriptor) throws MfcSolutionDescriptorParserFactoryException {

        MfcSolutionDescriptor mfcSolutionDescriptor = new MfcSolutionDescriptor();

        AtomicReference<MfcSolutionDescriptorParser> mfcProjectDescriptorParserRef = new AtomicReference<>();

        int lineNumber = 0;
        LineFileReader.read(descriptor, line -> {

            if(checkVS600(line, mfcSolutionDescriptor)){
                mfcProjectDescriptorParserRef.set(new MfcSolutionDescriptorParserVS600(mfcSolutionDescriptor));
                return false;
            } else if(checkVS2013(line, mfcSolutionDescriptor)){
                mfcProjectDescriptorParserRef.set(new MfcSolutionDescriptorParserVS2013(mfcSolutionDescriptor));
                return false;
            }


            if(lineNumber > 10){
                return false;
            } else {
                return true;
            }
        });

        MfcSolutionDescriptorParser mfcSolutionDescriptorParser = mfcProjectDescriptorParserRef.get();
        if(Objects.isNull(mfcSolutionDescriptorParser)){
            throw new MfcSolutionDescriptorParserFactoryException(String.format("Solution descriptor not supported, file: [%s]", descriptor.getAbsolutePath()));
        }

        return mfcSolutionDescriptorParser;
    }

    private boolean checkVS(String line, MfcSolutionDescriptor mfcSolutionDescriptor, String pattern){
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(line);
        if(m.find()){
            String version = m.group(1);
            mfcSolutionDescriptor.setVisualStudioVersion(version);
            return true;
        } else {
            return false;
        }
    }

    private boolean checkVS600(String line, MfcSolutionDescriptor mfcSolutionDescriptor){
        return checkVS(line, mfcSolutionDescriptor, "Microsoft Developer Studio Workspace File, Format Version (.*)");
    }

    private boolean checkVS2013(String line, MfcSolutionDescriptor mfcSolutionDescriptor){
        return checkVS(line, mfcSolutionDescriptor, "Microsoft Visual Studio Solution File, Format Version (.*)");
    }


}
