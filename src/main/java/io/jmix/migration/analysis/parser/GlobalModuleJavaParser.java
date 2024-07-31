package io.jmix.migration.analysis.parser;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.*;
import io.jmix.migration.model.GlobalModuleJavaFileParseResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class GlobalModuleJavaParser extends AbstractJavaParser<GlobalModuleJavaFileParseResult> {

    private static final Logger log = LoggerFactory.getLogger(GlobalModuleJavaParser.class);

    protected final Set<String> listenersCollector;

    public GlobalModuleJavaParser(Set<String> listenersCollector) {
        super();
        this.listenersCollector = listenersCollector;
    }

    @Nullable
    protected GlobalModuleJavaFileParseResult processCompilationUnit(CompilationUnit compilationUnit) {
        Optional<TypeDeclaration<?>> primaryTypeOpt = compilationUnit.getPrimaryType();
        if (primaryTypeOpt.isEmpty()) {
            return null;
        }

        TypeDeclaration<?> primaryTypeDeclaration = primaryTypeOpt.get();
        if (primaryTypeDeclaration.isClassOrInterfaceDeclaration()) {
            ClassOrInterfaceDeclaration classDeclaration = primaryTypeDeclaration.asClassOrInterfaceDeclaration();
            processPrimaryClassDeclaration(classDeclaration);
        }

        return new GlobalModuleJavaFileParseResult(); //todo
    }

    protected void processPrimaryClassDeclaration(ClassOrInterfaceDeclaration classDeclaration) {
        // Annotations

        NodeList<AnnotationExpr> annotations = classDeclaration.getAnnotations();
        for (AnnotationExpr annotationExpr : annotations) {
            Name annotationName = annotationExpr.getName();

            if ("Listeners".equals(annotationName.asString())) {
                log.info("[Listeners]");
                if (annotationExpr.isSingleMemberAnnotationExpr()) {
                    List<String> listenersOnEntity = extractSingleMemberAnnotationListOfStringsValue(annotationExpr.asSingleMemberAnnotationExpr());
                    listenersCollector.addAll(listenersOnEntity);
                } else {
                    //TODO not required in common case
                    log.warn("Multi-member annotation is not supported yet: {}", annotationExpr);
                }
            }
        }
    }

    protected List<String> extractSingleMemberAnnotationListOfStringsValue(SingleMemberAnnotationExpr annotationExpr) {
        List<String> result;
        Expression memberValue = annotationExpr.getMemberValue();
        if (memberValue.isStringLiteralExpr()) {
            result = List.of(memberValue.asStringLiteralExpr().asString());
        } else if (memberValue.isArrayInitializerExpr()) {
            result = memberValue.asArrayInitializerExpr().getValues().stream()
                    .filter(Expression::isStringLiteralExpr)
                    .map(Expression::asStringLiteralExpr)
                    .map(StringLiteralExpr::asString)
                    .toList();
        } else {
            log.error("Unable to extract value from {} - value is not a String or array of Strings", annotationExpr);
            result = Collections.emptyList();
        }
        return result;
    }

}
