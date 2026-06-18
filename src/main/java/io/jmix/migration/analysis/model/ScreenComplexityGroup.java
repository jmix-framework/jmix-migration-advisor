package io.jmix.migration.analysis.model;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

public class ScreenComplexityGroup {
    private final String name;
    private final int order;
    private final int amount;
    private final BigDecimal cost;
    private final BigDecimal total;
    private final List<String> screens;

    public ScreenComplexityGroup(String name, int order, int amount, BigDecimal cost, BigDecimal total) {
        this(name, order, amount, cost, total, Collections.emptyList());
    }

    public ScreenComplexityGroup(String name, int order, int amount, BigDecimal cost, BigDecimal total, List<String> screens) {
        this.name = name;
        this.order = order;
        this.amount = amount;
        this.cost = cost;
        this.total = total;
        this.screens = screens == null ? Collections.emptyList() : screens;
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

    public List<String> getScreens() {
        return screens;
    }
}
