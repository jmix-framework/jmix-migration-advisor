package io.jmix.migration.analysis.estimation;

import org.apache.commons.lang3.Range;

import java.math.BigDecimal;
import java.util.Objects;

public class ScreenComplexityGroup {

    private final String name;
    private final Range<Integer> range;
    private final BigDecimal cost;
    private final int order;

    public ScreenComplexityGroup(String name, Range<Integer> range, BigDecimal cost, int order) {
        this.name = name;
        this.range = range;
        this.cost = cost;
        this.order = order;
    }

    public String getName() {
        return name;
    }

    public Range<Integer> getRange() {
        return range;
    }

    public BigDecimal getCost() {
        return cost;
    }

    public int getOrder() {
        return order;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ScreenComplexityGroup that = (ScreenComplexityGroup) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }
}
