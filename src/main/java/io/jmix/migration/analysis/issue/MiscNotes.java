package io.jmix.migration.analysis.issue;

public class MiscNotes {

    public static MiscNote folderPaneEnabled() {
        return new MiscNote("folder-pane-enabled", "Folders panel is enabled", "Folders panel is not supported in Jmix");
    }

    public static MiscNote customWidgetsModule() {
        return new MiscNote("web-toolkit-module", "Custom widgets module ('web-toolkit') is present",
                "The project contains custom client-side (GWT) components. They cannot be migrated automatically"
                        + " and must be re-implemented on Vaadin Flow; estimate this work manually."
                        + " Screens using such components are underestimated by this report");
    }
}
