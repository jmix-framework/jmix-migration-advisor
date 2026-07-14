package io.jmix.migration.jmix.model;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Configuration facts: renamed properties found in application.properties, menu size,
 * Liquibase include of the classic UI data changelog.
 */
public class JmixConfigInfo {

    private final List<PropertyRename> propertyRenames;
    private final String menuConfigLocation;
    private final int menuScreenItemsCount;
    private final boolean uiDataChangelogIncluded;

    public JmixConfigInfo(List<PropertyRename> propertyRenames, @Nullable String menuConfigLocation,
                          int menuScreenItemsCount, boolean uiDataChangelogIncluded) {
        this.propertyRenames = List.copyOf(propertyRenames);
        this.menuConfigLocation = menuConfigLocation;
        this.menuScreenItemsCount = menuScreenItemsCount;
        this.uiDataChangelogIncluded = uiDataChangelogIncluded;
    }

    public List<PropertyRename> getPropertyRenames() {
        return propertyRenames;
    }

    @Nullable
    public String getMenuConfigLocation() {
        return menuConfigLocation;
    }

    public int getMenuScreenItemsCount() {
        return menuScreenItemsCount;
    }

    /**
     * True when the Liquibase master changelog includes {@code /io/jmix/uidata/...}:
     * it is replaced by {@code /io/jmix/flowuidata/...} in the current Jmix.
     */
    public boolean isUiDataChangelogIncluded() {
        return uiDataChangelogIncluded;
    }

    public static class PropertyRename {
        private final String property;
        private final String newProperty;
        private final String notes;

        public PropertyRename(String property, @Nullable String newProperty, String notes) {
            this.property = property;
            this.newProperty = newProperty;
            this.notes = notes;
        }

        public String getProperty() {
            return property;
        }

        @Nullable
        public String getNewProperty() {
            return newProperty;
        }

        public String getNotes() {
            return notes;
        }
    }
}
