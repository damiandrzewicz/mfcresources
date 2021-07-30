package de.atos.solumversion.components;

import de.atos.solumversion.domain.MfcProjectDescriptor;
import de.atos.solumversion.services.MfcProjectDescriptorParser;
import de.atos.solumversion.services.MfcProjectDescriptorParserVS2013;
import de.atos.solumversion.services.MfcProjectDescriptorParserVS600;
import de.atos.solumversion.utils.LineFileReader;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class MfcProjectDescriptorParserFactory {

    public Optional<MfcProjectDescriptorParser> create(File descriptor){

        MfcProjectDescriptor mfcProjectDescriptor = new MfcProjectDescriptor();

        AtomicReference<Optional<MfcProjectDescriptorParser>> mfcProjectDescriptorParser = new AtomicReference<>(Optional.empty());

        int lineNumber = 0;
        LineFileReader.read(descriptor, line -> {

            if(checkVS600(line, mfcProjectDescriptor)){
                mfcProjectDescriptorParser.set(Optional.of(new MfcProjectDescriptorParserVS600(mfcProjectDescriptor)));
                return false;
            } else if(checkVS2013(line, mfcProjectDescriptor)){
                mfcProjectDescriptorParser.set(Optional.of(new MfcProjectDescriptorParserVS2013(mfcProjectDescriptor)));
                return false;
            }


            if(lineNumber > 10){
                return false;
            } else {
                return true;
            }
        });

        return mfcProjectDescriptorParser.get();
    }

    private boolean checkVS(String line, MfcProjectDescriptor mfcProjectDescriptor, String pattern){
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(line);
        if(m.find()){
            String version = m.group(1);
            mfcProjectDescriptor.setVisualStudioVersion(version);
            return true;
        } else {
            return false;
        }
    }

    private boolean checkVS600(String line, MfcProjectDescriptor mfcProjectDescriptor){
        return checkVS(line, mfcProjectDescriptor, "Microsoft Developer Studio Workspace File, Format Version (.*)");
    }

    private boolean checkVS2013(String line, MfcProjectDescriptor mfcProjectDescriptor){
        return checkVS(line, mfcProjectDescriptor, "Microsoft Visual Studio Solution File, Format Version (.*)");
    }


}
