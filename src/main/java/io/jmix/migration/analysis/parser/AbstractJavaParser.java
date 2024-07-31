package io.jmix.migration.analysis.parser;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;

public abstract class AbstractJavaParser<R> {

    private static final Logger log = LoggerFactory.getLogger(AbstractJavaParser.class);

    protected final JavaParser javaParser;

    public AbstractJavaParser() {
        this.javaParser = createJavaParser();
    }

    @Nullable
    public R parseJavaFile(Path filePath) {
        File file = filePath.toFile();
        if(!shouldBeProcessed(file)) {
            return null;
        }

        ParseResult<CompilationUnit> parseResult = parseJavaFile(file);

        if (!parseResult.isSuccessful()) {
            return handleFailedParsingResult(parseResult);
        }
        if (parseResult.getResult().isEmpty()) {
            return handleEmptyParsingResult(parseResult);
        }

        return processCompilationUnit(parseResult.getResult().get());
    }

    @Nullable
    protected abstract R processCompilationUnit(CompilationUnit compilationUnit);

    protected boolean shouldBeProcessed(File file) {
        return !isFileEmpty(file);
    }

    protected boolean isFileEmpty(File file) {
        return file.length() == 0; //todo
    }

    protected R handleFailedParsingResult(ParseResult<CompilationUnit> parseResult) {
        throw new RuntimeException("Java file parsing failed");
    }

    protected R handleEmptyParsingResult(ParseResult<CompilationUnit> parseResult) {
        throw new RuntimeException("Parse result is empty");
    }

    protected ParseResult<CompilationUnit> parseJavaFile(File file) {
        try {
            return javaParser.parse(file);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    protected JavaParser createJavaParser() {
        return new JavaParser();
    }
}
