package io.jmix.migration.analysis.issue;

public class MiscNote {
    private final String code;
    private final String name;
    private final String notes;

    public MiscNote(String code, String name, String notes) {
        this.code = code;
        this.name = name;
        this.notes = notes;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getNotes() {
        return notes;
    }
}
