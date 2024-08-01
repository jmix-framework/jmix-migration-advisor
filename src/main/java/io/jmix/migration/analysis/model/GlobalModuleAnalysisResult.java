package io.jmix.migration.analysis.model;

import io.jmix.migration.analysis.MetricCodes;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class GlobalModuleAnalysisResult {
    private final Map<String, List<String>> entitiesPerPersistenceUnit;
    private final Set<String> legacyListeners;
    private final NumericMetric legacyListenersAmount;

    public GlobalModuleAnalysisResult(Map<String, List<String>> entitiesPerPersistenceUnit, Set<String> legacyListeners) {
        this.entitiesPerPersistenceUnit = entitiesPerPersistenceUnit;
        this.legacyListeners = legacyListeners;
        this.legacyListenersAmount = MetricCodes.createLegacyEntityListenersMetric(legacyListeners.size());
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

    public Set<String> getLegacyListeners() {
        return legacyListeners;
    }

    public NumericMetric getLegacyListenersAmount() {
        return legacyListenersAmount;
    }
}
