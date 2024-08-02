package io.jmix.migration.analysis.estimation;

import io.jmix.migration.analysis.Metrics;
import io.jmix.migration.analysis.estimation.rules.*;
import io.jmix.migration.analysis.issue.UiComponentIssue;
import io.jmix.migration.analysis.issue.UiComponentIssueType;
import io.jmix.migration.analysis.issue.UiComponentIssuesRegistry;
import io.jmix.migration.analysis.model.*;
import io.jmix.migration.analysis.parser.screen.ScreensCollector;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static io.jmix.migration.analysis.Metrics.*;

public class ScreenEstimator {

    private static final Logger log = LoggerFactory.getLogger(ScreenEstimator.class);

    protected final UiComponentIssuesRegistry uiComponentIssuesRegistry;
    protected final EstimationDataProvider estimationDataProvider;

    protected final Map<String, NumericMetricRule> numericMetricRules;

    public ScreenEstimator(UiComponentIssuesRegistry uiComponentIssuesRegistry,
                           EstimationDataProvider estimationDataProvider) {
        this.uiComponentIssuesRegistry = uiComponentIssuesRegistry;
        this.estimationDataProvider = estimationDataProvider;

        //todo rule factory/provider
        Map<String, NumericMetricRule> numericMetricRulesTmp = new HashMap<>();
        numericMetricRulesTmp.put(METHOD_CALLS_AMOUNT_METRIC_CODE, new ScreenControllerMethodsCallsRule(estimationDataProvider.getScreenControllerMethodsCallsComplexityThresholds()));
        numericMetricRulesTmp.put(UI_COMPONENT_CREATE_CALLS_AMOUNT_METRIC_CODE, new UiComponentCreateCallsRule(estimationDataProvider.getScreenDescriptorUiComponentCreateCallComplexityScore()));
        numericMetricRulesTmp.put(SCREEN_DESCRIPTOR_NESTED_DATA_ITEMS_METRIC_CODE, new ScreenDescriptorNestedDataItemsRule(estimationDataProvider.getScreenDescriptorHasNestedDataItemComplexityScore()));
        numericMetricRulesTmp.put(SCREEN_DESCRIPTOR_CHANGED_UI_COMPONENTS_SCORE_METRIC_CODE, new ScreenDescriptorChangedUiComponentsScoreRule(estimationDataProvider.getScreenChangedUiComponentsComplexityBaseValue()));
        numericMetricRulesTmp.put(SCREEN_DESCRIPTOR_EXTENDS_SCREEN_METRIC_CODE, new ScreenDescriptorExtendsScreenRule(estimationDataProvider.getScreenDescriptorExtendsScreenComplexityScore()));

        this.numericMetricRules = numericMetricRulesTmp;
    }

    public Map<String, ScreenComplexityScore> estimate(ScreensCollector screensCollector) {

        Map<String, ScreenInfo> screensByDescriptors = screensCollector.getScreensByDescriptors();
        List<ScreenInfo> allScreensInfos = new ArrayList<>(screensByDescriptors.values());
        Map<String, ScreenInfo> screensByControllers = screensCollector.getScreensByControllers();
        List<ScreenInfo> controllersWithoutDescriptors = screensByControllers.values().stream().filter(screenByController -> {
            String descriptorFile = screenByController.getDescriptorFile();
            return descriptorFile == null;
        }).toList();

        allScreensInfos.addAll(controllersWithoutDescriptors);

        Map<String, ScreenComplexityScore> result = new HashMap<>();
        allScreensInfos.forEach(screenInfo -> {
            ScreenComplexityScore score = estimate(screenInfo);
            result.put(resolveScreenName(screenInfo), score);
        });
        return result;
    }

    public ScreenComplexityScore estimate(ScreenInfo screenInfo) {
        List<NumericMetric> numericMetrics = new ArrayList<>();

        // Descriptor extension
        String extendedDescriptor = screenInfo.getExtendedDescriptor();
        if (StringUtils.isNotEmpty(extendedDescriptor)) {
            numericMetrics.add(Metrics.createScreenDescriptorExtendsScreenMetric());
        }

        // screen data
        ScreenData screenData = screenInfo.getScreenData();
        if (screenData != null) {
            List<ScreenDataItem> screenDataItems = screenData.getItems();

            int dataItemsWithoutQuery = 0;
            int nestedDataItems = 0;
            for (ScreenDataItem screenDataItem : screenDataItems) {
                String query = screenDataItem.getQuery();
                if (StringUtils.isEmpty(query)) {
                    dataItemsWithoutQuery++;
                }

                if (screenDataItem.getParent() != null) {
                    nestedDataItems++;
                }
            }
            //numericMetrics.add(new NumericMetric("screen-descriptor-data-items-without-query", dataItemsWithoutQuery)); //todo exclude single value data items
            numericMetrics.add(Metrics.createScreenDescriptorNestedDataItemsMetric(nestedDataItems));
        }

        // Layout
        Layout layout = screenInfo.getLayout();

        if (layout != null) {
            List<LayoutItem> allItems = layout.getAllItems();
            AtomicInteger nonIssuedUiComponents = new AtomicInteger();
            AtomicInteger combinedExtraComplexityScore = new AtomicInteger();

            allItems.forEach(layoutItem -> {
                UiComponentIssue issue = uiComponentIssuesRegistry.getIssue(layoutItem.getName());
                if (issue != null) {
                    if (!issue.getType().equals(UiComponentIssueType.ABSENT)) {
                        combinedExtraComplexityScore.addAndGet(issue.getExtraComplexityScore());
                    }
                } else {
                    nonIssuedUiComponents.incrementAndGet();
                }
            });
            numericMetrics.add(Metrics.createScreenDescriptorChangedUiComponentsScoreMetric(combinedExtraComplexityScore.get()));
        }

        // Controller
        ScreenControllerDetails controllerDetails = screenInfo.getControllerDetails();
        if (controllerDetails != null) {
            Map<String, Integer> combinedMethodMetricsValues = new HashMap<>();
            List<MethodDetails> methods = controllerDetails.getMethods();
            methods.forEach(methodDetails -> {
                List<NumericMetric> methodNumericMetrics = methodDetails.getNumericMetrics();
                methodNumericMetrics.forEach(methodNumericMetric -> {
                    String code = methodNumericMetric.getCode();
                    Integer currentValue = combinedMethodMetricsValues.getOrDefault(code, 0);
                    currentValue += methodNumericMetric.getValue();
                    combinedMethodMetricsValues.put(code, currentValue);
                });
            });

            combinedMethodMetricsValues.forEach((code, value) -> {
                numericMetrics.add(new NumericMetric(code, value));
            });
        }

        String screenName;
        if (controllerDetails != null) {
            screenName = controllerDetails.getClassName();
        } else {
            screenName = screenInfo.getDescriptorFile();
        }

        StringBuilder metricsSb = new StringBuilder();
        numericMetrics.forEach(metric -> {
            metricsSb.append("\nMetric '").append(metric.getCode()).append("' : '").append(metric.getValue()).append("'");
        });
        log.debug("Screen: {}: metrics:{}", screenName, metricsSb);

        ScreenComplexityScore score = new ScreenComplexityScore();
        numericMetrics.forEach(metric -> {
            int scoreForMetric = getScoreForMetric(metric);
            if (scoreForMetric > 0) {
                score.addRawValue(scoreForMetric);
            }
        });

        return score;
    }

    protected int getScoreForMetric(NumericMetric metric) {
        NumericMetricRule numericMetricRule = numericMetricRules.get(metric.getCode());
        if (numericMetricRule != null) {
            return numericMetricRule.apply(metric.getValue());
        } else {
            // todo rule not found
            log.warn("Rule not found for metric '{}'", metric.getCode());
            return 0;
        }
    }

    @Nullable
    protected String resolveScreenName(ScreenInfo screenInfo) {
        if (StringUtils.isNotEmpty(screenInfo.getScreenId())) {
            return screenInfo.getScreenId();
        }
        return StringUtils.firstNonEmpty(screenInfo.getScreenId(), screenInfo.getDescriptorFile(), screenInfo.getControllerClass());
    }
}
