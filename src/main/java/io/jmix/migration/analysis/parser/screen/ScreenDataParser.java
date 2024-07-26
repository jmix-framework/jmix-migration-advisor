package io.jmix.migration.analysis.parser.screen;

import io.jmix.migration.model.ScreenData;
import io.jmix.migration.model.ScreenDataItem;
import org.dom4j.Element;
import org.dom4j.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ScreenDataParser {

    private static final Logger log = LoggerFactory.getLogger(ScreenDataParser.class);

    protected static final Set<String> DATA_CONTAINER_ELEMENTS = Stream.of("instance", "collection", "keyValueInstance", "keyValueCollection")
            .collect(Collectors.toUnmodifiableSet());
    protected static final Set<String> DATASOURCE_ELEMENTS = Stream.of(
            "datasource", "collectionDatasource", "groupDatasource", "hierarchicalDatasource",
                    "runtimePropsDatasource", "valueCollectionDatasource", "valueGroupDatasource", "valueHierarchicalDatasource")
            .collect(Collectors.toUnmodifiableSet());

    public ScreenData parseScreenData(Element rootElement) {
        Element dataElement = rootElement.element("data");
        if (dataElement == null) {
            return new ScreenData();
        }
        return parseScreenDataElement(dataElement);
    }

    public ScreenData parseScreenDataElement(Element dataElement) {
        ScreenData screenData = new ScreenData();
        List<Element> childElements = dataElement.elements();
        for (Element childElement : childElements) {
            parseScreenDataItemElement(childElement, screenData, null);
        }
        return screenData;
    }

    public void parseScreenDataItemElement(Element itemElement, ScreenData screenData, @Nullable ScreenDataItem parentItem) {
        String itemElementName = itemElement.getQualifiedName();
        String itemElementId = itemElement.attributeValue("id");
        log.info("Data item: {}, ID: {}, parent: {}", itemElementName, itemElementId, parentItem);
        ScreenDataItem screenDataItem = new ScreenDataItem(itemElementName, itemElementId, parentItem);
        screenData.addItem(screenDataItem);

        List<Element> childElements = itemElement.elements();
        for(Element childElement: childElements) {
            String childElementName = childElement.getQualifiedName();
            if("loader".equalsIgnoreCase(childElementName)) {
                Element queryElement = childElement.element("query");
                if(queryElement != null) {
                    String queryText = queryElement.getTextTrim();
                    log.info("[QUERY]: text={}", queryText);
                    screenDataItem.setQuery(queryText);
                }
            } else if(DATA_CONTAINER_ELEMENTS.contains(childElementName)) {
                parseScreenDataItemElement(childElement, screenData, screenDataItem);
            }
        }
    }

    public ScreenData parseLegacyDsContext(Element rootElement) {
        Element dsContextElement = rootElement.element("dsContext");
        if (dsContextElement == null) {
            return new ScreenData();
        }
        return parseLegacyDsContextElement(dsContextElement);
    }

    public ScreenData parseLegacyDsContextElement(Element dsContextElement) {
        ScreenData screenData = new ScreenData();
        List<Element> childElements = dsContextElement.elements();
        for (Element childElement : childElements) {
            parseLegacyDsContextItemElement(childElement, screenData, null);
        }
        return screenData;
    }

    protected void parseLegacyDsContextItemElement(Element itemElement, ScreenData screenData, @Nullable ScreenDataItem parentItem) {
        String itemElementName = itemElement.getQualifiedName();
        String itemElementId = itemElement.attributeValue("id");
        log.info("dsContext item: {}, ID: {}, parent: {}", itemElementName, itemElementId, parentItem);
        ScreenDataItem screenDataItem = new ScreenDataItem(itemElementName, itemElementId, parentItem);
        screenData.addItem(screenDataItem);

        List<Element> childElements = itemElement.elements();
        for (Element childElement : childElements) {
            if("query".equalsIgnoreCase(childElement.getQualifiedName())) {
                String queryText = childElement.getTextTrim();
                log.info("[QUERY]: text={}", queryText);
                screenDataItem.setQuery(queryText);
            } else if (DATASOURCE_ELEMENTS.contains(childElement.getQualifiedName())) {
                parseLegacyDsContextItemElement(childElement, screenData, screenDataItem);
            }
        }
    }
}
