package io.jmix.migration.core.report;

import io.jmix.migration.core.incident.Requires;
import io.jmix.migration.core.incident.UiComponentIssue;
import io.jmix.migration.core.incident.UiComponentIssuesRegistry;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UiComponentsSection implements ReportSection {

    public static final String TYPE = "ui-components";

    private final List<Row> rows;

    public UiComponentsSection(List<Row> rows) {
        this.rows = List.copyOf(rows);
    }

    /**
     * Builds the section from per-component usage counters, keeping only the components
     * having a registry entry. The actual component name is used for the row, not
     * {@code issue.getComponent()}: for prefix entries ({@code match="chart:*"}) they differ.
     */
    public static UiComponentsSection fromComponentCounters(Map<String, Integer> componentCounters,
                                                            UiComponentIssuesRegistry issuesRegistry) {
        List<String> components = new ArrayList<>(componentCounters.keySet());
        components.sort(String::compareTo);

        List<Row> rows = new ArrayList<>();
        for (String component : components) {
            UiComponentIssue issue = issuesRegistry.getIssue(component);
            if (issue == null) {
                continue;
            }
            List<RequirementBadge> requirementBadges = issue.getRequires().stream()
                    .map(item -> new RequirementBadge(item.getKindName(), item.getSubject()))
                    .toList();
            rows.add(new Row(
                    component,
                    componentCounters.get(component),
                    issue.getNotes(),
                    issue.getType() == null ? null : issue.getType().name(),
                    issue.getExtraComplexityScore(),
                    requirementBadges));
        }
        return new UiComponentsSection(rows);
    }

    @Override
    public String getId() {
        return "ui";
    }

    @Override
    public String getTitle() {
        return "UI components";
    }

    @Override
    public String getType() {
        return TYPE;
    }

    public List<Row> getRows() {
        return rows;
    }

    public static class Row {
        private final String name;
        private final int amount;
        private final String notes;
        private final String typeName;
        private final int extraComplexityScore;
        private final List<RequirementBadge> requires;

        public Row(String name, int amount, String notes, @Nullable String typeName,
                   int extraComplexityScore, List<RequirementBadge> requires) {
            this.name = name;
            this.amount = amount;
            this.notes = notes;
            this.typeName = typeName;
            this.extraComplexityScore = extraComplexityScore;
            this.requires = List.copyOf(requires);
        }

        public String getName() {
            return name;
        }

        public int getAmount() {
            return amount;
        }

        public String getNotes() {
            return notes;
        }

        @Nullable
        public String getTypeName() {
            return typeName;
        }

        public int getExtraComplexityScore() {
            return extraComplexityScore;
        }

        public List<RequirementBadge> getRequires() {
            return requires;
        }
    }

    public static class RequirementBadge {
        private final String kindName;
        private final String subject;

        public RequirementBadge(String kindName, String subject) {
            this.kindName = kindName;
            this.subject = subject;
        }

        public String getKindName() {
            return kindName;
        }

        public String getSubject() {
            return subject;
        }
    }
}
