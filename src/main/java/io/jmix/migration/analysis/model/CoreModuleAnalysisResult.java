package io.jmix.migration.analysis.model;

import java.util.List;

public class CoreModuleAnalysisResult {
    private final List<String> appComponents;

    public CoreModuleAnalysisResult(List<String> appComponents) {
        this.appComponents = appComponents;
    }

    public List<String> getAppComponents() {
        return appComponents;
    }
}
