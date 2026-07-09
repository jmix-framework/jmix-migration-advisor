package io.jmix.migration.core.project;

import javax.annotation.Nullable;

public class GradleDependency {
    private final String group;
    private final String artifact;
    private final String version;

    public GradleDependency(String group, String artifact, @Nullable String version) {
        this.group = group;
        this.artifact = artifact;
        this.version = version;
    }

    public String getGroup() {
        return group;
    }

    public String getArtifact() {
        return artifact;
    }

    @Nullable
    public String getVersion() {
        return version;
    }

    /**
     * Registry key: {@code group:artifact}.
     */
    public String getGroupArtifact() {
        return group + ":" + artifact;
    }
}
