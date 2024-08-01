package io.jmix.migration.analysis.parser.screen;

import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.expr.Name;
import io.jmix.migration.analysis.model.ScreenControllerSuperClassDetails;
import io.jmix.migration.analysis.model.ScreenControllerSuperClassKind;

import java.io.File;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import static io.jmix.migration.analysis.parser.screen.ScreenConstants.*;
import static java.util.Map.entry;

public class ImportsHolder {

    private static final Map<String, String> ALL_BASIC_CLASSES_SIMPLE_NAME_TO_FQN_MAPPING = Map.ofEntries(
            entry(LEGACY_BROWSER_BASIC_CLASS_SIMPLE_NAME, LEGACY_BROWSER_BASIC_CLASS_FULL_NAME),
            entry(LEGACY_EDITOR_BASIC_CLASS_SIMPLE_NAME, LEGACY_EDITOR_BASIC_CLASS_FULL_NAME),
            entry(LEGACY_WINDOW_BASIC_CLASS_SIMPLE_NAME, LEGACY_WINDOW_BASIC_CLASS_FULL_NAME),
            entry(LEGACY_COMBINED_SCREEN_BASIC_CLASS_SIMPLE_NAME, LEGACY_COMBINED_SCREEN_BASIC_CLASS_FULL_NAME),
            entry(LEGACY_FRAME_BASIC_CLASS_SIMPLE_NAME, LEGACY_FRAME_BASIC_CLASS_FULL_NAME),
            entry(MAIN_WINDOW_BASIC_CLASS_SIMPLE_NAME, MAIN_WINDOW_BASIC_CLASS_FULL_NAME),
            entry(TOP_LEVEL_WINDOW_BASIC_CLASS_SIMPLE_NAME, TOP_LEVEL_WINDOW_BASIC_CLASS_FULL_NAME),
            entry(BROWSER_BASIC_CLASS_SIMPLE_NAME, BROWSER_BASIC_CLASS_FULL_NAME),
            entry(EDITOR_BASIC_CLASS_SIMPLE_NAME, EDITOR_BASIC_CLASS_FULL_NAME),
            entry(SCREEN_BASIC_CLASS_SIMPLE_NAME, SCREEN_BASIC_CLASS_FULL_NAME),
            entry(MASTER_DETAILS_BASIC_CLASS_SIMPLE_NAME, MASTER_DETAILS_BASIC_CLASS_FULL_NAME),
            entry(FRAGMENT_BASIC_CLASS_SIMPLE_NAME, FRAGMENT_BASIC_CLASS_FULL_NAME)
    );

    private final Set<String> directImports;
    private final Set<String> wildcardImports;
    private final List<Path> allSrcPaths;

    protected ImportsHolder(Set<String> directImports, Set<String> wildcardImports, List<Path> allSrcPaths) {
        this.directImports = directImports;
        this.wildcardImports = wildcardImports;
        this.allSrcPaths = allSrcPaths;
    }

    public static ImportsHolder create(NodeList<ImportDeclaration> importDeclarations, List<Path> modulePaths) {
        Set<String> directImportTmp = new HashSet<>();
        Set<String> wildcardImportTmp = new HashSet<>();
        for (ImportDeclaration importDeclaration : importDeclarations) {
            Name name = importDeclaration.getName();
            if (importDeclaration.isAsterisk()) {
                wildcardImportTmp.add(name.asString());
            } else {
                directImportTmp.add(name.asString());
            }
        }
        return new ImportsHolder(directImportTmp, wildcardImportTmp, modulePaths);
    }

    public ScreenControllerSuperClassDetails analyzeSuperClass3(String className, String currentPackage) {
        ScreenControllerSuperClassKind superClassKind;

        String simpleName;
        String fqn = null;
        boolean fileExistsInSrc = false;
        if (isFqn(className)) {
            // case: SubClass extends some.package.SuperClass
            simpleName = className.substring(className.lastIndexOf(".") + 1);
            fqn = className;
            superClassKind = ScreenControllerSuperClassKind.fromFqn(fqn);
            if (superClassKind.equals(ScreenControllerSuperClassKind.CUSTOM)) {
                fileExistsInSrc = isFqnFileExists(fqn);
            }
        } else {
            simpleName = className;
            if (ALL_BASIC_CLASSES_SIMPLE_NAME_TO_FQN_MAPPING.containsKey(className)) {
                String basicClassFqn = ALL_BASIC_CLASSES_SIMPLE_NAME_TO_FQN_MAPPING.get(className);
                String basicClassPackageName = basicClassFqn.substring(0, basicClassFqn.lastIndexOf("."));
                if (wildcardImports.contains(basicClassPackageName) || directImports.contains(basicClassFqn)) {
                    fqn = basicClassFqn;
                    superClassKind = ScreenControllerSuperClassKind.fromFqn(fqn);
                } else {
                    superClassKind = ScreenControllerSuperClassKind.CUSTOM;
                }
            } else {
                superClassKind = ScreenControllerSuperClassKind.CUSTOM;
            }

            if (superClassKind == ScreenControllerSuperClassKind.CUSTOM) {
                Optional<String> fqnOpt = directImports.stream()
                        .filter(directImport -> {
                            String directImportSimpleName = directImport.substring(directImport.lastIndexOf(".") + 1);
                            return directImportSimpleName.equals(className);
                        }).findFirst();
                if (fqnOpt.isPresent()) {
                    fqn = fqnOpt.get(); //todo check existence
                } else {
                    Set<String> fqnLocalCandidates = wildcardImports.stream().map(wi -> wi + "." + className).collect(Collectors.toSet());
                    fqnLocalCandidates.add(currentPackage + "." + className); // superclass can be in the same package
                    boolean found = false;
                    for (String fqnCandidate : fqnLocalCandidates) {
                        found = isFqnFileExists(fqnCandidate);
                        if (found) {
                            fqn = fqnCandidate;
                            break;
                        }
                    }
                    fileExistsInSrc = found;
                }
            }
        }

        return new ScreenControllerSuperClassDetails(simpleName, fqn, superClassKind, fileExistsInSrc); //todo not found case
    }

    protected boolean isFqn(String className) {
        return className.contains(".");
    }

    protected boolean isFqnFileExists(String fqnCandidate) {
        String fileNameCandidate = fqnCandidate.replace(".", "/") + ".java";
        Path filePathCandidate = Path.of(fileNameCandidate);
        for (Path modulePath : allSrcPaths) {
            Path fullPathCandidate = modulePath.resolve(filePathCandidate);
            File fileCandidate = fullPathCandidate.toFile();
            boolean exists = fileCandidate.exists();
            if (exists) {
                return true;
            }
        }
        return false;
    }
}
