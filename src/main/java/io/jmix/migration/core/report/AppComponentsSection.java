package io.jmix.migration.core.report;

import javax.annotation.Nullable;
import java.util.List;

public class AppComponentsSection implements ReportSection {

    public static final String TYPE = "app-components";

    private final List<Row> rows;

    public AppComponentsSection(List<Row> rows) {
        this.rows = List.copyOf(rows);
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

    public static class Row {
        private final String name;
        private final String packageName;
        private final String typeName;
        private final String originName;
        private final String notes;

        public Row(String name, @Nullable String packageName, @Nullable String typeName,
                   @Nullable String originName, String notes) {
            this.name = name;
            this.packageName = packageName;
            this.typeName = typeName;
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
        public String getOriginName() {
            return originName;
        }

        public String getNotes() {
            return notes;
        }
    }
}
