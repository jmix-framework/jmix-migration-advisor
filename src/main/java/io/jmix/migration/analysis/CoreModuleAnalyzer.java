package io.jmix.migration.analysis;

import io.jmix.migration.analysis.model.CoreModuleAnalysisResult;
import io.jmix.migration.analysis.parser.general.WebXmlParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.List;

public class CoreModuleAnalyzer extends BaseAnalyzer {

    private static final Logger log = LoggerFactory.getLogger(CoreModuleAnalyzer.class);

    private static final Path CORE_WEB_XML_RELATIVE_PATH = Path.of("web", "WEB-INF", "web.xml");

    protected final Path coreRootPath;
    protected final Path coreSrcPath;
    protected final String basePackage;

    public CoreModuleAnalyzer(Path coreRootPath, Path coreSrcPath, String basePackage) {
        this.coreRootPath = coreRootPath;
        this.coreSrcPath = coreSrcPath;
        this.basePackage = basePackage;
    }

    public CoreModuleAnalysisResult analyzeCoreModule() {
        log.info("---=== Start CORE module analysis ===---");

        Path webXmlFullPath = getCoreWebXmlRelativePathFilePath();
        List<String> appComponents = new WebXmlParser().processWebXml(webXmlFullPath);

        return new CoreModuleAnalysisResult(appComponents);
    }

    protected Path getCoreWebXmlRelativePathFilePath() {
        return coreRootPath.resolve(CORE_WEB_XML_RELATIVE_PATH);
    }
}
