package io.jmix.migration.analysis;

import io.jmix.migration.util.XmlUtils;
import org.apache.commons.io.FilenameUtils;
import org.dom4j.Document;
import org.dom4j.io.SAXReader;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

public abstract class BaseAnalyzer {

    protected final SAXReader saxReader;

    public BaseAnalyzer() {
        this.saxReader = XmlUtils.getSaxReader();
    }

    protected Document parseDocument(Path filePath) {
        File file = filePath.toFile();
        return XmlUtils.readDocument(file, saxReader);
    }

    protected boolean isDirectory(Path file) {
        return Files.isDirectory(file);
    }

    protected boolean isJavaSourceFile(Path file) {
        String extension = getFileExtension(file);
        return "java".equalsIgnoreCase(extension);
    }

    protected boolean isXmlFile(Path file) {
        String extension = getFileExtension(file);
        return "xml".equalsIgnoreCase(extension);
    }

    protected String getFileExtension(Path file) {
        String fileName = file.getFileName().toString();
        return FilenameUtils.getExtension(fileName);
    }
}
