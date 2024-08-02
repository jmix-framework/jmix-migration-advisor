package io.jmix.migration.analysis.model;

import io.jmix.migration.analysis.addon.CubaAppComponentInfo;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class ProjectEstimationResult {
    // Screens
    private final Map<ThresholdItem<Integer, BigDecimal>, List<String>> screensPerComplexity;
    private final BigDecimal screensTotalCost;
    //

    private final Map<String, Integer> allUiComponents;

    // Entities
    private final int entitiesAmount;
    private final Map<String, List<String>> entitiesPerPersistenceUnit;
    private final List<String> legacyListeners;
    private final BigDecimal legacyListenersCost;
    //

    // General
    private final BigDecimal initialMigrationCost;
    private final BigDecimal baseEntitiesMigrationCost;
    //

    // Addons
    private final List<CubaAppComponentInfo> appComponents;
    //

    private final BigDecimal totalEstimation;

    private ProjectEstimationResult(Builder builder) {
        this.screensPerComplexity = builder.screensPerComplexity;
        this.allUiComponents = builder.allUiComponents;
        this.entitiesAmount = countTotalEntities(builder.entitiesPerPersistenceUnit);
        this.entitiesPerPersistenceUnit = builder.entitiesPerPersistenceUnit;
        this.initialMigrationCost = builder.initialMigrationCost;
        this.baseEntitiesMigrationCost = builder.baseEntitiesMigrationCost;
        this.screensTotalCost = builder.screensTotalCost;
        this.legacyListenersCost = builder.legacyListenersCost;
        this.legacyListeners = builder.legacyListeners;
        this.appComponents = builder.appComponents;

        this.totalEstimation = createTotalEstimation(initialMigrationCost, baseEntitiesMigrationCost, screensTotalCost, legacyListenersCost);
    }

    public Map<ThresholdItem<Integer, BigDecimal>, List<String>> getScreensPerComplexity() {
        return screensPerComplexity;
    }

    public Map<String, Integer> getAllUiComponents() {
        return allUiComponents;
    }

    public int getEntitiesAmount() {
        return entitiesAmount;
    }

    public Map<String, List<String>> getEntitiesPerPersistenceUnit() {
        return entitiesPerPersistenceUnit;
    }

    public BigDecimal getInitialMigrationCost() {
        return initialMigrationCost;
    }

    public BigDecimal getBaseEntitiesMigrationCost() {
        return baseEntitiesMigrationCost;
    }

    public BigDecimal getScreensTotalCost() {
        return screensTotalCost;
    }

    public BigDecimal getLegacyListenersCost() {
        return legacyListenersCost;
    }

    public long getScreensTotalAmount() {
        return screensPerComplexity.values().stream().mapToLong(Collection::size).sum();
    }

    public List<String> getLegacyListeners() {
        return legacyListeners;
    }

    public BigDecimal getTotalEstimation() {
        return totalEstimation;
    }

    protected BigDecimal createTotalEstimation(BigDecimal... values) {
        return Arrays.stream(values).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
    }

    public List<CubaAppComponentInfo> getAppComponents() {
        return appComponents;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Map<ThresholdItem<Integer, BigDecimal>, List<String>> screensPerComplexity;
        private Map<String, Integer> allUiComponents;
        private Map<String, List<String>> entitiesPerPersistenceUnit;

        private BigDecimal screensTotalCost;

        private List<String> legacyListeners;

        private BigDecimal initialMigrationCost;
        private BigDecimal baseEntitiesMigrationCost;
        private BigDecimal legacyListenersCost;

        private List<CubaAppComponentInfo> appComponents;

        public Builder() {
        }

        public Builder setScreensPerComplexity(Map<ThresholdItem<Integer, BigDecimal>, List<String>> screensPerComplexity) {
            this.screensPerComplexity = screensPerComplexity;
            return this;
        }

        public Builder setAllUiComponents(Map<String, Integer> allUiComponents) {
            this.allUiComponents = allUiComponents;
            return this;
        }

        public Builder setEntitiesPerPersistenceUnit(Map<String, List<String>> entitiesPerPersistenceUnit) {
            this.entitiesPerPersistenceUnit = entitiesPerPersistenceUnit;
            return this;
        }

        public Builder setInitialMigrationCost(BigDecimal initialMigrationCost) {
            this.initialMigrationCost = initialMigrationCost;
            return this;
        }

        public Builder setBaseEntitiesMigrationCost(BigDecimal baseEntitiesMigrationCost) {
            this.baseEntitiesMigrationCost = baseEntitiesMigrationCost;
            return this;
        }

        public Builder setScreensTotalCost(BigDecimal screensTotalCost) {
            this.screensTotalCost = screensTotalCost;
            return this;
        }

        public Builder setLegacyListenersCost(BigDecimal legacyListenersCost) {
            this.legacyListenersCost = legacyListenersCost;
            return this;
        }

        public Builder setLegacyListeners(List<String> legacyListeners) {
            this.legacyListeners = legacyListeners;
            return this;
        }

        public Builder setAppComponents(List<CubaAppComponentInfo> appComponents) {
            this.appComponents = appComponents;
            return this;
        }

        public ProjectEstimationResult build() {
            return new ProjectEstimationResult(this);
        }
    }

    protected int countTotalEntities(Map<String, List<String>> entitiesPerPersistenceUnit) {
        AtomicInteger counter = new AtomicInteger();
        entitiesPerPersistenceUnit.forEach((unit, entities) -> {
            counter.addAndGet(entities.size());
        });
        return counter.get();
    }
}
