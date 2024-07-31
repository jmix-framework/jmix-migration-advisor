package io.jmix.migration.model;

public class BooleanMetric {
    private final String code;
    private final boolean value;

    public BooleanMetric(String code, boolean value) {
        this.code = code;
        this.value = value;
    }

    public String getCode() {
        return code;
    }

    public boolean isValue() {
        return value;
    }
}
