package io.jmix.migration.jmix.analyzer;

import io.jmix.migration.core.scan.PropertiesParser;
import io.jmix.migration.core.scan.XmlUtils;
import io.jmix.migration.jmix.model.JmixConfigInfo;
import io.jmix.migration.jmix.model.JmixConfigInfo.PropertyRename;
import io.jmix.migration.core.project.JmixModule;
import org.dom4j.Document;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Analyzes application.properties (against the renames registry), the menu configuration
 * and the Liquibase master changelog.
 */
public class JmixConfigAnalyzer {

    private static final Logger log = LoggerFactory.getLogger(JmixConfigAnalyzer.class);

    protected static final String RENAMES_RESOURCE = "/registries/jmix-config-renames.xml";
    protected static final String JMIX_MENU_NAMESPACE = "http://jmix.io/schema/ui/menu";
    protected static final String UI_DATA_CHANGELOG_INCLUDE = "/io/jmix/uidata/liquibase/changelog.xml";

    public JmixConfigInfo analyzeConfig(List<JmixModule> modules) {
        log.info("Start configuration analysis");

        Properties applicationProperties = loadApplicationProperties(modules);
        List<PropertyRename> renames = findPropertyRenames(applicationProperties);

        String menuLocation = applicationProperties.getProperty("jmix.ui.menu-config");
        int menuItems = countMenuScreenItems(modules, menuLocation);

        String changelogLocation = applicationProperties.getProperty("main.liquibase.change-log");
        boolean uiDataIncluded = isUiDataChangelogIncluded(modules, changelogLocation);

        return new JmixConfigInfo(renames, menuLocation, menuItems, uiDataIncluded);
    }

    protected Properties loadApplicationProperties(List<JmixModule> modules) {
        Properties merged = new Properties();
        PropertiesParser propertiesParser = new PropertiesParser();
        for (JmixModule module : modules) {
            Path propertiesFile = module.getResourcesDir().resolve("application.properties");
            if (Files.isRegularFile(propertiesFile)) {
                try {
                    merged.putAll(propertiesParser.parsePropertiesFile(propertiesFile));
                } catch (Exception e) {
                    log.warn("Unable to read '{}': {}", propertiesFile, e.getMessage());
                }
            }
        }
        return merged;
    }

    protected List<PropertyRename> findPropertyRenames(Properties applicationProperties) {
        List<PropertyRename> result = new ArrayList<>();
        loadRenamesRegistry().forEach((key, rename) -> {
            if (applicationProperties.containsKey(key)) {
                result.add(rename);
            }
        });
        return result;
    }

    protected Map<String, PropertyRename> loadRenamesRegistry() {
        try (InputStream stream = JmixConfigAnalyzer.class.getResourceAsStream(RENAMES_RESOURCE)) {
            if (stream == null) {
                throw new RuntimeException("Config renames registry resource is not found: " + RENAMES_RESOURCE);
            }
            Document document = XmlUtils.readDocument(stream, XmlUtils.getSaxReader());
            Element rootElement = document.getRootElement();
            Map<String, PropertyRename> registry = new LinkedHashMap<>();
            for (Element propertyElement : rootElement.elements("property")) {
                String key = propertyElement.attributeValue("key");
                String newKey = propertyElement.attributeValue("new-key");
                Element notesElement = propertyElement.element("notes");
                String notes = notesElement == null ? "" : notesElement.getTextTrim();
                registry.put(key, new PropertyRename(key, newKey, notes));
            }
            return registry;
        } catch (IOException e) {
            throw new RuntimeException("Unable to load config renames registry", e);
        }
    }

    protected int countMenuScreenItems(List<JmixModule> modules, @Nullable String menuLocation) {
        if (menuLocation == null || menuLocation.isBlank()) {
            return 0;
        }
        for (JmixModule module : modules) {
            Path menuFile = module.getResourcesDir().resolve(menuLocation.trim());
            if (!Files.isRegularFile(menuFile)) {
                continue;
            }
            try {
                Document document = XmlUtils.readDocument(menuFile.toFile());
                Element rootElement = document.getRootElement();
                if (rootElement != null && "menu-config".equals(rootElement.getName())) {
                    return countScreenItems(rootElement);
                }
            } catch (Exception e) {
                log.warn("Unable to parse menu config '{}': {}", menuFile, e.getMessage());
            }
        }
        return 0;
    }

    protected int countScreenItems(Element element) {
        int count = 0;
        for (Element child : element.elements()) {
            if ("item".equals(child.getName()) && child.attributeValue("screen") != null) {
                count++;
            }
            count += countScreenItems(child);
        }
        return count;
    }

    protected boolean isUiDataChangelogIncluded(List<JmixModule> modules, @Nullable String changelogLocation) {
        if (changelogLocation == null || changelogLocation.isBlank()) {
            return false;
        }
        for (JmixModule module : modules) {
            Path changelogFile = module.getResourcesDir().resolve(changelogLocation.trim());
            if (!Files.isRegularFile(changelogFile)) {
                continue;
            }
            try {
                Document document = XmlUtils.readDocument(changelogFile.toFile());
                Element rootElement = document.getRootElement();
                if (rootElement == null) {
                    return false;
                }
                return rootElement.elements("include").stream()
                        .anyMatch(include -> UI_DATA_CHANGELOG_INCLUDE.equals(include.attributeValue("file")));
            } catch (Exception e) {
                log.warn("Unable to parse Liquibase changelog '{}': {}", changelogFile, e.getMessage());
            }
        }
        return false;
    }
}
