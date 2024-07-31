package io.jmix.migration.model;

import org.apache.commons.lang3.Range;

import java.math.BigDecimal;

public class BigDecimalThresholdItem extends ThresholdItem<Integer, BigDecimal> {
    public BigDecimalThresholdItem(String name, Range<Integer> thresholdRange, BigDecimal outputValue, int order) {
        super(name, thresholdRange, outputValue, order);
    }
}
