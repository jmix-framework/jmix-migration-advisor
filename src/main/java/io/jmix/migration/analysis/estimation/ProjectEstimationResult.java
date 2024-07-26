package io.jmix.migration.analysis.estimation;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class ProjectEstimationResult {
    private final Map<ScreenComplexityGroup, List<String>> screensPerGroup;
    private final Map<String, Integer> allUiComponents;
    private final int entitiesAmount;
    private final Map<String, List<String>> entitiesPerPersistenceUnit;
    private final BigDecimal totalScreensCost;

    private final int initialMigrationCost;
    private final int baseEntitiesMigrationCost;

    private ProjectEstimationResult(Builder builder) {
        this.screensPerGroup = builder.screensPerGroup;
        this.allUiComponents = builder.allUiComponents;
        this.entitiesAmount = countTotalEntities(builder.entitiesPerPersistenceUnit);
        this.entitiesPerPersistenceUnit = builder.entitiesPerPersistenceUnit;
        this.initialMigrationCost = builder.initialMigrationCost;
        this.baseEntitiesMigrationCost = builder.baseEntitiesMigrationCost;
        this.totalScreensCost = builder.totalScreensCost;
    }

    public Map<ScreenComplexityGroup, List<String>> getScreensPerGroup() {
        return screensPerGroup;
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

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Map<ScreenComplexityGroup, List<String>> screensPerGroup;
        private Map<String, Integer> allUiComponents;
        private Map<String, List<String>> entitiesPerPersistenceUnit;

        private BigDecimal totalScreensCost;

        private int initialMigrationCost;
        private int baseEntitiesMigrationCost;

        public Builder() {
        }

        public Builder setScreensPerGroup(Map<ScreenComplexityGroup, List<String>> screensPerGroup) {
            this.screensPerGroup = screensPerGroup;
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
