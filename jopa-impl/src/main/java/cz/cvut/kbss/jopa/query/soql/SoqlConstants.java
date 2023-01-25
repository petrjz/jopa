package cz.cvut.kbss.jopa.query.soql;

/**
 * Constants of the Semantic Object Query Language (SOQL).
 */
class SoqlConstants {

    /**
     * {@code LIKE} operator.
     */
    static final String LIKE = "LIKE";

    /**
     * {@code DISTINCT} operator.
     */
    static final String DISTINCT = "DISTINCT";

    /**
     * {@code IN} operator.
     */
    static final String IN = "IN";

    /**
     * {@code NOT IN} operator.
     */
    static final String NOT_IN = "NOT IN";

    /**
     * {@code NOT} operator.
     */
    static final String NOT = "NOT";

    /**
     * SPARQL shortcut for {@code rdf:type} - {@code a}.
     */
    static final String RDF_TYPE = "a";

    private SoqlConstants() {
        throw new AssertionError();
    }
}
