package io.jmix.migration.analysis.estimation;

import io.jmix.migration.analysis.model.BigDecimalThresholdItem;
import io.jmix.migration.analysis.model.IntegerThresholdItem;
import io.jmix.migration.analysis.model.ThresholdItem;
import io.jmix.migration.util.XmlUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.File;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static org.dom4j.Node.ELEMENT_NODE;

/**
 * Provides data about estimation weights for different metrics
 */
public class EstimationDataProvider {

    private static final Logger log = LoggerFactory.getLogger(EstimationDataProvider.class);

    private static final String DEFAULT_FILE = "estimation/estimation-data.xml";

    private static final String THRESHOLDS_XPATH_TEMPLATE = "./estimation-unit[name = '%s']/content/thresholds/threshold";
    private static final String SIMPLE_COST_VALUE_XPATH_TEMPLATE = "./estimation-unit[name = '%s']/content/cost";

    protected final SAXReader saxReader;

    private int changedUiComponentsComplexityBaseValue;
    private int screenDescriptorExtendsScreenComplexityScore;
    private int screenDescriptorHasNestedDataItemComplexityScore;
    private int screenDescriptorUiComponentCreateCallComplexityScore;
    private int initialMigrationCost;
    private int baseEntitiesMigrationCost;
    private int legacyEntityListenerCost;

    private List<? extends ThresholdItem<Integer, Integer>> screenControllerMethodsCallsComplexityThresholds;
    private List<? extends ThresholdItem<Integer, BigDecimal>> screenComplexityTimeEstimationThresholds;

    public EstimationDataProvider() {
        this(null);
    }

    public EstimationDataProvider(@Nullable String externalFileName) {
        this.saxReader = XmlUtils.getSaxReader();
        initData(externalFileName);
    }

    public int getScreenChangedUiComponentsComplexityBaseValue() {
        return changedUiComponentsComplexityBaseValue;
    }

    public int getScreenDescriptorExtendsScreenComplexityScore() {
        return screenDescriptorExtendsScreenComplexityScore;
    }

    public int getScreenDescriptorHasNestedDataItemComplexityScore() {
        return screenDescriptorHasNestedDataItemComplexityScore;
    }

    public int getScreenDescriptorUiComponentCreateCallComplexityScore() {
        return screenDescriptorUiComponentCreateCallComplexityScore;
    }

    public int getInitialMigrationCost() {
        return initialMigrationCost;
    }

    public int getBaseEntitiesMigrationCost() {
        return baseEntitiesMigrationCost;
    }

    public int getLegacyEntityListenerCost() {
        return legacyEntityListenerCost;
    }

    public List<? extends ThresholdItem<Integer, Integer>> getScreenControllerMethodsCallsComplexityThresholds() {
        return screenControllerMethodsCallsComplexityThresholds;
    }

    public List<? extends ThresholdItem<Integer, BigDecimal>> getScreenComplexityTimeEstimationThresholds() {
        return screenComplexityTimeEstimationThresholds;
    }

    protected void initData(String externalFileName) {
        URL defaultFileResource = Thread.currentThread().getContextClassLoader().getResource(DEFAULT_FILE);
        if (defaultFileResource == null) {
            throw new RuntimeException("Resource is null");
        }
        File defaultFile = null;
        try {
            defaultFile = new File(defaultFileResource.toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        Element externalFileElement = null;
        if (StringUtils.isNotEmpty(externalFileName)) {
            File externalFile = new File(externalFileName);
            externalFileElement = loadDataFile(externalFile, false);
        }

        Element defaultFileElement = loadDataFile(defaultFile, true);
        if (defaultFileElement == null) {
            throw new RuntimeException("Default file '" + defaultFile.getAbsolutePath() + "' was not loaded");
        }

        this.changedUiComponentsComplexityBaseValue = loadChangedUiComponentsComplexityBaseValue(defaultFileElement, externalFileElement);
        this.screenDescriptorExtendsScreenComplexityScore = loadScreenDescriptorExtendsScreenComplexityScore(defaultFileElement, externalFileElement);
        this.screenDescriptorHasNestedDataItemComplexityScore = loadScreenDescriptorHasNestedDataItemComplexityScore(defaultFileElement, externalFileElement);
        this.screenDescriptorUiComponentCreateCallComplexityScore = loadScreenDescriptorUiComponentCreateCallComplexityScore(defaultFileElement, externalFileElement);
        this.initialMigrationCost = loadInitialMigrationCost(defaultFileElement, externalFileElement);
        this.baseEntitiesMigrationCost = loadBaseEntitiesMigrationCost(defaultFileElement, externalFileElement);
        this.legacyEntityListenerCost = loadLegacyEntityListenerCost(defaultFileElement, externalFileElement);

        this.screenControllerMethodsCallsComplexityThresholds = extractScreenControllerMethodsCallsComplexityThresholds(defaultFileElement, externalFileElement);
        this.screenComplexityTimeEstimationThresholds = extractScreenComplexityTimeEstimationThresholds(defaultFileElement, externalFileElement);
    }

    protected int loadChangedUiComponentsComplexityBaseValue(Element defaultRootElement, Element externalRootElement) {
        return extractSingleIntValue(defaultRootElement, externalRootElement, createSimpleCostXPathExpression("screen-changed-ui-components-complexity-base-value"));
    }

    protected int loadScreenDescriptorExtendsScreenComplexityScore(Element defaultRootElement, Element externalRootElement) {
        return extractSingleIntValue(defaultRootElement, externalRootElement, createSimpleCostXPathExpression("screen-descriptor-extends-screen-complexity-score"));
    }

    protected int loadScreenDescriptorHasNestedDataItemComplexityScore(Element defaultRootElement, Element externalRootElement) {
        return extractSingleIntValue(defaultRootElement, externalRootElement, createSimpleCostXPathExpression("screen-descriptor-has-nested-data-item-complexity-score"));
    }

    protected int loadScreenDescriptorUiComponentCreateCallComplexityScore(Element defaultRootElement, Element externalRootElement) {
        return extractSingleIntValue(defaultRootElement, externalRootElement, createSimpleCostXPathExpression("screen-descriptor-ui-component-create-call-complexity-score"));
    }

    protected int loadInitialMigrationCost(Element defaultRootElement, Element externalRootElement) {
        return extractSingleIntValue(defaultRootElement, externalRootElement, createSimpleCostXPathExpression("initial-migration-cost"));
    }

    protected int loadBaseEntitiesMigrationCost(Element defaultRootElement, Element externalRootElement) {
        return extractSingleIntValue(defaultRootElement, externalRootElement, createSimpleCostXPathExpression("base-entities-migration-cost"));
    }

    protected int loadLegacyEntityListenerCost(Element defaultRootElement, Element externalRootElement) {
        return extractSingleIntValue(defaultRootElement, externalRootElement, createSimpleCostXPathExpression("legacy-entity-listener-cost"));
    }


    protected int extractSingleIntValue(Element defaultRootElement, Element externalRootElement, String xpath) {
        String stringValue = null;
        if (externalRootElement != null) {
            stringValue = extractSingleStringValue(externalRootElement, xpath);
        }
        if (StringUtils.isBlank(stringValue)) {
            stringValue = extractSingleStringValue(defaultRootElement, xpath);
        }
        if (StringUtils.isBlank(stringValue)) {
            throw new RuntimeException("No data found by xpath: " + xpath);
        }
        return stringToInt(stringValue);
    }

    protected List<? extends ThresholdItem<Integer, Integer>> extractScreenControllerMethodsCallsComplexityThresholds(Element defaultRootElement, Element externalRootElement) {
        return extractThresholds(
                defaultRootElement,
                externalRootElement,
                createThresholdXPathExpression("screen-controller-method-calls"),
                this::stringToInt,
                IntegerThresholdItem::new);
    }

    protected List<? extends ThresholdItem<Integer, BigDecimal>> extractScreenComplexityTimeEstimationThresholds(Element defaultRootElement, Element externalRootElement) {
        return extractThresholds(
                defaultRootElement,
                externalRootElement,
                createThresholdXPathExpression("screen-complexity-time-estimation"),
                BigDecimal::new,
                BigDecimalThresholdItem::new);
    }

    @Nullable
    protected String extractSingleStringValue(Element rootElement, String xpathExpression) {
        Node node = rootElement.selectSingleNode(xpathExpression);
        if (node == null) {
            return null;
        }
        return node.getStringValue();
    }

    protected <V> List<? extends ThresholdItem<Integer, V>> extractThresholds(Element defaultRootElement,
                                                                              Element externalRootElement,
                                                                              String xpath,
                                                                              Function<String, V> outputValueConverter,
                                                                              ThresholdItemGenerator<V> itemGenerator) {
        List<Node> thresholdItemNodes = null;
        if (externalRootElement != null) {
            thresholdItemNodes = externalRootElement.selectNodes(xpath);
        }
        if (thresholdItemNodes == null || thresholdItemNodes.isEmpty()) {
            thresholdItemNodes = defaultRootElement.selectNodes(xpath);
        }

        if (thresholdItemNodes.isEmpty()) {
            throw new RuntimeException("No data found by xpath: " + xpath);
        }
        return createThresholdItems(thresholdItemNodes, outputValueConverter, itemGenerator);
    }

    protected <V> List<? extends ThresholdItem<Integer, V>> createThresholdItems(
            List<Node> thresholdItemNodes,
            Function<String, V> outputValueConverter,
            ThresholdItemGenerator<V> itemGenerator) {
        AtomicInteger minValue = new AtomicInteger();
        AtomicInteger counter = new AtomicInteger();
        return thresholdItemNodes.stream().map(node -> {
                    if (ELEMENT_NODE == node.getNodeType()) {
                        Element thresholdItemElement = (Element) node;
                        Element nameElement = thresholdItemElement.element("name");
                        Element thresholdValueElement = thresholdItemElement.element("threshold-value");
                        Element outputValueElement = thresholdItemElement.element("output-value");

                        String name = nameElement.getText();

                        String thresholdStringValue = thresholdValueElement.getText();
                        int thresholdValue = stringToInt(thresholdStringValue);

                        String outputStringValue = outputValueElement.getText();
                        V outputValue = outputValueConverter.apply(outputStringValue);

                        Range<Integer> range = Range.of(minValue.get(), thresholdValue);

                        ThresholdItem<Integer, V> thresholdItem = itemGenerator.createItem(name, range, outputValue, counter.getAndIncrement());

                        int delta = range.getMaximum() - range.getMinimum();
                        minValue.addAndGet(delta + 1);

                        return thresholdItem;
                    } else {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .toList();
    }

    protected String createThresholdXPathExpression(String estimationUnitName) {
        return THRESHOLDS_XPATH_TEMPLATE.formatted(estimationUnitName);
    }

    protected String createSimpleCostXPathExpression(String estimationUnitName) {
        return SIMPLE_COST_VALUE_XPATH_TEMPLATE.formatted(estimationUnitName);
    }


    protected int stringToInt(String stringValue) {
        if ("MAX".equalsIgnoreCase(stringValue)) {
            return Integer.MAX_VALUE;
        } else if ("MIN".equalsIgnoreCase(stringValue)) {
            return Integer.MIN_VALUE;
        } else {
            return Integer.parseInt(stringValue);
        }
    }

    @Nullable
    protected Element loadDataFile(File file, boolean strict) {
        if (!file.exists()) {
            if (strict) {
                throw new RuntimeException("File '" + file.getAbsolutePath() + "' not found");
            } else {
                log.error("File '{}' not found", file.getAbsolutePath());
                return null;
            }
        }

        if (!"xml".equalsIgnoreCase(FilenameUtils.getExtension(file.getName()))) {
            if (strict) {
                throw new RuntimeException("File '" + file.getAbsolutePath() + "' is not XML");
            } else {
                log.error("File '{}' is not XML", file.getAbsolutePath());
                return null;
            }
        }

        Document document = parseDocument(file);
        Element rootElement = document.getRootElement();
        if (!rootElement.getName().equalsIgnoreCase("data")) {
            if (strict) {
                throw new RuntimeException("Incorrect structure of file '" + file.getAbsolutePath() + "'");
            } else {
                log.error("Incorrect structure of file '{}'", file.getAbsolutePath());
            }
        }
        return rootElement;
    }

    protected Document parseDocument(File file) {
        return XmlUtils.readDocument(file, saxReader);
    }

    protected interface ThresholdItemGenerator<V> {
        ThresholdItem<Integer, V> createItem(String name, Range<Integer> range, V outputValue, int order);
    }
}
