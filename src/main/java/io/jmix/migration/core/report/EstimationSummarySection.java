package io.jmix.migration.core.report;

import java.math.BigDecimal;
import java.util.List;

public class EstimationSummarySection implements ReportSection {

    public static final String TYPE = "estimations";

    private final List<Row> rows;
    private final BigDecimal total;

    public EstimationSummarySection(List<Row> rows, BigDecimal total) {
        this.rows = List.copyOf(rows);
        this.total = total;
    }

    @Override
    public String getId() {
        return "estimations";
    }

    @Override
    public String getTitle() {
        return "Estimations";
    }

    @Override
    public String getType() {
        return TYPE;
    }

    public List<Row> getRows() {
        return rows;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public static class Row {
        private final String category;
        private final BigDecimal hours;

        public Row(String category, BigDecimal hours) {
            this.category = category;
            this.hours = hours;
        }

        public String getCategory() {
            return category;
        }

        public BigDecimal getHours() {
            return hours;
        }
    }
}
