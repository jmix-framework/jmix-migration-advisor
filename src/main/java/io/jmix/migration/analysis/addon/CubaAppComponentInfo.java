package io.jmix.migration.analysis.addon;

public class CubaAppComponentInfo {
    private final String appComponentPackage;
    private final String cubaName;
    private final AppComponentType appComponentType;
    private final AppComponentOrigin origin;
    private final String notes;

    protected CubaAppComponentInfo(String appComponentPackage, String cubaName, AppComponentType appComponentType, AppComponentOrigin origin, String notes) {
        this.appComponentPackage = appComponentPackage;
        this.cubaName = cubaName;
        this.appComponentType = appComponentType;
        this.origin = origin;
        this.notes = notes;
    }

    public static CubaAppComponentInfo create(String appComponentPackage, String cubaName, AppComponentType appComponentType, AppComponentOrigin origin, String notes) {
        return new CubaAppComponentInfo(appComponentPackage, cubaName, appComponentType, origin, notes);
    }

    public static CubaAppComponentInfo createMissing(String appComponentPackage) {
        return new CubaAppComponentInfo(appComponentPackage, appComponentPackage, AppComponentType.MISSING, AppComponentOrigin.UNKNOWN, "No data");
    }

    public String getAppComponentPackage() {
        return appComponentPackage;
    }

    public String getCubaName() {
        return cubaName;
    }

    public AppComponentType getAppComponentType() {
        return appComponentType;
    }

    public String getNotes() {
        return notes;
    }

    public AppComponentOrigin getOrigin() {
        return origin;
    }
}
