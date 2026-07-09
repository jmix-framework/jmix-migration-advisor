package io.jmix.migration.core.report;

import javax.annotation.Nullable;
import java.util.List;

public class NotesSection implements ReportSection {

    public static final String TYPE = "notes";

    private final List<Row> rows;

    public NotesSection(List<Row> rows) {
        this.rows = List.copyOf(rows);
    }

    @Override
    public String getId() {
        return "misc";
    }

    @Override
    public String getTitle() {
        return "Misc notes";
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
        private final String code;
        private final String notes;

        public Row(String name, @Nullable String code, String notes) {
            this.name = name;
            this.code = code;
            this.notes = notes;
        }

        public String getName() {
            return name;
        }

        @Nullable
        public String getCode() {
            return code;
        }

        public String getNotes() {
            return notes;
        }
    }
}
