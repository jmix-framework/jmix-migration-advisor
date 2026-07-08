package io.jmix.migration.analysis;

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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
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
        Configuration configuration = createFremarkerConfiguration();
        String fileName = createResultFileName();

        Map<String, Object> data = new HashMap<>();

        data.put("projectName", project);
        data.put("generatedAt", LocalDateTime.now().format(DISPLAY_DATE_FORMATTER));

        //Entities amount
        data.put("entitiesAmount", result.getEntitiesAmount());
        data.put("entitiesPerPersistenceUnit", result.getEntitiesPerPersistenceUnit());

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
        List<String> legacyListeners = result.getLegacyListeners();
        data.put("legacyListeners", legacyListeners);
        data.put("legacyListenersAmount", legacyListeners == null ? 0 : legacyListeners.size());

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

        // Write to file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            Template template = configuration.getTemplate("report-template.ftl");

            template.process(data, writer);
            writer.flush();
        } catch (IOException | TemplateException e) {
            throw new RuntimeException(e);
        }
    }

    protected Configuration createFremarkerConfiguration() {
        Configuration configuration = new Configuration(Configuration.VERSION_2_3_23);
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

            ScreenComplexityGroup screenComplexityGroup = new ScreenComplexityGroup(name, order, amount, cost, total, screens);
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
                                issue.getExtraComplexityScore()
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
