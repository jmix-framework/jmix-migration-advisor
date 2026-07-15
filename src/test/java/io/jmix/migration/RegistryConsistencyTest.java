package io.jmix.migration;

import io.jmix.migration.core.incident.UiComponentIssue;
import io.jmix.migration.core.incident.UiComponentIssueType;
import io.jmix.migration.core.incident.UiComponentIssuesRegistry;
import io.jmix.migration.core.model.License;
import io.jmix.migration.core.model.TargetStatus;
import io.jmix.migration.cuba.appcomponent.AppComponentType;
import io.jmix.migration.cuba.appcomponent.CubaAppComponentInfo;
import io.jmix.migration.cuba.appcomponent.CubaAppComponentsInfoRegistry;
import io.jmix.migration.jmix.addon.JmixAddonInfo;
import io.jmix.migration.jmix.addon.JmixAddonsRegistry;
import io.jmix.migration.jmix.analyzer.JmixConfigAnalyzer;
import io.jmix.migration.jmix.model.JmixConfigInfo.PropertyRename;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Guards the hand-edited registry resources against accidental data loss and format drift:
 * every registry must load, keep its key entries and hold the structural invariants that
 * the loaders cannot enforce per-entry. A failing assertion here usually means a manual
 * edit dropped or broke an entry.
 */
public class RegistryConsistencyTest {

    @Test
    public void uiComponentIssuesRegistryKeepsKeyEntriesAndInvariants() {
        UiComponentIssuesRegistry registry = UiComponentIssuesRegistry.create();

        // Key exact entries (a sample across statuses and both platforms)
        for (String component : List.of("filter", "groupTable", "popupView", "dateField", "label",
                "form", "groupBox", "fieldGroup", "rowsCount", "treeTable", "tokenList")) {
            assertNotNull(registry.getIssue(component), "Missing registry entry: " + component);
        }
        assertEquals(UiComponentIssueType.ABSENT, registry.getIssue("capsLockIndicator").getType());
        assertEquals(UiComponentIssueType.ABSENT, registry.getIssue("jsComponent").getType());

        // Namespaced add-on families are covered by prefix entries
        for (String component : List.of("chart:pieChart", "charts:serialChart", "maps:geoMap",
                "pivot:pivotTable", "gjs:grapesJsHtmlEditor", "search:searchField",
                "ntf:notificationsIndicator")) {
            assertNotNull(registry.getIssue(component), "Prefix entry does not cover: " + component);
        }

        List<UiComponentIssue> allIssues = registry.getAllIssues();
        assertTrue(allIssues.size() >= 55, "Suspicious registry shrink: " + allIssues.size() + " entries");

        for (UiComponentIssue issue : allIssues) {
            assertTrue(StringUtils.isNotBlank(issue.getNotes()), "Blank notes: " + issue.getComponent());
            if (issue.getType() == UiComponentIssueType.ABSENT) {
                assertEquals(0, issue.getExtraComplexityScore(),
                        "Absent entry must not be scored: " + issue.getComponent());
                assertTrue(issue.getRequires().isEmpty(),
                        "Absent entry must not have requires: " + issue.getComponent());
            }
        }
    }

    @Test
    public void jmixAddonsRegistryKeepsKeyEntriesAndInvariants() {
        JmixAddonsRegistry registry = JmixAddonsRegistry.create();

        // Critical artifacts; charts and pivot-table were once lost during a manual edit
        for (String artifact : List.of(
                "io.jmix.core:jmix-core-starter",
                "io.jmix.ui:jmix-ui-starter",
                "io.jmix.ui:jmix-ui-data-starter",
                "io.jmix.ui:jmix-ui-themes-compiled",
                "io.jmix.ui:jmix-ui-widgets-compiled",
                "io.jmix.ui:jmix-charts-starter",
                "io.jmix.ui:jmix-pivot-table-starter",
                "io.jmix.security:jmix-security-ui-starter",
                "io.jmix.datatools:jmix-datatools-ui-starter",
                "io.jmix.webdav:jmix-webdav-starter",
                "io.jmix.webdav:jmix-webdav-ui-starter",
                "io.jmix.webdav:jmix-webdav-rest-starter",
                "io.jmix.translations:jmix-translations-ru")) {
            assertNotNull(registry.getAddonInfo(artifact), "Missing addon entry: " + artifact);
        }

        assertTrue(registry.getAll().size() >= 55,
                "Suspicious registry shrink: " + registry.getAll().size() + " entries");

        for (JmixAddonInfo info : registry.getAll()) {
            String artifact = info.getArtifact();
            assertTrue(artifact.contains(":"), "Artifact is not group:artifact: " + artifact);
            assertTrue(StringUtils.isNotBlank(info.getNotes()), "Blank notes: " + artifact);
            switch (info.getFlowStatus()) {
                case RENAMED -> {
                    assertNotNull(info.getFlowArtifact(), "Renamed entry needs flow-artifact: " + artifact);
                    assertTrue(info.getFlowArtifact().contains(":"),
                            "flow-artifact is not group:artifact: " + artifact);
                }
                case ABSENT -> {
                    assertNull(info.getFlowArtifact(), "Absent entry must not have flow-artifact: " + artifact);
                    assertNull(info.getCostHint(), "Absent entry cost is indeterminate: " + artifact);
                }
                case AVAILABLE -> assertNull(info.getFlowArtifact(),
                        "Available entry must not have flow-artifact: " + artifact);
                default -> { /* replaced/merged: flow-artifact optional */ }
            }
        }

        for (String artifact : List.of("io.jmix.bpm:jmix-bpm-starter", "io.jmix.maps:jmix-maps-starter",
                "io.jmix.webdav:jmix-webdav-starter", "io.jmix.notifications:jmix-notifications-starter")) {
            assertEquals(License.COMMERCIAL, registry.getAddonInfo(artifact).getLicense(),
                    "Expected commercial license: " + artifact);
        }
    }

    @Test
    public void cubaAppComponentsRegistryKeepsKeyEntriesAndInvariants() {
        CubaAppComponentsInfoRegistry registry = CubaAppComponentsInfoRegistry.create();

        assertTrue(registry.getAll().size() >= 24,
                "Suspicious registry shrink: " + registry.getAll().size() + " entries");

        long baseApps = registry.getAll().stream()
                .filter(info -> info.getAppComponentType() == AppComponentType.BASE_APP)
                .count();
        assertEquals(1, baseApps, "Exactly one base-app entry is expected");
        assertNotNull(registry.getAppComponentInfo("com.haulmont.cuba"));

        for (String pkg : List.of("com.haulmont.addon.maps", "com.haulmont.addon.bproc", "com.haulmont.webdav")) {
            assertEquals(License.COMMERCIAL, registry.getAppComponentInfo(pkg).getLicense(),
                    "Expected commercial license: " + pkg);
        }

        // The BPM naming trap stays encoded: legacy CUBA BPM has no successor, BProc is available as Jmix BPM
        assertEquals(TargetStatus.ABSENT, registry.getAppComponentInfo("com.haulmont.bpm").getStatus());
        assertEquals(TargetStatus.AVAILABLE, registry.getAppComponentInfo("com.haulmont.addon.bproc").getStatus());
        assertEquals(TargetStatus.ABSENT, registry.getAppComponentInfo("com.haulmont.addon.dashboard").getStatus());

        for (CubaAppComponentInfo info : registry.getAll()) {
            String pkg = info.getAppComponentPackage();
            assertNotNull(info.getLicense(), "License is required: " + pkg);
            assertNotNull(info.getStatus(), "Status is required: " + pkg);
            assertNotNull(info.getOrigin(), "Origin is required: " + pkg);
            assertTrue(StringUtils.isNotBlank(info.getNotes()), "Blank notes: " + pkg);
        }
    }

    @Test
    public void configRenamesRegistryKeepsKeyEntries() {
        Map<String, PropertyRename> renames = new ExposingConfigAnalyzer().loadRegistry();

        assertTrue(renames.size() >= 5, "Suspicious registry shrink: " + renames.size() + " entries");
        PropertyRename loginScreenId = renames.get("jmix.ui.login-screen-id");
        assertNotNull(loginScreenId);
        assertEquals("jmix.ui.login-view-id", loginScreenId.getNewProperty());
        assertNotNull(renames.get("jmix.ui.main-screen-id"));
        assertNotNull(renames.get("jmix.ui.theme"));
    }

    protected static class ExposingConfigAnalyzer extends JmixConfigAnalyzer {
        protected Map<String, PropertyRename> loadRegistry() {
            return loadRenamesRegistry();
        }
    }
}
