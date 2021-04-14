package cz.cvut.kbss.jopa.test.query.runner;

import cz.cvut.kbss.jopa.model.CriteriaQueryImpl;
import cz.cvut.kbss.jopa.model.query.TypedQuery;
import cz.cvut.kbss.jopa.model.query.criteria.*;
import cz.cvut.kbss.jopa.sessions.CriteriaFactory;
import cz.cvut.kbss.jopa.test.*;
import cz.cvut.kbss.jopa.test.environment.Generators;
import cz.cvut.kbss.jopa.test.query.QueryTestEnvironment;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import java.util.Comparator;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.jupiter.api.Assertions.*;

public abstract class CriteriaRunner extends BaseQueryRunner {

    protected CriteriaRunner(Logger logger) {
        super(logger);
    }

    @Test
    public void testSimpleFindAll() {
        final List<OWLClassA> expected = QueryTestEnvironment.getData(OWLClassA.class);
        CriteriaFactory factory = getEntityManager().getCriteriaFactory();
        CriteriaQuery<OWLClassA> query = factory.createQuery(OWLClassA.class);
        Root<OWLClassA> root = query.from(OWLClassA.class);
        query.select(root);
        TypedQuery<OWLClassA> tq = getEntityManager().createQuery(query, OWLClassA.class);
        final List<OWLClassA> result = tq.getResultList();

        assertEquals(expected.size(), result.size());
        for (OWLClassA a : result) {
            assertNotNull(a.getStringAttribute());
            assertTrue(expected.stream().anyMatch(aa -> aa.getUri().equals(a.getUri())));
        }
    }

    @Test
    public void testSimpleFindAllWithUntypedCriteriaQuery() {
        final List<OWLClassA> expected = QueryTestEnvironment.getData(OWLClassA.class);
        CriteriaFactory factory = getEntityManager().getCriteriaFactory();
        CriteriaQuery query = factory.createQuery();
        query.select(query.from(OWLClassA.class));
        TypedQuery<OWLClassA> tq = getEntityManager().createQuery(query, OWLClassA.class);
        final List<OWLClassA> result = tq.getResultList();

        assertEquals(expected.size(), result.size());
        for (OWLClassA a : result) {
            assertNotNull(a.getStringAttribute());
            assertTrue(expected.stream().anyMatch(aa -> aa.getUri().equals(a.getUri())));
        }
    }

    @Test
    public void testSimpleCount() {
        final List<OWLClassA> expected = QueryTestEnvironment.getData(OWLClassA.class);
        CriteriaFactory factory = getEntityManager().getCriteriaFactory();
        CriteriaQuery<Integer> query = factory.createQuery(Integer.class);
        Root<OWLClassA> root = query.from(OWLClassA.class);
        query.select(factory.count(root));
        final Integer result = getEntityManager().createQuery(query, Integer.class).getSingleResult();

        assertEquals(expected.size(), result);
    }

    @Test
    public void testFindByDataPropertyAttribute() {
        final OWLClassA expected = Generators.getRandomItem(QueryTestEnvironment.getData(OWLClassA.class));
        CriteriaFactory factory = getEntityManager().getCriteriaFactory();
        CriteriaQuery<OWLClassA> query = factory.createQuery(OWLClassA.class);
        Root<OWLClassA> root = query.from(OWLClassA.class);
        Predicate restriction = factory.equal(root.getAttr(OWLClassA_.stringAttribute),expected.getStringAttribute(),"en");
        query.select(root).where(restriction);
        TypedQuery<OWLClassA> tq = getEntityManager().createQuery(query, OWLClassA.class);
        final OWLClassA result = tq.getSingleResult();

        assertEquals(expected.getUri(), result.getUri());
        assertEquals(expected.getStringAttribute(), result.getStringAttribute());
        assertEquals(expected.getTypes(), result.getTypes());
    }

//    @Test
//    public void testFindByDataNotPropertyAttribute() {
//        final OWLClassA unexpected = Generators.getRandomItem(QueryTestEnvironment.getData(OWLClassA.class));
//        CriteriaFactory factory = getEntityManager().getCriteriaFactory();
//        CriteriaQuery<OWLClassA> query = factory.createQuery(OWLClassA.class);
//        Root<OWLClassA> root = query.from(OWLClassA.class);
//        Predicate restriction = factory.equal(root.getAttr(OWLClassA_.stringAttribute), unexpected.getStringAttribute(),"en");
//        query.select(root).where(factory.not(restriction));
//        TypedQuery<OWLClassA> tq = getEntityManager().createQuery(query, OWLClassA.class);
//        final List<OWLClassA> result = tq.getResultList();
//
//        for (OWLClassA item : result) {
//            assertNotEquals(unexpected.getUri(), item.getUri());
//            assertNotEquals(unexpected.getStringAttribute(), item.getStringAttribute());
//        }
//    }

//    @Test
//    public void testFindByDataNotPropertyAttributeAndPropertyAttribute() {
//        final OWLClassT unexpected = Generators.getRandomItem(QueryTestEnvironment.getData(OWLClassT.class));
//        final int intThreshold = QueryTestEnvironment.getData(OWLClassT.class).size() / 2;
//        CriteriaFactory factory = getEntityManager().getCriteriaFactory();
//        CriteriaQuery<OWLClassT> query = factory.createQuery(OWLClassT.class);
//        Root<OWLClassT> root = query.from(OWLClassT.class);
//        Predicate firstRestriction = factory.equal(root.getAttr("owlClassA"), unexpected.getOwlClassA().getUri());
//        Predicate secondRestriction = factory.lessThan(root.getAttr("intAttribute"), intThreshold);
//        Predicate thirdRestriction = factory.greaterThan(root.getAttr("intAttribute"), 2);
//        Predicate restrictions = factory.and(firstRestriction,secondRestriction,thirdRestriction);
//        query.select(root).where(factory.not(restrictions));
//        TypedQuery<OWLClassT> tq = getEntityManager().createQuery(query, OWLClassT.class);
//        final List<OWLClassT> result = tq.getResultList();
//
//        assertFalse(result.isEmpty());
//        for (OWLClassT item : result) {
//            assertNotEquals(unexpected.getUri(), item.getUri());
//            assertTrue(intThreshold > item.getIntAttribute());
//        }
//    }

    @Test
    public void testFindByObjectPropertyAttribute() {
        final OWLClassD expected = Generators.getRandomItem(QueryTestEnvironment.getData(OWLClassD.class));
        CriteriaFactory factory = getEntityManager().getCriteriaFactory();
        CriteriaQuery<OWLClassD> query = factory.createQuery(OWLClassD.class);
        Root<OWLClassD> root = query.from(OWLClassD.class);
        Predicate restriction = factory.equal(root.getAttr("owlClassA"),expected.getOwlClassA().getUri());
        query.select(root).where(restriction);
        TypedQuery<OWLClassD> tq = getEntityManager().createQuery(query, OWLClassD.class);
        final OWLClassD result = tq.getSingleResult();

        assertEquals(expected.getUri(), result.getUri());
        assertEquals(expected.getOwlClassA().getUri(), result.getOwlClassA().getUri());
    }

    @Test
    public void testFindByConjunctionOfAttributes() {
        final OWLClassT sample = Generators.getRandomItem(QueryTestEnvironment.getData(OWLClassT.class));
        CriteriaFactory factory = getEntityManager().getCriteriaFactory();
        CriteriaQuery<OWLClassT> query = factory.createQuery(OWLClassT.class);
        Root<OWLClassT> root = query.from(OWLClassT.class);
        Predicate firstRestriction = factory.equal(root.getAttr("owlClassA"),sample.getOwlClassA().getUri());
        Predicate secondRestriction = factory.lessThanOrEqual(root.getAttr("intAttribute"),sample.getIntAttribute());
        query.select(root).where(firstRestriction,secondRestriction);
        TypedQuery<OWLClassT> tq = getEntityManager().createQuery(query, OWLClassT.class);
        final List<OWLClassT> result = tq.getResultList();

        assertFalse(result.isEmpty());
        for (OWLClassT item : result) {
            assertEquals(sample.getOwlClassA().getUri(), item.getOwlClassA().getUri());
            assertThat(item.getIntAttribute(), lessThanOrEqualTo(sample.getIntAttribute()));
        }
    }

    @Test
    public void testOrderBy() {
        final List<OWLClassT> expected = QueryTestEnvironment.getData(OWLClassT.class);
        expected.sort(Comparator.comparing(OWLClassT::getIntAttribute));
        CriteriaFactory factory = getEntityManager().getCriteriaFactory();
        CriteriaQuery<OWLClassT> query = factory.createQuery(OWLClassT.class);
        Root<OWLClassT> root = query.from(OWLClassT.class);
        query.select(root).orderBy(factory.asc(root.getAttr("intAttribute")));
        TypedQuery<OWLClassT> tq = getEntityManager().createQuery(query, OWLClassT.class);
        final List<OWLClassT> result = tq.getResultList();

        assertEquals(expected.size(), result.size());
        for (OWLClassT t : result) {
            assertTrue(expected.stream().anyMatch(tt -> tt.getUri().equals(t.getUri())));
        }
    }

    @Test
    public void testFindByDisjunctionOfAttributes() {
        final OWLClassT sample = Generators.getRandomItem(QueryTestEnvironment.getData(OWLClassT.class));
        CriteriaFactory factory = getEntityManager().getCriteriaFactory();
        CriteriaQuery<OWLClassT> query = factory.createQuery(OWLClassT.class);
        Root<OWLClassT> root = query.from(OWLClassT.class);
        Predicate firstRestriction = factory.equal(root.getAttr("owlClassA"),sample.getOwlClassA().getUri());
        Predicate secondRestriction = factory.lessThanOrEqual(root.getAttr("intAttribute"),sample.getIntAttribute());
        query.select(root).where(factory.or(firstRestriction,secondRestriction));
        TypedQuery<OWLClassT> tq = getEntityManager().createQuery(query, OWLClassT.class);
        final List<OWLClassT> result = tq.getResultList();

        assertFalse(result.isEmpty());
        for (OWLClassT item : result) {
            boolean matches = item.getOwlClassA().getUri().equals(sample.getOwlClassA().getUri());
            matches |= item.getIntAttribute() <= sample.getIntAttribute();
            assertTrue(matches);
        }
    }

    @Test
    public void testFindByTransitiveAttributeValue() {
        final OWLClassD expected = Generators.getRandomItem(QueryTestEnvironment.getData(OWLClassD.class));
        CriteriaFactory factory = getEntityManager().getCriteriaFactory();
        CriteriaQuery<OWLClassD> query = factory.createQuery(OWLClassD.class);
        Root<OWLClassD> root = query.from(OWLClassD.class);
        Predicate restrictions = factory.equal(root.getAttr("owlClassA").getAttr("stringAttribute"), expected.getOwlClassA().getStringAttribute(),"en");
        query.select(root).where(restrictions);
        TypedQuery<OWLClassD> tq = getEntityManager().createQuery(query, OWLClassD.class);
        final OWLClassD result = tq.getSingleResult();

        assertEquals(expected.getUri(), result.getUri());
        assertEquals(expected.getOwlClassA().getUri(), result.getOwlClassA().getUri());
    }

    @Test
    public void testFindByParameterExpression() {
        final OWLClassA expected = Generators.getRandomItem(QueryTestEnvironment.getData(OWLClassA.class));
        CriteriaFactory factory = getEntityManager().getCriteriaFactory();
        CriteriaQuery<OWLClassA> query = factory.createQuery(OWLClassA.class);
        Root<OWLClassA> root = query.from(OWLClassA.class);
        final ParameterExpression<String> strAtt = factory.parameter(String.class, "pOne");
        Predicate restriction = factory.equal(root.getAttr(OWLClassA_.stringAttribute), strAtt);
        query.select(root).where(restriction);
        TypedQuery<OWLClassA> tq = getEntityManager().createQuery(query, OWLClassA.class);
        tq.setParameter(strAtt, expected.getStringAttribute(), "en");
        final OWLClassA result = tq.getSingleResult();

        assertEquals(expected.getUri(), result.getUri());
        assertEquals(expected.getStringAttribute(), result.getStringAttribute());
        assertEquals(expected.getTypes(), result.getTypes());
    }

    @Test
    public void testFindByUnnamedParameterExpression() {
        final OWLClassA expected = Generators.getRandomItem(QueryTestEnvironment.getData(OWLClassA.class));
        CriteriaFactory factory = getEntityManager().getCriteriaFactory();
        CriteriaQuery<OWLClassA> query = factory.createQuery(OWLClassA.class);
        Root<OWLClassA> root = query.from(OWLClassA.class);
        final ParameterExpression<String> strAtt = factory.parameter(String.class);
        Predicate restriction = factory.equal(root.getAttr(OWLClassA_.stringAttribute), strAtt);
        query.select(root).where(restriction);
        TypedQuery<OWLClassA> tq = getEntityManager().createQuery(query, OWLClassA.class);
        tq.setParameter(strAtt, expected.getStringAttribute(), "en");
        final OWLClassA result = tq.getSingleResult();

        assertEquals(expected.getUri(), result.getUri());
        assertEquals(expected.getStringAttribute(), result.getStringAttribute());
        assertEquals(expected.getTypes(), result.getTypes());
    }

    @Test
    public void testFindByLiteral() {
        final OWLClassA expected = Generators.getRandomItem(QueryTestEnvironment.getData(OWLClassA.class));
        CriteriaFactory factory = getEntityManager().getCriteriaFactory();
        CriteriaQuery<OWLClassA> query = factory.createQuery(OWLClassA.class);
        Root<OWLClassA> root = query.from(OWLClassA.class);
        Predicate restriction = factory.equal(root.getAttr(OWLClassA_.stringAttribute), factory.literal(expected.getStringAttribute(),"en"));
        query.select(root).where(restriction);
        TypedQuery<OWLClassA> tq = getEntityManager().createQuery(query, OWLClassA.class);
        final OWLClassA result = tq.getSingleResult();

        assertEquals(expected.getUri(), result.getUri());
        assertEquals(expected.getStringAttribute(), result.getStringAttribute());
        assertEquals(expected.getTypes(), result.getTypes());
    }
}
