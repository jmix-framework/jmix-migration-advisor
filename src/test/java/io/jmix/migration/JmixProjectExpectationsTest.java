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
import java.util.List;
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
    public void featuresFixtureFactsMatchHandComputedExpectations() {
        JmixProjectAnalysisResult result = new JmixProjectAnalyzer()
                .analyzeProjectToResult(fixturePath("jmix17-features").toString(), null, null);

        assertEquals("com.company.feat2", result.getBasePackage());

        // Data model: 1 JPA entity, 1 DTO, 1 embeddable, 1 enum, 1 listener;
        // Customer.java and Address.java import javax.persistence
        assertEquals(List.of("com.company.feat2.entity.Customer"), result.getDataModel().getJpaEntities());
        assertEquals(List.of("com.company.feat2.entity.CustomerData"), result.getDataModel().getDtoEntities());
        assertEquals(List.of("com.company.feat2.entity.Address"), result.getDataModel().getEmbeddables());
        assertEquals(List.of("com.company.feat2.entity.CustomerStatus"), result.getDataModel().getEnums());
        assertEquals(List.of("com.company.feat2.listener.CustomerChangedListener"),
                result.getDataModel().getEntityEventListeners());
        assertEquals(2, result.getDataModel().getJavaxImportFilesCount());

        // Screens: 1 screen + 1 fragment linked via annotations and the jmix namespace
        assertEquals(1, result.getScreensCount());
        assertEquals(1, result.getFragmentsCount());
        assertTrue(result.getUiComponents().keySet().containsAll(
                        List.of("groupTable", "buttonsPanel", "button", "popupView", "sourceCodeEditor", "textField")),
                "UI components: " + result.getUiComponents().keySet());

        // Security and red flags
        assertEquals(List.of("com.company.feat2.security.FullAccessRole"),
                result.getSourcesScan().getResourceRoles());
        assertEquals(2, result.getSourcesScan().getScreenPolicyCount());
        assertEquals(1, result.getSourcesScan().getMenuPolicyCount());
        assertEquals(List.of("com.company.feat2.security.AppSecurityConfiguration"),
                result.getSourcesScan().getSecurityConfigs());
        assertEquals(2, result.getSourcesScan().getRedFlags().size(),
                "Red flags: " + result.getSourcesScan().getRedFlags().size());
        assertEquals(1, result.getSourcesScan().getKotlinFilesCount());

        // Configuration: 3 renamed/changed properties, 2 menu items, uidata changelog include
        assertEquals(3, result.getConfigInfo().getPropertyRenames().size());
        assertEquals(2, result.getConfigInfo().getMenuScreenItemsCount());
        assertTrue(result.getConfigInfo().isUiDataChangelogIncluded());

        // The intentionally broken Java file is reported, analysis is not aborted
        assertEquals(1, result.getUnparsedFiles().size());

        // Estimation, derived by hand from the jmix profile weights:
        // CustomerBrowse: components groupTable 3 + buttonsPanel 1 + popupView 5 + sourceCodeEditor 2 = 11,
        //   6 controller calls (L2: 8), total 19 -> Simple (1.5 h);
        // AddressFragment: textField only, score 0 -> Trivial (0.5 h);
        // initial 24 + jakarta sweep (2 files -> 4) + screens 2 + add-ons 0 + roles 1 x 1
        //   + config 2 + custom theme 1 x 8 = 41
        var estimation = result.getEstimation();
        assertEquals(0, new java.math.BigDecimal("2.0").compareTo(estimation.getScreensCost()),
                "Screens cost: " + estimation.getScreensCost());
        assertEquals(0, new java.math.BigDecimal("24").compareTo(estimation.getInitialMigrationCost()));
        assertEquals(0, new java.math.BigDecimal("4").compareTo(estimation.getJakartaSweepCost()));
        assertEquals(0, new java.math.BigDecimal("0").compareTo(estimation.getAddonsCost()));
        assertEquals(0, new java.math.BigDecimal("1").compareTo(estimation.getSecurityRolesCost()));
        assertEquals(0, new java.math.BigDecimal("2").compareTo(estimation.getConfigCost()));
        assertEquals(0, new java.math.BigDecimal("8").compareTo(estimation.getCustomThemesCost()));
        assertEquals(0, new java.math.BigDecimal("41").compareTo(estimation.getTotalCost()),
                "Total cost: " + estimation.getTotalCost());

        Map<String, List<String>> screensByGroup = estimation.getScreensPerComplexity().entrySet().stream()
                .collect(Collectors.toMap(entry -> entry.getKey().getName(), Map.Entry::getValue));
        assertEquals(List.of("feat2_AddressFragment"), screensByGroup.get("Trivial"));
        assertEquals(List.of("feat2_Customer.browse"), screensByGroup.get("Simple"));
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
