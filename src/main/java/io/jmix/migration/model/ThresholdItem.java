package io.jmix.migration.model;

import org.apache.commons.lang3.Range;

import java.util.Objects;

public class ThresholdItem<T, V> {
    private final String name;
    private final Range<T> thresholdRange;
    private final V outputValue;
    private final int order;

    public ThresholdItem(String name, Range<T> thresholdRange, V outputValue, int order) {
        this.name = name;
        this.thresholdRange = thresholdRange;
        this.outputValue = outputValue;
        this.order = order;
    }

    public String getName() {
        return name;
    }

    public Range<T> getThresholdRange() {
        return thresholdRange;
    }

    public V getOutputValue() {
        return outputValue;
    }

    public int getOrder() {
        return order;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ThresholdItem<?, ?> that = (ThresholdItem<?, ?>) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }
}
