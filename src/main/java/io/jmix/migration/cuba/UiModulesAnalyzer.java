package io.jmix.migration.cuba;
import io.jmix.migration.core.scan.BaseAnalyzer;
import io.jmix.migration.core.scan.UnparsedFilesCollector;

import io.jmix.migration.cuba.model.UiModulesAnalysisResult;
import io.jmix.migration.core.scan.PropertiesParser;
import io.jmix.migration.classicui.parser.ScreenClassProfile;
import io.jmix.migration.classicui.parser.ScreenControllerParser;
import io.jmix.migration.classicui.parser.ScreenDescriptorParser;
import io.jmix.migration.classicui.parser.ScreensCollector;
import io.jmix.migration.cuba.parser.screen.WebScreensXmlParser;
import org.dom4j.Document;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.Properties;

public class UiModulesAnalyzer extends BaseAnalyzer {

    private static final Logger log = LoggerFactory.getLogger(UiModulesAnalyzer.class);
    public static final String WEB_APP_PROPERTIES = "web-app.properties";
    public static final String WEB_SCREENS_XML = "web-screens.xml";

    protected final Path webSrcPath;
    protected final Path guiSrcPath;
    protected final String basePackage;
    protected final List<Path> allSrcPaths;
    protected final UnparsedFilesCollector unparsedFilesCollector;

    public UiModulesAnalyzer(Path webSrcPath, Path guiSrcPath, String basePackage,
                             UnparsedFilesCollector unparsedFilesCollector) {
        this.webSrcPath = webSrcPath;
        this.guiSrcPath = guiSrcPath;
        this.basePackage = basePackage;
        this.allSrcPaths = List.of(webSrcPath, guiSrcPath);
        this.unparsedFilesCollector = unparsedFilesCollector;
    }

    public UiModulesAnalysisResult analyzeUiModules() {
        log.info("Start UI modules analysis");

        Path basePackageLocalPath = packageToPath(basePackage);
        Path webModuleBasePackagePath = webSrcPath.resolve(basePackageLocalPath);

        Properties webAppProperties = null;
        PropertiesParser propertiesParser = new PropertiesParser();
        Path webAppPropertiesFilePath = getWebAppPropertiesFilePath(webModuleBasePackagePath, webSrcPath);
        if (webAppPropertiesFilePath.toFile().exists()) {
            try {
                webAppProperties = propertiesParser.parsePropertiesFile(webAppPropertiesFilePath);
            } catch (Exception e) {
                log.warn("Failed to parse '{}': {}", webAppPropertiesFilePath, e.getMessage());
                unparsedFilesCollector.add(webAppPropertiesFilePath, e);
            }
        }

        Path webScreensFilePath = getWebScreensXmlFilePath(webModuleBasePackagePath, webSrcPath);

        ScreensCollector screensCollector = new ScreensCollector();

        WebScreensXmlParser webScreensXmlParser = new WebScreensXmlParser(webSrcPath, guiSrcPath, basePackageLocalPath, screensCollector);
        if (webScreensFilePath.toFile().exists()) {
            try {
                webScreensXmlParser.processWebScreensXml(webScreensFilePath);
            } catch (Exception e) {
                log.warn("Failed to process '{}': {}", webScreensFilePath, e.getMessage());
                unparsedFilesCollector.add(webScreensFilePath, e);
            }
        } else {
            log.warn("'web-screens.xml' file is not found (checked '{}'), legacy screen registrations are unavailable",
                    webScreensFilePath);
        }

        analyzeWebModule(screensCollector);
        analyzeGuiModule(screensCollector);

        return new UiModulesAnalysisResult(screensCollector, webAppProperties);
    }

    protected void analyzeWebModule(ScreensCollector screensCollector) {
        if (webSrcPath.toFile().exists()) {
            processUiModuleScreenDescriptors(webSrcPath, screensCollector);
            processUiModuleScreenControllers(webSrcPath, screensCollector);
        }
    }

    protected void analyzeGuiModule(ScreensCollector screensCollector) {
        if (guiSrcPath.toFile().exists()) {
            processUiModuleScreenDescriptors(guiSrcPath, screensCollector);
            processUiModuleScreenControllers(guiSrcPath, screensCollector);
        }
    }

    protected void processUiModuleScreenDescriptors(Path moduleSrcPath, ScreensCollector screensCollector) {
        ScreenDescriptorParser screenDescriptorParser = new ScreenDescriptorParser(moduleSrcPath, screensCollector, null);

        try {
            Files.walkFileTree(moduleSrcPath, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (isXmlFile(file)) {
                        log.debug("Process file '{}'", file);
                        try {
                            processXmlFile(file, screenDescriptorParser);
                        } catch (Exception e) {
                            log.warn("Failed to parse XML file '{}': {}", file, e.getMessage());
                            unparsedFilesCollector.add(file, e);
                        }
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected void processUiModuleScreenControllers(Path moduleSrcPath, ScreensCollector screensCollector) {
        ScreenControllerParser screenControllerParser = new ScreenControllerParser(moduleSrcPath, allSrcPaths, screensCollector, ScreenClassProfile.cuba());

        try {
            Files.walkFileTree(moduleSrcPath, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (isJavaSourceFile(file)) {
                        log.debug("Process file `{}`", file);
                        processJavaFileSafely(file, screenControllerParser);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected void processXmlFile(Path filePath, ScreenDescriptorParser screenDescriptorParser) {
        log.debug("[Process XML file] File={}", filePath);

        Document document = parseDocument(filePath);
        Element rootElement = document.getRootElement();
        if (rootElement == null) {
            return;
        }

        if (screenDescriptorParser.isScreenDescriptor(rootElement) || screenDescriptorParser.isFragmentDescriptor(rootElement)) {
            screenDescriptorParser.parseXmlDescriptor(rootElement, filePath);
        }
    }

    protected void processJavaFileSafely(Path filePath, ScreenControllerParser screenControllerParser) {
        log.debug("[Process Java file] File={}", filePath);
        try {
            screenControllerParser.parseJavaFile(filePath);
        } catch (Exception e) {
            log.warn("Failed to parse Java file '{}': {}", filePath, e.getMessage());
            unparsedFilesCollector.add(filePath, e);
        }
    }

    protected Path getWebScreensXmlFilePath(Path basePackagePath, Path webSrcPath) {
        Path path = Path.of(basePackagePath.toString(), WEB_SCREENS_XML);
        if (!path.toFile().exists()) {
            path = Path.of(webSrcPath.toString(), WEB_SCREENS_XML);
        }
        return path;
    }

    protected Path getWebAppPropertiesFilePath(Path basePackagePath, Path webSrcPath) {
        Path path = Path.of(basePackagePath.toString(), WEB_APP_PROPERTIES);
        if (!path.toFile().exists()) {
            path = Path.of(webSrcPath.toString(), WEB_APP_PROPERTIES);
        }
        return path;
    }
}
