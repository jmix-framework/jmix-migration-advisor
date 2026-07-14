package io.jmix.migration.jmix.analyzer;

import io.jmix.migration.jmix.model.JmixSourcesScanResult;
import io.jmix.migration.jmix.model.JmixSourcesScanResult.RedFlag;
import io.jmix.migration.core.project.JmixModule;
import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Lightweight text scan of module sources for artifacts that cannot be migrated automatically
 * (UI customization red flags) plus security roles and Kotlin files. Text matching is
 * intentionally simple: the goal is inventory and escalation, not precise parsing.
 */
public class JmixSourcesScanner {

    private static final Logger log = LoggerFactory.getLogger(JmixSourcesScanner.class);

    protected static final Pattern PACKAGE_PATTERN = Pattern.compile("^\\s*package\\s+([\\w.]+)\\s*;", Pattern.MULTILINE);
    protected static final Pattern VAADIN_IMPORT_PATTERN =
            Pattern.compile("^\\s*import\\s+(com\\.vaadin\\.(?:ui|server|data|shared)\\.[\\w.*]+)\\s*;", Pattern.MULTILINE);

    public JmixSourcesScanResult scan(List<JmixModule> modules) {
        log.info("Start sources scan (red flags, security, Kotlin)");

        List<RedFlag> redFlags = new ArrayList<>();
        List<String> resourceRoles = new ArrayList<>();
        List<String> rowLevelRoles = new ArrayList<>();
        List<String> securityConfigs = new ArrayList<>();
        int[] screenPolicyCount = {0};
        int[] menuPolicyCount = {0};
        int[] kotlinFilesCount = {0};

        for (JmixModule module : modules) {
            Path javaDir = module.getJavaSourcesDir();
            if (Files.isDirectory(javaDir)) {
                scanSources(javaDir, redFlags, resourceRoles, rowLevelRoles, securityConfigs,
                        screenPolicyCount, menuPolicyCount, kotlinFilesCount);
            }
            scanThemes(module, redFlags);
        }

        redFlags.sort(Comparator.comparing(RedFlag::getCategory).thenComparing(RedFlag::getSubject));
        resourceRoles.sort(String::compareTo);
        rowLevelRoles.sort(String::compareTo);
        securityConfigs.sort(String::compareTo);

        return new JmixSourcesScanResult(redFlags, resourceRoles, rowLevelRoles, securityConfigs,
                screenPolicyCount[0], menuPolicyCount[0], kotlinFilesCount[0]);
    }

    protected void scanSources(Path javaDir, List<RedFlag> redFlags,
                               List<String> resourceRoles, List<String> rowLevelRoles, List<String> securityConfigs,
                               int[] screenPolicyCount, int[] menuPolicyCount, int[] kotlinFilesCount) {
        try {
            Files.walkFileTree(javaDir, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    String fileName = file.getFileName().toString();
                    if (fileName.endsWith(".kt")) {
                        kotlinFilesCount[0]++;
                        return FileVisitResult.CONTINUE;
                    }
                    if (!fileName.endsWith(".java")) {
                        return FileVisitResult.CONTINUE;
                    }
                    String content = readFile(file);
                    if (content == null) {
                        return FileVisitResult.CONTINUE;
                    }
                    String classFqn = resolveClassFqn(content, fileName);

                    Matcher vaadinImport = VAADIN_IMPORT_PATTERN.matcher(content);
                    if (vaadinImport.find()) {
                        redFlags.add(new RedFlag("Vaadin 8 API", classFqn,
                                "Uses '" + vaadinImport.group(1) + "'. Direct Vaadin 8 API usage needs"
                                        + " a case-by-case review: some APIs have Vaadin Flow counterparts,"
                                        + " the rest is re-implemented manually"));
                    }
                    if (content.contains("AbstractJavaScriptComponent") || content.contains("@JavaScript")
                            || content.contains("@WebJarResource")) {
                        redFlags.add(new RedFlag("JavaScript extension", classFqn,
                                "Client-side JavaScript integration; re-implement on Vaadin Flow manually"));
                    }
                    if (content.contains("CompositeComponent")) {
                        redFlags.add(new RedFlag("Composite component", classFqn,
                                "CompositeComponent is replaced by Flow UI fragments/composite views;"
                                        + " rework the component manually"));
                    }

                    if (content.contains("@ResourceRole")) {
                        resourceRoles.add(classFqn);
                    }
                    if (content.contains("@RowLevelRole")) {
                        rowLevelRoles.add(classFqn);
                    }
                    if (content.contains("@EnableWebSecurity") || content.contains("VaadinWebSecurity")) {
                        securityConfigs.add(classFqn);
                    }
                    screenPolicyCount[0] += countOccurrences(content, "@ScreenPolicy");
                    menuPolicyCount[0] += countOccurrences(content, "@MenuPolicy");
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected void scanThemes(JmixModule module, List<RedFlag> redFlags) {
        Path themesDir = module.getRootDir().resolve("src").resolve("main").resolve("themes");
        if (!Files.isDirectory(themesDir)) {
            return;
        }
        try (var themes = Files.list(themesDir)) {
            themes.filter(Files::isDirectory).forEach(themeDir ->
                    redFlags.add(new RedFlag("Custom theme", themeDir.getFileName().toString(),
                            "SCSS themes do not exist in Flow UI; recreate the customization"
                                    + " with CSS on top of the Lumo or Aura theme")));
        } catch (IOException e) {
            log.warn("Unable to list themes in '{}': {}", themesDir, e.getMessage());
        }
    }

    @Nullable
    protected String readFile(Path file) {
        try {
            return Files.readString(file, StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.debug("Unable to read '{}': {}", file, e.getMessage());
            return null;
        }
    }

    protected String resolveClassFqn(String content, String fileName) {
        String className = fileName.substring(0, fileName.length() - ".java".length());
        Matcher packageMatcher = PACKAGE_PATTERN.matcher(content);
        return packageMatcher.find() ? packageMatcher.group(1) + "." + className : className;
    }

    protected int countOccurrences(String content, String token) {
        int count = 0;
        int index = content.indexOf(token);
        while (index >= 0) {
            count++;
            index = content.indexOf(token, index + token.length());
        }
        return count;
    }
}
