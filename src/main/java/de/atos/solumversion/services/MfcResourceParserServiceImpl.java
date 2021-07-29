package de.atos.solumversion.services;

import de.atos.solumversion.domain.MfcResourceProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.Scanner;

@Service
@Slf4j
public class MfcResourceParserServiceImpl implements MfcResourceParserService {
    @Override
    public MfcResourceProperties parseResourceProperties(File resourceFile) throws MfcResourceParserServiceException {
        if(!resourceFile.exists()){
            throw new MfcResourceParserServiceException(String.format("Resource file not exists: [%s]", resourceFile.getPath()));
        }

        Scanner scanner = null;
        try {
            scanner = new Scanner(resourceFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        MfcResourceProperties mfcResourceProperties = new MfcResourceProperties();

        String content = "";

        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(resourceFile));
            String line = bufferedReader.readLine();
            while(line != null){
                //content += line + System.lineSeparator();
                if(line.contains("FILEVERSION")){
                    String fileversion = line.replace("FILEVERSION", "").trim().replace(",", ".");
                    mfcResourceProperties.setFileVersion(fileversion);
                } else if(line.contains("PRODUCTVERSION")){
                    String productversion = line.replace("PRODUCTVERSION", "").trim().replace(",", ".");
                    mfcResourceProperties.setProductVersion(productversion);
                } else if(line.contains("CompanyName")){
                    String productversion = line.replace("CompanyName", "").replace(",", ".").replace("\"", "").replace("VALUE", "").trim();
                    mfcResourceProperties.setCompanyName(productversion);
                } else if(line.contains("FileDescription")){
                    String productversion = line.replace("FileDescription", "").replace(",", "").replace("\"", "").replace("VALUE", "").trim();
                    mfcResourceProperties.setProductVersion(productversion);
                }else if(line.contains("InternalName")){
                    String productversion = line.replace("InternalName", "").replace(",", "").replace("\"", "").replace("VALUE", "").trim();
                    mfcResourceProperties.setInternalName(productversion);
                }else if(line.contains("LegalCopyright")){
                    String productversion = line.replace("LegalCopyright", "").replace(",", "").replace("\"", "").replace("VALUE", "").trim();
                    mfcResourceProperties.setLegalCopyright(productversion);
                }else if(line.contains("OriginalFilename")){
                    String productversion = line.replace("OriginalFilename", "").replace(",", "").replace("\"", "").replace("VALUE", "").trim();
                    mfcResourceProperties.setOriginalFilename(productversion);
                }else if(line.contains("ProductName")){
                    String productversion = line.replace("ProductName", "").replace(",", "").replace("\"", "").replace("VALUE", "").trim();
                    mfcResourceProperties.setProductName(productversion);
                }else if(line.contains("SpecialBuild")){
                    String productversion = line.replace("SpecialBuild", "").replace(",", "").replace("\"", "").replace("VALUE", "").trim();
                    mfcResourceProperties.setSpecialBuild(productversion);
                }else if(line.contains("FileDescription")){
                    String productversion = line.replace("FileDescription", "").replace(",", "").replace("\"", "").replace("VALUE", "").trim();
                    mfcResourceProperties.setFileDescription(productversion);
                }

                line = bufferedReader.readLine();
            }

            bufferedReader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

//        while (scanner.hasNextLine()) {
//            String line = scanner.nextLine();
//            content += line + System.lineSeparator();
//            if(line.contains("FILEVERSION")){
//                String fileversion = line.replace("FILEVERSION", "").trim().replace(",", ".");
//                resourceProperties.setFileVersion(fileversion);
//            } else if(line.contains("PRODUCTVERSION")){
//                String productversion = line.replace("PRODUCTVERSION", "").trim().replace(",", ".");
//                resourceProperties.setProductVersion(productversion);
//            }
//        }

        return mfcResourceProperties;
    }
}
