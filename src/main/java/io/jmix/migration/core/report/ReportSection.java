package io.jmix.migration.core.report;

/**
 * A report section. The {@code type} selects the rendering macro in the report template;
 * {@code id} and {@code title} feed the anchor and the table of contents.
 */
public interface ReportSection {

    String getId();

    String getTitle();

    String getType();
}
