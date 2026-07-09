package io.jmix.migration;

import io.jmix.migration.jmix.JmixProjectAnalyzer;
import io.jmix.migration.jmix.addon.JmixAddonInfo;
import io.jmix.migration.jmix.model.JmixProjectAnalysisResult;
import io.jmix.migration.core.project.GradleBuildParser;
import io.jmix.migration.core.project.JmixProjectDescriptor;
import io.jmix.migration.core.project.ProjectType;
import io.jmix.migration.core.project.ProjectTypeDetector;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pins the Gradle parsing, project type detection and add-on resolution facts
 * for the jmix17-minimal fixture to hand-derived values.
 */
public class JmixProjectExpectationsTest {

    protected static final Path FIXTURES_ROOT = Path.of("src", "test", "resources", "projects");

    @Test
    public void gradleFactsAreExtractedFromMinimalFixture() {
        JmixProjectDescriptor descriptor = new GradleBuildParser()
                .parse(fixturePath("jmix17-minimal"), null);

        assertEquals("jmix17-minimal", descriptor.getProjectName());
        assertEquals("1.7.2", descriptor.getJmixPluginVersion());
        assertEquals("1.7.2", descriptor.getBomVersion());
        assertEquals("11", descriptor.getJavaVersion());
        assertFalse(descriptor.isVaadinPluginPresent());
        assertFalse(descriptor.isCubaSignalsPresent());
        assertEquals(1, descriptor.getModules().size());
        // 12 io.jmix starters + hsqldb
        assertEquals(13, descriptor.getDependencies().size());
    }

    @Test
    public void projectTypesAreDetectedByBuildFingerprint() {
        GradleBuildParser parser = new GradleBuildParser();
        ProjectTypeDetector detector = new ProjectTypeDetector();

        assertEquals(ProjectType.JMIX_CLASSIC, detector.detect(parser.parse(fixturePath("jmix17-minimal"), null)));
        // cuba-features has no build.gradle; it is recognized by the modules/{global,web} layout
        assertEquals(ProjectType.CUBA, detector.detect(parser.parse(fixturePath("cuba-features"), null)));
    }

    @Test
    public void addonsAreResolvedAgainstRegistry() {
        JmixProjectAnalysisResult result = new JmixProjectAnalyzer()
                .analyzeProjectToResult(fixturePath("jmix17-minimal").toString(), null, null);

        assertEquals("com.company.jmixapp", result.getBasePackage());

        // 12 io.jmix dependencies become add-on rows; hsqldb is not an add-on
        assertEquals(12, result.getAddons().size());

        Map<String, JmixAddonInfo.FlowStatus> statuses = result.getAddons().stream()
                .filter(addon -> addon.getAddonInfo() != null)
                .collect(Collectors.toMap(
                        addon -> addon.getDependency().getGroupArtifact(),
                        addon -> addon.getAddonInfo().getFlowStatus()));

        assertEquals(JmixAddonInfo.FlowStatus.AVAILABLE, statuses.get("io.jmix.core:jmix-core-starter"));
        assertEquals(JmixAddonInfo.FlowStatus.REPLACED, statuses.get("io.jmix.ui:jmix-ui-starter"));
        assertEquals(JmixAddonInfo.FlowStatus.REPLACED, statuses.get("io.jmix.ui:jmix-ui-themes-compiled"));
        assertEquals(JmixAddonInfo.FlowStatus.RENAMED, statuses.get("io.jmix.security:jmix-security-ui-starter"));
        assertEquals(JmixAddonInfo.FlowStatus.RENAMED, statuses.get("io.jmix.datatools:jmix-datatools-ui-starter"));

        // The unknown add-on is present as a row without registry data
        JmixProjectAnalysisResult.ResolvedAddon unknown = result.getAddons().stream()
                .filter(addon -> addon.getDependency().getGroupArtifact().equals("io.jmix.foobar:jmix-foobar-starter"))
                .findFirst()
                .orElseThrow();
        assertNull(unknown.getAddonInfo());
    }

    @Test
    public void commandsRejectProjectsOfWrongType() {
        RuntimeException cubaOnJmixCommand = assertThrows(RuntimeException.class,
                () -> new JmixProjectAnalyzer()
                        .analyzeProjectToResult(fixturePath("cuba-features").toString(), null, null));
        assertTrue(cubaOnJmixCommand.getMessage().contains("looks like a CUBA Platform project"),
                cubaOnJmixCommand.getMessage());

        RuntimeException jmixOnCubaCommand = assertThrows(RuntimeException.class,
                () -> new io.jmix.migration.cuba.CubaProjectAnalyzer(null)
                        .analyzeProjectToResult(fixturePath("jmix17-minimal").toString(), "com.company.jmixapp"));
        assertTrue(jmixOnCubaCommand.getMessage().contains("Use the 'analyze-jmix' command"),
                jmixOnCubaCommand.getMessage());
    }

    protected Path fixturePath(String fixtureName) {
        return FIXTURES_ROOT.resolve(fixtureName).toAbsolutePath().normalize();
    }
}
