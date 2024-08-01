package io.jmix.migration.analysis.model;

import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.util.Set;

public class SuperClassDetails {
    private String extensionName;
    private String fullName;
    private Set<String> fullNameCandidates;

    public SuperClassDetails(String extensionName, String fullName, Set<String> fullNameCandidates) {
        this.extensionName = extensionName;
        this.fullName = fullName;
        this.fullNameCandidates = fullNameCandidates;
    }

    public String getExtensionName() {
        return extensionName;
    }

    public boolean hasDeterminedFullName() {
        return StringUtils.isNotEmpty(fullName);
    }

    @Nullable
    public String getFullName() {
        return fullName;
    }

    public Set<String> getFullNameCandidates() {
        return fullNameCandidates;
    }

    public void setExtensionName(String extensionName) {
        this.extensionName = extensionName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setFullNameCandidates(Set<String> fullNameCandidates) {
        this.fullNameCandidates = fullNameCandidates;
    }
}
