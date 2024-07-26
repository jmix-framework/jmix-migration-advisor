package io.jmix.migration.analysis.parser.screen;

import io.jmix.migration.model.NumericMetric;

public class MethodMetrics {

    public static final String METHOD_CALLS_AMOUNT_METRIC_CODE = "screen-controller-method-calls";
    public static final String UI_COMPONENT_CREATE_CALLS_AMOUNT_METRIC_CODE = "ui-component-create-calls";

    private MethodMetrics() {}

    public static NumericMetric createMethodCallsAmountMetric(int calls) {
        return new NumericMetric(METHOD_CALLS_AMOUNT_METRIC_CODE, calls);
    }

    public static NumericMetric createUiComponentsCreateAmountMetric(int calls) {
        return new NumericMetric(UI_COMPONENT_CREATE_CALLS_AMOUNT_METRIC_CODE, calls);
    }
}
