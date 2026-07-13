package io.jmix.migration.jmix.addon;

import io.jmix.migration.core.model.License;
import io.jmix.migration.core.model.Origin;
import io.jmix.migration.core.model.TargetStatus;

import javax.annotation.Nullable;

public class JmixAddonInfo {

    private final String artifact;
    private final String name;
    private final String category;
    private final License license;
    private final Origin origin;
    private final TargetStatus flowStatus;
    private final String flowArtifact;
    private final String notes;
    private final Integer costHint;

    public JmixAddonInfo(String artifact, String name, String category, License license, Origin origin,
                         TargetStatus flowStatus, @Nullable String flowArtifact, String notes, @Nullable Integer costHint) {
        this.artifact = artifact;
        this.name = name;
        this.category = category;
        this.license = license;
        this.origin = origin;
        this.flowStatus = flowStatus;
        this.flowArtifact = flowArtifact;
        this.notes = notes;
        this.costHint = costHint;
    }

    public String getArtifact() {
        return artifact;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public License getLicense() {
        return license;
    }

    public Origin getOrigin() {
        return origin;
    }

    public TargetStatus getFlowStatus() {
        return flowStatus;
    }

    @Nullable
    public String getFlowArtifact() {
        return flowArtifact;
    }

    public String getNotes() {
        return notes;
    }

    @Nullable
    public Integer getCostHint() {
        return costHint;
    }
}
