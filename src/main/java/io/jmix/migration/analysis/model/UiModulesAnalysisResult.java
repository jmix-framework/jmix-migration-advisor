package io.jmix.migration.analysis.model;

import io.jmix.migration.analysis.parser.screen.ScreensCollector;

import javax.annotation.Nullable;
import java.util.Properties;

public class UiModulesAnalysisResult {
    private final ScreensCollector screensCollector;
    private final Properties webAppProperties;

    public UiModulesAnalysisResult(ScreensCollector screensCollector, @Nullable Properties webAppProperties) {
        this.screensCollector = screensCollector;
        this.webAppProperties = webAppProperties;
    }

    public ScreensCollector getScreensCollector() {
        return screensCollector;
    }

    @Nullable
    public Properties getWebAppProperties() {
        return webAppProperties;
    }
}
