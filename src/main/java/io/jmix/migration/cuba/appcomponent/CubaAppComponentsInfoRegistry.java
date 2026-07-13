package io.jmix.migration.cuba.appcomponent;

import io.jmix.migration.core.model.License;
import io.jmix.migration.core.model.Origin;
import io.jmix.migration.core.model.TargetStatus;
import io.jmix.migration.core.scan.XmlUtils;
import org.dom4j.Document;
import org.dom4j.Element;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
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

    public Collection<CubaAppComponentInfo> getAll() {
        return registry.values();
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
            AppComponentType category = parseCategory(componentPackage, requiredAttribute(componentElement, "category"));
            License license = parseLicense(componentPackage, requiredAttribute(componentElement, "license"));
            Origin origin = parseOrigin(componentPackage, requiredAttribute(componentElement, "origin"));
            TargetStatus status = parseStatus(componentPackage, requiredAttribute(componentElement, "status"));
            Element notesElement = componentElement.element("notes");
            String notes = notesElement == null ? "" : notesElement.getTextTrim();

            if (registry.put(componentPackage, CubaAppComponentInfo.create(componentPackage, name, category, license, origin, status, notes)) != null) {
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

    protected static AppComponentType parseCategory(String componentPackage, String value) {
        return switch (value) {
            case "base-app" -> AppComponentType.BASE_APP;
            case "addon" -> AppComponentType.ADDON;
            case "translation" -> AppComponentType.TRANSLATION;
            case "theme" -> AppComponentType.THEME;
            default -> throw new RuntimeException("App component registry entry '" + componentPackage
                    + "': unknown category \"" + value + "\"");
        };
    }

    protected static License parseLicense(String componentPackage, String value) {
        return switch (value) {
            case "open-source" -> License.OPEN_SOURCE;
            case "commercial" -> License.COMMERCIAL;
            default -> throw new RuntimeException("App component registry entry '" + componentPackage
                    + "': unknown license \"" + value + "\"");
        };
    }

    protected static Origin parseOrigin(String componentPackage, String value) {
        return switch (value) {
            case "framework" -> Origin.FRAMEWORK;
            case "community" -> Origin.COMMUNITY;
            default -> throw new RuntimeException("App component registry entry '" + componentPackage
                    + "': unknown origin \"" + value + "\"");
        };
    }

    protected static TargetStatus parseStatus(String componentPackage, String value) {
        return switch (value) {
            case "available" -> TargetStatus.AVAILABLE;
            case "replaced" -> TargetStatus.REPLACED;
            case "merged" -> TargetStatus.MERGED;
            case "absent" -> TargetStatus.ABSENT;
            default -> throw new RuntimeException("App component registry entry '" + componentPackage
                    + "': unknown status \"" + value + "\"");
        };
    }
}
