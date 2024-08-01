package io.jmix.migration.analysis;

import io.jmix.migration.analysis.model.NumericMetric;

public class MetricCodes {

    public static final String METHOD_CALLS_AMOUNT_METRIC_CODE = "screen-controller-method-calls";
    public static final String UI_COMPONENT_CREATE_CALLS_AMOUNT_METRIC_CODE = "ui-component-create-calls";

    public static final String SCREEN_DESCRIPTOR_NESTED_DATA_ITEMS_METRIC_CODE = "screen-descriptor-nested-data-items";
    public static final String SCREEN_DESCRIPTOR_CHANGED_UI_COMPONENTS_SCORE_METRIC_CODE = "screen-descriptor-changed-ui-components-complexity-score";
    public static final String SCREEN_DESCRIPTOR_EXTENDS_SCREEN_METRIC_CODE = "screen-descriptor-extends-screen";
    public static final String LEGACY_ENTITY_LISTENERS_METRIC_CODE = "legacy-entity-listeners";

    public static NumericMetric createMethodCallsAmountMetric(int calls) {
        return new NumericMetric(METHOD_CALLS_AMOUNT_METRIC_CODE, calls);
    }

    public static NumericMetric createUiComponentsCreateAmountMetric(int calls) {
        return new NumericMetric(UI_COMPONENT_CREATE_CALLS_AMOUNT_METRIC_CODE, calls);
    }

    public static NumericMetric createScreenDescriptorNestedDataItemsMetric(int amount) {
        return new NumericMetric(SCREEN_DESCRIPTOR_NESTED_DATA_ITEMS_METRIC_CODE, amount);
    }

    public static NumericMetric createScreenDescriptorChangedUiComponentsScoreMetric(int score) {
        return new NumericMetric(SCREEN_DESCRIPTOR_CHANGED_UI_COMPONENTS_SCORE_METRIC_CODE, score);
    }

    public static NumericMetric createScreenDescriptorExtendsScreenMetric() {
        return new NumericMetric(SCREEN_DESCRIPTOR_EXTENDS_SCREEN_METRIC_CODE, 1);
    }

    public static NumericMetric createLegacyEntityListenersMetric(int amount) {
        return new NumericMetric(LEGACY_ENTITY_LISTENERS_METRIC_CODE, amount);
    }
}
