package io.jmix.migration.analysis.model;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class ScreenComplexityScore {
    private final AtomicInteger rawValue;
    private final Set<String> absentComponents = new LinkedHashSet<>();
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

    /**
     * UI components of the screen that have no equivalent in Jmix. Their migration cost is
     * indeterminate and is NOT included in the score value; the screen needs a manual decision.
     */
    public void addAbsentComponent(String componentName) {
        absentComponents.add(componentName);
    }

    public Set<String> getAbsentComponents() {
        return Collections.unmodifiableSet(absentComponents);
    }

    @Override
    public String toString() {
        return String.valueOf(getValue());
    }

    public static ScreenComplexityScore merge(ScreenComplexityScore a, ScreenComplexityScore b) {
        ScreenComplexityScore result = new ScreenComplexityScore(a.getValue() + b.getValue());
        result.absentComponents.addAll(a.absentComponents);
        result.absentComponents.addAll(b.absentComponents);
        return result;
    }
}
