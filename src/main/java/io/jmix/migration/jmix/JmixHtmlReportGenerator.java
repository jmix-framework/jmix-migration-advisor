package io.jmix.migration.jmix;

import io.jmix.migration.core.report.AddonsSection;
import io.jmix.migration.core.report.HtmlReportWriter;
import io.jmix.migration.core.report.OverviewSection;
import io.jmix.migration.core.report.ReportModel;
import io.jmix.migration.core.report.ReportSection;
import io.jmix.migration.jmix.addon.JmixAddonInfo;
import io.jmix.migration.jmix.model.JmixProjectAnalysisResult;
import io.jmix.migration.core.project.JmixModule;
import io.jmix.migration.core.project.JmixProjectDescriptor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Builds the platform-neutral {@link ReportModel} from the Jmix analysis result
 * and renders it via {@link HtmlReportWriter}.
 */
public class JmixHtmlReportGenerator {

    protected static final String REPORT_TITLE = "Jmix 1.x → Jmix migration report";
    protected static final String NOT_DETECTED = "not detected";
    protected static final String DISCLAIMER = "Preliminary report: it covers the build structure and add-ons only."
            + " Screens, entities, security and configuration analysis is not implemented yet;"
            + " treat this as an inventory, not an estimation.";

    private final HtmlReportWriter reportWriter;

    public JmixHtmlReportGenerator() {
        this.reportWriter = new HtmlReportWriter();
    }

    protected void generateHtmlReport(String project, JmixProjectAnalysisResult result) {
        reportWriter.writeToFile(reportWriter.createDefaultFileName(), buildReportModel(project, result));
    }

    /**
     * Renders the report to a string. Separated from file writing so tests can capture
     * the report content directly.
     */
    public String generateReportContent(String project, JmixProjectAnalysisResult result) {
        return reportWriter.render(buildReportModel(project, result));
    }

    public ReportModel buildReportModel(String project, JmixProjectAnalysisResult result) {
        AddonsSection addonsSection = buildAddonsSection(result);

        List<ReportSection> sections = new ArrayList<>();
        sections.add(buildOverviewSection(result, addonsSection));
        sections.add(addonsSection);
        return new ReportModel(project, REPORT_TITLE, sections);
    }

    protected OverviewSection buildOverviewSection(JmixProjectAnalysisResult result, AddonsSection addonsSection) {
        JmixProjectDescriptor descriptor = result.getProjectDescriptor();

        String effectiveVersion = descriptor.getEffectiveJmixVersion();
        String versionSub = descriptor.getJmixPluginVersion() != null
                ? "plugin " + descriptor.getJmixPluginVersion() : null;

        String modulesSub = descriptor.getModules().size() <= 4
                ? descriptor.getModules().stream().map(JmixModule::getName).collect(Collectors.joining(", "))
                : null;

        long unknownAddons = addonsSection.getRows().stream()
                .filter(row -> "UNKNOWN".equals(row.getStatusName()))
                .count();

        List<OverviewSection.Kpi> kpis = List.of(
                new OverviewSection.Kpi("Jmix version",
                        effectiveVersion == null ? NOT_DETECTED : effectiveVersion, versionSub, true),
                new OverviewSection.Kpi("Java",
                        descriptor.getJavaVersion() == null ? NOT_DETECTED : descriptor.getJavaVersion(), null, false),
                new OverviewSection.Kpi("Modules", descriptor.getModules().size(), modulesSub, false),
                new OverviewSection.Kpi("Add-ons", addonsSection.getRows().size(),
                        unknownAddons > 0 ? unknownAddons + " without Jmix data" : "all recognized", false),
                new OverviewSection.Kpi("Base package",
                        result.getBasePackage() == null ? NOT_DETECTED : result.getBasePackage(), null, false)
        );
        return new OverviewSection(kpis, DISCLAIMER);
    }

    protected AddonsSection buildAddonsSection(JmixProjectAnalysisResult result) {
        List<AddonsSection.Row> rows = new ArrayList<>();
        for (JmixProjectAnalysisResult.ResolvedAddon addon : result.getAddons()) {
            JmixAddonInfo info = addon.getAddonInfo();
            if (info == null) {
                rows.add(new AddonsSection.Row(
                        addon.getDependency().getArtifact(),
                        addon.getDependency().getGroupArtifact(),
                        "UNKNOWN",
                        null,
                        "No data about this add-on. Check the Jmix marketplace manually",
                        null));
            } else {
                rows.add(new AddonsSection.Row(
                        info.getName(),
                        info.getArtifact(),
                        info.getFlowStatus().name(),
                        info.getFlowArtifact(),
                        info.getNotes(),
                        info.getCostHint()));
            }
        }
        return new AddonsSection(rows);
    }
}
