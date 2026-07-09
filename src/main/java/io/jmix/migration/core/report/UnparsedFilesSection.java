package io.jmix.migration.core.report;

import io.jmix.migration.core.model.UnparsedFileEntry;

import java.util.List;

public class UnparsedFilesSection implements ReportSection {

    public static final String TYPE = "unparsed";

    private final List<UnparsedFileEntry> files;

    public UnparsedFilesSection(List<UnparsedFileEntry> files) {
        this.files = List.copyOf(files);
    }

    @Override
    public String getId() {
        return "unparsed";
    }

    @Override
    public String getTitle() {
        return "Not analyzed";
    }

    @Override
    public String getType() {
        return TYPE;
    }

    public List<UnparsedFileEntry> getFiles() {
        return files;
    }
}
