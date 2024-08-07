package io.jmix.migration.analysis.appcomponent;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.jmix.migration.analysis.appcomponent.AppComponentOrigin.EXTERNAL;
import static io.jmix.migration.analysis.appcomponent.AppComponentOrigin.FRAMEWORK;
import static io.jmix.migration.analysis.appcomponent.AppComponentType.*;

public class CubaAppComponentsInfoRegistry {
    private final Map<String, CubaAppComponentInfo> registry;

    protected CubaAppComponentsInfoRegistry(Map<String, CubaAppComponentInfo> registry) {
        this.registry = registry;
    }

    public static CubaAppComponentsInfoRegistry create() {
        return new CubaAppComponentsInfoRegistry(createInternalRegistry());
    }

    @Nullable
    public CubaAppComponentInfo getAppComponentInfo(String appComponentPackage) {
        return registry.get(appComponentPackage);
    }

    // TODO full list
    protected static Map<String, CubaAppComponentInfo> createInternalRegistry() {
        return Stream.of(
                CubaAppComponentInfo.create("com.haulmont.cuba", "Application", BASE_APP, FRAMEWORK, "CUBA base application"),
                CubaAppComponentInfo.create("com.haulmont.reports", "Reporting", ADDON, FRAMEWORK, "Available for Jmix"),
                CubaAppComponentInfo.create("com.haulmont.fts", "Full Text Search", ADDON, FRAMEWORK, "Search add-on has been reworked in Jmix"),
                CubaAppComponentInfo.create("com.haulmont.addon.maps", "Maps", ADDON, FRAMEWORK, "Available for Jmix"),
                CubaAppComponentInfo.create("com.haulmont.webdav", "WebDAV", ADDON, FRAMEWORK, "Available for Jmix"),
                CubaAppComponentInfo.create("com.haulmont.addon.grapesjs", "GrapesJS HTML Editor", ADDON, FRAMEWORK, "Unavailable for Jmix"),
                CubaAppComponentInfo.create("com.haulmont.addon.bproc", "BProc", ADDON, FRAMEWORK, "Available for Jmix"),
                CubaAppComponentInfo.create("com.haulmont.bpm", "BPM", ADDON, FRAMEWORK, "Legacy business process management add-on. Jmix BPM is similar to CUBA BProc"),
                CubaAppComponentInfo.create("com.haulmont.addon.restapi", "REST API", ADDON, FRAMEWORK, "Available for Jmix"),
                CubaAppComponentInfo.create("com.haulmont.charts", "Charts", ADDON, FRAMEWORK, "Available for Jmix"),
                CubaAppComponentInfo.create("com.haulmont.addon.helium", "Helium Theme", THEME, FRAMEWORK, ""),
                CubaAppComponentInfo.create("com.haulmont.addon.ldap", "LDAP", ADDON, FRAMEWORK, "Available for Jmix"),
                CubaAppComponentInfo.create("com.haulmont.addon.globalevents", "Global Events", ADDON, FRAMEWORK, "Jmix allows to send events from backend to UI sessions"),
                CubaAppComponentInfo.create("gr.netmechanics.cuba.afs", "Azure File Storage", ADDON, EXTERNAL, "Unavailable for Jmix"),
                CubaAppComponentInfo.create("ro.infoexpert.cuba.translationro", "Romanian Translation", TRANSLATION_ADDON, EXTERNAL, ""),
                CubaAppComponentInfo.create("gr.netmechanics.cuba.translationel", "Greek Translation", TRANSLATION_ADDON, EXTERNAL, "Available for Jmix"),
                CubaAppComponentInfo.create("it.nexbit.cuba.translationit", "Italian Translation", TRANSLATION_ADDON, EXTERNAL, "Available for Jmix"),
                CubaAppComponentInfo.create("de.balvi.cuba.translationde", "German Translation", TRANSLATION_ADDON, EXTERNAL, "Available for Jmix"),
                CubaAppComponentInfo.create("br.com.petersonbr.translations", "CUBA Translations", TRANSLATION_ADDON, EXTERNAL, "Check Jmix add-ons marketplace to see available translations"),
                CubaAppComponentInfo.create("cn.cuba.trans", "Simplified Chinese Translation", TRANSLATION_ADDON, FRAMEWORK, "Available for Jmix")
        ).collect(Collectors.toUnmodifiableMap(CubaAppComponentInfo::getAppComponentPackage, Function.identity()));
    }
}
