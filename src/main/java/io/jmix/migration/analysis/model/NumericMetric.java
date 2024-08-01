package io.jmix.migration.analysis.model;

public class NumericMetric {
    private final String code;
    private final int value;

    public NumericMetric(String code, int value) {
        this.code = code;
        this.value = value;
    }

    public String getCode() {
        return code;
    }

    public int getValue() {
        return value;
    }
}
