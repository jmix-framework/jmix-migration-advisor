package io.jmix.migration.classicui.parser;

import io.jmix.migration.classicui.model.*;
import io.jmix.migration.core.incident.UiComponentIssuesRegistry;
import org.dom4j.Element;

import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.List;

public class ScreenDescriptorParser {

    private static final Logger log = LoggerFactory.getLogger(ScreenDescriptorParser.class);

    protected final Path moduleSrcPath;
    protected final ScreensCollector screensCollector;
    protected final ScreenDataParser screenDataParser;
    protected final ScreenFacetsParser screenFacetsParser;
    protected final ScreenLayoutParser screenLayoutParser;

    // Required root namespace prefix, e.g. "http://jmix.io/schema/ui/" for Jmix 1.x descriptors.
    // Null accepts any namespace (CUBA descriptors may omit or vary it)
    protected final String requiredNamespacePrefix;

    public ScreenDescriptorParser(Path moduleSrcPath, ScreensCollector screensCollector,
                                  @Nullable String requiredNamespacePrefix) {
        this.requiredNamespacePrefix = requiredNamespacePrefix;
        this.moduleSrcPath = moduleSrcPath;
        this.screensCollector = screensCollector;
        this.screenDataParser = new ScreenDataParser();
        this.screenFacetsParser = new ScreenFacetsParser();
        // Namespace facts come from the component issues registry: component names of add-on
        // families are canonicalized by URI, so registry matching does not depend on the
        // prefixes chosen in descriptors
        this.screenLayoutParser = new ScreenLayoutParser(
                UiComponentIssuesRegistry.create().getNamespaceCanonicalization());
    }

    public boolean isScreenDescriptor(Element rootElement) {
        return "window".equals(rootElement.getQualifiedName()) && namespaceMatches(rootElement);
    }

    public boolean isFragmentDescriptor(Element element) {
        return "fragment".equals(element.getQualifiedName()) && namespaceMatches(element);
    }

    protected boolean namespaceMatches(Element rootElement) {
        if (requiredNamespacePrefix == null) {
            return true;
        }
        String namespaceUri = rootElement.getNamespaceURI();
        return namespaceUri != null && namespaceUri.startsWith(requiredNamespacePrefix);
    }

    public void parseXmlDescriptor(Element rootElement, Path filePath) {
        log.debug("Start parsing XML descriptor");
        boolean isLegacy = false;
        boolean registered = false;

        Path relativeFilePath = moduleSrcPath.relativize(filePath);
        String relativeFilePathString = relativeFilePath.toString();
        relativeFilePathString = relativeFilePathString.replace("\\", "/");

        LegacyScreenRegistration legacyScreenRegistration = screensCollector.getLegacyScreenRegistration(relativeFilePathString);
        if (legacyScreenRegistration != null) {
            // This is the descriptor of registered legacy screen
            isLegacy = true;
            registered = true;
        }
        if (rootElement.attribute("class") != null) {
            isLegacy = true;
        }

        log.debug("File '{}' is a {} screen descriptor", filePath, isLegacy ? "legacy" : "'Screen API'");

        UiUnitInfo screenInfo;
        if (isLegacy) {
            String screenId = legacyScreenRegistration != null ? legacyScreenRegistration.getScreenId() : null;
            String screenControllerClass = rootElement.attributeValue("class");
            screenInfo = screensCollector.initScreenInfo(screenId, relativeFilePathString, screenControllerClass, true);
            screenInfo.setRegistered(registered);
        } else {
            screenInfo = screensCollector.initScreenByXmlDescriptor(relativeFilePathString);
            if (screenInfo == null) {
                throw new RuntimeException("Unable to init screen info by descriptor " + relativeFilePathString);
            }
        }

        String rootElementName = rootElement.getQualifiedName();
        if ("fragment".equals(rootElementName)) {
            screenInfo.setFragment(true);
        }

        String extendsAttributeValue = rootElement.attributeValue("extends");
        if (extendsAttributeValue != null) {
            screenInfo.setExtendedDescriptor(extendsAttributeValue);
        }

        ScreenData screenData;
        if (isLegacy) {
            screenData = screenDataParser.parseLegacyDsContext(rootElement);
        } else {
            screenData = screenDataParser.parseScreenData(rootElement);
        }
        screenInfo.setScreenData(screenData);

        // Facets
        List<Facet> facets = screenFacetsParser.parseFacets(rootElement);
        screenInfo.setFacets(facets);

        // Layout
        Layout layout = screenLayoutParser.parseLayout(rootElement);
        screenInfo.setLayout(layout);

        screenInfo.setDescriptorProcessed(true);

        log.debug("Finish parsing XML descriptor");
    }
}
