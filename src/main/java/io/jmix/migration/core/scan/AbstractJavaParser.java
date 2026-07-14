package io.jmix.migration.core.scan;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.Problem;
import com.github.javaparser.ast.CompilationUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.stream.Collectors;

public abstract class AbstractJavaParser {

    private static final Logger log = LoggerFactory.getLogger(AbstractJavaParser.class);

    protected final JavaParser javaParser;

    public AbstractJavaParser() {
        this.javaParser = createJavaParser();
    }

    public void parseJavaFile(Path filePath) {
        File file = filePath.toFile();
        if (!shouldBeProcessed(file)) {
            return;
        }

        ParseResult<CompilationUnit> parseResult = parseJavaFile(file);

        if (!parseResult.isSuccessful()) {
            handleFailedParsingResult(parseResult);
        }
        if (parseResult.getResult().isEmpty()) {
            handleEmptyParsingResult(parseResult);
        }

        processCompilationUnit(parseResult.getResult().get());
    }

    protected abstract void processCompilationUnit(CompilationUnit compilationUnit);

    protected boolean shouldBeProcessed(File file) {
        return !isFileEmpty(file);
    }

    protected boolean isFileEmpty(File file) {
        return file.length() == 0; //todo
    }

    protected void handleFailedParsingResult(ParseResult<CompilationUnit> parseResult) {
        throw new RuntimeException("Java file parsing failed: " + describeProblems(parseResult));
    }

    protected void handleEmptyParsingResult(ParseResult<CompilationUnit> parseResult) {
        throw new RuntimeException("Parse result is empty");
    }

    protected String describeProblems(ParseResult<CompilationUnit> parseResult) {
        return parseResult.getProblems().stream()
                .limit(3)
                .map(Problem::getMessage)
                .collect(Collectors.joining("; "));
    }

    protected ParseResult<CompilationUnit> parseJavaFile(File file) {
        try {
            return javaParser.parse(file);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    protected JavaParser createJavaParser() {
        // Default JavaParser language level is Java 11; newer syntax (switch expressions,
        // records, text blocks) fails to parse without a raised level
        ParserConfiguration configuration = new ParserConfiguration()
                .setLanguageLevel(ParserConfiguration.LanguageLevel.BLEEDING_EDGE);
        return new JavaParser(configuration);
    }
}
