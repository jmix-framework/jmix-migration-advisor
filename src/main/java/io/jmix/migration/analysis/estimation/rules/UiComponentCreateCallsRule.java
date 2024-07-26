package io.jmix.migration.analysis.estimation.rules;

import static io.jmix.migration.analysis.parser.screen.MethodMetrics.UI_COMPONENT_CREATE_CALLS_AMOUNT_METRIC_CODE;

public class UiComponentCreateCallsRule implements NumericMetricRule {
    @Override
    public String getMetricCode() {
        return UI_COMPONENT_CREATE_CALLS_AMOUNT_METRIC_CODE;
    }

    @Override
    public int apply(int inputValue) {
        return inputValue * 10; //todo from xml
    }
}
