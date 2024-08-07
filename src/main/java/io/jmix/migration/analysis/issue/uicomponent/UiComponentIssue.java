package io.jmix.migration.analysis.issue.uicomponent;

public class UiComponentIssue {
    private final String component;
    private final UiComponentIssueType type;
    private final String notes;
    private final int complexityScore;

    public UiComponentIssue(String component, UiComponentIssueType type, String notes) {
        this(component, type, notes, 0);
    }

    public UiComponentIssue(String component, UiComponentIssueType type, String notes, int complexityScore) {
        this.component = component;
        this.type = type;
        this.notes = notes;
        this.complexityScore = complexityScore;
    }

    public String getComponent() {
        return component;
    }

    public UiComponentIssueType getType() {
        return type;
    }

    public String getNotes() {
        return notes;
    }

    public int getExtraComplexityScore() {
        return complexityScore;
    }
}
