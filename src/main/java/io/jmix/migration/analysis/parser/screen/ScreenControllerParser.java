package io.jmix.migration.analysis.parser.screen;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.Range;
import com.github.javaparser.ast.*;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import io.jmix.migration.analysis.Metrics;
import io.jmix.migration.analysis.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class ScreenControllerParser {

    private static final Logger log = LoggerFactory.getLogger(ScreenControllerParser.class);

    protected final Path moduleSrcPath;
    protected final List<Path> allSrcPaths;
    protected final ScreensCollector screensCollector;
    protected final JavaParser javaParser;

    public ScreenControllerParser(Path moduleSrcPath, List<Path> allSrcPaths, ScreensCollector screensCollector) {
        this.moduleSrcPath = moduleSrcPath;
        this.allSrcPaths = allSrcPaths;
        this.screensCollector = screensCollector;
        this.javaParser = new JavaParser();
    }

    public void parseJavaFile(Path filePath) {
        File file = filePath.toFile();
        if (file.length() == 0) {
            return;
        }

        ParseResult<CompilationUnit> parseResult = parseJavaFile(file);

        if (!parseResult.isSuccessful()) {
            throw new RuntimeException("Java file parsing failed");
        }
        if (parseResult.getResult().isEmpty()) {
            throw new RuntimeException("Parse result is empty");
        }

        processCompilationUnit(parseResult.getResult().get());
    }

    protected ParseResult<CompilationUnit> parseJavaFile(File file) {
        try {
            return javaParser.parse(file);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    protected void processCompilationUnit(CompilationUnit compilationUnit) {
        String packageValue = compilationUnit.getPackageDeclaration()
                .map(PackageDeclaration::getNameAsString)
                .orElseThrow(() -> new RuntimeException("Package not found"));
        log.debug("[Package] {}", packageValue);

        NodeList<ImportDeclaration> importDeclarations = compilationUnit.getImports();
        ImportsHolder importsHolder = ImportsHolder.create(importDeclarations, allSrcPaths);

        Optional<TypeDeclaration<?>> primaryTypeOpt = compilationUnit.getPrimaryType();
        if (primaryTypeOpt.isEmpty()) {
            return;
        }

        TypeDeclaration<?> primaryTypeDeclaration = primaryTypeOpt.get();
        if (primaryTypeDeclaration.isClassOrInterfaceDeclaration()) {
            ClassOrInterfaceDeclaration classDeclaration = primaryTypeDeclaration.asClassOrInterfaceDeclaration();
            processPrimaryClassDeclaration(packageValue, classDeclaration, importsHolder);
        }
    }

    protected void processPrimaryClassDeclaration(String packageValue, ClassOrInterfaceDeclaration classDeclaration, ImportsHolder importsHolder) {
        String classFqn = classDeclaration.getFullyQualifiedName().orElseThrow(() -> new RuntimeException("Unable to get class name"));
        String simpleName = classDeclaration.getName().asString();

        log.debug("[CLASS] {}", classFqn);

        //Extends
        NodeList<ClassOrInterfaceType> extendedTypes = classDeclaration.getExtendedTypes();
        Optional<ClassOrInterfaceType> extendedClassOpt = extendedTypes.getFirst();
        ScreenControllerSuperClassDetails superClassDetails = null;
        if (extendedClassOpt.isPresent()) {
            ClassOrInterfaceType extendedClass = extendedClassOpt.get();
            String extendedClassName = extendedClass.getNameAsString();
            superClassDetails = importsHolder.analyzeSuperClass3(extendedClassName, packageValue);
        }
        boolean isExtendBasicScreenClass = isExtendBasicScreenClass(superClassDetails);

        // Annotations
        String descriptorLocalName = null;
        String screenId = null;

        NodeList<AnnotationExpr> annotations = classDeclaration.getAnnotations();
        for (AnnotationExpr annotationExpr : annotations) {
            Name annotationName = annotationExpr.getName();

            if ("UiDescriptor".equals(annotationName.asString())) {
                log.debug("[UiDescriptor]");
                if (annotationExpr.isSingleMemberAnnotationExpr()) {
                    descriptorLocalName = extractSingleValueAnnotationStringValue(annotationExpr.asSingleMemberAnnotationExpr());
                } else {
                    //TODO not required in common case
                    log.warn("Multi-member annotation is not supported yet: {}", annotationExpr);
                }
            } else if ("UiController".equals(annotationName.asString())) {
                log.debug("[UiController]");
                if (annotationExpr.isSingleMemberAnnotationExpr()) {
                    screenId = extractSingleValueAnnotationStringValue(annotationExpr.asSingleMemberAnnotationExpr());
                } else {
                    //TODO not required in common case
                    log.warn("Multi-member annotation is not supported yet: {}", annotationExpr);
                }
            }
        }

        boolean isControllerClass = false;
        boolean isLegacy = false;
        ScreenInfo screenInfo;
        if (descriptorLocalName != null && screenId != null) {
            // Found Screens API screen controller
            isControllerClass = true;
            String descriptorFullName = packageValue.replace(".", "/") + "/" + descriptorLocalName;
            log.debug("Processing screen controller class: id = {}, descriptor = {}, controller = {}", screenId, descriptorFullName, classFqn);

            screenInfo = screensCollector.getScreenInfoByDescriptor(descriptorFullName);
            if (screenInfo == null) {
                log.error("Screen info NOT FOUND by descriptor '{}'", descriptorFullName);
                screenInfo = screensCollector.initScreenInfo(screenId, descriptorFullName, classFqn);
            } else {
                log.debug("Screen info FOUND by descriptor '{}'", descriptorFullName);
                screenInfo.setScreenId(screenId);
                screenInfo.setControllerClass(classFqn);
                screensCollector.updateScreenInfo(screenInfo);
            }
        } else {
            // check if it's a legacy screen controller
            screenInfo = screensCollector.getScreenInfoByController(classFqn);
            log.debug("ScreenInfo by controller class '{}': {}", classFqn, screenInfo);
            if (screenInfo != null) {
                isLegacy = screenInfo.isLegacy();
                isControllerClass = true;
                log.debug("Screen info exists. legacy = {}", isLegacy);
            } else {
                if (isExtendBasicScreenClass) {
                    screenInfo = screensCollector.initScreenInfo(null, null, classFqn, false);
                }
            }
        }


        if (isControllerClass) {
            // Controller class for specific screen
            analyzeControllerClass(classDeclaration, screenInfo, superClassDetails);
        } else if (isExtendBasicScreenClass) {
            // Custom class extends some basic framework screen class: CustomEditor extends StandardEditor
            analyzeControllerClass(classDeclaration, screenInfo, superClassDetails);
        } else {
            // Class can't be defined as controller yet.
            ClassGeneralDetails superClassGeneralDetails = null;
            if (superClassDetails != null) {
                superClassGeneralDetails = new ClassGeneralDetails(superClassDetails.getSimpleName(), superClassDetails.getFqn(), null);
            }
            ClassGeneralDetails classGeneralDetails = new ClassGeneralDetails(simpleName, classFqn, superClassGeneralDetails);
            screensCollector.addUnknownClass(classGeneralDetails); //todo
        }


        if (!isControllerClass && !isExtendBasicScreenClass) {
            log.debug("Class '{}' is not a controller/intermediate class", classFqn);
            return;
        }

        analyzeControllerClass(classDeclaration, screenInfo, superClassDetails);
    }

    protected void analyzeControllerClass(ClassOrInterfaceDeclaration primaryClassDeclaration,
                                          ScreenInfo screenInfo,
                                          ScreenControllerSuperClassDetails superClassDetails) {

        String classFqn = primaryClassDeclaration.getFullyQualifiedName()
                .orElseThrow(() -> new RuntimeException("Unable to get class name"));

        Optional<ClassOrInterfaceType> firstExtendedTypeOpt = primaryClassDeclaration.getExtendedTypes().getFirst();
        if (firstExtendedTypeOpt.isPresent()) {
            String name = firstExtendedTypeOpt.get().getNameAsString();
            screenInfo.setExtendedController(name);
        }

        // Methods
        List<MethodDetails> methodDetailsList = processMethods(primaryClassDeclaration);

        int linesCount = primaryClassDeclaration.getRange().map(Range::getLineCount).orElse(0);

        ScreenControllerDetails.Builder builder = ScreenControllerDetails.builder(classFqn)
                .setOverallLines(linesCount)
                .setMethods(methodDetailsList);

        primaryClassDeclaration.accept(new VoidVisitorAdapter<>() {
            @Override
            public void visit(ClassOrInterfaceDeclaration n, Object arg) {
                super.visit(n, arg);
                if (n.isNestedType()) {
                    log.debug("Find nested class {}", n.getFullyQualifiedName());
                    List<MethodDetails> methodDetails = processMethods(n);
                    builder.putNestedClassMethods(n.getNameAsString(), methodDetails);
                }
            }
        }, null);

        ScreenControllerDetails controllerDetails = builder.setSuperClassDetails(superClassDetails).build();
        screenInfo.setControllerDetails(controllerDetails);
    }

    @Nullable
    protected String extractSingleValueAnnotationStringValue(SingleMemberAnnotationExpr annotationExpr) {
        String result = null;
        Expression memberValue = annotationExpr.asSingleMemberAnnotationExpr().getMemberValue();
        if (memberValue.isStringLiteralExpr()) {
            result = memberValue.asStringLiteralExpr().asString();
        } else {
            log.error("Unable to extract String value from {} - value is not a String", annotationExpr);
        }
        return result;
    }

    protected List<MethodDetails> processMethods(ClassOrInterfaceDeclaration classDeclaration) {
        List<MethodDeclaration> methodDeclarations = classDeclaration.getMethods();
        return methodDeclarations.stream()
                .map(this::processMethodDeclaration)
                .toList();
    }

    protected MethodDetails processMethodDeclaration(MethodDeclaration methodDeclaration) {
        List<String> methodCalls = new ArrayList<>();
        AtomicInteger uiComponentsCreateCalls = new AtomicInteger();

        methodDeclaration.accept(new VoidVisitorAdapter<>() {
            @Override
            public void visit(MethodCallExpr n, Object arg) {
                super.visit(n, arg);
                n.getScope().ifPresent(scope -> {
                    if ("uiComponents".equalsIgnoreCase(scope.toString())) { //todo get actual name of injected UiComponents bean
                        uiComponentsCreateCalls.incrementAndGet();
                    }
                    String scopeValue = n.getScope().map(Node::toString).orElse("");
                    methodCalls.add(scopeValue + "#" + n.getName());
                });
            }
        }, null);

        NumericMetric uiComponentsCreateAmountMetric = Metrics.createUiComponentsCreateAmountMetric(uiComponentsCreateCalls.get());
        NumericMetric methodCallsAmountMetric = Metrics.createMethodCallsAmountMetric(methodCalls.size());
        return new MethodDetails(
                methodDeclaration.getSignature().asString(),
                List.of(uiComponentsCreateAmountMetric, methodCallsAmountMetric)
        );
    }

    protected boolean isExtendBasicScreenClass(@Nullable ScreenControllerSuperClassDetails superClassDetails) {
        if (superClassDetails == null) {
            return false;
        }
        return !superClassDetails.getSuperClassKind().equals(ScreenControllerSuperClassKind.CUSTOM);
    }
}
