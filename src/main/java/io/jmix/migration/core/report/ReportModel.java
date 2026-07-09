package io.jmix.migration.core.report;

import java.util.List;

/**
 * Platform-neutral model of a migration report: a header plus an ordered list of typed sections.
 * Built by a platform-specific builder, rendered by {@link HtmlReportWriter}.
 */
public class ReportModel {
    private final String projectName;
    private final String reportTitle;
    private final List<ReportSection> sections;

    public ReportModel(String projectName, String reportTitle, List<ReportSection> sections) {
        this.projectName = projectName;
        this.reportTitle = reportTitle;
        this.sections = List.copyOf(sections);
    }

    public String getProjectName() {
        return projectName;
    }

    public String getReportTitle() {
        return reportTitle;
    }

    public List<ReportSection> getSections() {
        return sections;
    }
}
