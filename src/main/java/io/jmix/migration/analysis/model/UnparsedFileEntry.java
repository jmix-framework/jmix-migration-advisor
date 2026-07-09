package io.jmix.migration.analysis.model;

/**
 * A file that could not be analyzed and is excluded from all metrics and estimations.
 */
public class UnparsedFileEntry {
    private final String path;
    private final String reason;

    public UnparsedFileEntry(String path, String reason) {
        this.path = path;
        this.reason = reason;
    }

    public String getPath() {
        return path;
    }

    public String getReason() {
        return reason;
    }
}
