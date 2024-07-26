package io.jmix.migration.model;

public class ScreenControllerSuperClassDetails {
    private final String simpleName;
    private final String fqn;
    private final ScreenControllerSuperClassKind superClassKind;
    private final boolean foundInSrc;

    public ScreenControllerSuperClassDetails(String simpleName, String fqn, ScreenControllerSuperClassKind superClassKind, boolean foundInSrc) {
        this.simpleName = simpleName;
        this.fqn = fqn;
        this.superClassKind = superClassKind;
        this.foundInSrc = foundInSrc;
    }

    public String getSimpleName() {
        return simpleName;
    }

    public String getFqn() {
        return fqn;
    }

    public ScreenControllerSuperClassKind getSuperClassKind() {
        return superClassKind;
    }

    public boolean isFoundInSrc() {
        return foundInSrc;
    }
}
