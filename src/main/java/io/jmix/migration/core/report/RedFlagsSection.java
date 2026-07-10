package io.jmix.migration.core.report;

import java.util.List;

/**
 * Findings that cannot be migrated automatically and need manual estimation.
 */
public class RedFlagsSection implements ReportSection {

    public static final String TYPE = "red-flags";

    private final List<Row> rows;

    public RedFlagsSection(List<Row> rows) {
        this.rows = List.copyOf(rows);
    }

    @Override
    public String getId() {
        return "red-flags";
    }

    @Override
    public String getTitle() {
        return "Red flags";
    }

    @Override
    public String getType() {
        return TYPE;
    }

    public List<Row> getRows() {
        return rows;
    }

    public static class Row {
        private final String category;
        private final String subject;
        private final String notes;

        public Row(String category, String subject, String notes) {
            this.category = category;
            this.subject = subject;
            this.notes = notes;
        }

        public String getCategory() {
            return category;
        }

        public String getSubject() {
            return subject;
        }

        public String getNotes() {
            return notes;
        }
    }
}
