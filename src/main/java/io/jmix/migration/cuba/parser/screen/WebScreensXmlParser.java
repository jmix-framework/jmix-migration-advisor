package io.jmix.migration.cuba.parser.screen;
import io.jmix.migration.classicui.parser.ScreensCollector;

import io.jmix.migration.core.scan.XmlUtils;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WebScreensXmlParser {

    private static final Logger log = LoggerFactory.getLogger(WebScreensXmlParser.class);

    protected final Path webSrcPath;
    protected final Path guiSrcPath;
    protected final Path basePackage;
    protected final ScreensCollector screensCollector;

    private final Set<String> processedFiles;

    public WebScreensXmlParser(Path webSrcPath, Path guiSrcPath, Path basePackage, ScreensCollector screensCollector) {
        this.webSrcPath = webSrcPath;
        this.guiSrcPath = guiSrcPath;
        this.basePackage = basePackage;
        this.screensCollector = screensCollector;
        this.processedFiles = new HashSet<>();
    }

    public ScreensCollector processWebScreensXml(Path webScreensFilePath) {
        log.info("Start web-screens.xml analysis");

        File file = webScreensFilePath.toFile();
        if (!file.exists()) {
            throw new IllegalArgumentException("'web-screens.xml' file not found in path: " + webScreensFilePath);
        }

        if (file.isDirectory()) {
            throw new IllegalArgumentException(webScreensFilePath + " is not a file");
        }

        processScreensFile(file, "web"); // TODO check possibility to start processing from non-web module xml file

        return screensCollector;
    }

    protected void processScreensFile(File file, String module) {
        log.debug("Process file: {}", file);
        if (processedFiles.contains(file.getAbsolutePath())) {
            // Legal for diamond-shaped includes; a genuine cycle is also just skipped
            log.warn("Screens file '{}' has been processed already, skip repeated include", file);
            return;
        }
        processedFiles.add(file.getAbsolutePath());

        Document document = XmlUtils.readDocument(file);
        Element rootElement = document.getRootElement();
        String tagName = rootElement.getName();
        boolean isWebScreensFile = "screen-config".equals(tagName);
        if (isWebScreensFile) {
            processRootElement(rootElement, module);
        }
    }

    protected void processRootElement(Element webScreensElement, String module) {
        List<Element> screenElements = webScreensElement.elements("screen");
        log.debug("Find 'screen' elements: {}", screenElements.size());
        for (Element screenElement : screenElements) {
            String screenId = screenElement.attributeValue("id");
            String descriptor = screenElement.attributeValue("template");
            if (descriptor == null) {
                //todo check case with class instead of template
                continue;
            }
            descriptor = removeLeadingSlash(descriptor);
            log.debug("Screen item: id = {}, descriptor = {}", screenId, descriptor);

            //screensCollector.initLegacyScreenInfo(screenId, descriptor); //todo remove
            screensCollector.addLegacyScreenRegistration(screenId, descriptor, module);
        }

        List<Element> includeElements = webScreensElement.elements("include");
        log.debug("Find 'include' elements: {}", includeElements.size());
        for (Element includeElement : includeElements) {
            String includedFile = includeElement.attributeValue("file");
            processIncludedFile(includedFile);
        }
    }

    protected void processIncludedFile(String includedFile) {
        if (StringUtils.isBlank(includedFile)) {
            return;
        }
        // CUBA convention is a leading slash: <include file="/com/company/app/screens.xml"/>.
        // On Windows such path is drive-relative: isAbsolute() is false, but getRoot() is not null,
        // and resolving it against a base path would produce a drive-root path like C:\com\...
        Path includedPath = Path.of(removeLeadingSlash(includedFile.trim()));
        if (includedPath.getRoot() != null) {
            includedPath = includedPath.subpath(0, includedPath.getNameCount());
        }
        Path webModuleFilePathCandidate = webSrcPath.resolve(basePackage).resolve(includedPath);
        Path webSrcFilePathCandidate = webSrcPath.resolve(includedPath);
        Path guiModuleFilePathCandidate = guiSrcPath.resolve(basePackage).resolve(includedPath);
        Path guiSrcFilePathCandidate = guiSrcPath.resolve(includedPath);
        if (webModuleFilePathCandidate.toFile().exists()) {
            log.debug("Found file in WEB module '{}'", webModuleFilePathCandidate);
            processScreensFile(webModuleFilePathCandidate.toFile(), "web");
        } else if (webSrcFilePathCandidate.toFile().exists()) {
            log.debug("Found file in WEB module src root '{}'", webSrcFilePathCandidate);
            processScreensFile(webSrcFilePathCandidate.toFile(), "web");
        } else if (guiModuleFilePathCandidate.toFile().exists()) {
            log.debug("Found file in GUI module '{}'", guiModuleFilePathCandidate);
            processScreensFile(guiModuleFilePathCandidate.toFile(), "gui");
        } else if (guiSrcFilePathCandidate.toFile().exists()) {
            log.debug("Found file in GUI module src root '{}'", guiSrcFilePathCandidate);
            processScreensFile(guiSrcFilePathCandidate.toFile(), "gui");
        } else {
            log.error("File '{}' not found in WEB/GUI modules", includedPath);
        }
    }

    protected String removeLeadingSlash(String source) {
        if (source.startsWith("/")) {
            source = source.substring(1);
        }
        return source;
    }
}
