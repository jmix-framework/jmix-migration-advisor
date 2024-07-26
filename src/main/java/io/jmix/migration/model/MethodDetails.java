package io.jmix.migration.model;

import java.util.List;

public class MethodDetails {
    private final String signature;
    private final List<NumericMetric> numericMetrics;

    public MethodDetails(String signature, List<NumericMetric> numericMetrics) {
        this.signature = signature;
        this.numericMetrics = numericMetrics;
    }

    public String getSignature() {
        return signature;
    }

    public List<NumericMetric> getNumericMetrics() {
        return numericMetrics;
    }
}
