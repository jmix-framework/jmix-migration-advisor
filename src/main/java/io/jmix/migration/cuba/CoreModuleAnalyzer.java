package io.jmix.migration.cuba;
import io.jmix.migration.core.scan.BaseAnalyzer;
import io.jmix.migration.core.scan.UnparsedFilesCollector;

import io.jmix.migration.cuba.model.CoreModuleAnalysisResult;
import io.jmix.migration.cuba.parser.general.WebXmlParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

public class CoreModuleAnalyzer extends BaseAnalyzer {

    private static final Logger log = LoggerFactory.getLogger(CoreModuleAnalyzer.class);

    private static final Path CORE_WEB_XML_RELATIVE_PATH = Path.of("web", "WEB-INF", "web.xml");

    protected final Path coreRootPath;
    protected final Path coreSrcPath;
    protected final String basePackage;
    protected final UnparsedFilesCollector unparsedFilesCollector;

    public CoreModuleAnalyzer(Path coreRootPath, Path coreSrcPath, String basePackage,
                              UnparsedFilesCollector unparsedFilesCollector) {
        this.coreRootPath = coreRootPath;
        this.coreSrcPath = coreSrcPath;
        this.basePackage = basePackage;
        this.unparsedFilesCollector = unparsedFilesCollector;
    }

    public CoreModuleAnalysisResult analyzeCoreModule() {
        log.info("Start CORE module analysis");

        Path webXmlFullPath = getCoreWebXmlRelativePathFilePath();
        List<String> appComponents = Collections.emptyList();
        if (webXmlFullPath.toFile().exists()) {
            try {
                appComponents = new WebXmlParser().processWebXml(webXmlFullPath);
            } catch (Exception e) {
                log.warn("Failed to process '{}': {}", webXmlFullPath, e.getMessage());
                unparsedFilesCollector.add(webXmlFullPath, e);
            }
        } else {
            log.warn("'web.xml' file is not found (checked '{}'), app components are not detected", webXmlFullPath);
        }

        return new CoreModuleAnalysisResult(appComponents);
    }

    protected Path getCoreWebXmlRelativePathFilePath() {
        return coreRootPath.resolve(CORE_WEB_XML_RELATIVE_PATH);
    }
}
