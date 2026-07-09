package io.jmix.migration.core.report;

import javax.annotation.Nullable;
import java.util.List;

public class OverviewSection implements ReportSection {

    public static final String TYPE = "overview";

    private final List<Kpi> kpis;
    private final String disclaimer;

    public OverviewSection(List<Kpi> kpis, String disclaimer) {
        this.kpis = List.copyOf(kpis);
        this.disclaimer = disclaimer;
    }

    @Override
    public String getId() {
        return "overview";
    }

    @Override
    public String getTitle() {
        return "Overview";
    }

    @Override
    public String getType() {
        return TYPE;
    }

    public List<Kpi> getKpis() {
        return kpis;
    }

    public String getDisclaimer() {
        return disclaimer;
    }

    public static class Kpi {
        private final String label;
        private final Object value;
        private final String sub;
        private final boolean accent;

        public Kpi(String label, Object value, @Nullable String sub, boolean accent) {
            this.label = label;
            this.value = value;
            this.sub = sub;
            this.accent = accent;
        }

        public String getLabel() {
            return label;
        }

        public Object getValue() {
            return value;
        }

        @Nullable
        public String getSub() {
            return sub;
        }

        public boolean isAccent() {
            return accent;
        }
    }
}
