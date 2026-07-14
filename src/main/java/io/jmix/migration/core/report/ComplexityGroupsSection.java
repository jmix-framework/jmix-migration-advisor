package io.jmix.migration.core.report;

import io.jmix.migration.core.estimation.ThresholdItem;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
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

    /**
     * Builds the section from the estimation output: screens grouped by complexity threshold.
     * Groups follow the threshold order; screen names inside a group are sorted for a stable report.
     */
    public static ComplexityGroupsSection fromScreensPerComplexity(
            Map<ThresholdItem<Integer, BigDecimal>, List<String>> screensPerComplexity,
            Map<String, List<String>> requiresDecision) {

        List<ThresholdItem<Integer, BigDecimal>> orderedThresholds = new ArrayList<>(screensPerComplexity.keySet());
        orderedThresholds.sort(Comparator.comparingInt(ThresholdItem::getOrder));

        List<Group> groups = new ArrayList<>();
        long totalAmount = 0;
        BigDecimal totalHours = BigDecimal.ZERO;
        BigDecimal maxGroupTotal = BigDecimal.ZERO;
        for (ThresholdItem<Integer, BigDecimal> threshold : orderedThresholds) {
            List<String> screens = screensPerComplexity.get(threshold);
            BigDecimal cost = threshold.getOutputValue();
            int amount = screens.size();
            BigDecimal total = cost.multiply(BigDecimal.valueOf(amount));

            List<String> sortedScreens = screens.stream().sorted().toList();
            groups.add(new Group(threshold.getName(), amount, cost, total, sortedScreens));

            totalAmount += amount;
            totalHours = totalHours.add(total);
            if (total.compareTo(maxGroupTotal) > 0) {
                maxGroupTotal = total;
            }
        }
        return new ComplexityGroupsSection(groups, totalAmount, totalHours, maxGroupTotal, requiresDecision);
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
