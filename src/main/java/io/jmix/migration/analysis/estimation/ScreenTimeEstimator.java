package io.jmix.migration.analysis.estimation;

import io.jmix.migration.model.ThresholdItem;
import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.tuple.Pair;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ScreenTimeEstimator {

    //protected final Map<Range<Integer>, BigDecimal> thresholdMap;
    //protected final List<ScreenComplexityGroup> complexityGroups;

    protected final List<? extends ThresholdItem<Integer, BigDecimal>> thresholds;

    public ScreenTimeEstimator(List<? extends ThresholdItem<Integer, BigDecimal>> thresholds) {
        /*this(Map.of(
                Range.of(0, 10), new BigDecimal("0.5"),
                Range.of(11, 25), new BigDecimal("2"),
                Range.of(26, 50), new BigDecimal("8"),
                Range.of(51, 100), new BigDecimal("16"),
                Range.of(101, Integer.MAX_VALUE), new BigDecimal("32")
        ));*/
        /*this(List.of(
                Pair.of(10, new BigDecimal("0.5")),
                Pair.of(25, new BigDecimal("2")),
                Pair.of(50, new BigDecimal("8")),
                Pair.of(100, new BigDecimal("16")),
                Pair.of(Integer.MAX_VALUE, new BigDecimal("32"))
        ));*/
        this.thresholds = thresholds;
    }

    /*public ScreenTimeEstimator(List<Pair<Integer, BigDecimal>> thresholdValues) {
        List<Pair<Integer, BigDecimal>> thresholdValuesInternal = new ArrayList<>(thresholdValues);
        thresholdValuesInternal.sort(Map.Entry.comparingByKey());
        int min = 0;
        int max;
        int order = 1;
        List<ScreenComplexityGroup> complexityGroupsTmp = new ArrayList<>();
        for (Pair<Integer, BigDecimal> thresholdValue : thresholdValuesInternal) {
            max = thresholdValue.getKey();
            complexityGroupsTmp.add(
                    new ScreenComplexityGroup("Level_" + order, Range.of(min, max), thresholdValue.getValue(), order)
            );
            min = max + 1;
            order++;
        }
        this.complexityGroups = complexityGroupsTmp;
    }*/

    /*public ScreenTimeEstimator(Map<Range<Integer>, BigDecimal> thresholdMap) {
        this.thresholdMap = thresholdMap;
    }*/

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

        /*ScreenComplexityGroup group = complexityGroups.stream()
                .filter(complexityGroup -> {
                    Range<Integer> range = complexityGroup.getRange();
                    boolean contains = range.contains(value);
                    return contains;
                }).findFirst()
                .orElseThrow(() -> new RuntimeException("No range found for score " + score.getValue()));
        return group;*/
    }

    /*public BigDecimal estimateHours(String name, ScreenComplexityScore score) {
        int value = adjustValue(score.getValue());
        *//*BigDecimal hours = thresholdMap.entrySet().stream()
                .filter(entry -> {
                    Range<Integer> range = entry.getKey();
                    boolean contains = range.contains(value);
                    return contains;
                }).findFirst().map(Map.Entry::getValue).orElse(BigDecimal.ZERO);*//*

        BigDecimal hours = complexityGroups.stream()
                .filter(complexityGroup -> {
                    Range<Integer> range = complexityGroup.getRange();
                    boolean contains = range.contains(value);
                    return contains;
                }).findFirst().map(ScreenComplexityGroup::getCost).orElse(BigDecimal.ZERO);
        return hours;
    }*/

    protected int adjustValue(int value) {
        //todo possible reduce value by some "default complexity".
        return value;
    }
}
