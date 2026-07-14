package io.jmix.migration.core.project;

import javax.annotation.Nullable;
import java.nio.file.Path;
import java.util.List;

/**
 * Facts about the analyzed project extracted from Gradle build files without executing Gradle.
 */
public class JmixProjectDescriptor {
    private final Path projectRoot;
    private final String projectName;
    private final String jmixPluginVersion;
    private final String bomVersion;
    private final String javaVersion;
    private final boolean vaadinPluginPresent;
    private final boolean cubaSignalsPresent;
    private final List<JmixModule> modules;
    private final List<GradleDependency> dependencies;

    public JmixProjectDescriptor(Path projectRoot,
                                 String projectName,
                                 @Nullable String jmixPluginVersion,
                                 @Nullable String bomVersion,
                                 @Nullable String javaVersion,
                                 boolean vaadinPluginPresent,
                                 boolean cubaSignalsPresent,
                                 List<JmixModule> modules,
                                 List<GradleDependency> dependencies) {
        this.projectRoot = projectRoot;
        this.projectName = projectName;
        this.jmixPluginVersion = jmixPluginVersion;
        this.bomVersion = bomVersion;
        this.javaVersion = javaVersion;
        this.vaadinPluginPresent = vaadinPluginPresent;
        this.cubaSignalsPresent = cubaSignalsPresent;
        this.modules = List.copyOf(modules);
        this.dependencies = List.copyOf(dependencies);
    }

    public Path getProjectRoot() {
        return projectRoot;
    }

    public String getProjectName() {
        return projectName;
    }

    @Nullable
    public String getJmixPluginVersion() {
        return jmixPluginVersion;
    }

    @Nullable
    public String getBomVersion() {
        return bomVersion;
    }

    @Nullable
    public String getJavaVersion() {
        return javaVersion;
    }

    public boolean isVaadinPluginPresent() {
        return vaadinPluginPresent;
    }

    public boolean isCubaSignalsPresent() {
        return cubaSignalsPresent;
    }

    public List<JmixModule> getModules() {
        return modules;
    }

    public List<GradleDependency> getDependencies() {
        return dependencies;
    }

    public boolean hasDependency(String groupArtifact) {
        return dependencies.stream().anyMatch(dependency -> dependency.getGroupArtifact().equals(groupArtifact));
    }

    /**
     * The effective platform version: explicit BOM version wins over the plugin version
     * (the plugin version may lag behind the BOM the project actually compiles against).
     */
    @Nullable
    public String getEffectiveJmixVersion() {
        return bomVersion != null ? bomVersion : jmixPluginVersion;
    }
}
