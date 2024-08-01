package io.jmix.migration.analysis.model;

import io.jmix.migration.analysis.parser.screen.ScreenConstants;

public enum ScreenControllerSuperClassKind {

    LEGACY_BROWSER(ScreenConstants.LEGACY_BROWSER_BASIC_CLASS_FULL_NAME),
    LEGACY_EDITOR(ScreenConstants.LEGACY_EDITOR_BASIC_CLASS_FULL_NAME),
    LEGACY_WINDOW(ScreenConstants.LEGACY_WINDOW_BASIC_CLASS_FULL_NAME),
    LEGACY_COMBINED(ScreenConstants.LEGACY_COMBINED_SCREEN_BASIC_CLASS_FULL_NAME),
    MAIN_WINDOW(ScreenConstants.MAIN_WINDOW_BASIC_CLASS_FULL_NAME),
    TOP_LEVEL_WINDOW(ScreenConstants.TOP_LEVEL_WINDOW_BASIC_CLASS_FULL_NAME),
    BROWSER(ScreenConstants.BROWSER_BASIC_CLASS_FULL_NAME),
    EDITOR(ScreenConstants.EDITOR_BASIC_CLASS_FULL_NAME),
    SCREEN(ScreenConstants.SCREEN_BASIC_CLASS_FULL_NAME),
    MASTER_DETAILS(ScreenConstants.MASTER_DETAILS_BASIC_CLASS_FULL_NAME),
    LEGACY_FRAME(ScreenConstants.LEGACY_FRAME_BASIC_CLASS_FULL_NAME),
    FRAGMENT(ScreenConstants.FRAGMENT_BASIC_CLASS_FULL_NAME),
    CUSTOM("");

    final String fqn;

    ScreenControllerSuperClassKind(String fqn) {
        this.fqn = fqn;
    }

    public static ScreenControllerSuperClassKind fromFqn(String fqn) {
        for (ScreenControllerSuperClassKind value : ScreenControllerSuperClassKind.values()) {
            if (value.getFqn().equals(fqn)) {
                return value;
            }
        }
        return CUSTOM;
    }

    public String getFqn() {
        return fqn;
    }
}
