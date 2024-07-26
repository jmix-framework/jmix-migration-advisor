package io.jmix.migration.analysis.parser.general;

import io.jmix.migration.util.XmlUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.File;
import java.nio.file.Path;
import java.util.*;

public class WebXmlParser {

    private static final Logger log = LoggerFactory.getLogger(WebXmlParser.class);

    public WebXmlParser() {
    }

    public List<String> processWebXml(Path filePath) {
        log.info("---=== Start analyze web.xml ===---");

        File file = filePath.toFile();
        if (!file.exists()) {
            throw new IllegalArgumentException("'persistence.xml' file not found in path: " + filePath);
        }

        if (file.isDirectory()) {
            throw new IllegalArgumentException(filePath + " is not a file");
        }

        return processFile(file);
    }

    protected List<String> processFile(File file) {
        log.info("Process file: {}", file);

        Document document = XmlUtils.readDocument(file);
        Element rootElement = document.getRootElement();
        String tagName = rootElement.getName();
        boolean isWebXmlFile = "web-app".equals(tagName);
        if (isWebXmlFile) {
            return processRootElement(rootElement);
        } else {
            throw new RuntimeException("Not a 'web.xml' file - unknown structure");
        }
    }

    protected List<String> processRootElement(Element persistenceElement) {
        List<Element> contextParamElements = persistenceElement.elements("context-param");
        log.debug("Find 'context-param' elements: {}", contextParamElements.size());
        List<String> appComponents = Collections.emptyList();
        for (Element contextParamElement : contextParamElements) {
            Element paramNameElement = contextParamElement.element("param-name");
            if(paramNameElement == null) {
                continue;
            }
            String paramNameElementValue = paramNameElement.getText();
            if("appComponents".equals(paramNameElementValue)) {
                Element paramValueElement = contextParamElement.element("param-value");
                if(paramValueElement == null) {
                    continue;
                }
                String paramValueElementValue = paramValueElement.getText();
                appComponents = Arrays.stream(paramValueElementValue.split(" ")).toList();
            }
        }

        return appComponents;
    }
}
