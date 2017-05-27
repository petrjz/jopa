package cz.cvut.kbss.jopa.model.annotations;

import java.lang.annotation.*;

/**
 * Namespace declaration allows the use of shorter notation for long URIs.
 * <p>
 * Namespace can be declared on package and class level. Class-level declaration overrides package-level namespace
 * declaration (in case the prefix is the same).
 *
 * @see <a href="https://www.w3.org/TR/REC-xml-names/" target="_top">https://www.w3.org/TR/REC-xml-names/</a>
 * @see Namespaces
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PACKAGE, ElementType.TYPE})
public @interface Namespace {

    /**
     * Prefix to be used as a shorthand for the fully qualified namespace URI.
     * <p>
     * Corresponds to {@code xsd} in the {@code xmlns:xsd="http://www.w3.org/2001/XMLSchema#"} XML namespace
     * declaration.
     *
     * @return The prefix to be used to reference a namespace
     */
    String prefix();

    /**
     * Fully qualified namespace URI, which is mapped by {@link #prefix()}.
     * <p>
     * Corresponds to {@code http://www.w3.org/2001/XMLSchema#} in the {@code xmlns:xsd="http://www.w3.org/2001/XMLSchema#"}
     * XML namespace declaration.
     *
     * @return The namespace URI
     */
    String namespace();
}
