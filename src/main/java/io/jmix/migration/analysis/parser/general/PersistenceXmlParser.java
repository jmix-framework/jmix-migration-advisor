package io.jmix.migration.analysis.parser.general;

import io.jmix.migration.util.XmlUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PersistenceXmlParser {

    private static final Logger log = LoggerFactory.getLogger(PersistenceXmlParser.class);

    public PersistenceXmlParser() {
    }

    @Nullable
    public Map<String, List<String>> processPersistenceXml(Path persistenceFilePath) {
        log.info("Start persistence.xml analysis");

        File file = persistenceFilePath.toFile();
        if (!file.exists()) {
            throw new IllegalArgumentException("'persistence.xml' file not found in path: " + persistenceFilePath);
        }

        if (file.isDirectory()) {
            throw new IllegalArgumentException(persistenceFilePath + " is not a file");
        }

        return processFile(file);
    }

    protected Map<String, List<String>> processFile(File file) {
        log.debug("Process file: {}", file);

        Document document = XmlUtils.readDocument(file);
        Element rootElement = document.getRootElement();
        String tagName = rootElement.getName();
        boolean isPersistenceFile = "persistence".equals(tagName);
        if (isPersistenceFile) {
            return processRootElement(rootElement);
        } else {
            throw new RuntimeException("Not a 'persistence.xml' file - unknown structure");
        }
    }

    protected Map<String, List<String>> processRootElement(Element persistenceElement) {
        List<Element> persistenceUnitElements = persistenceElement.elements("persistence-unit");
        log.debug("Find 'persistence-unit' elements: {}", persistenceUnitElements.size());
        Map<String, List<String>> entities = new HashMap<>();
        for (Element persistenceUnitElement : persistenceUnitElements) {
            String persistenceUnitName = persistenceUnitElement.attributeValue("name");
            List<Element> classElements = persistenceUnitElement.elements("class");
            List<String> entitiesOfUnit = new ArrayList<>();
            for (Element classElement : classElements) {
                String entityClassName = classElement.getText();
                entitiesOfUnit.add(entityClassName);
            }
            entities.put(persistenceUnitName, entitiesOfUnit);
        }

        return entities;
    }
}
