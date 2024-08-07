package io.jmix.migration.analysis;

import io.jmix.migration.analysis.model.GlobalModuleAnalysisResult;
import io.jmix.migration.analysis.parser.GlobalModuleJavaParser;
import io.jmix.migration.analysis.parser.general.PersistenceXmlParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GlobalModuleAnalyzer extends BaseAnalyzer {

    private static final Logger log = LoggerFactory.getLogger(GlobalModuleAnalyzer.class);

    protected final Path globalSrcPath;
    protected final String basePackage;

    public GlobalModuleAnalyzer(Path globalSrcPath, String basePackage) {
        this.globalSrcPath = globalSrcPath;
        this.basePackage = basePackage;
    }

    public GlobalModuleAnalysisResult analyzeGlobalModule() {
        log.info("---=== Start GLOBAL module analysis ===---");

        Path globalModuleBasePackagePath = globalSrcPath.resolve(packageToPath(basePackage));
        Path persistenceFilePath = getPersistenceFilePath(globalModuleBasePackagePath);

        PersistenceXmlParser persistenceXmlParser = new PersistenceXmlParser();
        Map<String, List<String>> entitiesPerPersistenceUnit = persistenceXmlParser.processPersistenceXml(persistenceFilePath);

        Set<String> listeners = processGlobalModuleJavaFiles(globalSrcPath);

        return new GlobalModuleAnalysisResult(entitiesPerPersistenceUnit, listeners);
    }

    protected Set<String> processGlobalModuleJavaFiles(Path moduleSrcPath) {
        Set<String> listeners = new HashSet<>();
        GlobalModuleJavaParser globalModuleJavaParser = new GlobalModuleJavaParser(listeners);

        try {
            Files.walkFileTree(moduleSrcPath, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (isJavaSourceFile(file)) {
                        globalModuleJavaParser.parseJavaFile(file);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return listeners;
    }

    protected Path getPersistenceFilePath(Path moduleBasePackagePath) {
        return Path.of(moduleBasePackagePath.toString(), "persistence.xml");
    }
}
