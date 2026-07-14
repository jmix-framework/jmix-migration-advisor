package io.jmix.migration.core.incident;

import io.jmix.migration.core.scan.XmlUtils;
import org.dom4j.Document;
import org.dom4j.Element;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Registry of classic UI components changed, replaced or absent in Jmix Flow UI.
 * Loaded from an XML resource; components without an entry have a direct Flow UI equivalent.
 * Supports exact entries ({@code component="groupTable"}) and prefix entries for namespaced
 * add-on components ({@code match="chart:*"}).
 */
public class UiComponentIssuesRegistry {

    protected static final String DEFAULT_RESOURCE = "/registries/ui-component-issues.xml";

    private final Map<String, UiComponentIssue> issuesByComponent;
    private final List<PrefixIssue> issuesByPrefix;

    protected UiComponentIssuesRegistry(Map<String, UiComponentIssue> issuesByComponent,
                                        List<PrefixIssue> issuesByPrefix) {
        this.issuesByComponent = issuesByComponent;
        this.issuesByPrefix = issuesByPrefix;
    }

    public static UiComponentIssuesRegistry create() {
        try (InputStream stream = UiComponentIssuesRegistry.class.getResourceAsStream(DEFAULT_RESOURCE)) {
            if (stream == null) {
                throw new RuntimeException("UI component issues registry resource is not found: " + DEFAULT_RESOURCE);
            }
            return load(stream);
        } catch (IOException e) {
            throw new RuntimeException("Unable to load UI component issues registry", e);
        }
    }

    /**
     * All registry entries: exact ones plus prefix ones (their {@code getComponent()} is the pattern).
     */
    public List<UiComponentIssue> getAllIssues() {
        List<UiComponentIssue> all = new ArrayList<>(issuesByComponent.values());
        issuesByPrefix.forEach(prefixIssue -> all.add(prefixIssue.issue()));
        return all;
    }

    @Nullable
    public UiComponentIssue getIssue(String component) {
        UiComponentIssue issue = issuesByComponent.get(component);
        if (issue != null) {
            return issue;
        }
        for (PrefixIssue prefixIssue : issuesByPrefix) {
            if (component.startsWith(prefixIssue.prefix())) {
                return prefixIssue.issue();
            }
        }
        return null;
    }

    protected static UiComponentIssuesRegistry load(InputStream stream) {
        Document document = XmlUtils.readDocument(stream, XmlUtils.getSaxReader());
        Element rootElement = document.getRootElement();
        if (!"ui-component-issues".equals(rootElement.getName())) {
            throw new RuntimeException("Unexpected root element of UI component issues registry: " + rootElement.getName());
        }

        Map<String, UiComponentIssue> byComponent = new HashMap<>();
        List<PrefixIssue> byPrefix = new ArrayList<>();
        for (Element issueElement : rootElement.elements("issue")) {
            String component = issueElement.attributeValue("component");
            String match = issueElement.attributeValue("match");
            if ((component == null) == (match == null)) {
                throw new RuntimeException("Registry entry must have exactly one of 'component' or 'match' attributes");
            }

            String entryName = component != null ? component : match;
            UiComponentIssue issue = readIssue(entryName, issueElement);

            if (component != null) {
                if (byComponent.put(component, issue) != null) {
                    throw new RuntimeException("Duplicated registry entry: '" + component + "'");
                }
            } else {
                if (!match.endsWith("*") || match.length() < 2) {
                    throw new RuntimeException("'match' pattern must be a non-empty prefix ending with '*': '" + match + "'");
                }
                byPrefix.add(new PrefixIssue(match.substring(0, match.length() - 1), issue));
            }
        }
        return new UiComponentIssuesRegistry(Map.copyOf(byComponent), List.copyOf(byPrefix));
    }

    protected static UiComponentIssue readIssue(String entryName, Element issueElement) {
        String type = issueElement.attributeValue("type");
        if (type == null) {
            throw new RuntimeException("Registry entry '" + entryName + "': 'type' attribute is required");
        }

        Element notesElement = issueElement.element("notes");
        if (notesElement == null) {
            throw new RuntimeException("Registry entry '" + entryName + "': 'notes' element is required");
        }
        String notes = notesElement.getTextTrim();

        String scoreValue = issueElement.attributeValue("score");
        List<Element> requiresElements = issueElement.elements("requires");

        if ("absent".equals(type)) {
            // The invariant is enforced by the createAbsent factory signature; validating the raw
            // data here produces a message pointing at the registry entry instead of a missing method
            if (scoreValue != null) {
                throw new RuntimeException("Registry entry '" + entryName + "': 'score' is not allowed for type=\"absent\"");
            }
            if (!requiresElements.isEmpty()) {
                throw new RuntimeException("Registry entry '" + entryName + "': 'requires' is not allowed for type=\"absent\"");
            }
            return UiComponentIssue.createAbsent(entryName, notes);
        }

        int score = scoreValue == null ? 0 : Integer.parseInt(scoreValue);
        Requires[] requires = requiresElements.stream()
                .map(requiresElement -> readRequires(entryName, requiresElement))
                .toArray(Requires[]::new);

        return switch (type) {
            case "changed" -> UiComponentIssue.createChanged(entryName, notes, score, requires);
            case "has-alternative" -> UiComponentIssue.createAlternative(entryName, notes, score, requires);
            case "has-workaround" -> UiComponentIssue.createWorkaround(entryName, notes, score, requires);
            default -> throw new RuntimeException("Registry entry '" + entryName + "': unknown type \"" + type + "\"");
        };
    }

    protected static Requires readRequires(String entryName, Element requiresElement) {
        String kind = requiresElement.attributeValue("kind");
        String subject = requiresElement.attributeValue("subject");
        if (kind == null || subject == null) {
            throw new RuntimeException("Registry entry '" + entryName + "': 'requires' element needs 'kind' and 'subject' attributes");
        }
        return switch (kind) {
            case "addon" -> Requires.addon(subject);
            case "commercial-addon" -> Requires.commercialAddon(subject);
            case "third-party" -> Requires.thirdParty(subject);
            default -> throw new RuntimeException("Registry entry '" + entryName + "': unknown requires kind \"" + kind + "\"");
        };
    }

    protected record PrefixIssue(String prefix, UiComponentIssue issue) {
    }
}
