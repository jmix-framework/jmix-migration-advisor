package io.jmix.migration.core.project;

public enum ProjectType {

    /**
     * CUBA Platform project (cuba Gradle plugin, modules/global-core-web layout).
     */
    CUBA,

    /**
     * Jmix project with the classic (Vaadin 8 based) UI: io.jmix plugin 1.x, jmix-ui starters.
     */
    JMIX_CLASSIC,

    /**
     * Jmix project with Flow UI: io.jmix plugin 2.x+, com.vaadin plugin, flowui starters.
     */
    JMIX_FLOW,

    UNKNOWN
}
