package io.jmix.migration.core.incident;

import java.util.List;

/**
 * Instances are created only via the static factories, one per {@link UiComponentIssueType}.
 * The factory signatures enforce the invariants: an {@code ABSENT} issue carries neither
 * a complexity score (its cost is indeterminate: it is escalated, not priced) nor
 * dependencies (there is no recipe to require anything).
 */
public class UiComponentIssue {
    private final String component;
    private final UiComponentIssueType type;
    private final String notes;
    private final int complexityScore;
    private final List<Requires> requires;

    private UiComponentIssue(String component, UiComponentIssueType type, String notes,
                             int complexityScore, List<Requires> requires) {
        this.component = component;
        this.type = type;
        this.notes = notes;
        this.complexityScore = complexityScore;
        this.requires = requires;
    }

    public static UiComponentIssue createChanged(String component, String notes,
                                                 int extraComplexityScore, Requires... requires) {
        return create(component, UiComponentIssueType.CHANGED, notes, extraComplexityScore, requires);
    }

    public static UiComponentIssue createAlternative(String component, String notes,
                                                     int extraComplexityScore, Requires... requires) {
        return create(component, UiComponentIssueType.HAS_ALTERNATIVE, notes, extraComplexityScore, requires);
    }

    public static UiComponentIssue createWorkaround(String component, String notes,
                                                    int extraComplexityScore, Requires... requires) {
        return create(component, UiComponentIssueType.HAS_WORKAROUND, notes, extraComplexityScore, requires);
    }

    public static UiComponentIssue createAbsent(String component, String notes) {
        return new UiComponentIssue(component, UiComponentIssueType.ABSENT, notes, 0, List.of());
    }

    private static UiComponentIssue create(String component, UiComponentIssueType type, String notes,
                                           int extraComplexityScore, Requires[] requires) {
        if (extraComplexityScore < 0) {
            throw new IllegalArgumentException(
                    "Extra complexity score must not be negative: '" + component + "'");
        }
        return new UiComponentIssue(component, type, notes, extraComplexityScore, List.of(requires));
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

    public List<Requires> getRequires() {
        return requires;
    }
}
