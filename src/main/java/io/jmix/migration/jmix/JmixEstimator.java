package io.jmix.migration.jmix;

import io.jmix.migration.classicui.estimation.ScreenEstimator;
import io.jmix.migration.classicui.parser.ScreensCollector;
import io.jmix.migration.core.estimation.EstimationDataProvider;
import io.jmix.migration.core.estimation.ScreenComplexityScore;
import io.jmix.migration.core.estimation.ScreenTimeEstimator;
import io.jmix.migration.core.estimation.ThresholdItem;
import io.jmix.migration.core.incident.UiComponentIssuesRegistry;
import io.jmix.migration.jmix.model.JmixConfigInfo;
import io.jmix.migration.jmix.model.JmixEstimationResult;
import io.jmix.migration.jmix.model.JmixProjectAnalysisResult;
import io.jmix.migration.jmix.model.JmixSourcesScanResult;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Turns the collected facts into hour estimates using the "jmix" weights profile.
 * Screens go through the shared two-stage model (metrics to score to hours);
 * add-on costs come from the add-ons registry hints; red flags are intentionally
 * not priced and stay escalated in the report.
 */
public class JmixEstimator {

    public static final String JMIX_PROFILE = "jmix";

    protected final EstimationDataProvider estimationDataProvider;
    protected final ScreenEstimator screenEstimator;
    protected final ScreenTimeEstimator screenTimeEstimator;

    public JmixEstimator(UiComponentIssuesRegistry uiComponentIssuesRegistry, @Nullable String estimationDataFile) {
        this.estimationDataProvider = new EstimationDataProvider(JMIX_PROFILE, estimationDataFile);
        this.screenEstimator = new ScreenEstimator(uiComponentIssuesRegistry, estimationDataProvider);
        this.screenTimeEstimator = new ScreenTimeEstimator(estimationDataProvider.getScreenComplexityTimeEstimationThresholds());
    }

    public JmixEstimationResult estimate(ScreensCollector screensCollector,
                                         JmixDataFacts dataFacts,
                                         List<JmixProjectAnalysisResult.ResolvedAddon> addons,
                                         JmixSourcesScanResult sourcesScan,
                                         JmixConfigInfo configInfo) {
        Map<String, ScreenComplexityScore> screenScores = screenEstimator.estimate(screensCollector);
        Map<ThresholdItem<Integer, BigDecimal>, List<String>> screensPerComplexity = new HashMap<>();
        Map<String, List<String>> screensRequireDecision = new TreeMap<>();
        BigDecimal screensCost = BigDecimal.ZERO;
        for (Map.Entry<String, ScreenComplexityScore> entry : screenScores.entrySet()) {
            String name = entry.getKey();
            ScreenComplexityScore score = entry.getValue();
            ThresholdItem<Integer, BigDecimal> complexityThreshold = screenTimeEstimator.estimate(score);

            screensPerComplexity.computeIfAbsent(complexityThreshold, key -> new ArrayList<>()).add(name);
            screensCost = screensCost.add(complexityThreshold.getOutputValue());

            if (!score.getAbsentComponents().isEmpty()) {
                screensRequireDecision.put(name, new ArrayList<>(score.getAbsentComponents()));
            }
        }

        BigDecimal initialCost = BigDecimal.valueOf(estimationDataProvider.getCost("initial-migration-cost"));
        BigDecimal jakartaCost = estimateByThresholds("jakarta-sweep-cost", dataFacts.javaxImportFilesCount());
        BigDecimal addonsCost = estimateAddonsCost(addons);
        BigDecimal rolesCost = BigDecimal.valueOf((long) dataFacts.rolesCount()
                * estimationDataProvider.getCost("security-role-cost"));
        BigDecimal configCost = hasConfigWork(configInfo)
                ? BigDecimal.valueOf(estimationDataProvider.getCost("config-migration-cost"))
                : BigDecimal.ZERO;
        BigDecimal themesCost = BigDecimal.valueOf((long) countCustomThemes(sourcesScan)
                * estimationDataProvider.getCost("custom-theme-cost"));

        return new JmixEstimationResult(screensPerComplexity, screensCost, screensRequireDecision,
                initialCost, jakartaCost, addonsCost, rolesCost, configCost, themesCost);
    }

    protected BigDecimal estimateByThresholds(String unitName, int inputValue) {
        return estimationDataProvider.getDecimalThresholds(unitName).stream()
                .filter(item -> item.getThresholdRange().contains(inputValue))
                .findFirst()
                .map(ThresholdItem::getOutputValue)
                .orElseThrow(() -> new RuntimeException(
                        "No threshold range of unit '" + unitName + "' covers value " + inputValue));
    }

    protected BigDecimal estimateAddonsCost(List<JmixProjectAnalysisResult.ResolvedAddon> addons) {
        int total = 0;
        for (JmixProjectAnalysisResult.ResolvedAddon addon : addons) {
            if (addon.getAddonInfo() != null && addon.getAddonInfo().getCostHint() != null) {
                total += addon.getAddonInfo().getCostHint();
            }
        }
        return BigDecimal.valueOf(total);
    }

    protected boolean hasConfigWork(JmixConfigInfo configInfo) {
        return !configInfo.getPropertyRenames().isEmpty()
                || configInfo.getMenuScreenItemsCount() > 0
                || configInfo.isUiDataChangelogIncluded();
    }

    protected int countCustomThemes(JmixSourcesScanResult sourcesScan) {
        return (int) sourcesScan.getRedFlags().stream()
                .filter(redFlag -> "Custom theme".equals(redFlag.getCategory()))
                .count();
    }

    /**
     * Scalar facts feeding non-screen categories.
     */
    public record JmixDataFacts(int javaxImportFilesCount, int rolesCount) {
    }
}
