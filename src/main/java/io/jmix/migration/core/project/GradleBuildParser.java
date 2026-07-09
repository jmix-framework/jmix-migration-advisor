package io.jmix.migration.core.project;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extracts project facts from Gradle build files WITHOUT executing Gradle: tolerant,
 * line-based regex parsing. Placeholder versions ({@code "$someVar"}) are not resolved
 * and are treated as undetected; the CLI provides an override parameter for such builds.
 */
public class GradleBuildParser {

    private static final Logger log = LoggerFactory.getLogger(GradleBuildParser.class);

    protected static final Pattern JMIX_PLUGIN_PATTERN =
            Pattern.compile("id\\s*\\(?\\s*['\"]io\\.jmix['\"]\\s*\\)?\\s*version\\s*\\(?\\s*['\"]([^'\"]+)['\"]");
    protected static final Pattern BOM_VERSION_PATTERN =
            Pattern.compile("bomVersion\\s*=?\\s*['\"]([^'\"]+)['\"]");
    protected static final Pattern VAADIN_PLUGIN_PATTERN =
            Pattern.compile("(apply\\s*\\(?\\s*plugin:\\s*['\"]com\\.vaadin['\"])|(id\\s*\\(?\\s*['\"]com\\.vaadin['\"])");
    protected static final Pattern CUBA_SIGNAL_PATTERN =
            Pattern.compile("(com\\.haulmont\\.gradle:cuba-plugin)|(apply\\s*\\(?\\s*plugin:\\s*['\"]cuba['\"])|(cubaVersion\\s*=)");
    protected static final Pattern DEPENDENCY_PATTERN =
            Pattern.compile("^\\s*(implementation|api|compileOnly|runtimeOnly)\\s*\\(?\\s*['\"]([A-Za-z0-9_.\\-]+):([A-Za-z0-9_.\\-]+)(?::([^'\"]+))?['\"]");
    protected static final Pattern SOURCE_COMPATIBILITY_PATTERN =
            Pattern.compile("sourceCompatibility\\s*=\\s*['\"]?([A-Za-z0-9_.]+)['\"]?");
    protected static final Pattern ROOT_PROJECT_NAME_PATTERN =
            Pattern.compile("rootProject\\.name\\s*=\\s*['\"]([^'\"]+)['\"]");
    protected static final Pattern INCLUDE_MODULE_PATTERN =
            Pattern.compile("['\"](:[A-Za-z0-9_.:\\-]+)['\"]");
    protected static final Pattern PROJECT_DIR_PATTERN =
            Pattern.compile("project\\s*\\(\\s*['\"](:[^'\"]+)['\"]\\s*\\)\\.projectDir\\s*=\\s*new\\s+File\\s*\\(\\s*settingsDir\\s*,\\s*['\"]([^'\"]+)['\"]\\s*\\)");

    public JmixProjectDescriptor parse(Path projectRoot, @Nullable String jmixVersionOverride) {
        List<String> settingsLines = readLines(projectRoot.resolve("settings.gradle"));

        String projectName = projectRoot.getFileName() == null ? "project" : projectRoot.getFileName().toString();
        Map<String, Path> moduleDirs = new LinkedHashMap<>();
        for (String line : settingsLines) {
            Matcher nameMatcher = ROOT_PROJECT_NAME_PATTERN.matcher(line);
            if (nameMatcher.find()) {
                projectName = nameMatcher.group(1);
            }
            if (line.stripLeading().startsWith("include")) {
                Matcher includeMatcher = INCLUDE_MODULE_PATTERN.matcher(line);
                while (includeMatcher.find()) {
                    String moduleId = includeMatcher.group(1);
                    // Gradle default: module ':a:b' lives in a/b relative to the root
                    Path defaultDir = projectRoot.resolve(moduleId.substring(1).replace(':', '/'));
                    moduleDirs.put(moduleId, defaultDir);
                }
            }
            Matcher projectDirMatcher = PROJECT_DIR_PATTERN.matcher(line);
            if (projectDirMatcher.find()) {
                moduleDirs.put(projectDirMatcher.group(1), projectRoot.resolve(projectDirMatcher.group(2)));
            }
        }

        List<JmixModule> modules = new ArrayList<>();
        if (moduleDirs.isEmpty()) {
            modules.add(new JmixModule(projectName, projectRoot));
        } else {
            moduleDirs.forEach((moduleId, dir) -> modules.add(new JmixModule(moduleId.substring(1), dir)));
        }

        // Merge facts from the root build file and every module build file
        String jmixPluginVersion = null;
        String bomVersion = null;
        String javaVersion = null;
        boolean vaadinPluginPresent = false;
        boolean cubaSignalsPresent = false;
        Map<String, GradleDependency> dependencies = new LinkedHashMap<>();

        List<Path> buildFiles = new ArrayList<>();
        buildFiles.add(projectRoot.resolve("build.gradle"));
        for (JmixModule module : modules) {
            Path moduleBuildFile = module.getRootDir().resolve("build.gradle");
            if (!buildFiles.contains(moduleBuildFile)) {
                buildFiles.add(moduleBuildFile);
            }
        }

        for (Path buildFile : buildFiles) {
            for (String line : readLines(buildFile)) {
                Matcher pluginMatcher = JMIX_PLUGIN_PATTERN.matcher(line);
                if (pluginMatcher.find() && jmixPluginVersion == null) {
                    jmixPluginVersion = sanitizeVersion(pluginMatcher.group(1));
                }
                Matcher bomMatcher = BOM_VERSION_PATTERN.matcher(line);
                if (bomMatcher.find() && bomVersion == null) {
                    bomVersion = sanitizeVersion(bomMatcher.group(1));
                }
                Matcher javaMatcher = SOURCE_COMPATIBILITY_PATTERN.matcher(line);
                if (javaMatcher.find() && javaVersion == null) {
                    javaVersion = javaMatcher.group(1).replace("JavaVersion.VERSION_", "").replace("_", ".");
                }
                if (VAADIN_PLUGIN_PATTERN.matcher(line).find()) {
                    vaadinPluginPresent = true;
                }
                if (CUBA_SIGNAL_PATTERN.matcher(line).find()) {
                    cubaSignalsPresent = true;
                }
                Matcher dependencyMatcher = DEPENDENCY_PATTERN.matcher(line);
                if (dependencyMatcher.find()) {
                    GradleDependency dependency = new GradleDependency(
                            dependencyMatcher.group(2), dependencyMatcher.group(3),
                            sanitizeVersion(dependencyMatcher.group(4)));
                    dependencies.putIfAbsent(dependency.getGroupArtifact(), dependency);
                }
            }
        }

        if (jmixVersionOverride != null && !jmixVersionOverride.isBlank()) {
            log.info("Jmix version override is provided: '{}'", jmixVersionOverride);
            jmixPluginVersion = jmixVersionOverride;
        }

        return new JmixProjectDescriptor(projectRoot, projectName, jmixPluginVersion, bomVersion, javaVersion,
                vaadinPluginPresent, cubaSignalsPresent, modules, new ArrayList<>(dependencies.values()));
    }

    /**
     * Unresolved Gradle placeholders like {@code "$jmixVersion"} are treated as undetected.
     */
    @Nullable
    protected String sanitizeVersion(@Nullable String version) {
        if (version == null || version.isBlank() || version.contains("$")) {
            return null;
        }
        return version;
    }

    protected List<String> readLines(Path file) {
        if (!Files.isRegularFile(file)) {
            return List.of();
        }
        try {
            return Files.readAllLines(file, StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.warn("Unable to read Gradle file '{}': {}", file, e.getMessage());
            return List.of();
        }
    }
}
