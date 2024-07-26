package io.jmix.migration.model;

import java.util.List;
import java.util.Map;

public class CoreModuleAnalysisResult {
    private final List<String> appComponents;

    public CoreModuleAnalysisResult(List<String> appComponents) {
        this.appComponents = appComponents;
    }

    public List<String> getAppComponents() {
        return appComponents;
    }
}
