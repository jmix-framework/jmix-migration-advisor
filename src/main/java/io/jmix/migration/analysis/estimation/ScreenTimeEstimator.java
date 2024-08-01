package io.jmix.migration.analysis.estimation;

import io.jmix.migration.analysis.model.ScreenComplexityScore;
import io.jmix.migration.analysis.model.ThresholdItem;
import org.apache.commons.lang3.Range;

import java.math.BigDecimal;
import java.util.List;

public class ScreenTimeEstimator {

    protected final List<? extends ThresholdItem<Integer, BigDecimal>> thresholds;

    public ScreenTimeEstimator(List<? extends ThresholdItem<Integer, BigDecimal>> thresholds) {
        this.thresholds = thresholds;
    }

    public ThresholdItem<Integer, BigDecimal> estimate(ScreenComplexityScore score) {
        int value = adjustValue(score.getValue());

        ThresholdItem<Integer, BigDecimal> thresholdItem = thresholds.stream()
                .filter(item -> {
                    Range<Integer> thresholdRange = item.getThresholdRange();
                    return thresholdRange.contains(value);
                })
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No range found for score " + score.getValue()));

        return thresholdItem;
    }

    protected int adjustValue(int value) {
        //todo possible reduce value by some "default complexity".
        return value;
    }
}
