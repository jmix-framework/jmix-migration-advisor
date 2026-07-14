package io.jmix.migration;

import io.jmix.migration.core.incident.UiComponentIssuesRegistry;
import io.jmix.migration.cuba.CubaProjectAnalyzer;
import io.jmix.migration.cuba.HtmlReportGenerator;
import io.jmix.migration.cuba.model.CubaProjectEstimationResult;
import io.jmix.migration.jmix.JmixHtmlReportGenerator;
import io.jmix.migration.jmix.JmixProjectAnalyzer;
import io.jmix.migration.jmix.model.JmixProjectAnalysisResult;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Golden-file snapshot tests pinning the end-to-end report content for fixture projects.
 * Refactorings must keep these green. An intentional report change is approved by regenerating
 * the golden files ({@code ./gradlew test -Dsnapshot.update=true}) and reviewing the diff.
 */
public class ReportSnapshotTest {

    protected static final Path FIXTURES_ROOT = Path.of("src", "test", "resources", "projects");
    protected static final Path GOLDENS_ROOT = Path.of("src", "test", "resources", "goldens");

    @Test
    public void cubaFeaturesReportMatchesGolden() throws IOException {
        checkReportSnapshot("cuba-features", "com.company.feat");
    }

    @Test
    public void cubaRobustnessReportMatchesGolden() throws IOException {
        checkReportSnapshot("cuba-robustness", "com.company.synth");
    }

    @Test
    public void jmixMinimalReportMatchesGolden() throws IOException {
        checkJmixReportSnapshot("jmix17-minimal");
    }

    @Test
    public void jmixFeaturesReportMatchesGolden() throws IOException {
        checkJmixReportSnapshot("jmix17-features");
    }

    protected void checkJmixReportSnapshot(String fixtureName) throws IOException {
        Path fixturePath = FIXTURES_ROOT.resolve(fixtureName).toAbsolutePath().normalize();
        assertTrue(Files.isDirectory(fixturePath), "Fixture project not found: " + fixturePath);

        JmixProjectAnalyzer analyzer = new JmixProjectAnalyzer();
        JmixProjectAnalysisResult result = analyzer.analyzeProjectToResult(fixturePath.toString(), null, null);

        String html = new JmixHtmlReportGenerator().generateReportContent(fixtureName, result);
        compareWithGolden(fixtureName, normalize(html, fixturePath));
    }

    protected void checkReportSnapshot(String fixtureName, String basePackage) throws IOException {
        Path fixturePath = FIXTURES_ROOT.resolve(fixtureName).toAbsolutePath().normalize();
        assertTrue(Files.isDirectory(fixturePath), "Fixture project not found: " + fixturePath);

        CubaProjectAnalyzer analyzer = new CubaProjectAnalyzer(null);
        CubaProjectEstimationResult result = analyzer.analyzeProjectToResult(fixturePath.toString(), basePackage);

        HtmlReportGenerator reportGenerator = new HtmlReportGenerator(UiComponentIssuesRegistry.create());
        String html = reportGenerator.generateReportContent(fixtureName, result);
        compareWithGolden(fixtureName, normalize(html, fixturePath));
    }

    protected void compareWithGolden(String fixtureName, String normalized) throws IOException {
        Path goldenPath = GOLDENS_ROOT.resolve(fixtureName + ".html");
        if (Boolean.getBoolean("snapshot.update")) {
            Files.createDirectories(goldenPath.getParent());
            Files.writeString(goldenPath, normalized, StandardCharsets.UTF_8);
            return;
        }

        assertTrue(Files.exists(goldenPath),
                "Golden file not found: " + goldenPath + ". Generate it with -Dsnapshot.update=true");
        String golden = normalizeLineEndings(Files.readString(goldenPath, StandardCharsets.UTF_8));
        assertEquals(golden, normalized,
                "Report content differs from the golden file. If the change is intentional,"
                        + " regenerate goldens with -Dsnapshot.update=true and review the diff");
    }

    /**
     * Removes run-specific parts: generation timestamps, the machine-specific fixture location
     * (also OS-specific path separators) and line endings.
     */
    protected String normalize(String html, Path fixturePath) {
        String result = normalizeLineEndings(html);
        result = result.replaceAll("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}", "TIMESTAMP");
        result = result.replace(fixturePath.toString(), "FIXTURE_ROOT");
        result = result.replace('\\', '/');
        return result;
    }

    protected String normalizeLineEndings(String text) {
        return text.replace("\r\n", "\n");
    }
}
