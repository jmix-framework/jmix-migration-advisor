package io.jmix.migration.core.project;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Fingerprints the analyzed project by Gradle build facts and the directory layout.
 * Annotation names alone cannot be trusted: CUBA and Jmix 1.x share
 * {@code @UiController}/{@code @UiDescriptor} and base class names, so the build files
 * and the module layout are the authoritative signals.
 */
public class ProjectTypeDetector {

    public ProjectType detect(JmixProjectDescriptor descriptor) {
        // The structural signal covers CUBA projects with exotic or partial builds:
        // the modules/{global,web} layout is fundamental to the platform
        if (descriptor.isCubaSignalsPresent() || hasCubaModulesLayout(descriptor.getProjectRoot())) {
            return ProjectType.CUBA;
        }
        if (descriptor.isVaadinPluginPresent()
                || descriptor.hasDependency("io.jmix.flowui:jmix-flowui-starter")
                || isMajorVersionAtLeast(descriptor.getEffectiveJmixVersion(), 2)) {
            return ProjectType.JMIX_FLOW;
        }
        if (descriptor.getJmixPluginVersion() != null
                || descriptor.getBomVersion() != null
                || descriptor.hasDependency("io.jmix.ui:jmix-ui-starter")) {
            return ProjectType.JMIX_CLASSIC;
        }
        return ProjectType.UNKNOWN;
    }

    protected boolean hasCubaModulesLayout(Path projectRoot) {
        Path modulesDir = projectRoot.resolve("modules");
        return Files.isDirectory(modulesDir.resolve("global")) && Files.isDirectory(modulesDir.resolve("web"));
    }

    protected boolean isMajorVersionAtLeast(String version, int major) {
        if (version == null || version.isBlank()) {
            return false;
        }
        int dotIndex = version.indexOf('.');
        String majorPart = dotIndex > 0 ? version.substring(0, dotIndex) : version;
        try {
            return Integer.parseInt(majorPart) >= major;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
