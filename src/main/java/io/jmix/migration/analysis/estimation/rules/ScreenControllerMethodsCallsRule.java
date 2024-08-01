package io.jmix.migration.analysis.estimation.rules;

import io.jmix.migration.analysis.model.ThresholdItem;
import org.apache.commons.lang3.Range;

import java.util.List;

import static io.jmix.migration.analysis.MetricCodes.METHOD_CALLS_AMOUNT_METRIC_CODE;


public class ScreenControllerMethodsCallsRule implements NumericMetricRule {

    protected final List<? extends ThresholdItem<Integer, Integer>> thresholds;

    public ScreenControllerMethodsCallsRule(List<? extends ThresholdItem<Integer, Integer>> thresholds) {
        this.thresholds = thresholds;
    }

    @Override
    public String getMetricCode() {
        return METHOD_CALLS_AMOUNT_METRIC_CODE;
    }

    @Override
    public int apply(int inputValue) {
        return thresholds.stream()
                .filter(item -> {
                    Range<Integer> range = item.getThresholdRange();
                    return range.contains(inputValue);
                })
                .findFirst()
                .map(ThresholdItem::getOutputValue)
                .orElse(0);
    }
}
