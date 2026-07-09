package io.jmix.migration.analysis.parser.screen;

import io.jmix.migration.analysis.model.ClassGeneralDetails;
import io.jmix.migration.analysis.model.LegacyScreenRegistration;
import io.jmix.migration.analysis.model.ScreenInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class ScreensCollector {

    private static final Logger log = LoggerFactory.getLogger(ScreensCollector.class);

    protected final Map<String, LegacyScreenRegistration> legacyScreenRegistrations;
    protected final Map<String, ClassGeneralDetails> unknownClasses;

    protected final Map<String, ScreenInfo> screensByDescriptors;
    protected final Map<String, ScreenInfo> screensByControllers;
    protected final Map<String, ScreenInfo> screensByIds;

    public ScreensCollector() {
        this.screensByDescriptors = new HashMap<>();
        this.screensByControllers = new HashMap<>();
        this.screensByIds = new HashMap<>();
        this.legacyScreenRegistrations = new HashMap<>();
        this.unknownClasses = new HashMap<>();
    }

    public Map<String, ScreenInfo> getScreensByDescriptors() {
        return screensByDescriptors;
    }

    public Map<String, ScreenInfo> getScreensByControllers() {
        return screensByControllers;
    }

    public Map<String, ScreenInfo> getScreensByIds() {
        return screensByIds;
    }

    /**
     * All distinct screens regardless of which identifiers (id, descriptor, controller) they are known by.
     * Consumers counting or estimating screens must use this method so different metrics are computed
     * over the same set of screens.
     */
    public Collection<ScreenInfo> getAllScreens() {
        Set<ScreenInfo> allScreens = Collections.newSetFromMap(new IdentityHashMap<>());
        allScreens.addAll(screensByIds.values());
        allScreens.addAll(screensByDescriptors.values());
        allScreens.addAll(screensByControllers.values());
        return allScreens;
    }

    @Nullable
    public ScreenInfo getScreenInfoByDescriptor(String xmlDescriptorFileName) {
        log.debug("Try to get Screen Info by descriptor '{}'", xmlDescriptorFileName);
        return screensByDescriptors.get(xmlDescriptorFileName);
    }

    @Nullable
    public ScreenInfo getScreenInfoByController(String controllerClassName) {
        log.debug("Try to get Screen Info by Controller '{}'", controllerClassName);
        return screensByControllers.get(controllerClassName);
    }

    @Nullable
    public ScreenInfo getScreenInfoByScreenId(String screenId) {
        log.debug("Try to get Screen Info by Id '{}'", screenId);
        return screensByIds.get(screenId);
    }

    public void addUnknownClass(ClassGeneralDetails classDetails) {
        unknownClasses.put(classDetails.getFqn(), classDetails);
    }

    public Set<ClassGeneralDetails> getUnknownClasses() {
        return new HashSet<>(unknownClasses.values());
    }

    public void removeUnknownClass(String fqn) {
        unknownClasses.remove(fqn);
    }

    public void addLegacyScreenRegistration(String screenId, String descriptor, String registrationModule) {
        LegacyScreenRegistration legacyScreenRegistration = new LegacyScreenRegistration(
                screenId, descriptor, registrationModule
        );
        legacyScreenRegistrations.put(descriptor, legacyScreenRegistration);
    }

    @Nullable
    public LegacyScreenRegistration getLegacyScreenRegistration(String descriptor) {
        return legacyScreenRegistrations.get(descriptor);
    }

    public ScreenInfo initScreenByXmlDescriptor(String xmlDescriptor) {
        return initScreenInfo(null, xmlDescriptor, null, false);
    }

    public ScreenInfo initLegacyScreenInfo(String screenId, String xmlDescriptor) {
        return initScreenInfo(screenId, xmlDescriptor, null, true);
    }

    public ScreenInfo initScreenInfo(String screenId, String xmlDescriptor, String controllerClassName) {
        return initScreenInfo(screenId, xmlDescriptor, controllerClassName, false);
    }

    public ScreenInfo initScreenInfo(String screenId, String xmlDescriptor, String controllerClassName, boolean legacy) {
        log.debug("Try to init Screen Info with ID = {}, Descriptor ={}, Controller = {}, Legacy = {}",
                screenId, xmlDescriptor, controllerClassName, legacy);

        ScreenInfo byId = screenId != null ? screensByIds.get(screenId) : null;
        ScreenInfo byDescriptor = xmlDescriptor != null ? screensByDescriptors.get(xmlDescriptor) : null;
        ScreenInfo byController = controllerClassName != null ? screensByControllers.get(controllerClassName) : null;

        ScreenInfo screenInfo = byId != null ? byId : (byDescriptor != null ? byDescriptor : byController);

        if (screenInfo == null) {
            log.debug("Create new Screen Info");

            screenInfo = new ScreenInfo();
            screenInfo.setScreenId(screenId);
            screenInfo.setDescriptorFile(xmlDescriptor);
            screenInfo.setControllerClass(controllerClassName);
            screenInfo.setLegacy(legacy);

            boolean saved = saveScreenInfo(screenInfo);
            if (!saved) {
                log.error("[ERROR] Screen ({}, {}, {}) has not been saved", screenId, xmlDescriptor, controllerClassName);
            }
            return screenInfo;
        }

        log.debug("Screen has been already initialized, merge identifiers into the existing record");

        // The same screen may have been registered earlier under different keys as separate records
        mergeScreenInfo(screenInfo, byDescriptor);
        mergeScreenInfo(screenInfo, byController);

        applyIdentifier(screenInfo.getScreenId(), screenId, "screen id", screenInfo::setScreenId);
        applyIdentifier(screenInfo.getDescriptorFile(), xmlDescriptor, "descriptor", screenInfo::setDescriptorFile);
        applyIdentifier(screenInfo.getControllerClass(), controllerClassName, "controller", screenInfo::setControllerClass);
        if (legacy) {
            screenInfo.setLegacy(true);
        }

        saveScreenInfo(screenInfo); // index the record under the newly merged keys as well
        return screenInfo;
    }

    protected void applyIdentifier(@Nullable String currentValue, @Nullable String newValue,
                                   String identifierKind, java.util.function.Consumer<String> setter) {
        if (newValue == null) {
            return;
        }
        if (currentValue == null) {
            setter.accept(newValue);
        } else if (!currentValue.equals(newValue)) {
            log.warn("Conflicting {} for the same screen: existing '{}', new '{}'. Existing value is kept",
                    identifierKind, currentValue, newValue);
        }
    }

    /**
     * Copies data from a duplicate record into the main one when the same screen turns out
     * to be registered as two separate records under different keys.
     */
    protected void mergeScreenInfo(ScreenInfo target, @Nullable ScreenInfo source) {
        if (source == null || source == target) {
            return;
        }
        log.debug("Merge duplicated screen records: ({}, {}, {}) <- ({}, {}, {})",
                target.getScreenId(), target.getDescriptorFile(), target.getControllerClass(),
                source.getScreenId(), source.getDescriptorFile(), source.getControllerClass());

        if (target.getScreenId() == null) {
            target.setScreenId(source.getScreenId());
        } else if (source.getScreenId() != null && !Objects.equals(target.getScreenId(), source.getScreenId())) {
            log.warn("Conflicting screen ids on merge: '{}' vs '{}'", target.getScreenId(), source.getScreenId());
        }
        if (target.getDescriptorFile() == null) {
            target.setDescriptorFile(source.getDescriptorFile());
        }
        if (target.getControllerClass() == null) {
            target.setControllerClass(source.getControllerClass());
        }
        if (target.getScreenData() == null) {
            target.setScreenData(source.getScreenData());
        }
        if (target.getFacets() == null) {
            target.setFacets(source.getFacets());
        }
        if (target.getLayout() == null) {
            target.setLayout(source.getLayout());
        }
        if (target.getControllerDetails() == null) {
            target.setControllerDetails(source.getControllerDetails());
        }
        if (target.getExtendedDescriptor() == null) {
            target.setExtendedDescriptor(source.getExtendedDescriptor());
        }
        if (target.getExtendedController() == null) {
            target.setExtendedController(source.getExtendedController());
        }
        if (source.isLegacy()) {
            target.setLegacy(true);
        }
        if (source.isFragment()) {
            target.setFragment(true);
        }
        if (source.isRegistered()) {
            target.setRegistered(true);
        }
        if (source.isDescriptorProcessed()) {
            target.setDescriptorProcessed(true);
        }
        if (source.isControllerProcessed()) {
            target.setControllerProcessed(true);
        }
    }

    public void updateScreenInfo(ScreenInfo screenInfo) {
        saveScreenInfo(screenInfo);
    }

    protected boolean saveScreenInfo(ScreenInfo screenInfo) {
        log.debug("Save Screen Info: ID={}, Descriptor={}, Controller={}",
                screenInfo.getScreenId(), screenInfo.getDescriptorFile(), screenInfo.getControllerClass());
        boolean saved = false;
        if (screenInfo.getScreenId() != null) {
            screensByIds.put(screenInfo.getScreenId(), screenInfo);
            saved = true;
        }
        if (screenInfo.getDescriptorFile() != null) {
            screensByDescriptors.put(screenInfo.getDescriptorFile(), screenInfo);
            saved = true;
        }
        if (screenInfo.getControllerClass() != null) {
            screensByControllers.put(screenInfo.getControllerClass(), screenInfo);
            saved = true;
        }
        return saved;
    }

}
