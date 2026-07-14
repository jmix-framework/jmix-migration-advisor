package io.jmix.migration.classicui.estimation.rules;
import io.jmix.migration.core.estimation.NumericMetricRule;

import static io.jmix.migration.classicui.Metrics.SCREEN_DESCRIPTOR_EXTENDS_SCREEN_METRIC_CODE;

// todo Create BooleanMetricRule?
public class ScreenDescriptorExtendsScreenRule implements NumericMetricRule {

    private final int score;

    public ScreenDescriptorExtendsScreenRule(int score) {
        this.score = score;
    }

    @Override
    public String getMetricCode() {
        return SCREEN_DESCRIPTOR_EXTENDS_SCREEN_METRIC_CODE;
    }

    @Override
    public int apply(int inputValue) {
        return score;
    }
}
