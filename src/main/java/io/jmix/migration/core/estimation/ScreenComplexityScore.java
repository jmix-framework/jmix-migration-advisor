package io.jmix.migration.core.estimation;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class ScreenComplexityScore {
    private final AtomicInteger rawValue;
    private final Set<String> componentsRequiringDecision = new LinkedHashSet<>();
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
     * UI components of the screen whose migration cost is indeterminate: components with no
     * equivalent in Jmix (registry status ABSENT) and project-custom components from
     * non-standard namespaces. NOT included in the score value; the screen needs a manual decision.
     */
    public void addComponentRequiringDecision(String componentName) {
        componentsRequiringDecision.add(componentName);
    }

    public Set<String> getComponentsRequiringDecision() {
        return Collections.unmodifiableSet(componentsRequiringDecision);
    }

    @Override
    public String toString() {
        return String.valueOf(getValue());
    }

    public static ScreenComplexityScore merge(ScreenComplexityScore a, ScreenComplexityScore b) {
        ScreenComplexityScore result = new ScreenComplexityScore(a.getValue() + b.getValue());
        result.componentsRequiringDecision.addAll(a.componentsRequiringDecision);
        result.componentsRequiringDecision.addAll(b.componentsRequiringDecision);
        return result;
    }
}
