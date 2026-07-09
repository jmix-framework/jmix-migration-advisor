package io.jmix.migration.analysis;

import io.jmix.migration.analysis.model.UnparsedFileEntry;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Accumulates files skipped during analysis so the report can show what the metrics do not cover.
 */
public class UnparsedFilesCollector {

    private static final int MAX_REASON_LENGTH = 300;

    private final List<UnparsedFileEntry> entries = new ArrayList<>();

    public void add(Path file, Exception e) {
        add(file, buildReason(e));
    }

    public void add(Path file, String reason) {
        entries.add(new UnparsedFileEntry(file.toString(), reason));
    }

    public List<UnparsedFileEntry> getEntries() {
        return List.copyOf(entries);
    }

    protected String buildReason(Exception e) {
        String message = e.getMessage();
        if (message == null || message.isBlank()) {
            message = e.getClass().getSimpleName();
        }
        if (message.length() > MAX_REASON_LENGTH) {
            message = message.substring(0, MAX_REASON_LENGTH) + "...";
        }
        return message;
    }
}
