package io.jmix.migration.analysis;

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

    private final UiComponentIssuesRegistry uiComponentIssuesRegistry = UiComponentIssuesRegistry.create();
    private final EstimationDataProvider estimationDataProvider = new EstimationDataProvider(); //todo provide external file
    private final ScreenEstimator screenEstimator = new ScreenEstimator(uiComponentIssuesRegistry, estimationDataProvider);
    private final ScreenTimeEstimator screenTimeEstimator = new ScreenTimeEstimator(estimationDataProvider.getScreenComplexityTimeEstimationThresholds());

    private final Map<String, NumericMetricRule> numericMetricRules;

    public ProjectAnalyzer() {
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
        ScreensCollector screensCollector = uiModulesAnalysisResult.getScreensCollector();

        log.info("\n\n-----===== TOTALS =====-----");
        ScreensTotalInfo screensTotalInfo = createScreensTotalInfo(screensCollector);
        log.info("Screens (all types): {}", screensTotalInfo.getScreens() + screensTotalInfo.getLegacyScreens() + screensTotalInfo.getFragments());
        log.info("Legacy screens: {}", screensTotalInfo.getLegacyScreens());
        log.info("Non-legacy screens: {}", screensTotalInfo.getScreens());
        log.info("Fragments: {}", screensTotalInfo.getFragments());
        log.info("Facets: {}", screensTotalInfo.getFacets());
        Map<String, Integer> uiComponents = screensTotalInfo.getUiComponents();
        StringBuilder uiCompSb = new StringBuilder("UiComponents:");
        uiComponents.forEach((name, count) -> {
            uiCompSb.append("\n").append(name).append(": ").append(count);
        });
        log.info(uiCompSb.toString());

        //screenEstimator.estimate(screensCollector);

        ProjectEstimationResult projectEstimationResult = estimateProject(coreModuleAnalysisResult, globalModuleAnalysisResult, uiModulesAnalysisResult);
        generateReport(projectEstimationResult);
    }

    protected void generateReport(ProjectEstimationResult result) {
        /*Configuration configuration = new Configuration(Configuration.VERSION_2_3_23);
        configuration.setDefaultEncoding("UTF-8");
        configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        configuration.setLogTemplateExceptions(false);

        configuration.setClassForTemplateLoading(CliRunner.class, "/templates");


        Map<String, Object> data = new HashMap<>();
        List<Map<String, Object>> groups;
        List<Map.Entry<ScreenComplexityGroup, List<String>>> entries = new ArrayList<>(result.getScreensPerGroup().entrySet());
        data.put("userDetails", model.getUserDetails());

        try {
            Template template = configuration.getTemplate("commonTemplate.ftl");

            // Console output
            Writer out = new OutputStreamWriter(System.out);
            template.process(prepareData(), out);
            out.flush();

        } catch (IOException | TemplateException e) {
            throw new RuntimeException(e);
        }*/

        DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                .append(DateTimeFormatter.ISO_LOCAL_DATE)
                .appendLiteral('T')
                .appendValue(HOUR_OF_DAY, 2)
                //.appendLiteral(':')
                .appendValue(MINUTE_OF_HOUR, 2)
                //.optionalStart()
                //.appendLiteral(':')
                .appendValue(SECOND_OF_MINUTE, 2)
                //.optionalStart()
                .appendFraction(MILLI_OF_SECOND, 0, 3, false)
                .toFormatter();

        String fileName = "results_" + LocalDateTime.now().format(formatter) + ".txt";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true))) {
            writer.write("General");
            writer.newLine();
            writer.newLine();

            writer.write("Entities: " + result.getEntitiesAmount());

            Map<ThresholdItem<Integer, BigDecimal>, List<String>> screensPerGroup = result.getScreensPerComplexity();
            List<ThresholdItem<Integer, BigDecimal>> groups = new ArrayList<>(screensPerGroup.keySet());
            groups.sort(Comparator.comparingInt(ThresholdItem::getOrder));

            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(System.lineSeparator()).append(System.lineSeparator())
                    .append("Screens complexity groups:");
            groups.forEach(group -> {
                int amount = screensPerGroup.get(group).size();
                stringBuilder.append(System.lineSeparator()).append("\t")
                        .append(group.getName())
                        .append(": Amount=").append(amount)
                        .append(", Cost=").append(group.getOutputValue())
                        .append(", Total=").append(group.getOutputValue().multiply(BigDecimal.valueOf(amount)));
            });
            writer.write(stringBuilder.toString());


            stringBuilder.delete(0, stringBuilder.length());
            stringBuilder.append(System.lineSeparator()).append(System.lineSeparator()).append("UiComponents");
            List<String> components = new ArrayList<>(result.getAllUiComponents().keySet());
            components.sort(String::compareTo);
            components.forEach(component -> {
                UiComponentIssue issue = uiComponentIssuesRegistry.getIssue(component);
                if (issue != null) {
                    stringBuilder.append(System.lineSeparator()).append("\t")
                            .append(issue.getComponent()).append(": ").append(issue.getNotes());
                }
            });
            writer.write(stringBuilder.toString());


        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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

        ScreensTotalInfo screensTotalInfo = createScreensTotalInfo(screensCollector);

        ProjectEstimationResult.Builder resultBuilder = ProjectEstimationResult.builder();
        ProjectEstimationResult result = resultBuilder
                .setInitialMigrationCost(estimationDataProvider.getInitialMigrationCost()) // todo rule based on amount of entities?
                .setBaseEntitiesMigrationCost(estimationDataProvider.getBaseEntitiesMigrationCost())
                .setScreensPerComplexity(screensPerComplexity)
                .setEntitiesPerPersistenceUnit(globalModuleAnalysisResult.getEntitiesPerPersistenceUnit())
                .setAllUiComponents(screensTotalInfo.getUiComponents())
                .setLegacyListenersCost(legacyListenersCost)
                .build();
        return result;
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

    protected void printScreenInfo(ScreenInfo screenInfo) {
        boolean legacy = screenInfo.isLegacy();
        String screenId = screenInfo.getScreenId();
        String descriptorFile = screenInfo.getDescriptorFile();
        String controllerClass = screenInfo.getControllerClass();

        log.info("\n\nScreen: Descriptor = {}, ID = {}, Controller = {}, Legacy = {}", descriptorFile, screenId, controllerClass, legacy);

        StringBuilder screenDataSb = new StringBuilder();
        ScreenData screenData = screenInfo.getScreenData();
        if (screenData == null) {
            screenDataSb.append("NONE");
        } else {
            List<ScreenDataItem> screenDataItems = screenData.getItems();
            screenDataItems.forEach(item -> screenDataSb.append("\n").append(item));
        }
        log.info("Screen data: {}", screenDataSb);

        StringBuilder screenFacetsSb = new StringBuilder();
        List<Facet> facets = screenInfo.getFacets();
        if (facets == null) {
            screenFacetsSb.append("NONE");
        } else {
            facets.forEach(facet -> screenFacetsSb.append("\n").append(facet));
        }
        log.info("Screen facets: {}", screenFacetsSb);

        StringBuilder screenLayoutSb = new StringBuilder();
        Layout layout = screenInfo.getLayout();
        if (layout == null) {
            screenLayoutSb.append("NONE");
        } else {
            List<LayoutItem> allLayoutItems = layout.getAllItems();
            allLayoutItems.forEach(item -> screenLayoutSb.append("\n").append(item));
        }
        log.info("Screen layout: {}", screenLayoutSb);

        log.info("---=== ESTIMATION ===---");
        //screenEstimator.estimate(screenInfo);
    }
}
