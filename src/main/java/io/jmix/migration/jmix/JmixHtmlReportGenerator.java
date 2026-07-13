package io.jmix.migration.jmix;

import io.jmix.migration.core.incident.UiComponentIssuesRegistry;
import io.jmix.migration.core.model.TargetStatus;
import io.jmix.migration.core.project.JmixModule;
import io.jmix.migration.core.project.JmixProjectDescriptor;
import io.jmix.migration.core.report.AddonsSection;
import io.jmix.migration.core.report.ComplexityGroupsSection;
import io.jmix.migration.core.report.EstimationSummarySection;
import io.jmix.migration.core.report.HtmlReportWriter;
import io.jmix.migration.core.report.NotesSection;
import io.jmix.migration.core.report.OverviewSection;
import io.jmix.migration.core.report.RedFlagsSection;
import io.jmix.migration.core.report.RenamesSection;
import io.jmix.migration.core.report.ReportModel;
import io.jmix.migration.core.report.ReportSection;
import io.jmix.migration.core.report.UiComponentsSection;
import io.jmix.migration.core.report.UnparsedFilesSection;
import io.jmix.migration.jmix.addon.JmixAddonInfo;
import io.jmix.migration.jmix.model.JmixConfigInfo;
import io.jmix.migration.jmix.model.JmixDataModelInfo;
import io.jmix.migration.jmix.model.JmixEstimationResult;
import io.jmix.migration.jmix.model.JmixProjectAnalysisResult;
import io.jmix.migration.jmix.model.JmixSourcesScanResult;

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
    protected static final String DISCLAIMER = "Estimates are expert-set and NOT calibrated against completed"
            + " migrations yet; treat the numbers as a rough lower-range estimate. Red flags are not included"
            + " in the hours. Some aspects cannot be evaluated automatically and need manual analysis."
            + " Kotlin sources are not analyzed.";

    private final UiComponentIssuesRegistry uiComponentIssuesRegistry;
    private final HtmlReportWriter reportWriter;

    public JmixHtmlReportGenerator(UiComponentIssuesRegistry uiComponentIssuesRegistry) {
        this.uiComponentIssuesRegistry = uiComponentIssuesRegistry;
        this.reportWriter = new HtmlReportWriter();
    }

    public JmixHtmlReportGenerator() {
        this(UiComponentIssuesRegistry.create());
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
        sections.add(buildEstimationsSection(result.getEstimation()));
        sections.add(ComplexityGroupsSection.fromScreensPerComplexity(
                result.getEstimation().getScreensPerComplexity(),
                result.getEstimation().getScreensRequireDecision()));
        sections.add(UiComponentsSection.fromComponentCounters(result.getUiComponents(), uiComponentIssuesRegistry));
        sections.add(addonsSection);
        sections.add(buildRedFlagsSection(result));
        sections.add(buildRenamesSection(result));
        sections.add(buildNotesSection(result));
        if (!result.getUnparsedFiles().isEmpty()) {
            sections.add(new UnparsedFilesSection(result.getUnparsedFiles()));
        }
        return new ReportModel(project, REPORT_TITLE, sections);
    }

    protected EstimationSummarySection buildEstimationsSection(JmixEstimationResult estimation) {
        List<EstimationSummarySection.Row> rows = List.of(
                new EstimationSummarySection.Row("Initial migration", estimation.getInitialMigrationCost()),
                new EstimationSummarySection.Row("Jakarta namespace sweep", estimation.getJakartaSweepCost()),
                new EstimationSummarySection.Row("Screens", estimation.getScreensCost()),
                new EstimationSummarySection.Row("Add-ons", estimation.getAddonsCost()),
                new EstimationSummarySection.Row("Security roles", estimation.getSecurityRolesCost()),
                new EstimationSummarySection.Row("Configuration", estimation.getConfigCost()),
                new EstimationSummarySection.Row("Custom themes", estimation.getCustomThemesCost())
        );
        return new EstimationSummarySection(rows, estimation.getTotalCost());
    }

    protected OverviewSection buildOverviewSection(JmixProjectAnalysisResult result, AddonsSection addonsSection) {
        JmixProjectDescriptor descriptor = result.getProjectDescriptor();
        JmixDataModelInfo dataModel = result.getDataModel();
        JmixSourcesScanResult sourcesScan = result.getSourcesScan();

        String effectiveVersion = descriptor.getEffectiveJmixVersion();
        String jmixVersionFact = effectiveVersion == null ? NOT_DETECTED : effectiveVersion;
        if (descriptor.getJmixPluginVersion() != null
                && !descriptor.getJmixPluginVersion().equals(effectiveVersion)) {
            jmixVersionFact += " (plugin " + descriptor.getJmixPluginVersion() + ")";
        }

        String modulesFact = descriptor.getModules().size() <= 4
                ? descriptor.getModules().stream().map(JmixModule::getName).collect(Collectors.joining(", "))
                : String.valueOf(descriptor.getModules().size());

        // General project facts go to the text strip; the KPI cards keep numeric metrics only
        List<OverviewSection.Fact> facts = new ArrayList<>();
        facts.add(new OverviewSection.Fact("Jmix version", jmixVersionFact));
        facts.add(new OverviewSection.Fact("Base package",
                result.getBasePackage() == null ? NOT_DETECTED : result.getBasePackage()));
        facts.add(new OverviewSection.Fact("Modules", modulesFact));
        if (descriptor.getJavaVersion() != null) {
            facts.add(new OverviewSection.Fact("Java", descriptor.getJavaVersion()));
        }

        long unknownAddons = addonsSection.getRows().stream()
                .filter(row -> "UNKNOWN".equals(row.getStatusName()))
                .count();
        String addonsSub = buildAddonsKpiSub(addonsSection.getEscalations().size(), unknownAddons);

        String entitiesSub = "+ " + dataModel.getDtoEntities().size() + " DTO, "
                + dataModel.getEmbeddables().size() + " embeddable, "
                + dataModel.getEnums().size() + " enums";

        int rolesCount = sourcesScan.getResourceRoles().size() + sourcesScan.getRowLevelRoles().size();

        List<OverviewSection.Kpi> kpis = List.of(
                new OverviewSection.Kpi("Total effort", result.getEstimation().getTotalCost(),
                        "man-hours (lower bound)", true),
                new OverviewSection.Kpi("JPA entities", dataModel.getJpaEntities().size(), entitiesSub, false),
                new OverviewSection.Kpi("Screens", result.getScreensCount(),
                        result.getFragmentsCount() + " fragments · "
                                + formatHours(result.getEstimation().getScreensCost()) + " man-hours", false),
                new OverviewSection.Kpi("Add-ons", addonsSection.getRows().size(), addonsSub, false),
                new OverviewSection.Kpi("Roles", rolesCount,
                        sourcesScan.getScreenPolicyCount() + " screen policies", false),
                new OverviewSection.Kpi("Red flags", sourcesScan.getRedFlags().size(),
                        "manual estimation required", false)
        );
        return new OverviewSection(facts, kpis, DISCLAIMER);
    }

    /**
     * KPI sub-line for the add-ons card: absent and unknown counts, or "all recognized".
     */
    protected String buildAddonsKpiSub(long absentCount, long unknownCount) {
        List<String> parts = new ArrayList<>();
        if (absentCount > 0) {
            parts.add(absentCount + " absent");
        }
        if (unknownCount > 0) {
            parts.add(unknownCount + " without Jmix data");
        }
        return parts.isEmpty() ? "all recognized" : String.join(" · ", parts);
    }

    /**
     * Matches the FreeMarker number format "0.##": no trailing zeros, plain decimal notation.
     */
    protected String formatHours(java.math.BigDecimal value) {
        return value.stripTrailingZeros().toPlainString();
    }

    protected AddonsSection buildAddonsSection(JmixProjectAnalysisResult result) {
        List<AddonsSection.Row> rows = new ArrayList<>();
        List<AddonsSection.Escalation> escalations = new ArrayList<>();
        for (JmixProjectAnalysisResult.ResolvedAddon addon : result.getAddons()) {
            JmixAddonInfo info = addon.getAddonInfo();
            if (info == null) {
                rows.add(new AddonsSection.Row(
                        addon.getDependency().getArtifact(),
                        addon.getDependency().getGroupArtifact(),
                        "UNKNOWN",
                        null,
                        null,
                        "No data about this add-on. Check the Jmix marketplace manually",
                        null));
            } else {
                rows.add(new AddonsSection.Row(
                        info.getName(),
                        info.getArtifact(),
                        info.getFlowStatus().name(),
                        info.getLicense().name(),
                        info.getFlowArtifact(),
                        info.getNotes(),
                        info.getCostHint()));
                if (info.getFlowStatus() == TargetStatus.ABSENT) {
                    escalations.add(new AddonsSection.Escalation(info.getName(), info.getNotes()));
                }
            }
        }
        return new AddonsSection(rows, escalations);
    }

    protected RedFlagsSection buildRedFlagsSection(JmixProjectAnalysisResult result) {
        List<RedFlagsSection.Row> rows = new ArrayList<>();
        for (JmixSourcesScanResult.RedFlag redFlag : result.getSourcesScan().getRedFlags()) {
            rows.add(new RedFlagsSection.Row(redFlag.getCategory(), redFlag.getSubject(), redFlag.getDetail()));
        }
        return new RedFlagsSection(rows);
    }

    protected RenamesSection buildRenamesSection(JmixProjectAnalysisResult result) {
        List<RenamesSection.Row> rows = new ArrayList<>();

        for (JmixConfigInfo.PropertyRename rename : result.getConfigInfo().getPropertyRenames()) {
            rows.add(new RenamesSection.Row("application.properties",
                    rename.getProperty(), rename.getNewProperty(), rename.getNotes()));
        }

        JmixSourcesScanResult sourcesScan = result.getSourcesScan();
        if (sourcesScan.getScreenPolicyCount() > 0) {
            rows.add(new RenamesSection.Row("Role annotations",
                    "@ScreenPolicy (" + sourcesScan.getScreenPolicyCount() + " occurrences)",
                    "@ViewPolicy",
                    "Package changes from securityui to securityflowui; screenIds become viewIds"));
        }
        if (sourcesScan.getMenuPolicyCount() > 0) {
            rows.add(new RenamesSection.Row("Role annotations",
                    "@MenuPolicy (" + sourcesScan.getMenuPolicyCount() + " occurrences)",
                    "@MenuPolicy",
                    "Same annotation name, package changes from securityui to securityflowui"));
        }

        JmixConfigInfo configInfo = result.getConfigInfo();
        if (configInfo.getMenuScreenItemsCount() > 0) {
            rows.add(new RenamesSection.Row("Menu",
                    "item screen= (" + configInfo.getMenuScreenItemsCount() + " items in "
                            + configInfo.getMenuConfigLocation() + ")",
                    "item view=",
                    "Namespace changes to http://jmix.io/schema/flowui/menu"));
        }
        if (configInfo.isUiDataChangelogIncluded()) {
            rows.add(new RenamesSection.Row("Liquibase master changelog",
                    "/io/jmix/uidata/liquibase/changelog.xml",
                    "/io/jmix/flowuidata/liquibase/changelog.xml",
                    "Classic UI settings tables are replaced by Flow UI ones"));
        }
        return new RenamesSection(rows);
    }

    protected NotesSection buildNotesSection(JmixProjectAnalysisResult result) {
        List<NotesSection.Row> rows = new ArrayList<>();

        int kotlinFiles = result.getSourcesScan().getKotlinFilesCount();
        if (kotlinFiles > 0) {
            rows.add(new NotesSection.Row(
                    "Kotlin sources are not analyzed",
                    "kotlin-files",
                    kotlinFiles + " Kotlin file(s) found. Kotlin analysis is not supported:"
                            + " screens and entities defined in Kotlin are missing from this report,"
                            + " the numbers are underestimated"));
        }
        if (!result.getSourcesScan().getSecurityConfigs().isEmpty()) {
            rows.add(new NotesSection.Row(
                    "Custom Spring Security configuration",
                    "security-config",
                    "Found: " + String.join(", ", result.getSourcesScan().getSecurityConfigs())
                            + ". Spring Security 5 to 6 migration and the Flow UI security chain"
                            + " need manual review"));
        }
        if (!result.getDataModel().getEntityEventListeners().isEmpty()) {
            rows.add(new NotesSection.Row(
                    "Entity event listeners",
                    "entity-listeners",
                    result.getDataModel().getEntityEventListeners().size()
                            + " EntityChangedEvent listener(s) found. They are portable to the current Jmix"
                            + " as is (only the javax to jakarta import sweep applies)"));
        }
        if (result.getDataModel().getJavaxImportFilesCount() > 0) {
            rows.add(new NotesSection.Row(
                    "Jakarta namespace sweep",
                    "jakarta-sweep",
                    result.getDataModel().getJavaxImportFilesCount()
                            + " file(s) import javax.persistence/validation/annotation."
                            + " The sweep is mechanical and mostly automated by the IDE or OpenRewrite"));
        }
        return new NotesSection(rows);
    }
}
