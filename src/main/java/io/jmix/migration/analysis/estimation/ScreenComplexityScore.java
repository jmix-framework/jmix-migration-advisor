package io.jmix.migration.analysis.estimation;

import java.util.concurrent.atomic.AtomicInteger;

public class ScreenComplexityScore {
    private final AtomicInteger rawValue;
    //todo decimal modifiers?

    public ScreenComplexityScore() {
        this(0);
    }

    public ScreenComplexityScore(int initialValue) {
        this.rawValue = new AtomicInteger(initialValue);
    }

    public void addRawValue(int value) {
        rawValue.addAndGet(value);
    }

    public int getValue() {
        return rawValue.get();
    }

    @Override
    public String toString() {
        return String.valueOf(getValue());
    }

    public static ScreenComplexityScore merge(ScreenComplexityScore a, ScreenComplexityScore b) {
        return new ScreenComplexityScore(a.getValue() + b.getValue());
    }
}
