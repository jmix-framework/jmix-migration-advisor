package io.jmix.migration.cuba;

import io.jmix.migration.core.estimation.ThresholdItem;
import io.jmix.migration.core.incident.MiscNote;
import io.jmix.migration.core.incident.Requires;
import io.jmix.migration.core.incident.UiComponentIssue;
import io.jmix.migration.core.incident.UiComponentIssuesRegistry;
import io.jmix.migration.core.report.AppComponentsSection;
import io.jmix.migration.core.report.ComplexityGroupsSection;
import io.jmix.migration.core.report.DataModelSection;
import io.jmix.migration.core.report.EstimationSummarySection;
import io.jmix.migration.core.report.HtmlReportWriter;
import io.jmix.migration.core.report.NotesSection;
import io.jmix.migration.core.report.OverviewSection;
import io.jmix.migration.core.report.ReportModel;
import io.jmix.migration.core.report.ReportSection;
import io.jmix.migration.core.report.UiComponentsSection;
import io.jmix.migration.core.report.UnparsedFilesSection;
import io.jmix.migration.cuba.appcomponent.AppComponentType;
import io.jmix.migration.cuba.appcomponent.CubaAppComponentInfo;
import io.jmix.migration.cuba.model.CubaProjectEstimationResult;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


/**
 * Builds the platform-neutral {@link ReportModel} from the CUBA analysis result
 * and renders it via {@link HtmlReportWriter}.
 */
public class HtmlReportGenerator {

    protected static final String REPORT_TITLE = "CUBA → Jmix migration report";
    protected static final String DISCLAIMER = "This is not a comprehensive estimation."
            + " Some aspects cannot be evaluated automatically and need manual analysis —"
            + " treat the numbers as a rough lower-range estimate. Analysis of Kotlin classes is not supported.";

    private final UiComponentIssuesRegistry uiComponentIssuesRegistry;
    private final HtmlReportWriter reportWriter;

    public HtmlReportGenerator(UiComponentIssuesRegistry uiComponentIssuesRegistry) {
        this.uiComponentIssuesRegistry = uiComponentIssuesRegistry;
        this.reportWriter = new HtmlReportWriter();
    }

    protected void generateHtmlReport(String project, CubaProjectEstimationResult result) {
        reportWriter.writeToFile(reportWriter.createDefaultFileName(), buildReportModel(project, result));
    }

    /**
     * Renders the report to a string. Separated from file writing so tests can capture
     * the report content directly.
     */
    public String generateReportContent(String project, CubaProjectEstimationResult result) {
        return reportWriter.render(buildReportModel(project, result));
    }

    public ReportModel buildReportModel(String project, CubaProjectEstimationResult result) {
        AppComponentsSection appComponentsSection = buildAppComponentsSection(result);
        List<String> legacyListeners = result.getLegacyListeners() == null
                ? List.of()
                : result.getLegacyListeners().stream().sorted().toList();

        List<ReportSection> sections = new ArrayList<>();
        sections.add(buildOverviewSection(result, appComponentsSection, legacyListeners));
        sections.add(buildEstimationsSection(result));
        sections.add(buildComplexitySection(result));
        sections.add(buildUiComponentsSection(result));
        sections.add(appComponentsSection);
        sections.add(buildDataModelSection(result, legacyListeners));
        sections.add(buildNotesSection(result));
        if (!result.getUnparsedFiles().isEmpty()) {
            sections.add(new UnparsedFilesSection(result.getUnparsedFiles()));
        }
        return new ReportModel(project, REPORT_TITLE, sections);
    }

    protected OverviewSection buildOverviewSection(CubaProjectEstimationResult result,
                                                   AppComponentsSection appComponentsSection,
                                                   List<String> legacyListeners) {
        long missingAmount = appComponentsSection.getRows().stream()
                .filter(row -> AppComponentType.MISSING.name().equals(row.getTypeName()))
                .count();

        List<OverviewSection.Kpi> kpis = List.of(
                new OverviewSection.Kpi("Total effort", result.getTotalEstimation(), "man-hours (lower bound)", true),
                new OverviewSection.Kpi("Entities", result.getEntitiesAmount(), null, false),
                new OverviewSection.Kpi("Screens", result.getScreensTotalAmount(),
                        formatHours(result.getScreensTotalCost()) + " man-hours", false),
                new OverviewSection.Kpi("App components", appComponentsSection.getRows().size(),
                        missingAmount > 0 ? missingAmount + " without Jmix data" : "all recognized", false),
                new OverviewSection.Kpi("Legacy listeners", legacyListeners.size(), null, false)
        );
        return new OverviewSection(kpis, DISCLAIMER);
    }

    protected EstimationSummarySection buildEstimationsSection(CubaProjectEstimationResult result) {
        List<EstimationSummarySection.Row> rows = List.of(
                new EstimationSummarySection.Row("Initial migration", result.getInitialMigrationCost()),
                new EstimationSummarySection.Row("Base entities", result.getBaseEntitiesMigrationCost()),
                new EstimationSummarySection.Row("Legacy listeners", result.getLegacyListenersCost()),
                new EstimationSummarySection.Row("Screens", result.getScreensTotalCost())
        );
        return new EstimationSummarySection(rows, result.getTotalEstimation());
    }

    protected ComplexityGroupsSection buildComplexitySection(CubaProjectEstimationResult result) {
        return ComplexityGroupsSection.fromScreensPerComplexity(
                result.getScreensPerComplexity(), result.getScreensRequireDecision());
    }

    protected UiComponentsSection buildUiComponentsSection(CubaProjectEstimationResult result) {
        return UiComponentsSection.fromComponentCounters(result.getAllUiComponents(), uiComponentIssuesRegistry);
    }

    protected AppComponentsSection buildAppComponentsSection(CubaProjectEstimationResult result) {
        List<AppComponentsSection.Row> rows = new ArrayList<>();
        for (CubaAppComponentInfo appComponent : result.getAppComponents()) {
            rows.add(new AppComponentsSection.Row(
                    appComponent.getName(),
                    appComponent.getAppComponentPackage(),
                    appComponent.getAppComponentTypeName(),
                    appComponent.getOriginName(),
                    appComponent.getNotes()
            ));
        }
        return new AppComponentsSection(rows);
    }

    protected DataModelSection buildDataModelSection(CubaProjectEstimationResult result, List<String> legacyListeners) {
        return new DataModelSection(
                result.getEntitiesAmount(),
                sortedEntitiesPerPersistenceUnit(result.getEntitiesPerPersistenceUnit()),
                legacyListeners);
    }

    protected NotesSection buildNotesSection(CubaProjectEstimationResult result) {
        List<NotesSection.Row> rows = new ArrayList<>();
        for (MiscNote note : result.getMiscNotes()) {
            rows.add(new NotesSection.Row(note.getName(), note.getCode(), note.getNotes()));
        }
        return new NotesSection(rows);
    }

    /**
     * Matches the FreeMarker number format "0.##": no trailing zeros, plain decimal notation.
     */
    protected String formatHours(BigDecimal value) {
        return value.stripTrailingZeros().toPlainString();
    }

    protected Map<String, List<String>> sortedEntitiesPerPersistenceUnit(Map<String, List<String>> entitiesPerPersistenceUnit) {
        Map<String, List<String>> sorted = new TreeMap<>();
        entitiesPerPersistenceUnit.forEach((unit, entities) ->
                sorted.put(unit, entities.stream().sorted().toList()));
        return sorted;
    }

}
