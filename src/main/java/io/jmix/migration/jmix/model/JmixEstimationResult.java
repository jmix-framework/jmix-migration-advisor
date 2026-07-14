package io.jmix.migration.jmix.model;

import io.jmix.migration.core.estimation.ThresholdItem;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Hour estimates per migration category. Screen hours use the two-stage model
 * (metrics to score to hours); red flags are NOT included: their cost is indeterminate
 * and they are escalated in a dedicated report section instead.
 */
public class JmixEstimationResult {

    private final Map<ThresholdItem<Integer, BigDecimal>, List<String>> screensPerComplexity;
    private final BigDecimal screensCost;
    private final Map<String, List<String>> screensRequireDecision;

    private final BigDecimal initialMigrationCost;
    private final BigDecimal jakartaSweepCost;
    private final BigDecimal addonsCost;
    private final BigDecimal securityRolesCost;
    private final BigDecimal configCost;
    private final BigDecimal customThemesCost;

    private final BigDecimal totalCost;

    public JmixEstimationResult(Map<ThresholdItem<Integer, BigDecimal>, List<String>> screensPerComplexity,
                                BigDecimal screensCost,
                                Map<String, List<String>> screensRequireDecision,
                                BigDecimal initialMigrationCost,
                                BigDecimal jakartaSweepCost,
                                BigDecimal addonsCost,
                                BigDecimal securityRolesCost,
                                BigDecimal configCost,
                                BigDecimal customThemesCost) {
        this.screensPerComplexity = Map.copyOf(screensPerComplexity);
        this.screensCost = screensCost;
        this.screensRequireDecision = screensRequireDecision;
        this.initialMigrationCost = initialMigrationCost;
        this.jakartaSweepCost = jakartaSweepCost;
        this.addonsCost = addonsCost;
        this.securityRolesCost = securityRolesCost;
        this.configCost = configCost;
        this.customThemesCost = customThemesCost;
        this.totalCost = sum(initialMigrationCost, jakartaSweepCost, screensCost,
                addonsCost, securityRolesCost, configCost, customThemesCost);
    }

    public Map<ThresholdItem<Integer, BigDecimal>, List<String>> getScreensPerComplexity() {
        return screensPerComplexity;
    }

    public BigDecimal getScreensCost() {
        return screensCost;
    }

    public long getScreensTotalAmount() {
        return screensPerComplexity.values().stream().mapToLong(List::size).sum();
    }

    public Map<String, List<String>> getScreensRequireDecision() {
        return screensRequireDecision;
    }

    public BigDecimal getInitialMigrationCost() {
        return initialMigrationCost;
    }

    public BigDecimal getJakartaSweepCost() {
        return jakartaSweepCost;
    }

    public BigDecimal getAddonsCost() {
        return addonsCost;
    }

    public BigDecimal getSecurityRolesCost() {
        return securityRolesCost;
    }

    public BigDecimal getConfigCost() {
        return configCost;
    }

    public BigDecimal getCustomThemesCost() {
        return customThemesCost;
    }

    public BigDecimal getTotalCost() {
        return totalCost;
    }

    protected BigDecimal sum(BigDecimal... values) {
        return Arrays.stream(values).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
