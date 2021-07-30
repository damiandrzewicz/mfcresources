package de.atos.solumversion.services;

import de.atos.solumversion.domain.MfcResourceProperties;
import de.atos.solumversion.utils.LineFileReader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.Scanner;

@Service
@Slf4j
public class MfcResourceParserServiceImpl implements MfcResourceParserService {

    private enum Property{
        FileVersion("FILEVERSION"),
        ProductVersion("PRODUCTVERSION"),
        CompanyName("CompanyName"),
        FileDescription("FileDescription"),
        InternalName("InternalName"),
        LegalCopyright("LegalCopyright"),
        OriginalFilename("OriginalFilename"),
        ProductName("ProductName"),
        SpecialBuild("SpecialBuild"),
        ;

        private final String text;

        Property(final String text){
            this.text = text;
        }

        @Override
        public String toString(){
            return text;
        }
    }



    @Override
    public MfcResourceProperties parseResourceProperties(File resourceFile) throws MfcResourceParserServiceException {

        MfcResourceProperties mfcResourceProperties = new MfcResourceProperties();

        LineFileReader.read(resourceFile, line -> {
            if(line.contains(Property.FileVersion.toString())){
                mfcResourceProperties.setFileVersion(getHeaderValue(Property.FileVersion.toString(), line));
            } else if(line.contains(Property.ProductVersion.toString())){
                mfcResourceProperties.setProductVersion(getHeaderValue(Property.ProductVersion.toString(), line));
            } else if(line.contains(Property.CompanyName.toString())){
                mfcResourceProperties.setCompanyName(getBlockValue(Property.CompanyName.toString(), line));
            } else if(line.contains(Property.FileDescription.toString())){
                mfcResourceProperties.setProductVersion(getBlockValue(Property.FileDescription.toString(), line));
            } else if(line.contains(Property.InternalName.toString())){
                mfcResourceProperties.setInternalName(getBlockValue(Property.InternalName.toString(), line));
            } else if(line.contains(Property.LegalCopyright.toString())){
                mfcResourceProperties.setLegalCopyright(getBlockValue(Property.LegalCopyright.toString(), line));
            } else if(line.contains(Property.OriginalFilename.toString())){
                mfcResourceProperties.setOriginalFilename(getBlockValue(Property.OriginalFilename.toString(), line));
            } else if(line.contains(Property.ProductName.toString())){
                mfcResourceProperties.setProductName(getBlockValue(Property.ProductName.toString(), line));
            } else if(line.contains(Property.SpecialBuild.toString())){
                mfcResourceProperties.setSpecialBuild(getBlockValue(Property.SpecialBuild.toString(), line));
            }
            return true;
        });


        return mfcResourceProperties;
    }

    @Override
    public void updateResourceProperties(File resourceFile, MfcResourceProperties mfcResourceProperties) throws MfcResourceParserServiceException {
        LineFileReader.read(resourceFile, line -> {
            if(line.contains(Property.FileVersion.toString()) && !mfcResourceProperties.getFileVersion().isEmpty()){
                line.replace(Property.FileVersion.toString(), mfcResourceProperties.getFileVersion());
            } else if(line.contains(Property.ProductVersion.toString()) && !mfcResourceProperties.getProductVersion().isEmpty()){
                line.replace(Property.ProductVersion.toString(), mfcResourceProperties.getProductVersion());
            } else if(line.contains(Property.CompanyName.toString()) && !mfcResourceProperties.getCompanyName().isEmpty()){
                line.replace(Property.CompanyName.toString(), mfcResourceProperties.getCompanyName());
            } else if(line.contains(Property.FileDescription.toString()) && !mfcResourceProperties.getFileDescription().isEmpty()){
                line.replace(Property.FileDescription.toString(), mfcResourceProperties.getFileDescription());
            } else if(line.contains(Property.InternalName.toString()) && !mfcResourceProperties.getInternalName().isEmpty()){
                line.replace(Property.InternalName.toString(), mfcResourceProperties.getInternalName());
            } else if(line.contains(Property.LegalCopyright.toString()) && !mfcResourceProperties.getLegalCopyright().isEmpty()){
                line.replace(Property.LegalCopyright.toString(), mfcResourceProperties.getLegalCopyright());
            } else if(line.contains(Property.OriginalFilename.toString()) && !mfcResourceProperties.getOriginalFilename().isEmpty()){
                line.replace(Property.OriginalFilename.toString(), mfcResourceProperties.getOriginalFilename());
            } else if(line.contains(Property.ProductName.toString()) && !mfcResourceProperties.getProductName().isEmpty()){
                line.replace(Property.ProductName.toString(), mfcResourceProperties.getProductName());
            } else if(line.contains(Property.SpecialBuild.toString()) && !mfcResourceProperties.getSpecialBuild().isEmpty()){
                line.replace(Property.SpecialBuild.toString(), mfcResourceProperties.getSpecialBuild());
            }
            return true;
        });
    }



    private String getHeaderValue(String key, String line){
        return line.replace(key, "").trim().replace(",", ".");
    }

    private String getBlockValue(String key, String line){
        return line.replace("CompanyName", "").replace(",", "").replace("\"", "").replace("VALUE", "").trim();
    }
}
