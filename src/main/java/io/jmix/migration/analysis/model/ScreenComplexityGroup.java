package io.jmix.migration.analysis.model;

import java.math.BigDecimal;

public class ScreenComplexityGroup {
    private final String name;
    private final int order;
    private final int amount;
    private final BigDecimal cost;
    private final BigDecimal total;

    public ScreenComplexityGroup(String name, int order, int amount, BigDecimal cost, BigDecimal total) {
        this.name = name;
        this.order = order;
        this.amount = amount;
        this.cost = cost;
        this.total = total;
    }

    public String getName() {
        return name;
    }

    public int getOrder() {
        return order;
    }

    public int getAmount() {
        return amount;
    }

    public BigDecimal getCost() {
        return cost;
    }

    public BigDecimal getTotal() {
        return total;
    }
}
