package io.jmix.migration.analysis.parser.screen;

import io.jmix.migration.model.Layout;
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
        log.info("Layout item: {}", elementName);
        layout.putItem(elementName);

        if (layoutItemElement.elements().isEmpty()) {
            return;
        }

        if (isSimpleContainerLayoutItem(elementName)) {
            log.info("'{}' is container component", elementName);
            List<Element> childElements = layoutItemElement.elements();
            for (Element childElement : childElements) {
                processLayoutItem(layout, childElement);
            }
            log.info("Finish processing container component '{}'", elementName);
        } else if (isTabbedContainerLayoutItem(elementName)) {
            log.info("'{}' is tabbed component", elementName);
            List<Element> tabElements = layoutItemElement.elements("tab");
            log.info("Tabbed component '{}' has {} tabs", elementName, tabElements.size());
            for (Element tabElement : tabElements) {
                List<Element> tabChildElements = tabElement.elements();
                log.info("Process tab");
                for (Element tabChildElement : tabChildElements) {
                    log.info("Tab child element: {}", tabChildElement.getQualifiedName());
                    processLayoutItem(layout, tabChildElement);
                }
            }
            log.info("Finish processing tabbed component '{}'", elementName);
        }
    }

    protected boolean isSimpleContainerLayoutItem(String name) {
        return SIMPLE_CONTAINER_COMPONENTS.contains(name);
    }

    protected boolean isTabbedContainerLayoutItem(String name) {
        return TABBED_CONTAINER_COMPONENT.contains(name);
    }
}
