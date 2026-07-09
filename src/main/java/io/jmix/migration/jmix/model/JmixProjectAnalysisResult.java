package io.jmix.migration.jmix.model;

import io.jmix.migration.jmix.addon.JmixAddonInfo;
import io.jmix.migration.core.project.GradleDependency;
import io.jmix.migration.core.project.JmixProjectDescriptor;

import javax.annotation.Nullable;
import java.util.List;

public class JmixProjectAnalysisResult {

    private final JmixProjectDescriptor projectDescriptor;
    private final String basePackage;
    private final List<ResolvedAddon> addons;

    public JmixProjectAnalysisResult(JmixProjectDescriptor projectDescriptor,
                                     @Nullable String basePackage,
                                     List<ResolvedAddon> addons) {
        this.projectDescriptor = projectDescriptor;
        this.basePackage = basePackage;
        this.addons = List.copyOf(addons);
    }

    public JmixProjectDescriptor getProjectDescriptor() {
        return projectDescriptor;
    }

    @Nullable
    public String getBasePackage() {
        return basePackage;
    }

    public List<ResolvedAddon> getAddons() {
        return addons;
    }

    /**
     * A Jmix starter dependency of the project together with the registry data about it,
     * if any ({@code null} info means "no data, check manually").
     */
    public static class ResolvedAddon {
        private final GradleDependency dependency;
        private final JmixAddonInfo addonInfo;

        public ResolvedAddon(GradleDependency dependency, @Nullable JmixAddonInfo addonInfo) {
            this.dependency = dependency;
            this.addonInfo = addonInfo;
        }

        public GradleDependency getDependency() {
            return dependency;
        }

        @Nullable
        public JmixAddonInfo getAddonInfo() {
            return addonInfo;
        }
    }
}
