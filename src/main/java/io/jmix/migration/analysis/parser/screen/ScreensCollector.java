package io.jmix.migration.analysis.parser.screen;

import io.jmix.migration.analysis.model.ClassGeneralDetails;
import io.jmix.migration.analysis.model.LegacyScreenRegistration;
import io.jmix.migration.analysis.model.ScreenInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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

        if (screenId != null && screensByIds.containsKey(screenId)) {
            log.debug("Screen with ID '{}' has been already initialized", screenId);
            return screensByIds.get(screenId);
        }
        if (xmlDescriptor != null && screensByDescriptors.containsKey(xmlDescriptor)) {
            log.debug("Screen with descriptor '{}' has been already initialized", xmlDescriptor);
            return screensByDescriptors.get(xmlDescriptor);
        }
        if (controllerClassName != null && screensByControllers.containsKey(controllerClassName)) {
            log.debug("Screen with controller '{}' has been already initialized", controllerClassName);
            return screensByControllers.get(controllerClassName);
        }

        log.debug("Create new Screen Info");

        ScreenInfo screenInfo = new ScreenInfo();
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
