package io.jmix.migration.model;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class GlobalModuleAnalysisResult {
    private final Map<String, List<String>> entitiesPerPersistenceUnit;

    public GlobalModuleAnalysisResult(Map<String, List<String>> entitiesPerPersistenceUnit) {
        this.entitiesPerPersistenceUnit = entitiesPerPersistenceUnit;
    }

    public Map<String, List<String>> getEntitiesPerPersistenceUnit() {
        return entitiesPerPersistenceUnit;
    }

    public int getTotalEntities() {
        AtomicInteger counter = new AtomicInteger();
        entitiesPerPersistenceUnit.forEach((unit, entities) -> {
            counter.addAndGet(entities.size());
        });
        return counter.get();
    }
}
