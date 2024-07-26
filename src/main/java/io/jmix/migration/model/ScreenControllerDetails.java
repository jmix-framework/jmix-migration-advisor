package io.jmix.migration.model;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScreenControllerDetails {
    private final String className;
    private final int overallLines;
    private final List<MethodDetails> methods;
    private final Map<String, List<MethodDetails>> nestedClassesMethods;
    private final ScreenControllerSuperClassDetails superClassDetails;

    private ScreenControllerDetails(Builder builder) {
        this.className = builder.className;
        this.overallLines = builder.overallLines;
        this.methods = builder.methods;
        this.nestedClassesMethods = builder.nestedClassesMethods;
        this.superClassDetails = builder.superClassDetails;
    }

    public static Builder builder(String className) {
        if(StringUtils.isBlank(className)) {
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
        private final Map<String, List<MethodDetails>> nestedClassesMethods;
        private ScreenControllerSuperClassDetails superClassDetails;

        private Builder(String className) {
            this.className = className;
            this.nestedClassesMethods = new HashMap<>();
        }

        public Builder setOverallLines(int overallLines) {
            this.overallLines = overallLines;
            return this;
        }

        public Builder setMethods(List<MethodDetails> methods) {
            this.methods = new ArrayList<>(methods);
            return this;
        }

        public Builder putNestedClassMethods(String className, List<MethodDetails> methods) {
            this.nestedClassesMethods.put(className, methods);
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
