package io.jmix.migration.analysis.parser.screen;

import io.jmix.migration.analysis.model.ScreenControllerSuperClassKind;
import io.jmix.migration.analysis.model.SuperClassDetails;

public class ControllerExtensionAnalysisResult {
    private final ScreenControllerSuperClassKind superClassKind;
    private final SuperClassDetails superClassDetails;

    public ControllerExtensionAnalysisResult(ScreenControllerSuperClassKind superClassKind,
                                             SuperClassDetails superClassDetails) {
        this.superClassKind = superClassKind;
        this.superClassDetails = superClassDetails;
    }

    public ScreenControllerSuperClassKind getSuperClassKind() {
        return superClassKind;
    }

    public SuperClassDetails getSuperClassDetails() {
        return superClassDetails;
    }
}
