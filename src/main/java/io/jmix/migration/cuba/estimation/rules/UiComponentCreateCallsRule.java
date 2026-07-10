package io.jmix.migration.cuba.estimation.rules;
import io.jmix.migration.core.estimation.NumericMetricRule;

import static io.jmix.migration.classicui.Metrics.UI_COMPONENT_CREATE_CALLS_AMOUNT_METRIC_CODE;

public class UiComponentCreateCallsRule implements NumericMetricRule {

    private final int cost;

    public UiComponentCreateCallsRule(int cost) {
        this.cost = cost;
    }

    @Override
    public String getMetricCode() {
        return UI_COMPONENT_CREATE_CALLS_AMOUNT_METRIC_CODE;
    }

    @Override
    public int apply(int inputValue) {
        return inputValue * cost;
    }
}
