package io.jmix.migration.cli;

import com.beust.jcommander.Parameter;
import io.jmix.migration.jmix.JmixProjectAnalyzer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AnalyzeJmixProjectCommand implements BaseCommand {

    private static final Logger log = LoggerFactory.getLogger(AnalyzeJmixProjectCommand.class);

    @Parameter(names = {"--project-dir"}, description = "Target project directory", required = true, order = 0)
    private String projectDirectory;

    @Parameter(names = {"--base-package"}, description = "Base package of the project."
            + " Resolved from the @SpringBootApplication class when omitted", required = false, order = 1)
    private String basePackage;

    @Parameter(names = {"--estimation-data-file"}, description = "External file with estimation data", required = false, order = 3)
    private String estimationDataFile;

    @Parameter(names = {"--jmix-version"}, description = "Jmix version override for builds where"
            + " the version cannot be read from build.gradle (e.g. placeholder variables)", required = false, order = 2)
    private String jmixVersion;

    @Override
    public void run() {
        log.info("Start 'AnalyzeJmixProjectCommand'");

        JmixProjectAnalyzer jmixProjectAnalyzer = new JmixProjectAnalyzer(estimationDataFile);
        jmixProjectAnalyzer.analyzeProject(projectDirectory, basePackage, jmixVersion);
    }
}
