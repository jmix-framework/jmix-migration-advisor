package io.jmix.migration.cuba.appcomponent;

import io.jmix.migration.core.model.License;
import io.jmix.migration.core.model.Origin;
import io.jmix.migration.core.model.TargetStatus;

import javax.annotation.Nullable;

public class CubaAppComponentInfo {
    private final String appComponentPackage;
    private final String name;
    private final AppComponentType appComponentType;
    private final License license;
    private final Origin origin;
    private final TargetStatus status;
    private final String notes;

    protected CubaAppComponentInfo(String appComponentPackage, String name, AppComponentType appComponentType,
                                   @Nullable License license, Origin origin, @Nullable TargetStatus status, String notes) {
        this.appComponentPackage = appComponentPackage;
        this.name = name;
        this.appComponentType = appComponentType;
        this.license = license;
        this.origin = origin;
        this.status = status;
        this.notes = notes;
    }

    public static CubaAppComponentInfo create(String appComponentPackage, String cubaName, AppComponentType appComponentType,
                                              License license, Origin origin, TargetStatus status, String notes) {
        return new CubaAppComponentInfo(appComponentPackage, cubaName, appComponentType, license, origin, status, notes);
    }

    public static CubaAppComponentInfo createMissing(String appComponentPackage) {
        return new CubaAppComponentInfo(appComponentPackage, appComponentPackage, AppComponentType.MISSING,
                null, Origin.UNKNOWN, null, "No data");
    }

    public String getAppComponentPackage() {
        return appComponentPackage;
    }

    public String getName() {
        return name;
    }

    public AppComponentType getAppComponentType() {
        return appComponentType;
    }

    public String getAppComponentTypeName() {
        return appComponentType == null ? "" : appComponentType.name();
    }

    /**
     * @return licensing, or {@code null} for components missing from the registry
     */
    @Nullable
    public License getLicense() {
        return license;
    }

    public String getLicenseName() {
        return license == null ? "" : license.name();
    }

    /**
     * @return status in the target Jmix version, or {@code null} for components missing from the registry
     */
    @Nullable
    public TargetStatus getStatus() {
        return status;
    }

    public String getStatusName() {
        return status == null ? "UNKNOWN" : status.name();
    }

    public String getNotes() {
        return notes;
    }

    public Origin getOrigin() {
        return origin;
    }

    public String getOriginName() {
        return origin == null ? "" : origin.name();
    }
}
