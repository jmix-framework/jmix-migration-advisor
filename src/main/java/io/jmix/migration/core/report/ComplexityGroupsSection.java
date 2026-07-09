package io.jmix.migration.core.report;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class ComplexityGroupsSection implements ReportSection {

    public static final String TYPE = "complexity";

    private final List<Group> groups;
    private final long totalAmount;
    private final BigDecimal totalHours;
    private final BigDecimal maxGroupTotal;

    // Screens with components having no Jmix equivalent: screen name to component names
    private final Map<String, List<String>> requiresDecision;

    public ComplexityGroupsSection(List<Group> groups, long totalAmount, BigDecimal totalHours,
                                   BigDecimal maxGroupTotal, Map<String, List<String>> requiresDecision) {
        this.groups = List.copyOf(groups);
        this.totalAmount = totalAmount;
        this.totalHours = totalHours;
        this.maxGroupTotal = maxGroupTotal;
        this.requiresDecision = requiresDecision;
    }

    @Override
    public String getId() {
        return "screens";
    }

    @Override
    public String getTitle() {
        return "Screens complexity";
    }

    @Override
    public String getType() {
        return TYPE;
    }

    public List<Group> getGroups() {
        return groups;
    }

    public long getTotalAmount() {
        return totalAmount;
    }

    public BigDecimal getTotalHours() {
        return totalHours;
    }

    public BigDecimal getMaxGroupTotal() {
        return maxGroupTotal;
    }

    public Map<String, List<String>> getRequiresDecision() {
        return requiresDecision;
    }

    public static class Group {
        private final String name;
        private final int amount;
        private final BigDecimal cost;
        private final BigDecimal total;
        private final List<String> screens;

        public Group(String name, int amount, BigDecimal cost, BigDecimal total, List<String> screens) {
            this.name = name;
            this.amount = amount;
            this.cost = cost;
            this.total = total;
            this.screens = List.copyOf(screens);
        }

        public String getName() {
            return name;
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
}
