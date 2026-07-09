package io.jmix.migration.core.estimation;

public interface NumericMetricRule {

    String getMetricCode();

    int apply(int inputValue);
}
