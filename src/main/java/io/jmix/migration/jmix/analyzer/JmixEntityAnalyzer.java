package io.jmix.migration.jmix.analyzer;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import io.jmix.migration.core.scan.AbstractJavaParser;
import io.jmix.migration.core.scan.UnparsedFilesCollector;
import io.jmix.migration.jmix.model.JmixDataModelInfo;
import io.jmix.migration.core.project.JmixModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Scans module sources for the annotation-registered data model: JPA entities
 * ({@code @JmixEntity} + {@code @Entity}), DTO entities ({@code @JmixEntity} without
 * {@code @Entity}), embeddables, {@code EnumClass} enums, entity event listeners and
 * the volume of the {@code javax} to {@code jakarta} namespace sweep.
 */
public class JmixEntityAnalyzer {

    private static final Logger log = LoggerFactory.getLogger(JmixEntityAnalyzer.class);

    public JmixDataModelInfo analyzeDataModel(List<JmixModule> modules, UnparsedFilesCollector unparsedFilesCollector) {
        log.info("Start data model analysis");
        EntityModelParser parser = new EntityModelParser();

        for (JmixModule module : modules) {
            Path javaDir = module.getJavaSourcesDir();
            if (!Files.isDirectory(javaDir)) {
                continue;
            }
            try {
                Files.walkFileTree(javaDir, new SimpleFileVisitor<>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        if (file.getFileName().toString().endsWith(".java")) {
                            try {
                                parser.parseJavaFile(file);
                            } catch (Exception e) {
                                // The screen analyzer walks the same files and reports them; avoid duplicates
                                log.debug("Failed to parse Java file '{}': {}", file, e.getMessage());
                            }
                        }
                        return FileVisitResult.CONTINUE;
                    }
                });
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return parser.buildResult();
    }

    protected static class EntityModelParser extends AbstractJavaParser {

        protected final List<String> jpaEntities = new ArrayList<>();
        protected final List<String> dtoEntities = new ArrayList<>();
        protected final List<String> embeddables = new ArrayList<>();
        protected final List<String> enums = new ArrayList<>();
        protected final List<String> entityEventListeners = new ArrayList<>();
        protected int javaxImportFilesCount;

        @Override
        protected void processCompilationUnit(CompilationUnit compilationUnit) {
            boolean hasJavaxImport = compilationUnit.getImports().stream().anyMatch(importDeclaration -> {
                String name = importDeclaration.getNameAsString();
                return name.startsWith("javax.persistence")
                        || name.startsWith("javax.validation")
                        || name.startsWith("javax.annotation");
            });
            if (hasJavaxImport) {
                javaxImportFilesCount++;
            }

            boolean referencesEntityChangedEvent = compilationUnit.getImports().stream()
                    .anyMatch(importDeclaration ->
                            importDeclaration.getNameAsString().equals("io.jmix.core.event.EntityChangedEvent"));

            Optional<TypeDeclaration<?>> primaryTypeOpt = compilationUnit.getPrimaryType();
            if (primaryTypeOpt.isEmpty()) {
                return;
            }
            TypeDeclaration<?> primaryType = primaryTypeOpt.get();
            String fqn = primaryType.getFullyQualifiedName().orElse(primaryType.getNameAsString());

            if (referencesEntityChangedEvent) {
                entityEventListeners.add(fqn);
            }

            if (primaryType instanceof EnumDeclaration enumDeclaration) {
                boolean isEnumClass = enumDeclaration.getImplementedTypes().stream()
                        .anyMatch(type -> "EnumClass".equals(type.getNameAsString()));
                if (isEnumClass) {
                    enums.add(fqn);
                }
                return;
            }
            if (!(primaryType instanceof ClassOrInterfaceDeclaration)) {
                return;
            }

            boolean jmixEntity = primaryType.getAnnotationByName("JmixEntity").isPresent();
            boolean jpaEntity = primaryType.getAnnotationByName("Entity").isPresent();
            boolean embeddable = primaryType.getAnnotationByName("Embeddable").isPresent();

            if (embeddable) {
                embeddables.add(fqn);
            } else if (jmixEntity && jpaEntity) {
                jpaEntities.add(fqn);
            } else if (jmixEntity) {
                dtoEntities.add(fqn);
            }
        }

        protected JmixDataModelInfo buildResult() {
            jpaEntities.sort(String::compareTo);
            dtoEntities.sort(String::compareTo);
            embeddables.sort(String::compareTo);
            enums.sort(String::compareTo);
            entityEventListeners.sort(String::compareTo);
            return new JmixDataModelInfo(jpaEntities, dtoEntities, embeddables, enums,
                    entityEventListeners, javaxImportFilesCount);
        }
    }
}
