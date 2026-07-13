package io.jmix.migration.core.report;

import javax.annotation.Nullable;
import java.util.List;

public class AppComponentsSection implements ReportSection {

    public static final String TYPE = "app-components";

    private final List<Row> rows;
    private final List<Escalation> escalations;

    public AppComponentsSection(List<Row> rows, List<Escalation> escalations) {
        this.rows = List.copyOf(rows);
        this.escalations = List.copyOf(escalations);
    }

    @Override
    public String getId() {
        return "app-components";
    }

    @Override
    public String getTitle() {
        return "App components";
    }

    @Override
    public String getType() {
        return TYPE;
    }

    public List<Row> getRows() {
        return rows;
    }

    /**
     * App components with no equivalent in the target version: not priced, surfaced for a manual decision.
     */
    public List<Escalation> getEscalations() {
        return escalations;
    }

    public static class Escalation {
        private final String name;
        private final String notes;

        public Escalation(String name, String notes) {
            this.name = name;
            this.notes = notes;
        }

        public String getName() {
            return name;
        }

        public String getNotes() {
            return notes;
        }
    }

    public static class Row {
        private final String name;
        private final String packageName;
        private final String typeName;
        private final String statusName;
        private final String licenseName;
        private final String originName;
        private final String notes;

        public Row(String name, @Nullable String packageName, @Nullable String typeName, @Nullable String statusName,
                   @Nullable String licenseName, @Nullable String originName, String notes) {
            this.name = name;
            this.packageName = packageName;
            this.typeName = typeName;
            this.statusName = statusName;
            this.licenseName = licenseName;
            this.originName = originName;
            this.notes = notes;
        }

        public String getName() {
            return name;
        }

        @Nullable
        public String getPackageName() {
            return packageName;
        }

        @Nullable
        public String getTypeName() {
            return typeName;
        }

        @Nullable
        public String getStatusName() {
            return statusName;
        }

        @Nullable
        public String getLicenseName() {
            return licenseName;
        }

        @Nullable
        public String getOriginName() {
            return originName;
        }

        public String getNotes() {
            return notes;
        }
    }
}
