package io.jmix.migration.analysis;

import io.jmix.migration.analysis.parser.ScreensCollector;
import io.jmix.migration.analysis.parser.screen.WebScreensXmlParser;
import io.jmix.migration.analysis.parser.screen.ScreenControllerParser;
import io.jmix.migration.analysis.parser.screen.ScreenDescriptorParser;
import io.jmix.migration.model.*;
import org.apache.commons.io.FilenameUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.stream.Collectors;

public class UiModulesAnalyzer extends BaseAnalyzer {

    private static final Logger log = LoggerFactory.getLogger(UiModulesAnalyzer.class);

    protected final Path webSrcPath;
    protected final Path guiSrcPath;
    protected final String basePackage;
    protected final List<Path> allSrcPaths;

    public UiModulesAnalyzer(Path webSrcPath, Path guiSrcPath, String basePackage) {
        this.webSrcPath = webSrcPath;
        this.guiSrcPath = guiSrcPath;
        this.basePackage = basePackage;
        this.allSrcPaths = List.of(webSrcPath, guiSrcPath);
    }

    public ScreensCollector analyzeUiModules() {
        log.info("---=== Start analyze UI modules ===---");

        String[] basePackageSplit = basePackage.split("\\.");
        Path basePackageLocalPath = Path.of("", basePackageSplit);
        Path webModuleBasePackagePath = webSrcPath.resolve(basePackageLocalPath);
        Path webScreensFilePath = getWebScreensXmlFilePath(webModuleBasePackagePath);

        ScreensCollector screensCollector = new ScreensCollector();

        WebScreensXmlParser webScreensXmlParser = new WebScreensXmlParser(webSrcPath, guiSrcPath, basePackageLocalPath, screensCollector);
        webScreensXmlParser.processWebScreensXml(webScreensFilePath);

        analyzeWebModule(screensCollector);
        analyzeGuiModule(screensCollector);

        Map<String, ScreenInfo> screensByControllers = screensCollector.getScreensByControllers();

        Set<String> classesForAdditionalCheck = new HashSet<>();
        Set<ClassGeneralDetails> unknownClasses = screensCollector.getUnknownClasses();
        Set<String> unknownClassesFqn = unknownClasses.stream().map(ClassGeneralDetails::getFqn).collect(Collectors.toSet());

        Collection<ScreenInfo> screenInfosWithControllers = screensByControllers.values();


        // Check if super class of unknown class is one of detected controller
        for(ClassGeneralDetails unknownClass : unknownClasses) {
            String fqn = unknownClass.getFqn();
            ClassGeneralDetails superClassDetails = unknownClass.getSuperClassDetails();
            if(superClassDetails != null) {
                String superFqn = superClassDetails.getFqn();
                ScreenInfo screenInfoBySuperFqn = screensCollector.getScreenInfoByController(superFqn);
                if(screenInfoBySuperFqn != null) {
                    classesForAdditionalCheck.add(fqn);
                }
            }
        }

        // Check if unknown class is a super class of some detected controller
        for(ScreenInfo controllerScreenInfo : screenInfosWithControllers) {
            // todo check if some controller has unknown class as a parent
            if(!controllerScreenInfo.isControllerProcessed()) {
                continue;
            }
            ScreenControllerDetails controllerDetails = controllerScreenInfo.getControllerDetails();
            if(controllerDetails == null) {
                continue;
            }
            ScreenControllerSuperClassDetails superClassDetails = controllerDetails.getSuperClassDetails();
            if(superClassDetails != null && unknownClassesFqn.contains(superClassDetails.getFqn())) {
                classesForAdditionalCheck.add(superClassDetails.getFqn());
            }
        }

        if(!classesForAdditionalCheck.isEmpty()) {
            classesForAdditionalCheck.forEach(screensCollector::removeUnknownClass);
            //analyzeAdditionalClasses(screensCollector, classesForAdditionalCheck); //todo implement forced processing of class as controller
        }


        return screensCollector;
    }

    protected void analyzeWebModule(ScreensCollector screensCollector) {
        if(webSrcPath.toFile().exists()) {
            processUiModuleScreenDescriptors(webSrcPath, screensCollector);
            processUiModuleScreenControllers(webSrcPath, screensCollector);
        }
    }

    protected void analyzeGuiModule(ScreensCollector screensCollector) {
        if(guiSrcPath.toFile().exists()) {
            processUiModuleScreenDescriptors(guiSrcPath, screensCollector);
            processUiModuleScreenControllers(guiSrcPath, screensCollector);
        }
    }

    protected void analyzeAdditionalClasses(ScreensCollector screensCollector, Set<String> classesToAnalyze) {
        processUiModuleScreenControllers(webSrcPath, screensCollector, classesToAnalyze);
        processUiModuleScreenControllers(guiSrcPath, screensCollector, classesToAnalyze);
    }

    protected void processUiModuleScreenDescriptors(Path moduleSrcPath, ScreensCollector screensCollector) {
        ScreenDescriptorParser screenDescriptorParser = new ScreenDescriptorParser(moduleSrcPath, screensCollector);

        try {
            Files.walkFileTree(moduleSrcPath, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (isXmlFile(file)) {
                        log.debug("Process file '{}'", file);
                        processXmlFile(file, screenDescriptorParser);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected void processUiModuleScreenControllers(Path moduleSrcPath, ScreensCollector screensCollector) {
        processUiModuleScreenControllers(moduleSrcPath, screensCollector, Collections.emptySet());
    }

    protected void processUiModuleScreenControllers(Path moduleSrcPath, ScreensCollector screensCollector, Set<String> specificClassesToAnalyze) {
        ScreenControllerParser screenControllerParser = new ScreenControllerParser(moduleSrcPath, allSrcPaths, screensCollector);

        try {
            Files.walkFileTree(moduleSrcPath, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (isJavaSourceFile(file)) {
                        if(specificClassesToAnalyze != null && !specificClassesToAnalyze.isEmpty()) {
                            Path relativePath = moduleSrcPath.relativize(file);
                            String relativePathString = relativePath.toString();
                            String fileNameNoExtension = FilenameUtils.removeExtension(relativePathString);
                            String fqn = fileNameNoExtension.replace(FileSystems.getDefault().getSeparator(), ".");
                            if(specificClassesToAnalyze.contains(fqn)) {
                                log.debug("Process file `{}`", file);
                                processJavaFile(file, screenControllerParser);
                            }
                        } else {
                            log.debug("Process file `{}`", file);
                            processJavaFile(file, screenControllerParser);
                        }
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected void visitUiModuleFiles(Path moduleSrcPath, ScreensCollector screensCollector) {
        ScreenDescriptorParser screenDescriptorParser = new ScreenDescriptorParser(moduleSrcPath, screensCollector);
        ScreenControllerParser screenControllerParser = new ScreenControllerParser(moduleSrcPath, allSrcPaths, screensCollector);

        try {
            Files.walkFileTree(moduleSrcPath, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    log.info("\n\n---=== Process file: {} ===---", file);

                    if (isDirectory(file)) {
                        log.debug("`{}` is directory", file);
                        return FileVisitResult.CONTINUE;
                    }

                    if (isXmlFile(file)) {
                        log.debug("`{}` is XML file", file);
                        processXmlFile(file, screenDescriptorParser);
                        return FileVisitResult.CONTINUE;
                    }

                    if (isJavaSourceFile(file)) {
                        log.debug("`{}` is java source file", file);
                        processJavaFile(file, screenControllerParser);
                        return FileVisitResult.CONTINUE;
                    }

                    log.debug("`{}` is something else", file); //todo
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected void processXmlFile(Path filePath, ScreenDescriptorParser screenDescriptorParser) {
        log.info("[Process XML file] File={}", filePath);

        Document document = parseDocument(filePath);
        Element rootElement = document.getRootElement();
        if(rootElement == null) {
            return;
        }

        if (screenDescriptorParser.isScreenDescriptor(rootElement) || screenDescriptorParser.isFragmentDescriptor(rootElement)) {
            screenDescriptorParser.parseXmlDescriptor2(rootElement, filePath);
        }
    }

    protected void processJavaFile(Path filePath, ScreenControllerParser screenControllerParser) {
        log.info("[Process Java file] File={}", filePath);
        screenControllerParser.parseJavaFile(filePath);
    }

    protected Path getWebScreensXmlFilePath(Path basePackagePath) {
        //todo check 'web-screens.xml' location property
        return Path.of(basePackagePath.toString(), "web-screens.xml");
    }
}
