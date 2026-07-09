package io.jmix.migration.core.incident;

/**
 * How a classic UI component maps to Jmix Flow UI. Components with a direct equivalent
 * have no issue entry at all.
 */
public enum UiComponentIssueType {

    /**
     * A direct analog exists with renames or minor differences: the replacement is
     * a mechanical XML/code edit, behavior is preserved.
     */
    CHANGED,

    /**
     * A different ready-made component or technique achieves the same or similar result;
     * the spot needs rethinking, but no custom code is written.
     */
    HAS_ALTERNATIVE,

    /**
     * The result is achievable only partially or via a combination of components plus
     * custom glue code following a known, bounded recipe.
     */
    HAS_WORKAROUND,

    /**
     * No replacement recipe exists: the functionality is dropped, redesigned or built
     * from scratch. Such cost is indeterminate and is never expressed as a score.
     */
    ABSENT
}
