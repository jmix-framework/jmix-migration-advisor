package io.jmix.migration.model;

import javax.annotation.Nullable;

public class ClassGeneralDetails {
    private final String simpleName;
    private final String fqn;
    private final ClassGeneralDetails superClassDetails;

    public ClassGeneralDetails(String simpleName, String fqn, ClassGeneralDetails superClassDetails) {
        this.simpleName = simpleName;
        this.fqn = fqn;
        this.superClassDetails = superClassDetails;
    }

    public String getSimpleName() {
        return simpleName;
    }

    public String getFqn() {
        return fqn;
    }

    @Nullable
    public ClassGeneralDetails getSuperClassDetails() {
        return superClassDetails;
    }
}
