package io.jmix.migration.analysis;

import io.jmix.migration.analysis.parser.general.PersistenceXmlParser;
import io.jmix.migration.model.GlobalModuleAnalysisResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class GlobalModuleAnalyzer extends BaseAnalyzer {

    private static final Logger log = LoggerFactory.getLogger(GlobalModuleAnalyzer.class);

    protected final Path globalSrcPath;
    protected final String basePackage;

    public GlobalModuleAnalyzer(Path globalSrcPath, String basePackage) {
        this.globalSrcPath = globalSrcPath;
        this.basePackage = basePackage;
    }

    public GlobalModuleAnalysisResult analyzeGlobalModule() {
        log.info("---=== Start analyze GLOBAL module ===---");

        String[] basePackageSplit = basePackage.split("\\.");
        Path basePackageLocalPath = Path.of("", basePackageSplit);
        Path globalModuleBasePackagePath = globalSrcPath.resolve(basePackageLocalPath);
        Path persistenceFilePath = getPersistenceFilePath(globalModuleBasePackagePath);

        PersistenceXmlParser persistenceXmlParser = new PersistenceXmlParser();
        Map<String, List<String>> entitiesPerPersistenceUnit = persistenceXmlParser.processPersistenceXml(persistenceFilePath);


        GlobalModuleAnalysisResult result = new GlobalModuleAnalysisResult(entitiesPerPersistenceUnit);
        //todo create result wrapper
        return result;
    }

    protected Path getPersistenceFilePath(Path moduleBasePackagePath) {
        return Path.of(moduleBasePackagePath.toString(), "persistence.xml");
    }
}
