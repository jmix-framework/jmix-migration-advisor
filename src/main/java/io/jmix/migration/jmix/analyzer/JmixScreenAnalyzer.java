package io.jmix.migration.jmix.analyzer;

import io.jmix.migration.classicui.model.LayoutItem;
import io.jmix.migration.classicui.model.UiUnitInfo;
import io.jmix.migration.classicui.parser.ScreenClassProfile;
import io.jmix.migration.classicui.parser.ScreenControllerParser;
import io.jmix.migration.classicui.parser.ScreenDescriptorParser;
import io.jmix.migration.classicui.parser.ScreensCollector;
import io.jmix.migration.core.scan.BaseAnalyzer;
import io.jmix.migration.core.scan.UnparsedFilesCollector;
import io.jmix.migration.core.project.JmixModule;
import org.dom4j.Document;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Collects Jmix 1.x (classic UI) screens: XML descriptors from module resources
 * (recognized by the {@code http://jmix.io/schema/ui/} namespace) and controller classes
 * annotated with {@code @UiController}/{@code @UiDescriptor} from {@code io.jmix.ui.screen}.
 * Reuses the shared classic UI parsing layer; legacy CUBA branches simply never trigger.
 */
public class JmixScreenAnalyzer extends BaseAnalyzer {

    private static final Logger log = LoggerFactory.getLogger(JmixScreenAnalyzer.class);

    protected static final String JMIX_UI_NAMESPACE_PREFIX = "http://jmix.io/schema/ui/";

    protected final UnparsedFilesCollector unparsedFilesCollector;

    public JmixScreenAnalyzer(UnparsedFilesCollector unparsedFilesCollector) {
        this.unparsedFilesCollector = unparsedFilesCollector;
    }

    public ScreensCollector analyzeScreens(List<JmixModule> modules) {
        log.info("Start screens analysis");
        ScreensCollector screensCollector = new ScreensCollector();

        List<Path> allJavaDirs = new ArrayList<>();
        for (JmixModule module : modules) {
            if (Files.isDirectory(module.getJavaSourcesDir())) {
                allJavaDirs.add(module.getJavaSourcesDir());
            }
        }

        for (JmixModule module : modules) {
            Path resourcesDir = module.getResourcesDir();
            if (Files.isDirectory(resourcesDir)) {
                processDescriptors(resourcesDir, screensCollector);
            }
        }
        for (JmixModule module : modules) {
            Path javaDir = module.getJavaSourcesDir();
            if (Files.isDirectory(javaDir)) {
                processControllers(javaDir, allJavaDirs, screensCollector);
            }
        }
        return screensCollector;
    }

    /**
     * Total usage counters of UI components across all collected screen layouts.
     */
    public Map<String, Integer> countTotalUiComponents(ScreensCollector screensCollector) {
        Map<String, Integer> totalUiComponents = new HashMap<>();
        screensCollector.getAllScreens().forEach(unitInfo -> {
            List<LayoutItem> layoutItems = Optional.ofNullable(unitInfo.getLayout())
                    .map(layout -> layout.getAllItems())
                    .orElse(List.of());
            layoutItems.forEach(layoutItem ->
                    totalUiComponents.merge(layoutItem.getName(), layoutItem.getQuantity(), Integer::sum));
        });
        return totalUiComponents;
    }

    protected void processDescriptors(Path resourcesDir, ScreensCollector screensCollector) {
        ScreenDescriptorParser descriptorParser =
                new ScreenDescriptorParser(resourcesDir, screensCollector, JMIX_UI_NAMESPACE_PREFIX);
        try {
            Files.walkFileTree(resourcesDir, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (isXmlFile(file)) {
                        try {
                            Document document = parseDocument(file);
                            Element rootElement = document.getRootElement();
                            if (rootElement != null
                                    && (descriptorParser.isScreenDescriptor(rootElement)
                                    || descriptorParser.isFragmentDescriptor(rootElement))) {
                                descriptorParser.parseXmlDescriptor(rootElement, file);
                            }
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

    protected void processControllers(Path javaDir, List<Path> allJavaDirs, ScreensCollector screensCollector) {
        ScreenControllerParser controllerParser =
                new ScreenControllerParser(javaDir, allJavaDirs, screensCollector, ScreenClassProfile.jmixClassic());
        try {
            Files.walkFileTree(javaDir, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (isJavaSourceFile(file)) {
                        try {
                            controllerParser.parseJavaFile(file);
                        } catch (Exception e) {
                            log.warn("Failed to parse Java file '{}': {}", file, e.getMessage());
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
}
