package io.jmix.migration.core.report;

import java.util.List;
import java.util.Map;

public class DataModelSection implements ReportSection {

    public static final String TYPE = "data-model";

    private final int entitiesAmount;
    private final Map<String, List<String>> entitiesPerUnit;
    private final List<String> legacyListeners;

    public DataModelSection(int entitiesAmount, Map<String, List<String>> entitiesPerUnit,
                            List<String> legacyListeners) {
        this.entitiesAmount = entitiesAmount;
        this.entitiesPerUnit = entitiesPerUnit;
        this.legacyListeners = List.copyOf(legacyListeners);
    }

    @Override
    public String getId() {
        return "data-model";
    }

    @Override
    public String getTitle() {
        return "Data model";
    }

    @Override
    public String getType() {
        return TYPE;
    }

    public int getEntitiesAmount() {
        return entitiesAmount;
    }

    public Map<String, List<String>> getEntitiesPerUnit() {
        return entitiesPerUnit;
    }

    public List<String> getLegacyListeners() {
        return legacyListeners;
    }
}
