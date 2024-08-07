package io.jmix.migration.analysis.model;

import java.math.BigDecimal;

public class EstimationItem {
    private final String category;
    private final BigDecimal estimation;

    public EstimationItem(String category, BigDecimal estimation) {
        this.category = category;
        this.estimation = estimation;
    }

    public String getCategory() {
        return category;
    }

    public BigDecimal getEstimation() {
        return estimation;
    }
}
