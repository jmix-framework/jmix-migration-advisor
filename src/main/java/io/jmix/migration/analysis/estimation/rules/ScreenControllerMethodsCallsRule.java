package io.jmix.migration.analysis.estimation.rules;

import io.jmix.migration.model.ThresholdItem;
import org.apache.commons.lang3.Range;

import java.util.List;
import java.util.Map;

import static io.jmix.migration.analysis.parser.screen.MethodMetrics.METHOD_CALLS_AMOUNT_METRIC_CODE;

public class ScreenControllerMethodsCallsRule implements NumericMetricRule {

    //protected Map<Range<Integer>, Integer> thresholdMap;
    protected final List<? extends ThresholdItem<Integer, Integer>> thresholds;

    /*public ScreenControllerMethodsCallsRule() {
        this(Map.of(
                Range.of(0, 5), 0,
                Range.of(6, 30), 10,
                Range.of(31, 80), 40,
                Range.of(81, 150), 80,
                Range.of(151, Integer.MAX_VALUE), 180
        ));
    }

    public ScreenControllerMethodsCallsRule(Map<Range<Integer>, Integer> thresholdMap) {
        this.thresholdMap = thresholdMap; //todo from xml
    }*/

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

        /*Integer score = thresholdMap.entrySet().stream()
                .filter(entry -> {
                    Range<Integer> range = entry.getKey();
                    boolean contains = range.contains(inputValue);
                    return contains;
                }).findFirst().map(Map.Entry::getValue).orElse(0);
        return score;*/
    }
}
