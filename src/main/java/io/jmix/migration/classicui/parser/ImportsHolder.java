package io.jmix.migration.classicui.parser;

import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.expr.Name;
import io.jmix.migration.classicui.model.ScreenControllerSuperClassDetails;
import io.jmix.migration.classicui.model.ScreenControllerSuperClassKind;

import java.io.File;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class ImportsHolder {

    private final Set<String> directImports;
    private final Set<String> wildcardImports;
    private final List<Path> allSrcPaths;
    private final ScreenClassProfile screenClassProfile;

    protected ImportsHolder(Set<String> directImports, Set<String> wildcardImports,
                            List<Path> allSrcPaths, ScreenClassProfile screenClassProfile) {
        this.directImports = directImports;
        this.wildcardImports = wildcardImports;
        this.allSrcPaths = allSrcPaths;
        this.screenClassProfile = screenClassProfile;
    }

    public static ImportsHolder create(NodeList<ImportDeclaration> importDeclarations, List<Path> modulePaths,
                                       ScreenClassProfile screenClassProfile) {
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
        return new ImportsHolder(directImportTmp, wildcardImportTmp, modulePaths, screenClassProfile);
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
            superClassKind = screenClassProfile.kindByFqn(fqn);
            if (superClassKind.equals(ScreenControllerSuperClassKind.CUSTOM)) {
                fileExistsInSrc = isFqnFileExists(fqn);
            }
        } else {
            simpleName = className;
            String basicClassFqn = screenClassProfile.getBasicClassFqn(className);
            if (basicClassFqn != null) {
                String basicClassPackageName = basicClassFqn.substring(0, basicClassFqn.lastIndexOf("."));
                if (wildcardImports.contains(basicClassPackageName) || directImports.contains(basicClassFqn)) {
                    fqn = basicClassFqn;
                    superClassKind = screenClassProfile.kindByFqn(fqn);
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
