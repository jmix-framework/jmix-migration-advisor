package io.jmix.migration.util;

import org.apache.commons.io.IOUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.dom.DOMDocument;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.*;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class XmlUtils {

    private static final Logger log = LoggerFactory.getLogger(XmlUtils.class);

    public static Document readDocument(File file) {
        return readDocument(file, getSaxReader());
    }

    public static Document readDocument(File file, SAXReader xmlReader) {
        FileInputStream inputStream = null;
        try {
            if(file.length() == 0) {
                log.warn("Empty file: {}", file.getName());
                return new DOMDocument();
            }
            inputStream = new FileInputStream(file);
            return readDocument(inputStream, xmlReader);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Unable to read XML from file", e);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }

    public static Document readDocument(InputStream stream, SAXReader xmlReader) {
        try (InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            return xmlReader.read(reader);
        } catch (IOException | DocumentException e) {
            if(e.getCause() instanceof SAXParseException) {

            }
            throw new RuntimeException("Unable to read XML from stream", e);
        }
    }

    public static SAXReader getSaxReader() {
        try {
            return new SAXReader(getParser().getXMLReader());
        } catch (SAXException e) {
            throw new RuntimeException("Unable to create SAX reader", e);
        }
    }

    public static SAXParser getParser() {
        SAXParser parser;
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setValidating(false);
        factory.setNamespaceAware(false);
        XMLReader xmlReader;
        try {
            parser = factory.newSAXParser();
            xmlReader = parser.getXMLReader();
        } catch (ParserConfigurationException | SAXException e) {
            throw new RuntimeException("Unable to create SAX parser", e);
        }

        setParserFeature(xmlReader, "http://xml.org/sax/features/namespaces", true);
        setParserFeature(xmlReader, "http://xml.org/sax/features/namespace-prefixes", false);

        // external entites
        setParserFeature(xmlReader, "http://xml.org/sax/properties/external-general-entities", false);
        setParserFeature(xmlReader, "http://xml.org/sax/properties/external-parameter-entities", false);

        // external DTD
        setParserFeature(xmlReader, "http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

        // use Locator2 if possible
        setParserFeature(xmlReader, "http://xml.org/sax/features/use-locator2", true);

        return parser;
    }

    private static void setParserFeature(XMLReader reader,
                                         String featureName, boolean value) {
        try {
            reader.setFeature(featureName, value);
        } catch (SAXNotSupportedException | SAXNotRecognizedException e) {
            log.trace("Error while setting XML reader feature", e);
        }
    }
}
