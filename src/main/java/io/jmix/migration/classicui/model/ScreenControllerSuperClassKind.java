package io.jmix.migration.classicui.model;

/**
 * Kind of the framework base class a screen controller extends. FQN resolution is
 * platform-specific and lives in {@code ScreenClassProfile}.
 */
public enum ScreenControllerSuperClassKind {

    LEGACY_BROWSER,
    LEGACY_EDITOR,
    LEGACY_WINDOW,
    LEGACY_COMBINED,
    MAIN_WINDOW,
    TOP_LEVEL_WINDOW,
    BROWSER,
    EDITOR,
    SCREEN,
    MASTER_DETAILS,
    LEGACY_FRAME,
    FRAGMENT,
    CUSTOM
}
