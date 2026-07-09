package io.jmix.migration.cuba.estimation.rules;
import io.jmix.migration.core.estimation.NumericMetricRule;

import io.jmix.migration.cuba.Metrics;

public class ScreenDescriptorNestedDataItemsRule implements NumericMetricRule {

    private final int cost;

    public ScreenDescriptorNestedDataItemsRule(int cost) {
        this.cost = cost;
    }

    @Override
    public String getMetricCode() {
        return Metrics.SCREEN_DESCRIPTOR_NESTED_DATA_ITEMS_METRIC_CODE;
    }

    @Override
    public int apply(int inputValue) {
        return inputValue * cost;
    }
}
