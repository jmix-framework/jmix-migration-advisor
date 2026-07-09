package io.jmix.migration.core.report;

import freemarker.core.HTMLOutputFormat;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static java.time.temporal.ChronoField.HOUR_OF_DAY;
import static java.time.temporal.ChronoField.MILLI_OF_SECOND;
import static java.time.temporal.ChronoField.MINUTE_OF_HOUR;
import static java.time.temporal.ChronoField.SECOND_OF_MINUTE;

/**
 * Renders a {@link ReportModel} into a self-contained HTML document via the shared
 * FreeMarker template. The template dispatches sections to macros by {@link ReportSection#getType()}.
 */
public class HtmlReportWriter {

    protected static final String TEMPLATE_NAME = "report-template.ftl";

    protected static final DateTimeFormatter DISPLAY_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public String render(ReportModel model) {
        Configuration configuration = createFreemarkerConfiguration();

        Map<String, Object> data = new HashMap<>();
        data.put("model", model);
        data.put("generatedAt", LocalDateTime.now().format(DISPLAY_DATE_FORMATTER));

        try {
            Template template = configuration.getTemplate(TEMPLATE_NAME);
            StringWriter writer = new StringWriter();
            template.process(data, writer);
            return writer.toString();
        } catch (IOException | TemplateException e) {
            throw new RuntimeException("Unable to render report", e);
        }
    }

    public void writeToFile(String fileName, ReportModel model) {
        String content = render(model);
        // The template declares UTF-8, so the file charset must not depend on the JVM default
        try {
            Files.writeString(Path.of(fileName), content, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Unable to write report file '" + fileName + "'", e);
        }
    }

    /**
     * Default report file name in the current directory: {@code results_<timestamp>.html}.
     */
    public String createDefaultFileName() {
        DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                .append(DateTimeFormatter.ISO_LOCAL_DATE)
                .appendLiteral('T')
                .appendValue(HOUR_OF_DAY, 2)
                .appendValue(MINUTE_OF_HOUR, 2)
                .appendValue(SECOND_OF_MINUTE, 2)
                .appendFraction(MILLI_OF_SECOND, 0, 3, false)
                .toFormatter();

        return "results_" + LocalDateTime.now().format(formatter) + ".html";
    }

    protected Configuration createFreemarkerConfiguration() {
        Configuration configuration = new Configuration(Configuration.VERSION_2_3_33);
        // Auto-escape all interpolations: many rendered values (project name, screen ids,
        // entity/listener class names, app component packages) come from the analyzed project
        configuration.setOutputFormat(HTMLOutputFormat.INSTANCE);
        configuration.setDefaultEncoding("UTF-8");
        configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        configuration.setLogTemplateExceptions(false);
        // Render numbers with a dot decimal separator and no grouping (e.g. "1234.5", not "1.234,5"),
        // independent of the host locale, so values stay clean and safe to embed into inline CSS.
        configuration.setLocale(Locale.US);
        configuration.setNumberFormat("0.##");
        configuration.setClassForTemplateLoading(HtmlReportWriter.class, "/templates");
        return configuration;
    }
}
