package io.jmix.migration.analysis;

import io.jmix.migration.analysis.appcomponent.AppComponentType;
import io.jmix.migration.analysis.appcomponent.CubaAppComponentInfo;
import io.jmix.migration.analysis.appcomponent.CubaAppComponentsInfoRegistry;
import io.jmix.migration.analysis.estimation.EstimationDataProvider;
import io.jmix.migration.analysis.estimation.ScreenEstimator;
import io.jmix.migration.analysis.estimation.ScreenTimeEstimator;
import io.jmix.migration.analysis.estimation.rules.LegacyEntityListenersRule;
import io.jmix.migration.analysis.estimation.rules.NumericMetricRule;
import io.jmix.migration.analysis.issue.MiscNote;
import io.jmix.migration.analysis.issue.MiscNotes;
import io.jmix.migration.analysis.issue.uicomponent.UiComponentIssuesRegistry;
import io.jmix.migration.analysis.model.*;
import io.jmix.migration.analysis.parser.screen.ScreensCollector;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static io.jmix.migration.analysis.Metrics.LEGACY_ENTITY_LISTENERS_METRIC_CODE;

public class CubaProjectAnalyzer {

    private static final Logger log = LoggerFactory.getLogger(CubaProjectAnalyzer.class);

    public static final String MODULES_DIR = "modules";
    public static final String CORE_MODULE_DIR = "core";
    public static final String GLOBAL_MODULE_DIR = "global";
    public static final String WEB_MODULE_DIR = "web";
    public static final String GUI_MODULE_DIR = "gui";
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
        if (StringUtils.isBlank(projectPathString)) {
            throw new RuntimeException("No project path is specified");
        }

        Path projectPath = Path.of(projectPathString).toAbsolutePath().normalize();
        log.info("---=== Start project analysis ===---");
        log.info("Project path = '{}', Base package = '{}'", projectPath, basePackage);

        // Core module
        Path coreRootPath = projectPath.resolve(MODULES_DIR).resolve(CORE_MODULE_DIR);
        Path coreSrcPath = coreRootPath.resolve(SRC_DIR);
        CoreModuleAnalyzer coreModuleAnalyzer = new CoreModuleAnalyzer(coreRootPath, coreSrcPath, basePackage);
        CoreModuleAnalysisResult coreModuleAnalysisResult = coreModuleAnalyzer.analyzeCoreModule();

        // Global module
        Path globalRootPath = projectPath.resolve(MODULES_DIR).resolve(GLOBAL_MODULE_DIR);
        Path globalSrcPath = globalRootPath.resolve(SRC_DIR);
        GlobalModuleAnalyzer globalModuleAnalyzer = new GlobalModuleAnalyzer(globalSrcPath, basePackage);
        GlobalModuleAnalysisResult globalModuleAnalysisResult = globalModuleAnalyzer.analyzeGlobalModule();


        // UI modules
        Path webSrcPath = projectPath.resolve(MODULES_DIR).resolve(WEB_MODULE_DIR).resolve(SRC_DIR);
        Path guiSrcPath = projectPath.resolve(MODULES_DIR).resolve(GUI_MODULE_DIR).resolve(SRC_DIR);
        UiModulesAnalyzer uiModulesAnalyzer = new UiModulesAnalyzer(webSrcPath, guiSrcPath, basePackage);
        UiModulesAnalysisResult uiModulesAnalysisResult = uiModulesAnalyzer.analyzeUiModules();

        CubaProjectEstimationResult cubaProjectEstimationResult = estimateProject(coreModuleAnalysisResult, globalModuleAnalysisResult, uiModulesAnalysisResult);
        reportGenerator.generateHtmlReport(projectPathString, cubaProjectEstimationResult);
    }

    protected CubaProjectEstimationResult estimateProject(CoreModuleAnalysisResult coreModuleAnalysisResult,
                                                          GlobalModuleAnalysisResult globalModuleAnalysisResult,
                                                          UiModulesAnalysisResult uiModulesAnalysisResult) {
        ScreensCollector screensCollector = uiModulesAnalysisResult.getScreensCollector();
        Map<String, ScreenComplexityScore> screenScores = screenEstimator.estimate(screensCollector);
        Map<ThresholdItem<Integer, BigDecimal>, List<String>> screensPerComplexity = new HashMap<>();
        BigDecimal screenSumHours = screenScores.entrySet().stream().map(entry -> {
            String name = entry.getKey();
            ScreenComplexityScore score = entry.getValue();
            ThresholdItem<Integer, BigDecimal> complexityThreshold = screenTimeEstimator.estimate(score);

            List<String> screensInGroup = screensPerComplexity.computeIfAbsent(complexityThreshold, key -> new ArrayList<>());
            screensInGroup.add(name);
            return complexityThreshold.getOutputValue();
        }).reduce(BigDecimal::add).orElse(new BigDecimal("0"));

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

        ScreensTotalInfo screensTotalInfo = createScreensTotalInfo(screensCollector);

        List<MiscNote> miscNotes = new ArrayList<>();
        Properties webAppProperties = uiModulesAnalysisResult.getWebAppProperties();
        if (webAppProperties != null) {
            boolean foldersPaneEnabled = isFoldersPaneEnabled(webAppProperties);
            if (foldersPaneEnabled) {
                miscNotes.add(MiscNotes.folderPaneEnabled());
            }
        }

        CubaProjectEstimationResult.Builder resultBuilder = CubaProjectEstimationResult.builder();
        return resultBuilder
                .setInitialMigrationCost(BigDecimal.valueOf(estimationDataProvider.getInitialMigrationCost())) // todo rule based on amount of entities?
                .setBaseEntitiesMigrationCost(BigDecimal.valueOf(estimationDataProvider.getBaseEntitiesMigrationCost()))
                .setScreensPerComplexity(screensPerComplexity)
                .setEntitiesPerPersistenceUnit(globalModuleAnalysisResult.getEntitiesPerPersistenceUnit())
                .setAllUiComponents(screensTotalInfo.getUiComponents())
                .setLegacyListenersCost(BigDecimal.valueOf(legacyListenersCost))
                .setLegacyListeners(new ArrayList<>(globalModuleAnalysisResult.getLegacyListeners()))
                .setScreensTotalCost(screenSumHours)
                .setAppComponents(appComponents)
                .setMiscNotes(miscNotes)
                .build();
    }

    protected ScreensTotalInfo createScreensTotalInfo(ScreensCollector screensCollector) {
        ScreensTotalInfo screensTotalInfo = new ScreensTotalInfo();
        AtomicInteger legacyScreensCounter = new AtomicInteger();
        AtomicInteger screensCounter = new AtomicInteger();
        AtomicInteger fragmentsCounter = new AtomicInteger();
        Map<String, Integer> totalUiComponents = new HashMap<>();
        Map<String, Integer> totalFacets = new HashMap<>();

        Collection<ScreenInfo> screenInfos = screensCollector.getScreensByDescriptors().values();
        screenInfos.forEach(screenInfo -> {
            List<Facet> facets = screenInfo.getFacets();
            if (facets != null) {
                facets.forEach(facet -> {
                    Integer facetCount = totalFacets.getOrDefault(facet.getName(), 0);
                    facetCount++;
                    totalFacets.put(facet.getName(), facetCount);
                });
            }

            List<LayoutItem> layoutItems = Optional.ofNullable(screenInfo.getLayout())
                    .map(Layout::getAllItems)
                    .orElse(Collections.emptyList());
            layoutItems.forEach(layoutItem -> {
                Integer uiComponentCounter = totalUiComponents.getOrDefault(layoutItem.getName(), 0);
                uiComponentCounter += layoutItem.getQuantity();
                totalUiComponents.put(layoutItem.getName(), uiComponentCounter);
            });

            if (screenInfo.isLegacy()) {
                legacyScreensCounter.incrementAndGet();
            } else {
                if (screenInfo.isFragment()) {
                    fragmentsCounter.incrementAndGet();
                } else {
                    screensCounter.incrementAndGet();
                }
            }
        });

        screensTotalInfo.setScreens(screensCounter.get());
        screensTotalInfo.setFragments(fragmentsCounter.get());
        screensTotalInfo.setLegacyScreens(legacyScreensCounter.get());
        screensTotalInfo.setFacets(totalFacets);
        screensTotalInfo.setUiComponents(totalUiComponents);

        return screensTotalInfo;
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
