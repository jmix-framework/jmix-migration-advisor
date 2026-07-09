package io.jmix.migration.jmix;

import io.jmix.migration.jmix.addon.JmixAddonsRegistry;
import io.jmix.migration.jmix.model.JmixProjectAnalysisResult;
import io.jmix.migration.jmix.project.BasePackageResolver;
import io.jmix.migration.core.project.GradleBuildParser;
import io.jmix.migration.core.project.JmixProjectDescriptor;
import io.jmix.migration.core.project.ProjectType;
import io.jmix.migration.core.project.ProjectTypeDetector;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Analyzes a Jmix 1.x (classic UI) project in context of migration to the current Jmix version.
 */
public class JmixProjectAnalyzer {

    private static final Logger log = LoggerFactory.getLogger(JmixProjectAnalyzer.class);

    protected static final String JMIX_GROUP_PREFIX = "io.jmix";

    private final GradleBuildParser gradleBuildParser;
    private final ProjectTypeDetector projectTypeDetector;
    private final BasePackageResolver basePackageResolver;
    private final JmixAddonsRegistry addonsRegistry;
    private final JmixHtmlReportGenerator reportGenerator;

    public JmixProjectAnalyzer() {
        this.gradleBuildParser = new GradleBuildParser();
        this.projectTypeDetector = new ProjectTypeDetector();
        this.basePackageResolver = new BasePackageResolver();
        this.addonsRegistry = JmixAddonsRegistry.create();
        this.reportGenerator = new JmixHtmlReportGenerator();
    }

    public void analyzeProject(String projectPathString, @Nullable String basePackage,
                               @Nullable String jmixVersionOverride) {
        JmixProjectAnalysisResult result = analyzeProjectToResult(projectPathString, basePackage, jmixVersionOverride);
        reportGenerator.generateHtmlReport(projectPathString, result);
    }

    /**
     * Analysis without report generation. Separated so tests can inspect the result
     * and render the report to a string.
     */
    public JmixProjectAnalysisResult analyzeProjectToResult(String projectPathString,
                                                            @Nullable String basePackage,
                                                            @Nullable String jmixVersionOverride) {
        if (StringUtils.isBlank(projectPathString)) {
            throw new RuntimeException("No project path is specified");
        }

        Path projectPath = Path.of(projectPathString).toAbsolutePath().normalize();
        log.info("Start Jmix project analysis");
        log.info("Project path = '{}'", projectPath);

        JmixProjectDescriptor descriptor = gradleBuildParser.parse(projectPath, jmixVersionOverride);
        validateProjectType(descriptor, projectPath);

        String effectiveBasePackage = basePackage;
        if (StringUtils.isBlank(effectiveBasePackage)) {
            effectiveBasePackage = basePackageResolver.resolveBasePackage(descriptor.getModules());
        }
        if (StringUtils.isBlank(effectiveBasePackage)) {
            log.warn("Base package is not specified and could not be resolved from a @SpringBootApplication class");
        }

        List<JmixProjectAnalysisResult.ResolvedAddon> addons = resolveAddons(descriptor);

        return new JmixProjectAnalysisResult(descriptor, effectiveBasePackage, addons);
    }

    protected void validateProjectType(JmixProjectDescriptor descriptor, Path projectPath) {
        ProjectType projectType = projectTypeDetector.detect(descriptor);
        switch (projectType) {
            case CUBA -> throw new RuntimeException(
                    "The project looks like a CUBA Platform project. Use the 'analyze-cuba' command instead");
            case JMIX_FLOW -> throw new RuntimeException(
                    "The project already uses Flow UI (Jmix 2+). Analysis of Jmix 2.x+ projects is not supported yet");
            case UNKNOWN -> throw new RuntimeException(
                    "Unable to detect a Jmix 1.x project in '" + projectPath + "': no 'io.jmix' Gradle plugin"
                            + " or Jmix starters found. Check --project-dir; for exotic builds provide --jmix-version");
            case JMIX_CLASSIC -> log.info("Project type: Jmix 1.x (classic UI), version = '{}'",
                    descriptor.getEffectiveJmixVersion());
        }
    }

    protected List<JmixProjectAnalysisResult.ResolvedAddon> resolveAddons(JmixProjectDescriptor descriptor) {
        List<JmixProjectAnalysisResult.ResolvedAddon> addons = new ArrayList<>();
        descriptor.getDependencies().stream()
                .filter(dependency -> dependency.getGroup().startsWith(JMIX_GROUP_PREFIX))
                .sorted(Comparator.comparing(dependency -> dependency.getGroupArtifact()))
                .forEach(dependency -> addons.add(new JmixProjectAnalysisResult.ResolvedAddon(
                        dependency, addonsRegistry.getAddonInfo(dependency.getGroupArtifact()))));
        return addons;
    }
}
