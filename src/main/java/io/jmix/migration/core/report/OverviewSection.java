package io.jmix.migration.core.report;

import javax.annotation.Nullable;
import java.util.List;

public class OverviewSection implements ReportSection {

    public static final String TYPE = "overview";

    private final List<Fact> facts;
    private final List<Kpi> kpis;
    private final String disclaimer;

    public OverviewSection(List<Kpi> kpis, String disclaimer) {
        this(List.of(), kpis, disclaimer);
    }

    public OverviewSection(List<Fact> facts, List<Kpi> kpis, String disclaimer) {
        this.facts = List.copyOf(facts);
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

    /**
     * General project facts (versions, packages) rendered as a text strip above the KPI cards.
     * Unlike KPIs, fact values are arbitrary-length strings.
     */
    public List<Fact> getFacts() {
        return facts;
    }

    public List<Kpi> getKpis() {
        return kpis;
    }

    public String getDisclaimer() {
        return disclaimer;
    }

    public static class Fact {
        private final String label;
        private final String value;

        public Fact(String label, String value) {
            this.label = label;
            this.value = value;
        }

        public String getLabel() {
            return label;
        }

        public String getValue() {
            return value;
        }
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
