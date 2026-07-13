package io.jmix.migration;

import io.jmix.migration.cuba.CubaProjectAnalyzer;
import io.jmix.migration.cuba.model.CubaProjectEstimationResult;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pins the estimation numbers for the cuba-features fixture to values derived BY HAND from
 * the fixture contents, the component issues registry and estimation-data.xml. Unlike the HTML
 * golden (which pins whatever the tool currently produces), these assertions encode the intent:
 * if a golden regeneration changes a number, this test explains which expectation broke.
 *
 * <p>Derivation per screen (weights: groupTable 3, calendar 3, fieldGroup 2, suggestionField 2,
 * link 1, buttonsPanel 1, maskedField 2, tokenList 2, grid 2, cssLayout 0, linkButton 1,
 * currencyField 1, htmlBox 3, slider 2, optionsGroup 1, embedded 2, popupView 5, filter 2;
 * nested data item = n x 15, uiComponents.create = n x 10, descriptor extends = 5,
 * method calls: 0..5 -> 0, 6..30 -> 10, 31..80 -> 40, 81..150 -> 80, 151+ -> 180;
 * hours per group: Trivial(0..10) 0.5, Simple(11..35) 2, Medium(36..85) 8,
 * Complex(86..200) 16, Hard(201+) 32):</p>
 *
 * <ul>
 * <li>boundary-a.xml: 3+3+2+2 = 10, Trivial (upper bound)</li>
 * <li>boundary-b.xml: 10+1 = 11, Simple (lower bound)</li>
 * <li>feat_AddressFragment: currencyField 1 = 1, Trivial</li>
 * <li>feat_Customer.editExt: extends 5 + linkButton 1 = 6, Trivial</li>
 * <li>feat_SharedView: 3+2 = 5, Trivial</li>
 * <li>feat_ОтчётПродаж: optionsGroup 1 + embedded 2 + lookupField 1 = 4, Trivial</li>
 * <li>feat_Customer.lookup: buttonsPanel 1 + filter 2 + nested ds 15 + 5 calls (L1: 0) = 18, Simple;
 *     contains capsLockIndicator (ABSENT), goes to requires-decision</li>
 * <li>feat_Order.browse: 3+1+3+2+2+2 = 13 + 6 calls (L2: 10) = 23, Simple</li>
 * <li>feat_Customer.edit: 2+2+5 = 9 + nested 15 + create 10 + 8 calls (L2: 10) = 44, Medium</li>
 * <li>BulkOperationsWindow: 151 calls (L5: 180) = 180, Complex</li>
 * </ul>
 *
 * <p>Totals: 10 screens; hours = 5 x 0.5 + 3 x 2 + 8 + 16 = 32.5;
 * overall = 100 (initial) + 16 (base entities) + 3 x 1 (listeners) + 32.5 = 151.5.</p>
 */
public class EstimationExpectationsTest {

    protected static final Path FIXTURES_ROOT = Path.of("src", "test", "resources", "projects");

    @Test
    public void cubaFeaturesNumbersMatchHandComputedExpectations() {
        CubaProjectEstimationResult result = analyze("cuba-features", "com.company.feat");

        assertEquals(4, result.getEntitiesAmount());
        assertEquals(3, result.getLegacyListeners().size());
        assertEquals(0, result.getUnparsedFiles().size());
        assertEquals(10, result.getScreensTotalAmount());
        assertEquals(0, new BigDecimal("32.5").compareTo(result.getScreensTotalCost()),
                "Screens total hours: expected 32.5, actual " + result.getScreensTotalCost());
        assertEquals(0, new BigDecimal("151.5").compareTo(result.getTotalEstimation()),
                "Total estimation: expected 151.5, actual " + result.getTotalEstimation());

        Map<String, Set<String>> screensByGroup = result.getScreensPerComplexity().entrySet().stream()
                .collect(Collectors.toMap(entry -> entry.getKey().getName(), entry -> new HashSet<>(entry.getValue())));

        assertEquals(Set.of(
                "com/company/feat/web/screens/boundary/boundary-a.xml",
                "feat_AddressFragment",
                "feat_Customer.editExt",
                "feat_SharedView",
                "feat_ОтчётПродаж"
        ), screensByGroup.get("Trivial"));

        assertEquals(Set.of(
                "com/company/feat/web/screens/boundary/boundary-b.xml",
                "feat_Customer.lookup",
                "feat_Order.browse"
        ), screensByGroup.get("Simple"));

        assertEquals(Set.of("feat_Customer.edit"), screensByGroup.get("Medium"));

        assertEquals(Set.of("com.company.feat.web.screens.bulk.BulkOperationsWindow"),
                screensByGroup.get("Complex"));

        assertEquals(Map.of("feat_Customer.lookup", List.of("capsLockIndicator")),
                result.getScreensRequireDecision());
    }

    @Test
    public void cubaRobustnessSkipsExactlyTheBrokenFiles() {
        CubaProjectEstimationResult result = analyze("cuba-robustness", "com.company.synth");

        List<String> unparsedPaths = result.getUnparsedFiles().stream()
                .map(entry -> entry.getPath().replace('\\', '/'))
                .sorted()
                .toList();
        assertEquals(2, unparsedPaths.size(), "Unexpected unparsed files: " + unparsedPaths);
        assertTrue(unparsedPaths.get(0).endsWith("modules/global/src/Broken.java"), unparsedPaths.get(0));
        assertTrue(unparsedPaths.get(1).endsWith("modules/web/src/com/company/synth/BrokenXml.xml"), unparsedPaths.get(1));
    }

    protected CubaProjectEstimationResult analyze(String fixtureName, String basePackage) {
        Path fixturePath = FIXTURES_ROOT.resolve(fixtureName).toAbsolutePath().normalize();
        assertTrue(java.nio.file.Files.isDirectory(fixturePath), "Fixture project not found: " + fixturePath);
        CubaProjectAnalyzer analyzer = new CubaProjectAnalyzer(null);
        return analyzer.analyzeProjectToResult(fixturePath.toString(), basePackage);
    }
}
