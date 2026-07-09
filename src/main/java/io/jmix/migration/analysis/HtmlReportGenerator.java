package io.jmix.migration.analysis;

import freemarker.core.HTMLOutputFormat;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import io.jmix.migration.CliRunner;
import io.jmix.migration.analysis.appcomponent.AppComponentType;
import io.jmix.migration.analysis.appcomponent.CubaAppComponentInfo;
import io.jmix.migration.analysis.issue.uicomponent.UiComponentIssue;
import io.jmix.migration.analysis.issue.uicomponent.UiComponentIssuesRegistry;
import io.jmix.migration.analysis.model.*;

import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.*;

import static java.time.temporal.ChronoField.*;
import static java.time.temporal.ChronoField.MILLI_OF_SECOND;

public class HtmlReportGenerator {

    private static final DateTimeFormatter DISPLAY_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final UiComponentIssuesRegistry uiComponentIssuesRegistry;

    public HtmlReportGenerator(UiComponentIssuesRegistry uiComponentIssuesRegistry) {
        this.uiComponentIssuesRegistry = uiComponentIssuesRegistry;
    }

    protected void generateHtmlReport(String project, CubaProjectEstimationResult result) {
        String fileName = createResultFileName();
        String content = generateReportContent(project, result);
        // The template declares UTF-8, so the file charset must not depend on the JVM default
        try {
            Files.writeString(Path.of(fileName), content, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Renders the report to a string. Separated from file writing so tests can capture
     * the report content directly.
     */
    public String generateReportContent(String project, CubaProjectEstimationResult result) {
        Configuration configuration = createFremarkerConfiguration();

        Map<String, Object> data = new HashMap<>();

        data.put("projectName", project);
        data.put("generatedAt", LocalDateTime.now().format(DISPLAY_DATE_FORMATTER));

        //Entities amount
        data.put("entitiesAmount", result.getEntitiesAmount());
        data.put("entitiesPerPersistenceUnit", sortedEntitiesPerPersistenceUnit(result.getEntitiesPerPersistenceUnit()));

        //Screens
        List<ScreenComplexityGroup> complexityGroupRows = createComplexityGroupRows(result);
        data.put("screenComplexityGroups", complexityGroupRows);
        data.put("screensTotalHours", result.getScreensTotalCost());
        data.put("screensTotalAmount", result.getScreensTotalAmount());
        data.put("screensMaxGroupTotal", complexityGroupRows.stream()
                .map(ScreenComplexityGroup::getTotal)
                .max(Comparator.naturalOrder())
                .orElse(BigDecimal.ZERO));

        //Legacy entity listeners
        List<String> legacyListeners = result.getLegacyListeners() == null
                ? List.of()
                : result.getLegacyListeners().stream().sorted().toList();
        data.put("legacyListeners", legacyListeners);
        data.put("legacyListenersAmount", legacyListeners.size());

        // UI components
        List<UiComponentNotesRow> uiComponentNotesRows = createUiComponentIssuesRows(result);
        data.put("uiComponentNotes", uiComponentNotesRows);

        //Addons
        List<CubaAppComponentInfo> appComponents = result.getAppComponents();
        data.put("appComponents", appComponents);
        data.put("appComponentsAmount", appComponents.size());
        data.put("missingAppComponentsAmount", appComponents.stream()
                .filter(c -> c.getAppComponentType() == AppComponentType.MISSING)
                .count());

        // General estimations
        List<EstimationItem> estimationItemsRows = createEstimationItemsRows(result);
        data.put("estimationItems", estimationItemsRows);

        // Total
        data.put("totalEstimation", result.getTotalEstimation());

        // Misc
        data.put("miscNotes", result.getMiscNotes());

        // Screens with components having no Jmix equivalent
        data.put("screensRequireDecision", result.getScreensRequireDecision());

        // Files skipped during analysis
        data.put("unparsedFiles", result.getUnparsedFiles());

        try {
            Template template = configuration.getTemplate("report-template.ftl");
            StringWriter writer = new StringWriter();
            template.process(data, writer);
            return writer.toString();
        } catch (IOException | TemplateException e) {
            throw new RuntimeException(e);
        }
    }

    protected Map<String, List<String>> sortedEntitiesPerPersistenceUnit(Map<String, List<String>> entitiesPerPersistenceUnit) {
        Map<String, List<String>> sorted = new TreeMap<>();
        entitiesPerPersistenceUnit.forEach((unit, entities) ->
                sorted.put(unit, entities.stream().sorted().toList()));
        return sorted;
    }

    protected Configuration createFremarkerConfiguration() {
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
        configuration.setClassForTemplateLoading(CliRunner.class, "/templates");
        return configuration;
    }

    protected List<ScreenComplexityGroup> createComplexityGroupRows(CubaProjectEstimationResult result) {
        Map<ThresholdItem<Integer, BigDecimal>, List<String>> screensPerComplexity = result.getScreensPerComplexity();
        List<ScreenComplexityGroup> complexityGroupRows = new ArrayList<>();
        screensPerComplexity.forEach(((thresholdItem, screens) -> {
            String name = thresholdItem.getName();
            int order = thresholdItem.getOrder();
            BigDecimal cost = thresholdItem.getOutputValue();
            int amount = screens.size();
            BigDecimal total = cost.multiply(BigDecimal.valueOf(amount));

            // Screen names come from a HashMap-backed pipeline: sort for a stable report
            List<String> sortedScreens = screens.stream().sorted().toList();
            ScreenComplexityGroup screenComplexityGroup = new ScreenComplexityGroup(name, order, amount, cost, total, sortedScreens);
            complexityGroupRows.add(screenComplexityGroup);
        }));
        complexityGroupRows.sort(Comparator.comparingInt(ScreenComplexityGroup::getOrder));

        return complexityGroupRows;
    }

    protected List<UiComponentNotesRow> createUiComponentIssuesRows(CubaProjectEstimationResult result) {
        Map<String, Integer> allUiComponents = result.getAllUiComponents();
        List<String> components = new ArrayList<>(allUiComponents.keySet());
        components.sort(String::compareTo);
        List<UiComponentNotesRow> uiComponentIssuesRows = new ArrayList<>();
        components.forEach(component -> {
            UiComponentIssue issue = uiComponentIssuesRegistry.getIssue(component);
            if (issue != null) {
                uiComponentIssuesRows.add(
                        new UiComponentNotesRow(
                                issue.getComponent(),
                                allUiComponents.get(issue.getComponent()),
                                issue.getNotes(),
                                issue.getType() == null ? null : issue.getType().name(),
                                issue.getExtraComplexityScore(),
                                issue.getRequires()
                        )
                );
            }
        });
        return uiComponentIssuesRows;
    }

    protected List<EstimationItem> createEstimationItemsRows(CubaProjectEstimationResult result) {
        List<EstimationItem> estimationItemsRows = new ArrayList<>();
        estimationItemsRows.add(createEstimationItemRow("Initial migration", result.getInitialMigrationCost()));
        estimationItemsRows.add(createEstimationItemRow("Base entities", result.getBaseEntitiesMigrationCost()));
        estimationItemsRows.add(createEstimationItemRow("Legacy listeners", result.getLegacyListenersCost()));
        estimationItemsRows.add(createEstimationItemRow("Screens", result.getScreensTotalCost()));
        return estimationItemsRows;
    }

    protected EstimationItem createEstimationItemRow(String category, BigDecimal estimation) {
        return new EstimationItem(category, estimation);
    }

    protected String createResultFileName() {
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
}
