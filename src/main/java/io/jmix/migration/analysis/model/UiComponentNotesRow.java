package io.jmix.migration.analysis.model;

public class UiComponentNotesRow {
    private final String name;
    private final int amount;
    private final String notes;

    public UiComponentNotesRow(String name, int amount, String notes) {
        this.name = name;
        this.amount = amount;
        this.notes = notes;
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
}
