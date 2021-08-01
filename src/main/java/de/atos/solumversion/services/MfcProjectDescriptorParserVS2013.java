package de.atos.solumversion.services;

import de.atos.solumversion.domain.MfcProjectDescriptor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.*;
import java.io.File;
import java.io.IOException;
import java.util.Objects;


@Slf4j
public class MfcProjectDescriptorParserVS2013 implements MfcProjectDescriptorParser{

    private class Parser extends DefaultHandler{

        private StringBuilder currentValue = new StringBuilder();

        @Getter
        private MfcProjectDescriptor mfcProjectDescriptor;

        boolean configTypeRelease = false;
        boolean configTypeDebug = false;

        @Override
        public void startDocument() throws SAXException {
            mfcProjectDescriptor = new MfcProjectDescriptor();
        }

//        @Override
//        public void endDocument() throws SAXException {
//            super.endDocument();
//        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

            currentValue.setLength(0);

            configTypeRelease = false;
            configTypeDebug = false;

            String label = attributes.getValue("Label");
            if(qName.equalsIgnoreCase("PropertyGroup") && Objects.nonNull(label) && label.equals("Configuration")){
                String condition = attributes.getValue("Condition");
                if(Objects.nonNull(condition) && condition.contains("Release")){
                    configTypeRelease = true;
                } else if(Objects.nonNull(condition) && condition.contains("Debug")){
                    configTypeDebug = true;
                }
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {

            if(qName.equalsIgnoreCase("ConfigurationType")){
                MfcProjectDescriptor.Configuration configuration = (new MfcProjectDescriptor()).new Configuration();
                configuration.setConfigurationType(MfcProjectDescriptor.ConfigurationType.valueOf(currentValue.toString()));

                if(configTypeRelease){
                    mfcProjectDescriptor.setConfigurationRelease(configuration);
                } else if(configTypeDebug){
                    mfcProjectDescriptor.setConfigurationDebug(configuration);
                }
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            currentValue.append(ch, start, length);
        }

        @Override
        public void warning(SAXParseException e) throws SAXException {
            super.warning(e);
        }

        @Override
        public void error(SAXParseException e) throws SAXException {
            super.error(e);
        }

        @Override
        public void fatalError(SAXParseException e) throws SAXException {
            super.fatalError(e);
        }
    };

    public MfcProjectDescriptor parse(File descriptor) {

        MfcProjectDescriptor mfcProjectDescriptor = new MfcProjectDescriptor();

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(descriptor);
            doc.getDocumentElement().normalize();
            NodeList nodeList = doc.getElementsByTagName("PropertyGroup");

            for(int i = 0; i < nodeList.getLength(); i++){
                Node item = nodeList.item(i);
                NamedNodeMap attributes = item.getAttributes();
                Node condition = attributes.getNamedItem("Condition");
                Node label = attributes.getNamedItem("Label");

                if(Objects.nonNull(condition) && Objects.nonNull(label)){
                    String conditionContext = condition.getTextContent();
                    String labelContext = label.getTextContent();

                    if(conditionContext.contains("Release|Win32") && labelContext.equals("Configuration")){
                        MfcProjectDescriptor.Configuration config = mfcProjectDescriptor.new Configuration();

                        NodeList childNodes = item.getChildNodes();
                        for(int k = 0; k < childNodes.getLength(); k++){
                            Node item1 = childNodes.item(k);
                            if(item1.getNodeName().equals("ConfigurationType")){
                                config.setConfigurationType(MfcProjectDescriptor.ConfigurationType.valueOf(item1.getTextContent()));
                                mfcProjectDescriptor.setConfigurationRelease(config);
                                return mfcProjectDescriptor;
                            }
                        }
                    }
                }
            }

        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }


//        SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
//
//        try {
//            SAXParser saxParser = saxParserFactory.newSAXParser();
//
//            Parser parser = new Parser();
//            saxParser.parse(descriptor, parser);
//
//            return parser.getMfcProjectDescriptor();
//        } catch (ParserConfigurationException e) {
//            e.printStackTrace();
//        } catch (SAXException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        return null;
    }
}
