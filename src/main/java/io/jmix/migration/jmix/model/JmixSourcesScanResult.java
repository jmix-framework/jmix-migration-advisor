package io.jmix.migration.jmix.model;

import java.util.List;

/**
 * Result of the lightweight text scan of module sources: UI customization red flags,
 * security artifacts and Kotlin files.
 */
public class JmixSourcesScanResult {

    private final List<RedFlag> redFlags;
    private final List<String> resourceRoles;
    private final List<String> rowLevelRoles;
    private final List<String> securityConfigs;
    private final int screenPolicyCount;
    private final int menuPolicyCount;
    private final int kotlinFilesCount;

    public JmixSourcesScanResult(List<RedFlag> redFlags, List<String> resourceRoles, List<String> rowLevelRoles,
                                 List<String> securityConfigs, int screenPolicyCount, int menuPolicyCount,
                                 int kotlinFilesCount) {
        this.redFlags = List.copyOf(redFlags);
        this.resourceRoles = List.copyOf(resourceRoles);
        this.rowLevelRoles = List.copyOf(rowLevelRoles);
        this.securityConfigs = List.copyOf(securityConfigs);
        this.screenPolicyCount = screenPolicyCount;
        this.menuPolicyCount = menuPolicyCount;
        this.kotlinFilesCount = kotlinFilesCount;
    }

    public List<RedFlag> getRedFlags() {
        return redFlags;
    }

    public List<String> getResourceRoles() {
        return resourceRoles;
    }

    public List<String> getRowLevelRoles() {
        return rowLevelRoles;
    }

    public List<String> getSecurityConfigs() {
        return securityConfigs;
    }

    public int getScreenPolicyCount() {
        return screenPolicyCount;
    }

    public int getMenuPolicyCount() {
        return menuPolicyCount;
    }

    public int getKotlinFilesCount() {
        return kotlinFilesCount;
    }

    /**
     * A finding that cannot be migrated automatically and needs manual estimation.
     */
    public static class RedFlag {
        private final String category;
        private final String subject;
        private final String detail;

        public RedFlag(String category, String subject, String detail) {
            this.category = category;
            this.subject = subject;
            this.detail = detail;
        }

        public String getCategory() {
            return category;
        }

        public String getSubject() {
            return subject;
        }

        public String getDetail() {
            return detail;
        }
    }
}
