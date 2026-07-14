package io.jmix.migration.classicui.parser;

import io.jmix.migration.classicui.model.ScreenControllerSuperClassKind;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

import static io.jmix.migration.classicui.model.ScreenControllerSuperClassKind.*;

/**
 * Platform-specific set of framework screen base classes. CUBA and Jmix 1.x share the class
 * simple names ({@code StandardLookup}, {@code StandardEditor}, {@code Screen}) but differ
 * in packages, so the profile is the only place aware of the actual FQNs.
 */
public class ScreenClassProfile {

    private final Map<String, String> basicClassesBySimpleName;
    private final Map<String, ScreenControllerSuperClassKind> kindsByFqn;

    protected ScreenClassProfile(Map<String, String> basicClassesBySimpleName,
                                 Map<String, ScreenControllerSuperClassKind> kindsByFqn) {
        this.basicClassesBySimpleName = Map.copyOf(basicClassesBySimpleName);
        this.kindsByFqn = Map.copyOf(kindsByFqn);
    }

    public static ScreenClassProfile cuba() {
        Builder builder = new Builder();
        builder.register("com.haulmont.cuba.gui.components.AbstractLookup", LEGACY_BROWSER);
        builder.register("com.haulmont.cuba.gui.components.AbstractEditor", LEGACY_EDITOR);
        builder.register("com.haulmont.cuba.gui.components.AbstractWindow", LEGACY_WINDOW);
        builder.register("com.haulmont.cuba.gui.components.EntityCombinedScreen", LEGACY_COMBINED);
        builder.register("com.haulmont.cuba.gui.components.AbstractMainWindow", MAIN_WINDOW);
        builder.register("com.haulmont.cuba.gui.components.AbstractTopLevelWindow", TOP_LEVEL_WINDOW);
        builder.register("com.haulmont.cuba.gui.components.AbstractFrame", LEGACY_FRAME);
        builder.register("com.haulmont.cuba.gui.screen.StandardLookup", BROWSER);
        builder.register("com.haulmont.cuba.gui.screen.StandardEditor", EDITOR);
        builder.register("com.haulmont.cuba.gui.screen.Screen", SCREEN);
        builder.register("com.haulmont.cuba.gui.screen.MasterDetailScreen", MASTER_DETAILS);
        builder.register("com.haulmont.cuba.gui.screen.ScreenFragment", FRAGMENT);
        return builder.build();
    }

    public static ScreenClassProfile jmixClassic() {
        Builder builder = new Builder();
        builder.register("io.jmix.ui.screen.StandardLookup", BROWSER);
        builder.register("io.jmix.ui.screen.StandardEditor", EDITOR);
        builder.register("io.jmix.ui.screen.Screen", SCREEN);
        builder.register("io.jmix.ui.screen.MasterDetailScreen", MASTER_DETAILS);
        builder.register("io.jmix.ui.screen.ScreenFragment", FRAGMENT);
        builder.register("io.jmix.ui.app.main.StandardMainScreen", MAIN_WINDOW);
        return builder.build();
    }

    @Nullable
    public String getBasicClassFqn(String simpleName) {
        return basicClassesBySimpleName.get(simpleName);
    }

    public ScreenControllerSuperClassKind kindByFqn(@Nullable String fqn) {
        if (fqn == null) {
            return CUSTOM;
        }
        return kindsByFqn.getOrDefault(fqn, CUSTOM);
    }

    protected static class Builder {
        private final Map<String, String> basicClassesBySimpleName = new HashMap<>();
        private final Map<String, ScreenControllerSuperClassKind> kindsByFqn = new HashMap<>();

        protected void register(String fqn, ScreenControllerSuperClassKind kind) {
            String simpleName = fqn.substring(fqn.lastIndexOf('.') + 1);
            basicClassesBySimpleName.put(simpleName, fqn);
            kindsByFqn.put(fqn, kind);
        }

        protected ScreenClassProfile build() {
            return new ScreenClassProfile(basicClassesBySimpleName, kindsByFqn);
        }
    }
}
