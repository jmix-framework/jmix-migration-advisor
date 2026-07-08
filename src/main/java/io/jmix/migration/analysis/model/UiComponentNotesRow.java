package io.jmix.migration.analysis.model;

public class UiComponentNotesRow {
    private final String name;
    private final int amount;
    private final String notes;
    private final String type;
    private final int extraComplexityScore;

    public UiComponentNotesRow(String name, int amount, String notes) {
        this(name, amount, notes, null, 0);
    }

    public UiComponentNotesRow(String name, int amount, String notes, String type, int extraComplexityScore) {
        this.name = name;
        this.amount = amount;
        this.notes = notes;
        this.type = type;
        this.extraComplexityScore = extraComplexityScore;
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
}
