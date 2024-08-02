package io.jmix.migration.analysis.estimation.rules;

import static io.jmix.migration.analysis.Metrics.LEGACY_ENTITY_LISTENERS_METRIC_CODE;

public class LegacyEntityListenersRule implements NumericMetricRule {

    private final int cost;

    public LegacyEntityListenersRule(int cost) {
        this.cost = cost;
    }

    @Override
    public String getMetricCode() {
        return LEGACY_ENTITY_LISTENERS_METRIC_CODE;
    }

    @Override
    public int apply(int inputValue) {
        return inputValue * cost;
    }
}
