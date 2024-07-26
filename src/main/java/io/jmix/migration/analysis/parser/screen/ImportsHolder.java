package io.jmix.migration.analysis.parser.screen;

import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.expr.Name;
import io.jmix.migration.model.ScreenControllerSuperClassDetails;
import io.jmix.migration.model.ScreenControllerSuperClassKind;

import java.io.File;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import static io.jmix.migration.analysis.parser.screen.ScreenConstants.*;
import static java.util.Map.entry;

public class ImportsHolder {

    /*private static final String LEGACY_SCREENS_BASE_PACKAGE = "com.haulmont.cuba.gui.components";
    private static final String SCREENS_BASE_PACKAGE = "com.haulmont.cuba.gui.screen";

    private static final String LEGACY_BROWSER_BASIC_CLASS_SIMPLE_NAME = "AbstractLookup";
    private static final String LEGACY_BROWSER_BASIC_CLASS_FULL_NAME = "com.haulmont.cuba.gui.components.AbstractLookup";

    private static final String LEGACY_EDITOR_BASIC_CLASS_SIMPLE_NAME = "AbstractEditor";
    private static final String LEGACY_EDITOR_BASIC_CLASS_FULL_NAME = "com.haulmont.cuba.gui.components.AbstractEditor";

    private static final String LEGACY_WINDOW_BASIC_CLASS_SIMPLE_NAME = "AbstractWindow";
    private static final String LEGACY_WINDOW_BASIC_CLASS_FULL_NAME = "com.haulmont.cuba.gui.components.AbstractWindow";

    private static final String LEGACY_COMBINED_SCREEN_BASIC_CLASS_SIMPLE_NAME = "EntityCombinedScreen";
    private static final String LEGACY_COMBINED_SCREEN_BASIC_CLASS_FULL_NAME = "com.haulmont.cuba.gui.components.EntityCombinedScreen";

    private static final String MAIN_WINDOW_BASIC_CLASS_SIMPLE_NAME = "AbstractMainWindow";
    private static final String MAIN_WINDOW_BASIC_CLASS_FULL_NAME = "com.haulmont.cuba.gui.components.AbstractMainWindow";

    private static final String TOP_LEVEL_WINDOW_BASIC_CLASS_SIMPLE_NAME = "AbstractTopLevelWindow";
    private static final String TOP_LEVEL_WINDOW_BASIC_CLASS_FULL_NAME = "com.haulmont.cuba.gui.components.AbstractTopLevelWindow";

    private static final String BROWSER_BASIC_CLASS_SIMPLE_NAME = "StandardLookup";
    private static final String BROWSER_BASIC_CLASS_FULL_NAME = "com.haulmont.cuba.gui.screen.StandardLookup";

    private static final String EDITOR_BASIC_CLASS_SIMPLE_NAME = "StandardEditor";
    private static final String EDITOR_BASIC_CLASS_FULL_NAME = "com.haulmont.cuba.gui.screen.StandardEditor";

    private static final String MASTER_DETAILS_BASIC_CLASS_SIMPLE_NAME = "MasterDetailScreen";
    private static final String MASTER_DETAILS_BASIC_CLASS_FULL_NAME = "com.haulmont.cuba.gui.screen.MasterDetailScreen";

    private static final String SCREEN_BASIC_CLASS_SIMPLE_NAME = "Screen";
    private static final String SCREEN_BASIC_CLASS_FULL_NAME = "com.haulmont.cuba.gui.screen.Screen";*/

    private static final Set<String> LEGACY_SCREEN_BASIC_CLASS_SIMPLE_NAMES = Set.of(
            LEGACY_BROWSER_BASIC_CLASS_SIMPLE_NAME,
            LEGACY_EDITOR_BASIC_CLASS_SIMPLE_NAME,
            LEGACY_WINDOW_BASIC_CLASS_SIMPLE_NAME,
            LEGACY_COMBINED_SCREEN_BASIC_CLASS_SIMPLE_NAME
    );

    private static final Set<String> LEGACY_SCREEN_BASIC_CLASS_FULL_NAMES = Set.of(
            LEGACY_BROWSER_BASIC_CLASS_FULL_NAME,
            LEGACY_EDITOR_BASIC_CLASS_FULL_NAME,
            LEGACY_WINDOW_BASIC_CLASS_FULL_NAME,
            LEGACY_COMBINED_SCREEN_BASIC_CLASS_FULL_NAME
    );

    private static final Set<String> MAIN_WINDOW_BASIC_CLASS_SIMPLE_NAMES = Set.of(
            MAIN_WINDOW_BASIC_CLASS_SIMPLE_NAME, TOP_LEVEL_WINDOW_BASIC_CLASS_SIMPLE_NAME
    );
    private static final Set<String> MAIN_WINDOW_BASIC_CLASS_FULL_NAMES = Set.of(
            MAIN_WINDOW_BASIC_CLASS_FULL_NAME,
            TOP_LEVEL_WINDOW_BASIC_CLASS_FULL_NAME
    );

    private static final Set<String> SCREEN_BASIC_CLASS_SIMPLE_NAMES = Set.of(
            BROWSER_BASIC_CLASS_SIMPLE_NAME,
            EDITOR_BASIC_CLASS_SIMPLE_NAME,
            MASTER_DETAILS_BASIC_CLASS_SIMPLE_NAME,
            SCREEN_BASIC_CLASS_SIMPLE_NAME
    );
    private static final Set<String> SCREEN_BASIC_CLASS_FULL_NAMES = Set.of(
            BROWSER_BASIC_CLASS_FULL_NAME,
            EDITOR_BASIC_CLASS_FULL_NAME,
            MASTER_DETAILS_BASIC_CLASS_FULL_NAME,
            SCREEN_BASIC_CLASS_FULL_NAME
    );

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

    private static final Set<String> ALL_BASIC_CLASSES_SIMPLE_NAMES = new HashSet<>(ALL_BASIC_CLASSES_SIMPLE_NAME_TO_FQN_MAPPING.keySet());

    /*private static final String LEGACY_FRAME_BASIC_CLASS_SIMPLE_NAME = "AbstractFrame";
    private static final String LEGACY_FRAME_BASIC_CLASS_FULL_NAME = "com.haulmont.cuba.gui.components.AbstractFrame";

    private static final String FRAGMENT_BASIC_CLASS_SIMPLE_NAME = "ScreenFragment";
    private static final String FRAGMENT_BASIC_CLASS_FULL_NAME = "com.haulmont.cuba.gui.screen.ScreenFragment";*/


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

    /*public ControllerExtensionAnalysisResult analyzeSuperClass(String className) {
        ScreenControllerSuperClassKind superClassKind;
        SuperClassDetails superClassDetails;
        String fqn;
        Set<String> fqnCandidates;
        if (isFqn(className)) {
            // case: SubClass extends some.package.SuperClass
            fqn = className;
            fqnCandidates = Collections.emptySet();
            if (LEGACY_SCREEN_BASIC_CLASS_FULL_NAMES.contains(className)) {
                superClassKind = ScreenControllerSuperClassKind.LEGACY_SCREEN_BASIC_CLASS;
            } else if (MAIN_WINDOW_BASIC_CLASS_FULL_NAMES.contains(className)) {
                superClassKind = ScreenControllerSuperClassKind.MAIN_WINDOW_CLASS;
            } else if (SCREEN_BASIC_CLASS_FULL_NAMES.contains(className)) {
                superClassKind = ScreenControllerSuperClassKind.SCREEN_BASIC_CLASS;
            } else if (LEGACY_FRAME_BASIC_CLASS_FULL_NAME.equals(className)) {
                superClassKind = ScreenControllerSuperClassKind.LEGACY_FRAME;
            } else if (FRAGMENT_BASIC_CLASS_FULL_NAME.equals(className)) {
                superClassKind = ScreenControllerSuperClassKind.FRAGMENT;
            } else {
                superClassKind = ScreenControllerSuperClassKind.OTHER;
            }
        } else {
            if (LEGACY_SCREEN_BASIC_CLASS_SIMPLE_NAMES.contains(className)) {
                if (wildcardImports.contains(LEGACY_SCREENS_BASE_PACKAGE)
                        || directImports.contains(LEGACY_SCREENS_BASE_PACKAGE + "." + className)) {
                    superClassKind = ScreenControllerSuperClassKind.LEGACY_SCREEN_BASIC_CLASS;
                    fqn = LEGACY_SCREENS_BASE_PACKAGE + "." + className;
                } else {
                    superClassKind = ScreenControllerSuperClassKind.OTHER;
                }
            } else if (MAIN_WINDOW_BASIC_CLASS_SIMPLE_NAMES.contains(className)) {
                if (wildcardImports.contains(LEGACY_SCREENS_BASE_PACKAGE)
                        || directImports.contains(LEGACY_SCREENS_BASE_PACKAGE + "." + className)) {
                    superClassKind = ScreenControllerSuperClassKind.MAIN_WINDOW_CLASS;
                    fqn = LEGACY_SCREENS_BASE_PACKAGE + "." + className;
                } else {
                    superClassKind = ScreenControllerSuperClassKind.OTHER;
                }
            } else if (SCREEN_BASIC_CLASS_SIMPLE_NAMES.contains(className)) {
                if (wildcardImports.contains(SCREENS_BASE_PACKAGE)
                        || directImports.contains(SCREENS_BASE_PACKAGE + "." + className)) {
                    superClassKind = ScreenControllerSuperClassKind.SCREEN_BASIC_CLASS;
                    fqn = LEGACY_SCREENS_BASE_PACKAGE + "." + className;
                } else {
                    superClassKind = ScreenControllerSuperClassKind.OTHER;
                }
            } else if (LEGACY_FRAME_BASIC_CLASS_SIMPLE_NAME.equals(className)) {
                if (wildcardImports.contains(LEGACY_SCREENS_BASE_PACKAGE)
                        || directImports.contains(LEGACY_SCREENS_BASE_PACKAGE + "." + className)) {
                    superClassKind = ScreenControllerSuperClassKind.LEGACY_FRAME;
                    fqn = LEGACY_SCREENS_BASE_PACKAGE + "." + className;
                } else {
                    superClassKind = ScreenControllerSuperClassKind.OTHER;
                }
            } else if (FRAGMENT_BASIC_CLASS_SIMPLE_NAME.contains(className)) {
                if (wildcardImports.contains(SCREENS_BASE_PACKAGE)
                        || directImports.contains(SCREENS_BASE_PACKAGE + "." + className)) {
                    superClassKind = ScreenControllerSuperClassKind.FRAGMENT;
                } else {
                    superClassKind = ScreenControllerSuperClassKind.OTHER;
                }
            } else {
                superClassKind = ScreenControllerSuperClassKind.OTHER;
            }
        }

        if (superClassKind != ScreenControllerSuperClassKind.OTHER) {

        }

    }*/

    /*public ControllerExtensionAnalysisResult analyzeSuperClass2(String className, String currentPackage) {
        ScreenControllerSuperClassKind superClassKind;

        String fqn = null;
        Set<String> fqnCandidates = Collections.emptySet();
        if (isFqn(className)) {
            // case: SubClass extends some.package.SuperClass
            fqn = className;
            superClassKind = ScreenControllerSuperClassKind.fromFqn(fqn);
        } else {
            if (ALL_BASIC_CLASSES_SIMPLE_NAME_TO_FQN_MAPPING.containsKey(className)) {
                String basicClassFqn = ALL_BASIC_CLASSES_SIMPLE_NAME_TO_FQN_MAPPING.get(className);
                String basicClassPackageName = basicClassFqn.substring(0, basicClassFqn.lastIndexOf("."));
                if (wildcardImports.contains(basicClassPackageName) || directImports.contains(basicClassFqn)) {
                    fqn = basicClassFqn;
                    superClassKind = ScreenControllerSuperClassKind.fromFqn(fqn);
                } else {
                    superClassKind = ScreenControllerSuperClassKind.OTHER;
                }
            } else {
                superClassKind = ScreenControllerSuperClassKind.OTHER;
            }

            if (superClassKind == ScreenControllerSuperClassKind.OTHER) {
                boolean hasDirectImport = directImports.stream()
                        .map(directImport -> directImport.substring(0, directImport.lastIndexOf(".")))
                        .anyMatch(directImportSimpleName -> directImportSimpleName.equals(className));
                if(hasDirectImport) {

                }

                Set<String> fqnLocalCandidates = wildcardImports.stream().map(wi -> wi + "." + className).collect(Collectors.toSet());
                fqnLocalCandidates.add(currentPackage + "." + className); // superclass can be in the same package

                boolean found = false;
                for (String fqnCandidate : fqnLocalCandidates) {
                    if (found) {
                        break;
                    }
                    String fileNameCandidate = fqnCandidate.replace(".", "/") + ".java";
                    Path filePathCandidate = Path.of(fileNameCandidate);
                    for (Path modulePath : allSrcPaths) {
                        Path fullPathCandidate = modulePath.resolve(filePathCandidate);
                        File fileCandidate = fullPathCandidate.toFile();
                        boolean exists = fileCandidate.exists();
                        if (exists) {
                            fqn = fqnCandidate;
                            found = true;
                        }
                    }
                }
            }
        }

        SuperClassDetails superClassDetails = new SuperClassDetails(className, fqn, Collections.emptySet());
        ControllerExtensionAnalysisResult result = new ControllerExtensionAnalysisResult(superClassKind, superClassDetails);
        return result;
    }*/

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

                        /*String fileNameCandidate = fqnCandidate.replace(".", "/") + ".java";
                        Path filePathCandidate = Path.of(fileNameCandidate);
                        for (Path modulePath : modulePaths) {
                            Path fullPathCandidate = modulePath.resolve(filePathCandidate);
                            File fileCandidate = fullPathCandidate.toFile();
                            boolean exists = fileCandidate.exists();
                            if (exists) {
                                fqn = fqnCandidate;
                                found = true;
                            }
                        }*/
                    }
                    fileExistsInSrc = found;
                }
            }
        }

        ScreenControllerSuperClassDetails result = new ScreenControllerSuperClassDetails(simpleName, fqn, superClassKind, fileExistsInSrc); //todo not found case
        return result;
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
