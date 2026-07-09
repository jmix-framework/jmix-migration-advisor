package io.jmix.migration.analysis.issue.uicomponent;

/**
 * A dependency needed by the primary (most complete) replacement recipe of a UI component issue.
 * <p>
 * The requirement is not strict: a simpler recipe may work without the dependency;
 * such gradations belong to the issue notes. Several values on one issue mean a conjunction:
 * all of them are needed by the primary recipe.
 */
public class Requires {

    public enum Kind {
        ADDON,
        COMMERCIAL_ADDON,
        THIRD_PARTY
    }

    private final Kind kind;
    private final String subject;

    private Requires(Kind kind, String subject) {
        this.kind = kind;
        this.subject = subject;
    }

    public static Requires addon(String subject) {
        return new Requires(Kind.ADDON, subject);
    }

    public static Requires commercialAddon(String subject) {
        return new Requires(Kind.COMMERCIAL_ADDON, subject);
    }

    public static Requires thirdParty(String subject) {
        return new Requires(Kind.THIRD_PARTY, subject);
    }

    public Kind getKind() {
        return kind;
    }

    public String getSubject() {
        return subject;
    }

    public String getKindName() {
        return kind.name();
    }
}
