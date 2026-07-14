package io.jmix.migration.core.report;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Mechanical renames: a migration checklist of artifacts that change name or location
 * without a redesign.
 */
public class RenamesSection implements ReportSection {

    public static final String TYPE = "renames";

    private final List<Row> rows;

    public RenamesSection(List<Row> rows) {
        this.rows = List.copyOf(rows);
    }

    @Override
    public String getId() {
        return "renames";
    }

    @Override
    public String getTitle() {
        return "Mechanical renames";
    }

    @Override
    public String getType() {
        return TYPE;
    }

    public List<Row> getRows() {
        return rows;
    }

    public static class Row {
        private final String subject;
        private final String current;
        private final String replacement;
        private final String notes;

        public Row(String subject, String current, @Nullable String replacement, String notes) {
            this.subject = subject;
            this.current = current;
            this.replacement = replacement;
            this.notes = notes;
        }

        public String getSubject() {
            return subject;
        }

        public String getCurrent() {
            return current;
        }

        @Nullable
        public String getReplacement() {
            return replacement;
        }

        public String getNotes() {
            return notes;
        }
    }
}
