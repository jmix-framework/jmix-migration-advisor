package io.jmix.migration.analysis.parser.screen;

import io.jmix.migration.analysis.model.Facet;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

public class ScreenFacetsParser {

    private static final Logger log = LoggerFactory.getLogger(ScreenFacetsParser.class);

    public List<Facet> parseFacets(Element rootElement) {
        Element facetsElement = rootElement.element("facets");
        if (facetsElement == null) {
            log.debug("No facets found");
            return Collections.emptyList();
        }
        return parseFacetElement(facetsElement);
    }

    public List<Facet> parseFacetElement(@Nonnull Element facetsElement) {
        List<Element> childElements = facetsElement.elements();
        return childElements.stream()
                .map(e -> new Facet(e.getQualifiedName()))
                .toList();
    }
}
