package io.jmix.migration.cuba.appcomponent;

import io.jmix.migration.core.scan.XmlUtils;
import org.dom4j.Document;
import org.dom4j.Element;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Registry of known CUBA app components (add-ons) with notes about their availability in Jmix.
 * Loaded from an XML resource.
 */
public class CubaAppComponentsInfoRegistry {

    protected static final String DEFAULT_RESOURCE = "/registries/cuba-app-components.xml";

    private final Map<String, CubaAppComponentInfo> registry;

    protected CubaAppComponentsInfoRegistry(Map<String, CubaAppComponentInfo> registry) {
        this.registry = registry;
    }

    public static CubaAppComponentsInfoRegistry create() {
        try (InputStream stream = CubaAppComponentsInfoRegistry.class.getResourceAsStream(DEFAULT_RESOURCE)) {
            if (stream == null) {
                throw new RuntimeException("App components registry resource is not found: " + DEFAULT_RESOURCE);
            }
            return load(stream);
        } catch (IOException e) {
            throw new RuntimeException("Unable to load app components registry", e);
        }
    }

    @Nullable
    public CubaAppComponentInfo getAppComponentInfo(String appComponentPackage) {
        return registry.get(appComponentPackage);
    }

    protected static CubaAppComponentsInfoRegistry load(InputStream stream) {
        Document document = XmlUtils.readDocument(stream, XmlUtils.getSaxReader());
        Element rootElement = document.getRootElement();
        if (!"cuba-app-components".equals(rootElement.getName())) {
            throw new RuntimeException("Unexpected root element of app components registry: " + rootElement.getName());
        }

        Map<String, CubaAppComponentInfo> registry = new HashMap<>();
        for (Element componentElement : rootElement.elements("component")) {
            String componentPackage = requiredAttribute(componentElement, "package");
            String name = requiredAttribute(componentElement, "name");
            AppComponentType type = parseType(componentPackage, requiredAttribute(componentElement, "type"));
            AppComponentOrigin origin = parseOrigin(componentPackage, requiredAttribute(componentElement, "origin"));
            Element notesElement = componentElement.element("notes");
            String notes = notesElement == null ? "" : notesElement.getTextTrim();

            if (registry.put(componentPackage, CubaAppComponentInfo.create(componentPackage, name, type, origin, notes)) != null) {
                throw new RuntimeException("Duplicated app component registry entry: '" + componentPackage + "'");
            }
        }
        return new CubaAppComponentsInfoRegistry(Map.copyOf(registry));
    }

    protected static String requiredAttribute(Element element, String attributeName) {
        String value = element.attributeValue(attributeName);
        if (value == null) {
            throw new RuntimeException("App component registry entry: '" + attributeName + "' attribute is required");
        }
        return value;
    }

    protected static AppComponentType parseType(String componentPackage, String value) {
        return switch (value) {
            case "base-app" -> AppComponentType.BASE_APP;
            case "addon" -> AppComponentType.ADDON;
            case "translation-addon" -> AppComponentType.TRANSLATION_ADDON;
            case "theme" -> AppComponentType.THEME;
            default -> throw new RuntimeException("App component registry entry '" + componentPackage
                    + "': unknown type \"" + value + "\"");
        };
    }

    protected static AppComponentOrigin parseOrigin(String componentPackage, String value) {
        return switch (value) {
            case "framework" -> AppComponentOrigin.FRAMEWORK;
            case "external" -> AppComponentOrigin.EXTERNAL;
            default -> throw new RuntimeException("App component registry entry '" + componentPackage
                    + "': unknown origin \"" + value + "\"");
        };
    }
}
