package io.jmix.migration.jmix.model;

import io.jmix.migration.core.model.UnparsedFileEntry;
import io.jmix.migration.core.project.GradleDependency;
import io.jmix.migration.core.project.JmixProjectDescriptor;
import io.jmix.migration.jmix.addon.JmixAddonInfo;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

public class JmixProjectAnalysisResult {

    private final JmixProjectDescriptor projectDescriptor;
    private final String basePackage;
    private final List<ResolvedAddon> addons;
    private final JmixDataModelInfo dataModel;
    private final JmixSourcesScanResult sourcesScan;
    private final JmixConfigInfo configInfo;
    private final int screensCount;
    private final int fragmentsCount;
    private final Map<String, Integer> uiComponents;
    private final List<UnparsedFileEntry> unparsedFiles;

    public JmixProjectAnalysisResult(JmixProjectDescriptor projectDescriptor,
                                     @Nullable String basePackage,
                                     List<ResolvedAddon> addons,
                                     JmixDataModelInfo dataModel,
                                     JmixSourcesScanResult sourcesScan,
                                     JmixConfigInfo configInfo,
                                     int screensCount,
                                     int fragmentsCount,
                                     Map<String, Integer> uiComponents,
                                     List<UnparsedFileEntry> unparsedFiles) {
        this.projectDescriptor = projectDescriptor;
        this.basePackage = basePackage;
        this.addons = List.copyOf(addons);
        this.dataModel = dataModel;
        this.sourcesScan = sourcesScan;
        this.configInfo = configInfo;
        this.screensCount = screensCount;
        this.fragmentsCount = fragmentsCount;
        this.uiComponents = Map.copyOf(uiComponents);
        this.unparsedFiles = List.copyOf(unparsedFiles);
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

    public JmixDataModelInfo getDataModel() {
        return dataModel;
    }

    public JmixSourcesScanResult getSourcesScan() {
        return sourcesScan;
    }

    public JmixConfigInfo getConfigInfo() {
        return configInfo;
    }

    public int getScreensCount() {
        return screensCount;
    }

    public int getFragmentsCount() {
        return fragmentsCount;
    }

    public Map<String, Integer> getUiComponents() {
        return uiComponents;
    }

    public List<UnparsedFileEntry> getUnparsedFiles() {
        return unparsedFiles;
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
