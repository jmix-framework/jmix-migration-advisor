package io.jmix.migration.analysis.estimation.rules;

import static io.jmix.migration.analysis.MetricCodes.SCREEN_DESCRIPTOR_CHANGED_UI_COMPONENTS_SCORE_METRIC_CODE;

public class ScreenDescriptorChangedUiComponentsScoreRule implements NumericMetricRule {

    private final int baseValue;

    public ScreenDescriptorChangedUiComponentsScoreRule(int baseValue) {
        this.baseValue = baseValue;
    }

    @Override
    public String getMetricCode() {
        return SCREEN_DESCRIPTOR_CHANGED_UI_COMPONENTS_SCORE_METRIC_CODE;
    }


    @Override
    public int apply(int inputValue) {
        return baseValue + inputValue;
    }
}
