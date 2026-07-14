package io.jmix.migration.core.project;

import java.nio.file.Path;

/**
 * A Gradle module of the analyzed Jmix project. A typical Jmix project has a single module;
 * composite projects list modules in settings.gradle.
 */
public class JmixModule {
    private final String name;
    private final Path rootDir;

    public JmixModule(String name, Path rootDir) {
        this.name = name;
        this.rootDir = rootDir;
    }

    public String getName() {
        return name;
    }

    public Path getRootDir() {
        return rootDir;
    }

    public Path getJavaSourcesDir() {
        return rootDir.resolve("src").resolve("main").resolve("java");
    }

    public Path getResourcesDir() {
        return rootDir.resolve("src").resolve("main").resolve("resources");
    }
}
