package io.jmix.migration.analysis.estimation.rules;

public interface NumericMetricRule {

    String getMetricCode();

    int apply(int inputValue);
}
