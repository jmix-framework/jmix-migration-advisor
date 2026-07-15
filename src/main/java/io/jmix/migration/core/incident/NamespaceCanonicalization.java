package io.jmix.migration.core.incident;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Set;

/**
 * Namespace facts of the component issues registry: which XML namespace URIs belong to which
 * add-on component family (prefix entry). Used by the layout parser to canonicalize component
 * names: the prefix written in a descriptor is a per-file convention, the URI is the identity.
 */
public class NamespaceCanonicalization {

    private static final NamespaceCanonicalization EMPTY
            = new NamespaceCanonicalization(Map.of(), Set.of());

    private final Map<String, String> prefixByUri;
    private final Set<String> strictPrefixes;

    protected NamespaceCanonicalization(Map<String, String> prefixByUri, Set<String> strictPrefixes) {
        this.prefixByUri = Map.copyOf(prefixByUri);
        this.strictPrefixes = Set.copyOf(strictPrefixes);
    }

    public static NamespaceCanonicalization of(Map<String, String> prefixByUri, Set<String> strictPrefixes) {
        return new NamespaceCanonicalization(prefixByUri, strictPrefixes);
    }

    public static NamespaceCanonicalization empty() {
        return EMPTY;
    }

    /**
     * @return the canonical family prefix for the namespace URI, or {@code null} when the URI
     * is not known to the registry
     */
    @Nullable
    public String canonicalPrefix(String namespaceUri) {
        return prefixByUri.get(namespaceUri);
    }

    /**
     * A strict prefix belongs to a family with declared URIs: a component written with this
     * prefix but a different namespace URI is NOT that family (a custom component).
     * Families without declared URIs keep conventional prefix matching.
     */
    public boolean isStrictPrefix(String prefix) {
        return strictPrefixes.contains(prefix);
    }
}
