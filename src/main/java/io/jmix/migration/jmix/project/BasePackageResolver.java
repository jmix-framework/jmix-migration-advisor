package io.jmix.migration.jmix.project;

import io.jmix.migration.core.project.JmixModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Infers the project base package from the location of the {@code @SpringBootApplication} class.
 */
public class BasePackageResolver {

    private static final Logger log = LoggerFactory.getLogger(BasePackageResolver.class);

    protected static final Pattern PACKAGE_PATTERN = Pattern.compile("^\\s*package\\s+([\\w.]+)\\s*;", Pattern.MULTILINE);

    @Nullable
    public String resolveBasePackage(List<JmixModule> modules) {
        for (JmixModule module : modules) {
            Path javaSources = module.getJavaSourcesDir();
            if (!Files.isDirectory(javaSources)) {
                continue;
            }
            try (Stream<Path> files = Files.walk(javaSources)) {
                String basePackage = files
                        .filter(file -> file.getFileName().toString().endsWith(".java"))
                        .map(this::extractPackageIfApplication)
                        .filter(result -> result != null)
                        .findFirst()
                        .orElse(null);
                if (basePackage != null) {
                    log.info("Base package resolved from @SpringBootApplication class: '{}'", basePackage);
                    return basePackage;
                }
            } catch (IOException e) {
                log.warn("Unable to scan '{}' for the application class: {}", javaSources, e.getMessage());
            }
        }
        return null;
    }

    @Nullable
    protected String extractPackageIfApplication(Path javaFile) {
        try {
            String content = Files.readString(javaFile, StandardCharsets.UTF_8);
            if (!content.contains("@SpringBootApplication")) {
                return null;
            }
            Matcher matcher = PACKAGE_PATTERN.matcher(content);
            return matcher.find() ? matcher.group(1) : null;
        } catch (IOException e) {
            log.debug("Unable to read '{}': {}", javaFile, e.getMessage());
            return null;
        }
    }
}
