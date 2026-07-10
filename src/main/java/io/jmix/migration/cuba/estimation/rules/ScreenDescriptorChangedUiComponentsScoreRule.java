package io.jmix.migration.cuba.estimation.rules;
import io.jmix.migration.core.estimation.NumericMetricRule;

import static io.jmix.migration.classicui.Metrics.SCREEN_DESCRIPTOR_CHANGED_UI_COMPONENTS_SCORE_METRIC_CODE;

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
        // inputValue is the accumulated extra complexity score of all changed UI components
        // in the screen; baseValue scales it and must not act as a flat per-screen addition
        return baseValue * inputValue;
    }
}
