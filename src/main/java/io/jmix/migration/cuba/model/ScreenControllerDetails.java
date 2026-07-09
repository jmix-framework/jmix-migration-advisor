package io.jmix.migration.cuba.model;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class ScreenControllerDetails {
    private final String className;
    private final int overallLines;
    private final List<MethodDetails> methods;
    private final ScreenControllerSuperClassDetails superClassDetails;

    private ScreenControllerDetails(Builder builder) {
        this.className = builder.className;
        this.overallLines = builder.overallLines;
        this.methods = builder.methods;
        this.superClassDetails = builder.superClassDetails;
    }

    public static Builder builder(String className) {
        if (StringUtils.isBlank(className)) {
            throw new IllegalArgumentException("Class name is not specified");
        }
        return new Builder(className);
    }

    public String getClassName() {
        return className;
    }

    public int getOverallLines() {
        return overallLines;
    }

    public List<MethodDetails> getMethods() {
        return methods;
    }

    public ScreenControllerSuperClassDetails getSuperClassDetails() {
        return superClassDetails;
    }

    public static class Builder {
        private final String className;
        private int overallLines;
        private List<MethodDetails> methods;
        private ScreenControllerSuperClassDetails superClassDetails;

        private Builder(String className) {
            this.className = className;
        }

        public Builder setOverallLines(int overallLines) {
            this.overallLines = overallLines;
            return this;
        }

        public Builder setMethods(List<MethodDetails> methods) {
            this.methods = new ArrayList<>(methods);
            return this;
        }

        public Builder setSuperClassDetails(ScreenControllerSuperClassDetails superClassDetails) {
            this.superClassDetails = superClassDetails;
            return this;
        }

        public ScreenControllerDetails build() {
            return new ScreenControllerDetails(this);
        }
    }
}
