package io.jmix.migration.analysis.estimation;

import io.jmix.migration.model.ThresholdItem;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class ProjectEstimationResult {
    private final Map<ThresholdItem<Integer, BigDecimal>, List<String>> screensPerComplexity;
    private final Map<String, Integer> allUiComponents;
    private final int entitiesAmount;
    private final Map<String, List<String>> entitiesPerPersistenceUnit;
    private final BigDecimal totalScreensCost;

    private final int initialMigrationCost;
    private final int baseEntitiesMigrationCost;

    private ProjectEstimationResult(Builder builder) {
        this.screensPerComplexity = builder.screensPerComplexity;
        this.allUiComponents = builder.allUiComponents;
        this.entitiesAmount = countTotalEntities(builder.entitiesPerPersistenceUnit);
        this.entitiesPerPersistenceUnit = builder.entitiesPerPersistenceUnit;
        this.initialMigrationCost = builder.initialMigrationCost;
        this.baseEntitiesMigrationCost = builder.baseEntitiesMigrationCost;
        this.totalScreensCost = builder.totalScreensCost;
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

    public int getInitialMigrationCost() {
        return initialMigrationCost;
    }

    public int getBaseEntitiesMigrationCost() {
        return baseEntitiesMigrationCost;
    }

    public BigDecimal getTotalScreensCost() {
        return totalScreensCost;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Map<ThresholdItem<Integer, BigDecimal>, List<String>> screensPerComplexity;
        private Map<String, Integer> allUiComponents;
        private Map<String, List<String>> entitiesPerPersistenceUnit;

        private BigDecimal totalScreensCost;

        private int initialMigrationCost;
        private int baseEntitiesMigrationCost;

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

        public Builder setInitialMigrationCost(int initialMigrationCost) {
            this.initialMigrationCost = initialMigrationCost;
            return this;
        }

        public Builder setBaseEntitiesMigrationCost(int baseEntitiesMigrationCost) {
            this.baseEntitiesMigrationCost = baseEntitiesMigrationCost;
            return this;
        }

        public Builder setTotalScreensCost(BigDecimal totalScreensCost) {
            this.totalScreensCost = totalScreensCost;
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
