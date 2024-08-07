package io.jmix.migration.command;

import com.beust.jcommander.Parameter;
import io.jmix.migration.analysis.CubaProjectAnalyzer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AnalyzeCubaProjectCommand implements BaseCommand {

    private static final Logger log = LoggerFactory.getLogger(AnalyzeCubaProjectCommand.class);

    @Parameter(names = {"--project-dir"}, description = "Target project directory", required = true, order = 0)
    private String projectDirectory;

    @Parameter(names = {"--base-package"}, description = "Base package of the project", required = true, order = 1)
    private String basePackage;

    @Parameter(names = {"--estimation-data-file"}, description = "External file with estimation data", required = false, order = 2)
    private String estimationDataFile;

    @Override
    public void run() {
        log.info("Start 'AnalyzeCubaProjectCommand'");

        CubaProjectAnalyzer cubaProjectAnalyzer = new CubaProjectAnalyzer(estimationDataFile);
        cubaProjectAnalyzer.analyzeProject(projectDirectory, basePackage);
    }
}
