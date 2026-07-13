package io.jmix.migration.core.report;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Add-ons of a Jmix project with their status in the target Jmix version.
 */
public class AddonsSection implements ReportSection {

    public static final String TYPE = "addons";

    private final List<Row> rows;

    public AddonsSection(List<Row> rows) {
        this.rows = List.copyOf(rows);
    }

    @Override
    public String getId() {
        return "add-ons";
    }

    @Override
    public String getTitle() {
        return "Add-ons";
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
        private final String artifact;
        private final String statusName;
        private final String licenseName;
        private final String flowArtifact;
        private final String notes;
        private final Integer costHint;

        public Row(String name, String artifact, @Nullable String statusName, @Nullable String licenseName,
                   @Nullable String flowArtifact, String notes, @Nullable Integer costHint) {
            this.name = name;
            this.artifact = artifact;
            this.statusName = statusName;
            this.licenseName = licenseName;
            this.flowArtifact = flowArtifact;
            this.notes = notes;
            this.costHint = costHint;
        }

        public String getName() {
            return name;
        }

        public String getArtifact() {
            return artifact;
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
        public String getFlowArtifact() {
            return flowArtifact;
        }

        public String getNotes() {
            return notes;
        }

        @Nullable
        public Integer getCostHint() {
            return costHint;
        }
    }
}
