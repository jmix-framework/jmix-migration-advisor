package io.jmix.migration.core.model;

/**
 * Status of a source-platform add-on / app component in the target Jmix version.
 * Shared by both registries (CUBA app components and Jmix 1.x add-ons).
 */
public enum TargetStatus {

    /** The same functionality is available, nothing beyond a version bump. */
    AVAILABLE,

    /** The dependency is replaced with another artifact mechanically. */
    RENAMED,

    /** A different add-on covers the functionality; configuration or usage is reworked. */
    REPLACED,

    /** The functionality is part of another add-on or the core. */
    MERGED,

    /** No equivalent in the target version: the cost is indeterminate, requires a decision. */
    ABSENT
}
