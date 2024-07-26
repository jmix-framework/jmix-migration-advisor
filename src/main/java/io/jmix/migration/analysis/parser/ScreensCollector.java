package io.jmix.migration.analysis.parser;

import io.jmix.migration.model.ClassGeneralDetails;
import io.jmix.migration.model.LegacyScreenRegistration;
import io.jmix.migration.model.ScreenInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.*;

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
        log.info("Try to get Screen Info by descriptor '{}'", xmlDescriptorFileName);
        return screensByDescriptors.get(xmlDescriptorFileName);
    }

    @Nullable
    public ScreenInfo getScreenInfoByController(String controllerClassName) {
        log.info("Try to get Screen Info by Controller '{}'", controllerClassName);
        return screensByControllers.get(controllerClassName);
    }

    @Nullable
    public ScreenInfo getScreenInfoByScreenId(String screenId) {
        log.info("Try to get Screen Info by Id '{}'", screenId);
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

    /*public void addCustomExtendedClass(SuperClassDetails superClassDetails) {
        SuperClassDetails existingSuperClassDetails = customExtendedClasses.get(superClassDetails.getExtensionName());
        if(existingSuperClassDetails == null) {
            customExtendedClasses.put(superClassDetails.getExtensionName(), superClassDetails);
        } else {
            String existingFullName = existingSuperClassDetails.getFullName();
            String newFullName = superClassDetails.getFullName();
            if(StringUtils.isNoneEmpty(existingFullName, newFullName) && !existingFullName.equals(newFullName)) {
                existingSuperClassDetails.setFullNameCandidates(Set.of(existingFullName, newFullName));
                existingSuperClassDetails.setFullName(null);
            } else if (StringUtils.isEmpty(existingFullName) && StringUtils.isNotEmpty(newFullName)) {
                existingSuperClassDetails.setFullName(newFullName);
                existingSuperClassDetails.setFullNameCandidates(Collections.emptySet());
            } else if (StringUtils.isAllEmpty(existingFullName, newFullName)) {
                existingSuperClassDetails.getFullNameCandidates().add(newFullName);
            }
        }
    }*/

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
        log.info("Save Screen Info: ID={}, Descriptor={}, Controller={}",
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

        //todo by controller file?
        return saved;
    }

}
