package io.jmix.migration.analysis.model;

import io.jmix.migration.analysis.issue.uicomponent.Requires;

import java.util.List;

public class UiComponentNotesRow {
    private final String name;
    private final int amount;
    private final String notes;
    private final String type;
    private final int extraComplexityScore;
    private final List<Requires> requires;

    public UiComponentNotesRow(String name, int amount, String notes, String type,
                               int extraComplexityScore, List<Requires> requires) {
        this.name = name;
        this.amount = amount;
        this.notes = notes;
        this.type = type;
        this.extraComplexityScore = extraComplexityScore;
        this.requires = requires;
    }

    public String getName() {
        return name;
    }

    public int getAmount() {
        return amount;
    }

    public String getNotes() {
        return notes;
    }

    public String getType() {
        return type;
    }

    public int getExtraComplexityScore() {
        return extraComplexityScore;
    }

    public List<Requires> getRequires() {
        return requires;
    }
}
