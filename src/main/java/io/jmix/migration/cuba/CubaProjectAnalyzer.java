package io.jmix.migration.cuba;
import io.jmix.migration.classicui.model.*;
import io.jmix.migration.core.estimation.*;
import io.jmix.migration.core.model.UnparsedFileEntry;
import io.jmix.migration.core.project.GradleBuildParser;
import io.jmix.migration.core.project.JmixProjectDescriptor;
import io.jmix.migration.core.project.ProjectType;
import io.jmix.migration.core.project.ProjectTypeDetector;
import io.jmix.migration.core.scan.UnparsedFilesCollector;

import io.jmix.migration.cuba.appcomponent.AppComponentType;
import io.jmix.migration.cuba.appcomponent.CubaAppComponentInfo;
import io.jmix.migration.cuba.appcomponent.CubaAppComponentsInfoRegistry;
import io.jmix.migration.core.estimation.EstimationDataProvider;
import io.jmix.migration.classicui.estimation.ScreenEstimator;
import io.jmix.migration.core.estimation.ScreenTimeEstimator;
import io.jmix.migration.cuba.estimation.rules.LegacyEntityListenersRule;
import io.jmix.migration.core.estimation.NumericMetricRule;
import io.jmix.migration.core.incident.MiscNote;
import io.jmix.migration.cuba.MiscNotes;
import io.jmix.migration.core.incident.UiComponentIssuesRegistry;
import io.jmix.migration.cuba.model.*;
import io.jmix.migration.classicui.parser.ScreensCollector;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static io.jmix.migration.classicui.Metrics.LEGACY_ENTITY_LISTENERS_METRIC_CODE;

public class CubaProjectAnalyzer {

    private static final Logger log = LoggerFactory.getLogger(CubaProjectAnalyzer.class);

    public static final String MODULES_DIR = "modules";
    public static final String CORE_MODULE_DIR = "core";
    public static final String GLOBAL_MODULE_DIR = "global";
    public static final String WEB_MODULE_DIR = "web";
    public static final String GUI_MODULE_DIR = "gui";
    public static final String WEB_TOOLKIT_MODULE_DIR = "web-toolkit";
    public static final String SRC_DIR = "src";

    private final EstimationDataProvider estimationDataProvider;
    private final ScreenEstimator screenEstimator;
    private final ScreenTimeEstimator screenTimeEstimator;
    private final CubaAppComponentsInfoRegistry appComponentsInfoRegistry;
    private final Map<String, NumericMetricRule> numericMetricRules;
    private final HtmlReportGenerator reportGenerator;

    public CubaProjectAnalyzer(@Nullable String estimationDataFile) {
        UiComponentIssuesRegistry uiComponentIssuesRegistry = UiComponentIssuesRegistry.create();
        this.estimationDataProvider = new EstimationDataProvider(estimationDataFile);
        this.screenEstimator = new ScreenEstimator(uiComponentIssuesRegistry, estimationDataProvider);
        this.screenTimeEstimator = new ScreenTimeEstimator(estimationDataProvider.getScreenComplexityTimeEstimationThresholds());
        this.appComponentsInfoRegistry = CubaAppComponentsInfoRegistry.create();
        this.numericMetricRules = generateMetricRules();
        this.reportGenerator = new HtmlReportGenerator(uiComponentIssuesRegistry);
    }

    protected Map<String, NumericMetricRule> generateMetricRules() {
        Map<String, NumericMetricRule> result = new HashMap<>();
        result.put(LEGACY_ENTITY_LISTENERS_METRIC_CODE, new LegacyEntityListenersRule(estimationDataProvider.getLegacyEntityListenerCost()));
        return result;
    }

    public void analyzeProject(String projectPathString, String basePackage) {
        CubaProjectEstimationResult result = analyzeProjectToResult(projectPathString, basePackage);
        reportGenerator.generateHtmlReport(projectPathString, result);
    }

    /**
     * Analysis without report generation. Separated so tests can inspect the result
     * and render the report to a string.
     */
    public CubaProjectEstimationResult analyzeProjectToResult(String projectPathString, String basePackage) {
        if (StringUtils.isBlank(projectPathString)) {
            throw new RuntimeException("No project path is specified");
        }

        Path projectPath = Path.of(projectPathString).toAbsolutePath().normalize();
        log.info("Start project analysis");
        log.info("Project path = '{}', Base package = '{}'", projectPath, basePackage);

        validateProjectType(projectPath);

        UnparsedFilesCollector unparsedFilesCollector = new UnparsedFilesCollector();

        // Core module
        Path coreRootPath = projectPath.resolve(MODULES_DIR).resolve(CORE_MODULE_DIR);
        Path coreSrcPath = coreRootPath.resolve(SRC_DIR);
        CoreModuleAnalyzer coreModuleAnalyzer = new CoreModuleAnalyzer(coreRootPath, coreSrcPath, basePackage, unparsedFilesCollector);
        CoreModuleAnalysisResult coreModuleAnalysisResult = coreModuleAnalyzer.analyzeCoreModule();

        // Global module
        Path globalRootPath = projectPath.resolve(MODULES_DIR).resolve(GLOBAL_MODULE_DIR);
        Path globalSrcPath = globalRootPath.resolve(SRC_DIR);
        GlobalModuleAnalyzer globalModuleAnalyzer = new GlobalModuleAnalyzer(globalSrcPath, basePackage, unparsedFilesCollector);
        GlobalModuleAnalysisResult globalModuleAnalysisResult = globalModuleAnalyzer.analyzeGlobalModule();


        // UI modules
        Path webSrcPath = projectPath.resolve(MODULES_DIR).resolve(WEB_MODULE_DIR).resolve(SRC_DIR);
        Path guiSrcPath = projectPath.resolve(MODULES_DIR).resolve(GUI_MODULE_DIR).resolve(SRC_DIR);
        UiModulesAnalyzer uiModulesAnalyzer = new UiModulesAnalyzer(webSrcPath, guiSrcPath, basePackage, unparsedFilesCollector);
        UiModulesAnalysisResult uiModulesAnalysisResult = uiModulesAnalyzer.analyzeUiModules();

        boolean webToolkitModulePresent = Files.isDirectory(projectPath.resolve(MODULES_DIR).resolve(WEB_TOOLKIT_MODULE_DIR));

        return estimateProject(
                coreModuleAnalysisResult, globalModuleAnalysisResult, uiModulesAnalysisResult,
                unparsedFilesCollector.getEntries(), webToolkitModulePresent);
    }

    protected void validateProjectType(Path projectPath) {
        JmixProjectDescriptor descriptor = new GradleBuildParser().parse(projectPath, null);
        ProjectType projectType = new ProjectTypeDetector().detect(descriptor);
        switch (projectType) {
            case JMIX_CLASSIC -> throw new RuntimeException(
                    "The project looks like a Jmix project. Use the 'analyze-jmix' command instead");
            case JMIX_FLOW -> throw new RuntimeException(
                    "The project uses Flow UI (Jmix 2+) and does not need CUBA migration analysis");
            case UNKNOWN -> throw new RuntimeException(
                    "Unable to detect a CUBA Platform project in '" + projectPath + "': no CUBA Gradle plugin"
                            + " signals and no modules/global, modules/web layout. Check --project-dir");
            case CUBA -> log.info("Project type: CUBA Platform");
        }
    }

    protected CubaProjectEstimationResult estimateProject(CoreModuleAnalysisResult coreModuleAnalysisResult,
                                                          GlobalModuleAnalysisResult globalModuleAnalysisResult,
                                                          UiModulesAnalysisResult uiModulesAnalysisResult,
                                                          List<UnparsedFileEntry> unparsedFiles,
                                                          boolean webToolkitModulePresent) {
        ScreensCollector screensCollector = uiModulesAnalysisResult.getScreensCollector();
        Map<String, ScreenComplexityScore> screenScores = screenEstimator.estimate(screensCollector);
        Map<ThresholdItem<Integer, BigDecimal>, List<String>> screensPerComplexity = new HashMap<>();
        Map<String, List<String>> screensRequireDecision = new TreeMap<>();
        BigDecimal screenSumHours = BigDecimal.ZERO;
        for (Map.Entry<String, ScreenComplexityScore> entry : screenScores.entrySet()) {
            String name = entry.getKey();
            ScreenComplexityScore score = entry.getValue();
            ThresholdItem<Integer, BigDecimal> complexityThreshold = screenTimeEstimator.estimate(score);

            screensPerComplexity.computeIfAbsent(complexityThreshold, key -> new ArrayList<>()).add(name);
            screenSumHours = screenSumHours.add(complexityThreshold.getOutputValue());

            if (!score.getAbsentComponents().isEmpty()) {
                screensRequireDecision.put(name, new ArrayList<>(score.getAbsentComponents()));
            }
        }

        NumericMetric legacyListenersAmountMetric = globalModuleAnalysisResult.getLegacyListenersAmount();
        NumericMetricRule legacyListenersAmountMetricRule = numericMetricRules.get(legacyListenersAmountMetric.getCode());
        int legacyListenersCost = legacyListenersAmountMetricRule.apply(legacyListenersAmountMetric.getValue());

        List<String> appComponentPackages = coreModuleAnalysisResult.getAppComponents();
        List<CubaAppComponentInfo> appComponents = new ArrayList<>();
        appComponentPackages.forEach(componentPackage -> {
            CubaAppComponentInfo appComponentInfo = appComponentsInfoRegistry.getAppComponentInfo(componentPackage);
            if (appComponentInfo == null) {
                appComponents.add(CubaAppComponentInfo.createMissing(componentPackage));
                return;
            }

            if (AppComponentType.BASE_APP.equals(appComponentInfo.getAppComponentType())) {
                return;
            }

            appComponents.add(appComponentInfo);
        });

        Map<String, Integer> totalUiComponents = countTotalUiComponents(screensCollector);

        List<MiscNote> miscNotes = new ArrayList<>();
        Properties webAppProperties = uiModulesAnalysisResult.getWebAppProperties();
        if (webAppProperties != null) {
            boolean foldersPaneEnabled = isFoldersPaneEnabled(webAppProperties);
            if (foldersPaneEnabled) {
                miscNotes.add(MiscNotes.folderPaneEnabled());
            }
        }
        if (webToolkitModulePresent) {
            miscNotes.add(MiscNotes.customWidgetsModule());
        }

        CubaProjectEstimationResult.Builder resultBuilder = CubaProjectEstimationResult.builder();
        return resultBuilder
                .setInitialMigrationCost(BigDecimal.valueOf(estimationDataProvider.getInitialMigrationCost())) // todo rule based on amount of entities?
                .setBaseEntitiesMigrationCost(BigDecimal.valueOf(estimationDataProvider.getBaseEntitiesMigrationCost()))
                .setScreensPerComplexity(screensPerComplexity)
                .setEntitiesPerPersistenceUnit(globalModuleAnalysisResult.getEntitiesPerPersistenceUnit())
                .setAllUiComponents(totalUiComponents)
                .setLegacyListenersCost(BigDecimal.valueOf(legacyListenersCost))
                .setLegacyListeners(new ArrayList<>(globalModuleAnalysisResult.getLegacyListeners()))
                .setScreensTotalCost(screenSumHours)
                .setAppComponents(appComponents)
                .setMiscNotes(miscNotes)
                .setScreensRequireDecision(screensRequireDecision)
                .setUnparsedFiles(unparsedFiles)
                .build();
    }

    protected Map<String, Integer> countTotalUiComponents(ScreensCollector screensCollector) {
        Map<String, Integer> totalUiComponents = new HashMap<>();
        screensCollector.getAllScreens().forEach(screenInfo -> {
            List<LayoutItem> layoutItems = Optional.ofNullable(screenInfo.getLayout())
                    .map(Layout::getAllItems)
                    .orElse(Collections.emptyList());
            layoutItems.forEach(layoutItem ->
                    totalUiComponents.merge(layoutItem.getName(), layoutItem.getQuantity(), Integer::sum));
        });
        return totalUiComponents;
    }

    protected boolean isFoldersPaneEnabled(Properties webAppProperties) {
        Object foldersPaneEnabledPropertyValue = getPropertyValue(webAppProperties, "cuba.web.folders-pane-enabled", false);
        if (foldersPaneEnabledPropertyValue instanceof Boolean) {
            return (boolean) foldersPaneEnabledPropertyValue;
        }
        if (foldersPaneEnabledPropertyValue instanceof String) {
            return Boolean.parseBoolean((String) foldersPaneEnabledPropertyValue);
        }
        return false;
    }

    protected Object getPropertyValue(Properties properties, String property, Object defaultValue) {
        Object value = properties.get(property);
        if (value != null) {
            return value;
        }

        if (property.contains("-")) {
            String ccProperty = property.toLowerCase();
            String[] tokens = ccProperty.split("-");
            StringBuilder internalPropertyBuilder = new StringBuilder(tokens[0]);
            for (int i = 1; i < tokens.length; i++) {
                internalPropertyBuilder.append(tokens[i].substring(0, 1).toUpperCase()).append(tokens[i].substring(1));
            }
            ccProperty = internalPropertyBuilder.toString();
            value = properties.get(ccProperty);
        }

        return value == null ? defaultValue : value;
    }
}
