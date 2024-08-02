package io.jmix.migration.analysis;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import io.jmix.migration.CliRunner;
import io.jmix.migration.analysis.addon.AppComponentType;
import io.jmix.migration.analysis.addon.CubaAppComponentInfo;
import io.jmix.migration.analysis.addon.CubaAppComponentsInfoRegistry;
import io.jmix.migration.analysis.estimation.EstimationDataProvider;
import io.jmix.migration.analysis.estimation.ScreenEstimator;
import io.jmix.migration.analysis.estimation.ScreenTimeEstimator;
import io.jmix.migration.analysis.estimation.rules.LegacyEntityListenersRule;
import io.jmix.migration.analysis.estimation.rules.NumericMetricRule;
import io.jmix.migration.analysis.issue.UiComponentIssue;
import io.jmix.migration.analysis.issue.UiComponentIssuesRegistry;
import io.jmix.migration.analysis.model.*;
import io.jmix.migration.analysis.parser.screen.ScreensCollector;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static io.jmix.migration.analysis.MetricCodes.LEGACY_ENTITY_LISTENERS_METRIC_CODE;
import static java.time.temporal.ChronoField.*;

public class ProjectAnalyzer {

    private static final Logger log = LoggerFactory.getLogger(ProjectAnalyzer.class);

    public static final String MODULES_DIR = "modules";
    public static final String CORE_MODULE_DIR = "core";
    public static final String GLOBAL_MODULE_DIR = "global";
    public static final String WEB_MODULE_DIR = "web";
    public static final String GUI_MODULE_DIR = "gui";
    public static final String SRC_DIR = "src";

    private final UiComponentIssuesRegistry uiComponentIssuesRegistry;
    private final EstimationDataProvider estimationDataProvider;
    private final ScreenEstimator screenEstimator;
    private final ScreenTimeEstimator screenTimeEstimator;
    private final CubaAppComponentsInfoRegistry appComponentsInfoRegistry;
    private final Map<String, NumericMetricRule> numericMetricRules;

    public ProjectAnalyzer() {
        this.uiComponentIssuesRegistry = UiComponentIssuesRegistry.create();
        this.estimationDataProvider = new EstimationDataProvider(); //todo provide external file
        this.screenEstimator = new ScreenEstimator(uiComponentIssuesRegistry, estimationDataProvider);
        this.screenTimeEstimator = new ScreenTimeEstimator(estimationDataProvider.getScreenComplexityTimeEstimationThresholds());
        this.appComponentsInfoRegistry = CubaAppComponentsInfoRegistry.create();
        this.numericMetricRules = generateMetricRules();
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
        log.info("---=== Start analyze project ===---");
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

        ProjectEstimationResult projectEstimationResult = estimateProject(coreModuleAnalysisResult, globalModuleAnalysisResult, uiModulesAnalysisResult);
        generateHtmlReport(projectPathString, projectEstimationResult);
    }

    protected void generateHtmlReport(String project, ProjectEstimationResult result) {
        Configuration configuration = createFremarkerConfiguration();
        String fileName = createResultFileName();

        Map<String, Object> data = new HashMap<>();

        data.put("projectName", project);

        //Entities amount
        data.put("entitiesAmount", result.getEntitiesAmount());

        //Screens
        List<Map<String, Object>> complexityGroupRows = createComplexityGroupRows(result);
        data.put("screenComplexityGroups", complexityGroupRows);
        data.put("screensTotalHours", result.getScreensTotalCost());
        data.put("screensTotalAmount", result.getScreensTotalAmount());

        // UI components
        List<Map<String, Object>> uiComponentIssuesRows = createUiComponentIssuesRows(result);
        data.put("uiComponentIssues", uiComponentIssuesRows);

        //Addons
        List<Map<String, Object>> appComponentsRows = createAppComponentsRows(result);
        data.put("appComponents", appComponentsRows);

        // General estimations
        List<Map<String, Object>> estimationItemsRows = createEstimationItemsRows(result);
        data.put("estimationItems", estimationItemsRows);

        // Total
        data.put("totalEstimation", result.getTotalEstimation());

        // Write to file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true))) {
            Template template = configuration.getTemplate("report-template.ftl");

            template.process(data, writer);
            writer.flush();
        } catch (IOException | TemplateException e) {
            throw new RuntimeException(e);
        }
    }

    protected Configuration createFremarkerConfiguration() {
        Configuration configuration = new Configuration(Configuration.VERSION_2_3_23);
        configuration.setDefaultEncoding("UTF-8");
        configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        configuration.setLogTemplateExceptions(false);
        configuration.setClassForTemplateLoading(CliRunner.class, "/templates");
        return configuration;
    }

    protected List<Map<String, Object>> createComplexityGroupRows(ProjectEstimationResult result) {
        Map<ThresholdItem<Integer, BigDecimal>, List<String>> screensPerComplexity = result.getScreensPerComplexity();
        List<Map<String, Object>> complexityGroupRows = new ArrayList<>();
        screensPerComplexity.forEach(((thresholdItem, screens) -> {
            String name = thresholdItem.getName();
            int order = thresholdItem.getOrder();
            BigDecimal cost = thresholdItem.getOutputValue();
            int amount = screens.size();
            BigDecimal total = cost.multiply(BigDecimal.valueOf(amount));
            Map<String, Object> rowData = new HashMap<>();
            rowData.put("name", name);
            rowData.put("order", order);
            rowData.put("cost", cost);
            rowData.put("amount", amount);
            rowData.put("total", total);
            complexityGroupRows.add(rowData);
        }));
        complexityGroupRows.sort((o1, o2) -> {
            int order1 = (int) o1.get("order");
            int order2 = (int) o2.get("order");
            return Integer.compare(order1, order2);
        });

        return complexityGroupRows;
    }

    protected List<Map<String, Object>> createUiComponentIssuesRows(ProjectEstimationResult result) {
        List<String> components = new ArrayList<>(result.getAllUiComponents().keySet());
        components.sort(String::compareTo);
        List<Map<String, Object>> uiComponentIssuesRows = new ArrayList<>();
        components.forEach(component -> {
            UiComponentIssue issue = uiComponentIssuesRegistry.getIssue(component);
            if (issue != null) {
                uiComponentIssuesRows.add(Map.of(
                        "name", issue.getComponent(),
                        "notes", issue.getNotes()
                ));
            }
        });
        return uiComponentIssuesRows;
    }

    protected List<Map<String, Object>> createAppComponentsRows(ProjectEstimationResult result) {
        List<Map<String, Object>> appComponentsRows = new ArrayList<>();
        List<CubaAppComponentInfo> appComponents = result.getAppComponents();
        appComponents.forEach(addon -> {
            appComponentsRows.add(Map.of("name", addon.getCubaName(), "notes", addon.getNotes()));
        });
        return appComponentsRows;
    }

    protected List<Map<String, Object>> createEstimationItemsRows(ProjectEstimationResult result) {
        List<Map<String, Object>> estimationItemsRows = new ArrayList<>();
        estimationItemsRows.add(createEstimationItemRow("Initial migration", result.getInitialMigrationCost()));
        estimationItemsRows.add(createEstimationItemRow("Base entities", result.getBaseEntitiesMigrationCost()));
        estimationItemsRows.add(createEstimationItemRow("Legacy listeners", result.getBaseEntitiesMigrationCost()));
        estimationItemsRows.add(createEstimationItemRow("Screens", result.getScreensTotalCost()));
        return estimationItemsRows;
    }

    protected Map<String, Object> createEstimationItemRow(String category, BigDecimal estimation) {
        return Map.of(
                "category", category,
                "estimation", estimation
        );
    }

    protected String createResultFileName() {
        DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                .append(DateTimeFormatter.ISO_LOCAL_DATE)
                .appendLiteral('T')
                .appendValue(HOUR_OF_DAY, 2)
                .appendValue(MINUTE_OF_HOUR, 2)
                .appendValue(SECOND_OF_MINUTE, 2)
                .appendFraction(MILLI_OF_SECOND, 0, 3, false)
                .toFormatter();

        return "results_" + LocalDateTime.now().format(formatter) + ".html";
    }

    protected ProjectEstimationResult estimateProject(CoreModuleAnalysisResult coreModuleAnalysisResult,
                                                      GlobalModuleAnalysisResult globalModuleAnalysisResult,
                                                      UiModulesAnalysisResult uiModulesAnalysisResult) {
        ScreensCollector screensCollector = uiModulesAnalysisResult.getScreensCollector();
        Map<String, ScreenComplexityScore> screenScores = screenEstimator.estimate(screensCollector);
        StringBuilder screenScoresSb = new StringBuilder("Screens Scores:");
        Map<ThresholdItem<Integer, BigDecimal>, List<String>> screensPerComplexity = new HashMap<>();
        BigDecimal screenSumHours = screenScores.entrySet().stream().map(entry -> {
            String name = entry.getKey();
            ScreenComplexityScore score = entry.getValue();
            ThresholdItem<Integer, BigDecimal> complexityThreshold = screenTimeEstimator.estimate(score);

            List<String> screensInGroup = screensPerComplexity.computeIfAbsent(complexityThreshold, key -> new ArrayList<>());
            screensInGroup.add(name);

            screenScoresSb.append("\nScreen: '").append(name).append("'. Score: ").append(score.getValue()).append(". Hours: ").append(complexityThreshold.getOutputValue());
            return complexityThreshold.getOutputValue();
        }).reduce(BigDecimal::add).orElse(new BigDecimal("0"));

        screenScoresSb.append("\n\nTotal screens hours: ").append(screenSumHours);

        screensPerComplexity.forEach((complexityThreshold, screens) -> {
            screenScoresSb
                    .append("\n\nGroup: ").append(complexityThreshold.getName())
                    .append(", Cost: ").append(complexityThreshold.getOutputValue())
                    .append(", Amount: ").append(screens.size())
                    .append(", TotalCost: ").append(complexityThreshold.getOutputValue().multiply(BigDecimal.valueOf(screens.size())));
        });

        log.info("{}", screenScoresSb);

        NumericMetric legacyListenersAmountMetric = globalModuleAnalysisResult.getLegacyListenersAmount();
        NumericMetricRule legacyListenersAmountMetricRule = numericMetricRules.get(legacyListenersAmountMetric.getCode());
        int legacyListenersCost = legacyListenersAmountMetricRule.apply(legacyListenersAmountMetric.getValue());

        List<String> appComponentPackages = coreModuleAnalysisResult.getAppComponents();
        List<CubaAppComponentInfo> appComponents = new ArrayList<>();
        appComponentPackages.forEach(componentPackage -> {
            CubaAppComponentInfo appComponentInfo = appComponentsInfoRegistry.getAppComponentInfo(componentPackage);
            if(appComponentInfo == null) {
                appComponents.add(CubaAppComponentInfo.createMissing(componentPackage));
                return;
            }

            if(AppComponentType.BASE_APP.equals(appComponentInfo.getAppComponentType())) {
                return;
            }

            appComponents.add(appComponentInfo);
        });

        ScreensTotalInfo screensTotalInfo = createScreensTotalInfo(screensCollector);

        ProjectEstimationResult.Builder resultBuilder = ProjectEstimationResult.builder();
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
}
