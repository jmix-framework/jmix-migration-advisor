package io.jmix.migration.analysis.model;

public class LegacyScreenRegistration {
    private final String screenId;
    private final String descriptor;
    private final String registrationModule;

    public LegacyScreenRegistration(String screenId, String descriptor, String registrationModule) {
        this.screenId = screenId;
        this.descriptor = descriptor;
        this.registrationModule = registrationModule;
    }

    public String getScreenId() {
        return screenId;
    }

    public String getDescriptor() {
        return descriptor;
    }

    public String getRegistrationModule() {
        return registrationModule;
    }
}
