package io.jmix.migration.cuba.parser.screen;

import io.jmix.migration.cuba.model.Layout;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ScreenLayoutParser {

    private static final Logger log = LoggerFactory.getLogger(ScreenLayoutParser.class);

    protected static final Set<String> SIMPLE_CONTAINER_COMPONENTS = Stream.of(
            "buttonsPanel", "cssLayout", "flowBox", "groupBox", "hbox", "vbox", "scrollBox", "split", "htmlBox"
    ).collect(Collectors.toUnmodifiableSet());

    protected static final Set<String> TABBED_CONTAINER_COMPONENT = Stream.of("accordion", "tabSheet")
            .collect(Collectors.toUnmodifiableSet());

    protected static final Set<String> TABLE_COMPONENTS = Stream.of(
            "table", "groupTable", "treeTable", "dataGrid", "treeDataGrid"
    ).collect(Collectors.toUnmodifiableSet());

    public Layout parseLayout(Element rootElement) {
        Element layoutElement = rootElement.element("layout");
        if (layoutElement == null) {
            return new Layout();
        }

        return parseLayoutElement(layoutElement);
    }

    public Layout parseLayoutElement(Element layoutElement) {
        List<Element> childElements = layoutElement.elements();
        Layout layout = new Layout();
        for (Element childElement : childElements) {
            processLayoutItem(layout, childElement);
        }
        return layout;
    }

    protected void processLayoutItem(Layout layout, Element layoutItemElement) {
        String elementName = layoutItemElement.getQualifiedName();
        log.debug("Layout item: {}", elementName);
        layout.putItem(elementName);

        if (layoutItemElement.elements().isEmpty()) {
            return;
        }

        if (isSimpleContainerLayoutItem(elementName)) {
            log.debug("'{}' is container component", elementName);
            List<Element> childElements = layoutItemElement.elements();
            for (Element childElement : childElements) {
                processLayoutItem(layout, childElement);
            }
            log.debug("Finish processing container component '{}'", elementName);
        } else if (isTabbedContainerLayoutItem(elementName)) {
            log.debug("'{}' is tabbed component", elementName);
            List<Element> tabElements = layoutItemElement.elements("tab");
            log.debug("Tabbed component '{}' has {} tabs", elementName, tabElements.size());
            for (Element tabElement : tabElements) {
                List<Element> tabChildElements = tabElement.elements();
                log.debug("Process tab");
                for (Element tabChildElement : tabChildElements) {
                    log.debug("Tab child element: {}", tabChildElement.getQualifiedName());
                    processLayoutItem(layout, tabChildElement);
                }
            }
            log.debug("Finish processing tabbed component '{}'", elementName);
        } else if (isTableLayoutItem(elementName)) {
            // Studio-generated browse screens nest a buttonsPanel inside the table element
            log.debug("'{}' is table component", elementName);
            Element buttonsPanelElement = layoutItemElement.element("buttonsPanel");
            if (buttonsPanelElement != null) {
                processLayoutItem(layout, buttonsPanelElement);
            }
            log.debug("Finish processing table component '{}'", elementName);
        } else if (isGridLayoutItem(elementName)) {
            log.debug("'{}' is grid layout component", elementName);
            Element rowsElement = layoutItemElement.element("rows");
            if (rowsElement != null) {
                for (Element rowElement : rowsElement.elements("row")) {
                    for (Element rowChildElement : rowElement.elements()) {
                        processLayoutItem(layout, rowChildElement);
                    }
                }
            }
            log.debug("Finish processing grid layout component '{}'", elementName);
        }
    }

    protected boolean isSimpleContainerLayoutItem(String name) {
        return SIMPLE_CONTAINER_COMPONENTS.contains(name);
    }

    protected boolean isTabbedContainerLayoutItem(String name) {
        return TABBED_CONTAINER_COMPONENT.contains(name);
    }

    protected boolean isTableLayoutItem(String name) {
        return TABLE_COMPONENTS.contains(name);
    }

    protected boolean isGridLayoutItem(String name) {
        return "grid".equals(name);
    }
}
