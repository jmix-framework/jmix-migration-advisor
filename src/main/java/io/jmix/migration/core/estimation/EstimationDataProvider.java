package io.jmix.migration.core.estimation;

import io.jmix.migration.core.estimation.BigDecimalThresholdItem;
import io.jmix.migration.core.estimation.IntegerThresholdItem;
import io.jmix.migration.core.estimation.ThresholdItem;
import io.jmix.migration.core.scan.XmlUtils;
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
import java.io.InputStream;
import java.math.BigDecimal;
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

    public static final String CUBA_PROFILE = "cuba";

    private static final String PROFILE_RESOURCE_TEMPLATE = "/estimation/estimation-data-%s.xml";

    private static final String THRESHOLDS_XPATH_TEMPLATE = "./estimation-unit[name = '%s']/content/thresholds/threshold";
    private static final String SIMPLE_COST_VALUE_XPATH_TEMPLATE = "./estimation-unit[name = '%s']/content/cost";

    protected final SAXReader saxReader;

    // Units are read lazily by name: each profile bundles only the units its estimator uses,
    // and a missing unit fails on access with the unit name in the message
    private Element defaultRootElement;
    private Element externalRootElement;

    public EstimationDataProvider() {
        this(null);
    }

    public EstimationDataProvider(@Nullable String externalFileName) {
        this(CUBA_PROFILE, externalFileName);
    }

    /**
     * @param profile          name of the bundled estimation weights profile (one per source platform,
     *                         resource {@code /estimation/estimation-data-<profile>.xml})
     * @param externalFileName optional user-provided file overriding profile values
     */
    public EstimationDataProvider(String profile, @Nullable String externalFileName) {
        this.saxReader = XmlUtils.getSaxReader();
        initData(PROFILE_RESOURCE_TEMPLATE.formatted(profile), externalFileName);
    }

    /**
     * Plain cost value of the estimation unit.
     */
    public int getCost(String unitName) {
        return extractSingleIntValue(defaultRootElement, externalRootElement, createSimpleCostXPathExpression(unitName));
    }

    /**
     * Threshold list of the estimation unit with integer output values.
     */
    public List<? extends ThresholdItem<Integer, Integer>> getIntThresholds(String unitName) {
        return extractThresholds(defaultRootElement, externalRootElement,
                createThresholdXPathExpression(unitName), this::stringToInt, IntegerThresholdItem::new);
    }

    /**
     * Threshold list of the estimation unit with decimal (hours) output values.
     */
    public List<? extends ThresholdItem<Integer, BigDecimal>> getDecimalThresholds(String unitName) {
        return extractThresholds(defaultRootElement, externalRootElement,
                createThresholdXPathExpression(unitName), BigDecimal::new, BigDecimalThresholdItem::new);
    }

    public int getScreenChangedUiComponentsComplexityBaseValue() {
        return getCost("screen-changed-ui-components-complexity-base-value");
    }

    public int getScreenDescriptorExtendsScreenComplexityScore() {
        return getCost("screen-descriptor-extends-screen-complexity-score");
    }

    public int getScreenDescriptorHasNestedDataItemComplexityScore() {
        return getCost("screen-descriptor-has-nested-data-item-complexity-score");
    }

    public int getScreenDescriptorUiComponentCreateCallComplexityScore() {
        return getCost("screen-descriptor-ui-component-create-call-complexity-score");
    }

    public int getInitialMigrationCost() {
        return getCost("initial-migration-cost");
    }

    public int getBaseEntitiesMigrationCost() {
        return getCost("base-entities-migration-cost");
    }

    public int getLegacyEntityListenerCost() {
        return getCost("legacy-entity-listener-cost");
    }

    public List<? extends ThresholdItem<Integer, Integer>> getScreenControllerMethodsCallsComplexityThresholds() {
        return getIntThresholds("screen-controller-method-calls");
    }

    public List<? extends ThresholdItem<Integer, BigDecimal>> getScreenComplexityTimeEstimationThresholds() {
        return getDecimalThresholds("screen-complexity-time-estimation");
    }

    protected void initData(String profileResource, String externalFileName) {
        InputStream defaultFileResourceStream = EstimationDataProvider.class.getResourceAsStream(profileResource);
        if (defaultFileResourceStream == null) {
            throw new RuntimeException("Estimation data profile resource is not found: " + profileResource);
        }

        if (StringUtils.isNotEmpty(externalFileName)) {
            // The file is explicitly requested by the user: failing to load it must abort the run
            // instead of silently producing a report based on default weights
            File externalFile = new File(externalFileName);
            this.externalRootElement = loadDataFile(externalFile, true);
        }

        this.defaultRootElement = loadDataFile(defaultFileResourceStream, true);
        if (defaultRootElement == null) {
            throw new RuntimeException("Estimation data profile '" + profileResource + "' was not loaded");
        }
    }

    protected int extractSingleIntValue(Element defaultRootElement, Element externalRootElement, String xpath) {
        String stringValue = null;
        if (externalRootElement != null) {
            stringValue = extractSingleStringValue(externalRootElement, xpath);
            if (StringUtils.isBlank(stringValue)) {
                log.warn("No value found in external estimation data file by xpath \"{}\", default value is used", xpath);
            }
        }
        if (StringUtils.isBlank(stringValue)) {
            stringValue = extractSingleStringValue(defaultRootElement, xpath);
        }
        if (StringUtils.isBlank(stringValue)) {
            throw new RuntimeException("No data found by xpath: " + xpath);
        }
        return stringToInt(stringValue);
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
            if (thresholdItemNodes == null || thresholdItemNodes.isEmpty()) {
                log.warn("No thresholds found in external estimation data file by xpath \"{}\", default values are used", xpath);
            }
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
        validateEstimationDataElement(rootElement, strict);
        return rootElement;
    }

    @Nullable
    protected Element loadDataFile(InputStream inputStream, boolean strict) {
        Document document = parseDocument(inputStream);
        Element rootElement = document.getRootElement();
        validateEstimationDataElement(rootElement, strict);
        return rootElement;
    }

    protected void validateEstimationDataElement(Element rootElement, boolean strict) {
        if (!rootElement.getName().equalsIgnoreCase("data")) {
            if (strict) {
                throw new RuntimeException("Incorrect structure of file");
            } else {
                log.error("Incorrect structure of file");
            }
        }
    }

    protected Document parseDocument(File file) {
        return XmlUtils.readDocument(file, saxReader);
    }

    protected Document parseDocument(InputStream inputStream) {
        return XmlUtils.readDocument(inputStream, saxReader);
    }

    protected interface ThresholdItemGenerator<V> {
        ThresholdItem<Integer, V> createItem(String name, Range<Integer> range, V outputValue, int order);
    }
}
