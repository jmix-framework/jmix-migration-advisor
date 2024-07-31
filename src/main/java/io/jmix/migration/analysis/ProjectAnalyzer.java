package io.jmix.migration.analysis;

import io.jmix.migration.analysis.estimation.*;
import io.jmix.migration.analysis.issue.UiComponentIssue;
import io.jmix.migration.analysis.issue.UiComponentIssuesRegistry;
import io.jmix.migration.analysis.parser.ScreensCollector;
import io.jmix.migration.model.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static java.time.temporal.ChronoField.*;

public class ProjectAnalyzer {

    private static final Logger log = LoggerFactory.getLogger(ProjectAnalyzer.class);

    public static final String MODULES_DIR = "modules";
    public static final String CORE_MODULE_DIR = "core";
    public static final String GLOBAL_MODULE_DIR = "global";
    public static final String WEB_MODULE_DIR = "web";
    public static final String GUI_MODULE_DIR = "gui";
    public static final String SRC_DIR = "src";

    private final UiComponentIssuesRegistry uiComponentIssuesRegistry = UiComponentIssuesRegistry.create();
    private final EstimationDataProvider estimationDataProvider = new EstimationDataProvider(); //todo provide external file
    private final ScreenEstimator screenEstimator = new ScreenEstimator(uiComponentIssuesRegistry, estimationDataProvider);
    private final ScreenTimeEstimator screenTimeEstimator = new ScreenTimeEstimator(estimationDataProvider.getScreenComplexityTimeEstimationThresholds());

    public void analyzeProject(String projectPathString, String basePackage) {
        if (StringUtils.isBlank(projectPathString)) {
            throw new RuntimeException("No project path is specified");
        }

        Path projectPath = Path.of(projectPathString).toAbsolutePath().normalize();
        log.info("---=== Start analyze project ===---");
        log.info("Project path = '{}', Base package = '{}'", projectPath, basePackage);

        // Core module
        Path coreRootPath = projectPath.resolve(MODULES_DIR).resolve(CORE_MODULE_DIR);
        Path coreSrcPath = coreRootPath.resolve(SRC_DIR);
        CoreModuleAnalyzer coreModuleAnalyzer = new CoreModuleAnalyzer(coreRootPath, coreSrcPath, basePackage);
        CoreModuleAnalysisResult coreModuleAnalysisResult = coreModuleAnalyzer.analyzeCoreModule();

        // Global module
        Path globalRootPath = projectPath.resolve(MODULES_DIR).resolve(GLOBAL_MODULE_DIR);
        Path globalSrcPath = globalRootPath.resolve(SRC_DIR);
        GlobalModuleAnalyzer globalModuleAnalyzer = new GlobalModuleAnalyzer(globalSrcPath, basePackage);
        GlobalModuleAnalysisResult globalModuleAnalysisResult = globalModuleAnalyzer.analyzeGlobalModule();


        // UI modules
        Path webSrcPath = projectPath.resolve(MODULES_DIR).resolve(WEB_MODULE_DIR).resolve(SRC_DIR);
        Path guiSrcPath = projectPath.resolve(MODULES_DIR).resolve(GUI_MODULE_DIR).resolve(SRC_DIR);
        UiModulesAnalyzer uiModulesAnalyzer = new UiModulesAnalyzer(webSrcPath, guiSrcPath, basePackage);
        UiModulesAnalysisResult uiModulesAnalysisResult = uiModulesAnalyzer.analyzeUiModules();
        ScreensCollector screensCollector = uiModulesAnalysisResult.getScreensCollector();

        log.info("\n\n-----===== RESULTS =====-----");

        Set<String> screenDescriptors = screensCollector.getScreensByDescriptors().keySet();
        Set<String> screenIds = screensCollector.getScreensByIds().keySet();
        Set<String> screenControllers = screensCollector.getScreensByControllers().keySet();
        log.info("General: Screens: With ID={}, with descriptors={}, with controllers={}", screenIds.size(), screenDescriptors.size(), screenControllers.size());

        log.info("\n\n Details");

        /*log.info("\n\n-= Screens by Descriptors =-");
        Map<String, ScreenInfo> screensByDescriptors = screensCollector.getScreensByDescriptors();
        screensByDescriptors.values().forEach(this::printScreenInfo);*/

        /*log.info("\n\n-= Screens by ID =-");
        Map<String, ScreenInfo> screensByIds = screensCollector.getScreensByIds();
        screensByIds.values().forEach(this::printScreenInfo);

        log.info("\n\n-= Screens by Controllers =-");
        Map<String, ScreenInfo> screensByControllers = screensCollector.getScreensByControllers();
        screensByControllers.values().forEach(this::printScreenInfo);*/

        log.info("\n\n-----===== TOTALS =====-----");
        ScreensTotalInfo screensTotalInfo = createScreensTotalInfo(screensCollector);
        log.info("Screens (all types): {}", screensTotalInfo.getScreens() + screensTotalInfo.getLegacyScreens() + screensTotalInfo.getFragments());
        log.info("Legacy screens: {}", screensTotalInfo.getLegacyScreens());
        log.info("Non-legacy screens: {}", screensTotalInfo.getScreens());
        log.info("Fragments: {}", screensTotalInfo.getFragments());
        log.info("Facets: {}", screensTotalInfo.getFacets());
        Map<String, Integer> uiComponents = screensTotalInfo.getUiComponents();
        StringBuilder uiCompSb = new StringBuilder("UiComponents:");
        uiComponents.forEach((name, count) -> {
            uiCompSb.append("\n").append(name).append(": ").append(count);
        });
        log.info(uiCompSb.toString());

        //screenEstimator.estimate(screensCollector);

        ProjectEstimationResult projectEstimationResult = estimateProject(coreModuleAnalysisResult, globalModuleAnalysisResult, uiModulesAnalysisResult);
        generateReport(projectEstimationResult);
    }

    protected void generateReport(ProjectEstimationResult result) {
        /*Configuration configuration = new Configuration(Configuration.VERSION_2_3_23);
        configuration.setDefaultEncoding("UTF-8");
        configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        configuration.setLogTemplateExceptions(false);

        configuration.setClassForTemplateLoading(CliRunner.class, "/templates");


        Map<String, Object> data = new HashMap<>();
        List<Map<String, Object>> groups;
        List<Map.Entry<ScreenComplexityGroup, List<String>>> entries = new ArrayList<>(result.getScreensPerGroup().entrySet());
        data.put("userDetails", model.getUserDetails());

        try {
            Template template = configuration.getTemplate("commonTemplate.ftl");

            // Console output
            Writer out = new OutputStreamWriter(System.out);
            template.process(prepareData(), out);
            out.flush();

        } catch (IOException | TemplateException e) {
            throw new RuntimeException(e);
        }*/

        DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                .append(DateTimeFormatter.ISO_LOCAL_DATE)
                .appendLiteral('T')
                .appendValue(HOUR_OF_DAY, 2)
                //.appendLiteral(':')
                .appendValue(MINUTE_OF_HOUR, 2)
                //.optionalStart()
                //.appendLiteral(':')
                .appendValue(SECOND_OF_MINUTE, 2)
                //.optionalStart()
                .appendFraction(MILLI_OF_SECOND, 0, 3, false)
                .toFormatter();

        String fileName = "results_" + LocalDateTime.now().format(formatter) + ".txt";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true))) {
            writer.write("General");
            writer.newLine();
            writer.newLine();

            writer.write("Entities: " + result.getEntitiesAmount());

            Map<ThresholdItem<Integer, BigDecimal>, List<String>> screensPerGroup = result.getScreensPerComplexity();
            List<ThresholdItem<Integer, BigDecimal>> groups = new ArrayList<>(screensPerGroup.keySet());
            groups.sort(Comparator.comparingInt(ThresholdItem::getOrder));

            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(System.lineSeparator()).append(System.lineSeparator())
                    .append("Screens complexity groups:");
            groups.forEach(group -> {
                int amount = screensPerGroup.get(group).size();
                stringBuilder.append(System.lineSeparator()).append("\t")
                        .append(group.getName())
                        .append(": Amount=").append(amount)
                        .append(", Cost=").append(group.getOutputValue())
                        .append(", Total=").append(group.getOutputValue().multiply(BigDecimal.valueOf(amount)));
            });
            writer.write(stringBuilder.toString());


            stringBuilder.delete(0, stringBuilder.length());
            stringBuilder.append(System.lineSeparator()).append(System.lineSeparator()).append("UiComponents");
            List<String> components = new ArrayList<>(result.getAllUiComponents().keySet());
            components.sort(String::compareTo);
            components.forEach(component -> {
                UiComponentIssue issue = uiComponentIssuesRegistry.getIssue(component);
                if(issue != null) {
                    stringBuilder.append(System.lineSeparator()).append("\t")
                            .append(issue.getComponent()).append(": ").append(issue.getNotes());
                }
            });
            writer.write(stringBuilder.toString());




        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected ProjectEstimationResult estimateProject(CoreModuleAnalysisResult coreModuleAnalysisResult,
                                                      GlobalModuleAnalysisResult globalModuleAnalysisResult,
                                                      UiModulesAnalysisResult uiModulesAnalysisResult) {
        ScreensCollector screensCollector = uiModulesAnalysisResult.getScreensCollector();
        Map<String, ScreenComplexityScore> screenScores = screenEstimator.estimate(screensCollector);
        StringBuilder screenScoresSb = new StringBuilder("Screens Scores:");
        Map<ThresholdItem<Integer, BigDecimal>, List<String>> screensPerGroup = new HashMap<>();
        BigDecimal screenSumHours = screenScores.entrySet().stream().map(entry -> {
            String name = entry.getKey();
            ScreenComplexityScore score = entry.getValue();
            ThresholdItem<Integer, BigDecimal> complexityThreshold = screenTimeEstimator.estimate(score);

            List<String> screensInGroup = screensPerGroup.computeIfAbsent(complexityThreshold, key -> new ArrayList<>());
            screensInGroup.add(name);

            screenScoresSb.append("\nScreen: '").append(name).append("'. Score: ").append(score.getValue()).append(". Hours: ").append(complexityThreshold.getOutputValue());
            return complexityThreshold.getOutputValue();
        }).reduce(BigDecimal::add).orElse(new BigDecimal("0"));

        screenScoresSb.append("\n\nTotal screens hours: ").append(screenSumHours);

        screensPerGroup.forEach((complexityThreshold, screens) -> {
            screenScoresSb
                    .append("\n\nGroup: ").append(complexityThreshold.getName())
                    .append(", Cost: ").append(complexityThreshold.getOutputValue())
                    .append(", Amount: ").append(screens.size())
                    .append(", TotalCost: ").append(complexityThreshold.getOutputValue().multiply(BigDecimal.valueOf(screens.size())));
        });

        log.info("{}", screenScoresSb);

        ScreensTotalInfo screensTotalInfo = createScreensTotalInfo(screensCollector);

        ProjectEstimationResult.Builder resultBuilder = ProjectEstimationResult.builder();
        ProjectEstimationResult result = resultBuilder
                .setInitialMigrationCost(100) // todo rule based on amount of entities
                .setBaseEntitiesMigrationCost(16) //todo rule
                .setScreensPerComplexity(screensPerGroup)
                .setEntitiesPerPersistenceUnit(globalModuleAnalysisResult.getEntitiesPerPersistenceUnit())
                .setAllUiComponents(screensTotalInfo.getUiComponents())
                .build();
        return result;
    }

    protected ScreensTotalInfo createScreensTotalInfo(ScreensCollector screensCollector) {
        ScreensTotalInfo screensTotalInfo = new ScreensTotalInfo();
        AtomicInteger legacyScreensCounter = new AtomicInteger();
        AtomicInteger screensCounter = new AtomicInteger();
        AtomicInteger fragmentsCounter = new AtomicInteger();
        Map<String, Integer> totalUiComponents = new HashMap<>();
        Map<String, Integer> totalFacets = new HashMap<>();

        Collection<ScreenInfo> screenInfos = screensCollector.getScreensByDescriptors().values();
        screenInfos.forEach(screenInfo -> {
            List<Facet> facets = screenInfo.getFacets();
            if (facets != null) {
                facets.forEach(facet -> {
                    Integer facetCount = totalFacets.getOrDefault(facet.getName(), 0);
                    facetCount++;
                    totalFacets.put(facet.getName(), facetCount);
                });
            }

            List<LayoutItem> layoutItems = Optional.ofNullable(screenInfo.getLayout())
                    .map(Layout::getAllItems)
                    .orElse(Collections.emptyList());
            layoutItems.forEach(layoutItem -> {
                Integer uiComponentCounter = totalUiComponents.getOrDefault(layoutItem.getName(), 0);
                uiComponentCounter += layoutItem.getQuantity();
                totalUiComponents.put(layoutItem.getName(), uiComponentCounter);
            });

            if (screenInfo.isLegacy()) {
                legacyScreensCounter.incrementAndGet();
            } else {
                if (screenInfo.isFragment()) {
                    fragmentsCounter.incrementAndGet();
                } else {
                    screensCounter.incrementAndGet();
                }
            }
        });

        screensTotalInfo.setScreens(screensCounter.get());
        screensTotalInfo.setFragments(fragmentsCounter.get());
        screensTotalInfo.setLegacyScreens(legacyScreensCounter.get());
        screensTotalInfo.setFacets(totalFacets);
        screensTotalInfo.setUiComponents(totalUiComponents);

        return screensTotalInfo;
    }

    protected void printScreenInfo(ScreenInfo screenInfo) {
        boolean legacy = screenInfo.isLegacy();
        String screenId = screenInfo.getScreenId();
        String descriptorFile = screenInfo.getDescriptorFile();
        String controllerClass = screenInfo.getControllerClass();

        log.info("\n\nScreen: Descriptor = {}, ID = {}, Controller = {}, Legacy = {}", descriptorFile, screenId, controllerClass, legacy);

        StringBuilder screenDataSb = new StringBuilder();
        ScreenData screenData = screenInfo.getScreenData();
        if (screenData == null) {
            screenDataSb.append("NONE");
        } else {
            List<ScreenDataItem> screenDataItems = screenData.getItems();
            screenDataItems.forEach(item -> screenDataSb.append("\n").append(item));
        }
        log.info("Screen data: {}", screenDataSb);

        StringBuilder screenFacetsSb = new StringBuilder();
        List<Facet> facets = screenInfo.getFacets();
        if (facets == null) {
            screenFacetsSb.append("NONE");
        } else {
            facets.forEach(facet -> screenFacetsSb.append("\n").append(facet));
        }
        log.info("Screen facets: {}", screenFacetsSb);

        StringBuilder screenLayoutSb = new StringBuilder();
        Layout layout = screenInfo.getLayout();
        if (layout == null) {
            screenLayoutSb.append("NONE");
        } else {
            List<LayoutItem> allLayoutItems = layout.getAllItems();
            allLayoutItems.forEach(item -> screenLayoutSb.append("\n").append(item));
        }
        log.info("Screen layout: {}", screenLayoutSb);

        log.info("---=== ESTIMATION ===---");
        //screenEstimator.estimate(screenInfo);
    }



    /*public void visitProject(String projectPathString, String basePackage) {
        if (StringUtils.isBlank(projectPathString)) {
            throw new RuntimeException("No project path is specified");
        }

        Path projectPath = Path.of(projectPathString).toAbsolutePath().normalize();
        log.info("Project path = '{}', Base package = '{}'", projectPath, basePackage);

        // web module
        Path webSrcPath = projectPath.resolve(MODULES_DIR).resolve(WEB_MODULE_DIR).resolve(SRC_DIR);

        //todo resolve web-screens.xml
        String[] split = basePackage.split("\\.");
        Path basePackagePath = Path.of(webSrcPath.toString(), split);
        Path webScreensFilePath = getWebScreensXmlFilePath(basePackagePath);

        ScreensCollector screensCollector = new ScreensCollector();
        processWebScreensXml(screensCollector, webScreensFilePath);

        try {
            Files.walkFileTree(webSrcPath, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    log.info("START VISIT FILE: {}", file);

                    if (isDirectory(file)) {
                        log.info("`{}` is directory", file);
                        return FileVisitResult.CONTINUE;
                    }

                    if (isXmlFile(file)) {
                        log.info("`{}` is XML file", file);
                        processXmlFile(webSrcPath, file, screensCollector);
                        return FileVisitResult.CONTINUE;
                    }

                    if (isJavaSourceFile(file)) {
                        log.info("`{}` is source file", file);
                        processJavaFile(webSrcPath, file, screensCollector);
                        return FileVisitResult.CONTINUE;
                    }

                    log.info("`{}` is something else", file); //todo
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }*/

    /*protected Path getWebScreensXmlFilePath(Path basePackagePath) {
        //todo check 'web-screens.xml' location property
        return Path.of(basePackagePath.toString(), "web-screens.xml");
    }

    protected void processWebScreensXml(ScreensCollector screensCollector, Path webScreensFilePath) {
        File file = webScreensFilePath.toFile();
        if (!file.exists()) {
            throw new IllegalArgumentException("web-screens.xml file not found by path: " + webScreensFilePath);
        }

        if (file.isDirectory()) {
            throw new IllegalArgumentException(webScreensFilePath + " is not a file");
        }

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(file);
            Element rootElement = document.getDocumentElement();
            String tagName = rootElement.getTagName();
            boolean isWebScreens = "screen-config".equals(tagName);
            if (isWebScreens) {
                XPath xPath = xPathFactory.newXPath();
                try {
                    NodeList screenNodeList = (NodeList) xPath.evaluate(
                            "/screen-config/screen",
                            rootElement, XPathConstants.NODESET);
                    log.info("Find screen nodes [{}]: {}", screenNodeList.getLength(), screenNodeList);

                    for (int i = 0; i < screenNodeList.getLength(); i++) {
                        Node screenNode = screenNodeList.item(i);
                        if (isElementNode(screenNode)) {
                            Element screenElement = (Element) screenNode;
                            String id = screenElement.getAttribute("id");
                            String descriptor = screenElement.getAttribute("template");
                            log.info("Screen item '{}' ({}):, id = {}, descriptor = {}", screenElement.getNodeName(), screenElement.getTagName(), id, descriptor);

                            screensCollector.initLegacyScreenInfo(id, descriptor);
                        }
                    }
                } catch (XPathExpressionException e) {
                    throw new RuntimeException(e);
                }
            }

        } catch (ParserConfigurationException | IOException | SAXException e) {
            throw new RuntimeException(e);
        }

    }

    protected void processXmlFile(Path webSrcPath, Path filePath, ScreensCollector screensCollector) {
        //todo separate processor
        log.info("[Process XML file] File={}, WebScr={}", filePath, webSrcPath);

        File file = filePath.toFile();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(file);
            Element rootElement = document.getDocumentElement();

            if (isScreenDescriptor(rootElement)) {


                //ScreenInfo screenInfo = new ScreenInfo();

                boolean isLegacy = false;
                if (rootElement.hasAttribute("class")) {
                    isLegacy = true; //todo get legacy flag from ScreenInfo?
                }
                log.info("File '{}' is a {} screen descriptor", filePath, isLegacy ? "legacy" : "'Screen API'");

                Path relativeFilePath = webSrcPath.relativize(filePath);
                String relativeFilePathString = relativeFilePath.toString();
                relativeFilePathString = relativeFilePathString.replace("\\", "/");
                log.info("Relative file path = {} -> {}", relativeFilePath, relativeFilePathString);

                ScreenInfo screenInfo;
                if (isLegacy) {
                    // Legacy screen info should be initialized via web-screens.xml analysis
                    screenInfo = screensCollector.getScreenInfoByDescriptor(relativeFilePathString);
                    if (screenInfo == null) {
                        log.error("[ERROR] screenInfo not found for legacy screen '{}'", relativeFilePathString);
                        throw new RuntimeException("Screen Info is null");
                    }
                    String screenControllerClass = rootElement.getAttribute("class");
                    screenInfo.setControllerClass(screenControllerClass);
                } else {
                    screenInfo = screensCollector.initScreenByXmlDescriptor(relativeFilePathString);
                    if (screenInfo == null) {
                        throw new RuntimeException("Screen Info is null");
                    }
                }

                //screenInfo.setDescriptorFile(filePath.toString());

                ScreenData screenData;
                if (isLegacy) {
                    screenData = processLegacyDsContext(rootElement);
                } else {
                    screenData = processScreenData(rootElement);
                }

                screenInfo.setScreenData(screenData);

                // Facets
                List<Facet> facets = processFacets(rootElement);
                screenInfo.setFacets(facets);

                // Layout
                Layout layout = processLayout(rootElement);
                screenInfo.setLayout(layout);
            }

        } catch (ParserConfigurationException | IOException | SAXException e) {
            throw new RuntimeException(e); //todo
        }
    }

    protected boolean isDirectory(Path file) {
        return Files.isDirectory(file);
    }

    protected boolean isJavaSourceFile(Path file) {
        String extension = getFileExtension(file);
        return "java".equalsIgnoreCase(extension);
    }

    protected boolean isXmlFile(Path file) {
        String extension = getFileExtension(file);
        return "xml".equalsIgnoreCase(extension);
    }*/

    /*protected String getFileExtension(Path file) {
        String fileName = file.getFileName().toString();
        String extension = FilenameUtils.getExtension(fileName);
        log.info("{}: FileName={}, Extension={}", file, fileName, extension);
        return extension;
    }

    protected boolean isScreenDescriptor(Element element) {
        String tagName = element.getTagName();
        return "window".equals(tagName); //todo additional checks?
    }

    protected ScreenData processScreenData(Element rootElement) {
        NodeList dataNodeList = getChildElementsByTag(rootElement, "data"); //todo move to upper level
        ScreenData screenData = new ScreenData();
        if (dataNodeList.getLength() > 0) {
            Element dataElement = (Element) dataNodeList.item(0);
            NodeList dataChildNodes = dataElement.getChildNodes();
            log.info("Data has {} elements", dataChildNodes.getLength());
            for (int i = 0; i < dataChildNodes.getLength(); i++) {
                Node dataChildNode = dataChildNodes.item(i);
                if (isElementNode(dataChildNode)) {
                    log.info("Data item: {}", dataChildNode.getNodeName());
                    screenData.addItem(new ScreenDataItem(dataChildNode.getNodeName()));
                }

            }
            //todo nested
        }
        return screenData;
    }

    protected ScreenData processLegacyDsContext(Element rootElement) {
        ScreenData screenData = new ScreenData();
        Element dsContextElement = getSingleChildElement(rootElement, "dsContext"); //todo move to upper level
        if(dsContextElement != null) {
            NodeList dsContextChildNodes = dsContextElement.getChildNodes();
            log.info("dsContext has {} elements", dsContextChildNodes.getLength());
            for (int i = 0; i < dsContextChildNodes.getLength(); i++) {
                Node dsContextChildNode = dsContextChildNodes.item(i);
                if (isElementNode(dsContextChildNode)) {
                    log.info("dsContext item: {}", dsContextChildNode.getNodeName());
                    screenData.addItem(new ScreenDataItem(dsContextChildNode.getNodeName()));
                }
                //todo nested
            }
        }
        return screenData;
    }

    protected List<Facet> processFacets(Element rootElement) {
        Element facetsElement = getSingleChildElement(rootElement, "facets");
        if(facetsElement != null) {
            NodeList facetsChildNodes = facetsElement.getChildNodes();
            log.info("Facets has {} elements", facetsChildNodes.getLength());
            List<Facet> facets = new ArrayList<>();
            for (int i = 0; i < facetsChildNodes.getLength(); i++) {
                Node facetsChildNode = facetsChildNodes.item(i);
                if (isElementNode(facetsChildNode)) {
                    log.info("Facets item: {}", facetsChildNode.getNodeName());
                    facets.add(new Facet(facetsChildNode.getNodeName()));
                }
            }
            return facets;
        } else {
            return Collections.emptyList();
        }
    }

    protected Layout processLayout(Element rootElement) {
        NodeList layoutNodeList = getChildElementsByTag(rootElement, "layout");
        log.info("layout nodes: {}", layoutNodeList.getLength());
        Layout layout = new Layout();
        if (layoutNodeList.getLength() > 0) {
            Element layoutElement = (Element) layoutNodeList.item(0);
            NodeList layoutChildNodes = layoutElement.getChildNodes();
            for (int i = 0; i < layoutChildNodes.getLength(); i++) {
                Node layoutChildNode = layoutChildNodes.item(i);
                if (isElementNode(layoutChildNode)) {
                    processLayoutItem(layout, (Element) layoutChildNode);
                }
            }
        }
        return layout;
    }

    protected void processLayoutItem(Layout layout, Element layoutItemElement) {
        String nodeName = layoutItemElement.getNodeName();
        log.info("Layout item: {}", nodeName);
        layout.putItem(nodeName);

        if (!layoutItemElement.hasChildNodes()) {
            return;
        }

        if (isSimpleContainerLayoutItem(nodeName)) {
            log.info("'{}' is container component", nodeName);
            NodeList childNodes = layoutItemElement.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); i++) {
                Node layoutChildNode = childNodes.item(i);
                if (isElementNode(layoutChildNode)) {
                    processLayoutItem(layout, (Element) layoutChildNode);
                }
            }
            log.info("Finish processing container component '{}'", nodeName);
        } else if (isTabbedContainerLayoutItem(nodeName)) {
            log.info("'{}' is tabbed component", nodeName);
            NodeList tabs = getChildElementsByTag(layoutItemElement, "tab");
            log.info("Tabbed component '{}' has {} tabs", nodeName, tabs.getLength());
            for (int t = 0; t < tabs.getLength(); t++) {
                Node tabNode = tabs.item(t);
                if (isElementNode(tabNode)) {
                    NodeList tabChildNodes = tabNode.getChildNodes();
                    log.info("Process tabNode with {} child items", tabChildNodes.getLength());
                    for (int i = 0; i < tabChildNodes.getLength(); i++) {
                        Node tabChildNode = tabChildNodes.item(i);
                        if (isElementNode(tabChildNode)) {
                            log.info("Tab child node: {}", tabChildNode);
                            processLayoutItem(layout, (Element) tabChildNode);
                        }
                    }
                }
            }
            log.info("Finish processing tabbed component '{}'", nodeName);
        }
    }

    protected void processJavaFile(Path webSrcPath, Path filePath, ScreensCollector screensCollector) {
        log.info("[Process Java file] File={}, WebScr={}", filePath, webSrcPath);

        File file = filePath.toFile();

        Path fileRelativeFullPath = webSrcPath.relativize(filePath);
        log.info("fileRelativeFullPath={}", fileRelativeFullPath);
        String fileRelativeFullPathNoExt = FilenameUtils.removeExtension(fileRelativeFullPath.toString());
        log.info("fileRelativeFullPathNoExt={}", fileRelativeFullPathNoExt);
        String firstReplace = fileRelativeFullPathNoExt.replace("\\", ".");
        log.info("firstReplace={}", firstReplace);
        String secondReplace = firstReplace.replace("/", ".");
        log.info("secondReplace={}", secondReplace);

        String className = secondReplace;

        log.info("Class name generated from file: {}", className);

        ScreenInfo screenInfoByController = screensCollector.getScreenInfoByController(className);
        log.info("ScreenInfo by controller class '{}': {}", className, screenInfoByController);
        if (screenInfoByController != null) {
            boolean legacy = screenInfoByController.isLegacy();
            log.info("Screen info exists. legacy = {}", legacy);
            if (legacy) {
                //todo handle this case if some data from source code need to be collected
                return;
            }
        }

        //todo check if has legacy screen info for this class

        Pattern packagePattern = Pattern.compile("^package ([\\w|.]*);$");
        Pattern uiControllerPattern = Pattern.compile("^@UiController\\(\"(.+)\"\\)$");
        Pattern uiDescriptorPattern = Pattern.compile("^@UiDescriptor\\(\"(.+)\"\\)$");

        boolean packageFound = false;
        boolean uiControllerFound = false;
        boolean uiDescriptorFound = false;

        String packageValue = null;
        String descriptorLocalName = null;
        String screenId = null;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (packageFound && uiDescriptorFound && uiControllerFound) { //todo
                    log.info("Collected all info from this file - break");
                    break;
                }

                // package
                if (!packageFound) {
                    Matcher packageMatcher = packagePattern.matcher(line);
                    if (packageMatcher.find()) {
                        packageValue = packageMatcher.group(1);
                        packageFound = true;
                    }
                }

                // @UiDescriptor
                if (!uiDescriptorFound) {
                    Matcher uiDescriptorMatcher = uiDescriptorPattern.matcher(line);
                    if (uiDescriptorMatcher.find()) {
                        descriptorLocalName = uiDescriptorMatcher.group(1); //todo check if it is absolute path
                        uiDescriptorFound = true;
                    }
                }

                // @UiController
                if (!uiControllerFound) {
                    Matcher uiControllerMatcher = uiControllerPattern.matcher(line);
                    if (uiControllerMatcher.find()) {
                        screenId = uiControllerMatcher.group(1);
                        uiControllerFound = true;
                    }
                }
            }

            if (packageFound && uiDescriptorFound && uiControllerFound) {
                // Screens API screen controller
                String descriptorFullName = packageValue.replace(".", "/") + "." + descriptorLocalName;
                String fileName = FilenameUtils.removeExtension(file.getName());
                String controllerClassName = packageValue + "." + fileName;
                log.info("Processing screen controller class: id = {}, descriptor = {}, controller ={}", screenId, descriptorFullName, controllerClassName);


                ScreenInfo screenInfo = screensCollector.getScreenInfoByDescriptor(descriptorFullName);
                if (screenInfo == null) {
                    log.info("Screen info NOT FOUND by descriptor '{}'", descriptorFullName);
                    screensCollector.initScreenInfo(screenId, descriptorFullName, controllerClassName);
                } else {
                    log.info("Screen info FOUND by descriptor '{}'", descriptorFullName);
                    screenInfo.setScreenId(screenId);
                    screenInfo.setControllerClass(controllerClassName);
                }
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    protected boolean isElementNode(Node node) {
        return node != null && node.getNodeType() == Node.ELEMENT_NODE;
    }

    protected boolean isSimpleContainerLayoutItem(String name) {
        Set<String> tags = Stream.of("buttonsPanel", "cssLayout", "flowBox", "groupBox", "hbox", "vbox", "scrollBox", "split", "htmlBox")
                .collect(Collectors.toUnmodifiableSet());

        return tags.contains(name);
    }

    protected boolean isTabbedContainerLayoutItem(String name) {
        Set<String> tags = Stream.of("accordion", "tabSheet").collect(Collectors.toUnmodifiableSet());

        return tags.contains(name);
    }

    protected NodeList getChildElementsByTag(Element currentElement, String childTag) {
        XPath xPath = xPathFactory.newXPath();
        try {
            String expression = "./" + childTag;
            log.debug("[XPATH] try to evaluate elements within '{}' by expression '{}'", currentElement.getNodeName(), expression);
            return (NodeList) xPath.evaluate(expression, currentElement, XPathConstants.NODESET);
        } catch (XPathExpressionException e) {
            throw new RuntimeException(e);
        }
    }

    @Nullable
    protected Element getSingleChildElement(Element currentElement, String childTag) {
        return getSingleChildElement(currentElement, childTag, false);
    }*/

    /*@Nullable
    protected Element getSingleChildElement(Element currentElement, String childTag, boolean strict) {
        NodeList childNodes = getChildElementsByTag(currentElement, childTag);
        int amountOfChildNodes = childNodes.getLength();

        if (amountOfChildNodes == 0) {
            return null;
        } else if (amountOfChildNodes > 1 && strict) {
            throw new RuntimeException(
                    String.format(
                            "Unable to get single child: Element '%s' contains more than 1 child element with name '%s' and in strict mode is enabled",
                            currentElement.getNodeName(), childTag
                    )
            );
        } else {
            return (Element) childNodes.item(0);
        }
    }*/
}
