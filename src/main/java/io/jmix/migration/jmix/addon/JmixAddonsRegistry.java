package io.jmix.migration.jmix.addon;

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
 * Registry of known Jmix 1.x add-ons (starters) with their status in the current Jmix version.
 * Loaded from an XML resource. Add-ons without an entry are reported as "no data".
 */
public class JmixAddonsRegistry {

    protected static final String DEFAULT_RESOURCE = "/registries/jmix-addons.xml";

    private final Map<String, JmixAddonInfo> registry;

    protected JmixAddonsRegistry(Map<String, JmixAddonInfo> registry) {
        this.registry = registry;
    }

    public static JmixAddonsRegistry create() {
        try (InputStream stream = JmixAddonsRegistry.class.getResourceAsStream(DEFAULT_RESOURCE)) {
            if (stream == null) {
                throw new RuntimeException("Jmix addons registry resource is not found: " + DEFAULT_RESOURCE);
            }
            return load(stream);
        } catch (IOException e) {
            throw new RuntimeException("Unable to load Jmix addons registry", e);
        }
    }

    /**
     * @param groupArtifact dependency key {@code group:artifact}
     */
    @Nullable
    public JmixAddonInfo getAddonInfo(String groupArtifact) {
        return registry.get(groupArtifact);
    }

    public Collection<JmixAddonInfo> getAll() {
        return registry.values();
    }

    protected static JmixAddonsRegistry load(InputStream stream) {
        Document document = XmlUtils.readDocument(stream, XmlUtils.getSaxReader());
        Element rootElement = document.getRootElement();
        if (!"jmix-addons".equals(rootElement.getName())) {
            throw new RuntimeException("Unexpected root element of Jmix addons registry: " + rootElement.getName());
        }

        Map<String, JmixAddonInfo> registry = new HashMap<>();
        for (Element addonElement : rootElement.elements("addon")) {
            String artifact = requiredAttribute(addonElement, "artifact");
            String name = requiredAttribute(addonElement, "name");
            String category = requiredAttribute(addonElement, "category");
            License license = parseLicense(artifact, requiredAttribute(addonElement, "license"));
            Origin origin = parseOrigin(artifact, requiredAttribute(addonElement, "origin"));
            TargetStatus flowStatus = parseFlowStatus(artifact, requiredAttribute(addonElement, "flow-status"));

            Element flowArtifactElement = addonElement.element("flow-artifact");
            String flowArtifact = flowArtifactElement == null ? null : flowArtifactElement.getTextTrim();
            Element notesElement = addonElement.element("notes");
            String notes = notesElement == null ? "" : notesElement.getTextTrim();
            Element costHintElement = addonElement.element("cost-hint");
            Integer costHint = costHintElement == null ? null : Integer.valueOf(costHintElement.getTextTrim());

            JmixAddonInfo addonInfo = new JmixAddonInfo(artifact, name, category, license, origin, flowStatus, flowArtifact, notes, costHint);
            if (registry.put(artifact, addonInfo) != null) {
                throw new RuntimeException("Duplicated Jmix addon registry entry: '" + artifact + "'");
            }
        }
        return new JmixAddonsRegistry(Map.copyOf(registry));
    }

    protected static String requiredAttribute(Element element, String attributeName) {
        String value = element.attributeValue(attributeName);
        if (value == null) {
            throw new RuntimeException("Jmix addon registry entry: '" + attributeName + "' attribute is required");
        }
        return value;
    }

    protected static TargetStatus parseFlowStatus(String artifact, String value) {
        return switch (value) {
            case "available" -> TargetStatus.AVAILABLE;
            case "renamed" -> TargetStatus.RENAMED;
            case "replaced" -> TargetStatus.REPLACED;
            case "merged" -> TargetStatus.MERGED;
            case "absent" -> TargetStatus.ABSENT;
            default -> throw new RuntimeException("Jmix addon registry entry '" + artifact
                    + "': unknown flow-status \"" + value + "\"");
        };
    }

    protected static License parseLicense(String artifact, String value) {
        return switch (value) {
            case "open-source" -> License.OPEN_SOURCE;
            case "commercial" -> License.COMMERCIAL;
            default -> throw new RuntimeException("Jmix addon registry entry '" + artifact
                    + "': unknown license \"" + value + "\"");
        };
    }

    protected static Origin parseOrigin(String artifact, String value) {
        return switch (value) {
            case "framework" -> Origin.FRAMEWORK;
            case "community" -> Origin.COMMUNITY;
            default -> throw new RuntimeException("Jmix addon registry entry '" + artifact
                    + "': unknown origin \"" + value + "\"");
        };
    }
}
