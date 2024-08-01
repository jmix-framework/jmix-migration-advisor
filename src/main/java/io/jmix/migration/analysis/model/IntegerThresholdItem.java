package io.jmix.migration.analysis.model;

import org.apache.commons.lang3.Range;

public class IntegerThresholdItem extends ThresholdItem<Integer, Integer> {

    public IntegerThresholdItem(String name, Range<Integer> thresholdRange, Integer outputValue, int order) {
        super(name, thresholdRange, outputValue, order);
    }
}
