package cz.cvut.kbss.jopa.test.integration.runners;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import cz.cvut.kbss.jopa.model.EntityManager;
import cz.cvut.kbss.jopa.test.OWLClassA;
import cz.cvut.kbss.jopa.test.OWLClassB;
import cz.cvut.kbss.jopa.test.OWLClassC;
import cz.cvut.kbss.jopa.test.OWLClassD;
import cz.cvut.kbss.jopa.test.OWLClassE;
import cz.cvut.kbss.jopa.test.OWLClassG;
import cz.cvut.kbss.jopa.test.OWLClassH;
import cz.cvut.kbss.jopa.test.OWLClassI;
import cz.cvut.kbss.jopa.test.utils.Generators;

public class CreateOperationsRunner {

	public CreateOperationsRunner() {
		init();
	}

	private OWLClassA entityA;
	private OWLClassB entityB;
	private OWLClassC entityC;
	private OWLClassD entityD;
	// Generated IRI
	private OWLClassE entityE;
	// Lazy reference to OWLClassA
	private OWLClassI entityI;
	// Two relationships
	private OWLClassG entityG;
	private OWLClassH entityH;

	private void init() {
		this.entityA = new OWLClassA();
		this.entityA.setUri(URI.create("http://krizik.felk.cvut.cz/ontologies/jopa/tests/entityA"));
		this.entityA.setStringAttribute("entityAStringAttribute");
		final Set<String> types = new HashSet<String>();
		types.add("http://krizik.felk.cvut.cz/ontologies/jopa/entities#OWLClassU");
		this.entityA.setTypes(types);
		this.entityB = new OWLClassB();
		this.entityB.setUri(URI.create("http://krizik.felk.cvut.cz/ontologies/jopa/tests/entityB"));
		this.entityB.setStringAttribute("entityBStringAttribute");
		this.entityC = new OWLClassC();
		this.entityC.setUri(URI.create("http://krizik.felk.cvut.cz/ontologies/jopa/tests/entityC"));
		this.entityD = new OWLClassD();
		this.entityD.setUri(URI.create("http://krizik.felk.cvut.cz/ontologies/jopa/tests/entityD"));
		this.entityD.setOwlClassA(entityA);
		this.entityE = new OWLClassE();
		this.entityE.setStringAttribute("entityEStringAttribute");
		this.entityI = new OWLClassI();
		this.entityI.setUri(URI.create("http://krizik.felk.cvut.cz/ontologies/jopa/tests/entityI"));
		this.entityI.setOwlClassA(entityA);
		this.entityH = new OWLClassH();
		this.entityH.setUri(URI.create("http://krizik.felk.cvut.cz/ontologies/jopa/tests/entityH"));
		this.entityH.setOwlClassA(entityA);
		this.entityG = new OWLClassG();
		this.entityG.setUri(URI.create("http://krizik.felk.cvut.cz/ontologies/jopa/tests/entityG"));
		this.entityG.setOwlClassH(entityH);
	}

	public void initBeforeTest() {
		entityE.setUri(null);
		entityC.setSimpleList(null);
		entityC.setReferencedList(null);
	}

	public void persistWithGenerated(EntityManager em, URI ctx) {
		em.getTransaction().begin();
		em.persist(entityA, ctx);
		em.persist(entityE, ctx);
		em.getTransaction().commit();

		final OWLClassA resA1 = em.find(OWLClassA.class, entityA.getUri(), ctx);
		assertNotNull(resA1);
		assertEquals(entityA.getStringAttribute(), resA1.getStringAttribute());
		assertEquals(entityA.getTypes().size(), resA1.getTypes().size());
		assertTrue(entityA.getTypes().containsAll(resA1.getTypes()));

		assertNotNull(entityE.getUri());
		final OWLClassE resE = em.find(OWLClassE.class, entityE.getUri(), ctx);
		assertNotNull(resE);
		assertEquals(entityE.getStringAttribute(), resE.getStringAttribute());
	}

	public void persistCascade(EntityManager em, URI ctx) {
		em.getTransaction().begin();
		em.persist(entityG, ctx);
		em.getTransaction().commit();

		final OWLClassA resA2 = em.find(OWLClassA.class, entityA.getUri(), ctx);
		assertNotNull(resA2);
		final OWLClassH resH = em.find(OWLClassH.class, entityH.getUri(), ctx);
		assertNotNull(resH);
		assertEquals(resH.getOwlClassA(), resA2);
		final OWLClassG resG = em.find(OWLClassG.class, entityG.getUri(), ctx);
		assertNotNull(resG);
		assertEquals(resG.getOwlClassH(), resH);
		assertEquals(resG.getOwlClassH().getOwlClassA(), resA2);
	}

	public void persistTwice(EntityManager em, URI ctx) {
		em.getTransaction().begin();
		em.persist(entityB, ctx);
		em.persist(entityB, ctx);
		em.getTransaction().commit();
		fail("This line should not have been reached.");
	}

	public void persistWithoutCascade(EntityManager em, URI ctx) {
		em.getTransaction().begin();
		em.persist(entityD, ctx);
		em.getTransaction().commit();
		fail("This line should not have been reached.");
	}

	public void persistSimpleList(EntityManager em, URI ctx) {
		entityC.setSimpleList(Generators.createSimpleList(5));
		em.getTransaction().begin();
		em.persist(entityC, ctx);
		for (OWLClassA a : entityC.getSimpleList()) {
			em.persist(a, ctx);
		}
		em.getTransaction().commit();

		final OWLClassA a = em.find(OWLClassA.class, entityC.getSimpleList().get(1).getUri(), ctx);
		assertNotNull(a);
		final OWLClassC c = em.find(OWLClassC.class, entityC.getUri(), ctx);
		assertNotNull(c);
		assertNotNull(c.getSimpleList());
		assertFalse(c.getSimpleList().isEmpty());
		assertEquals(entityC.getSimpleList().size(), c.getSimpleList().size());
		assertTrue(c.getSimpleList().contains(a));
	}

	public void persistSimpleListNoCascade(EntityManager em, URI ctx) {
		entityC.setSimpleList(Generators.createSimpleList(10));
		em.getTransaction().begin();
		em.persist(entityC, ctx);
		em.getTransaction().commit();
		fail("This line should not have been reached.");
	}

	public void persistReferencedList(EntityManager em, URI ctx) {
		entityC.setReferencedList(Generators.createReferencedList(5));
		em.getTransaction().begin();
		em.persist(entityC, ctx);
		for (OWLClassA a : entityC.getReferencedList()) {
			em.persist(a, ctx);
		}
		assertTrue(em.contains(entityC));
		assertTrue(em.contains(entityC.getReferencedList().get(0)));
		em.getTransaction().commit();

		final OWLClassC c = em.find(OWLClassC.class, entityC.getUri(), ctx);
		assertNotNull(c);
		assertNotNull(c.getReferencedList());
		assertFalse(c.getReferencedList().isEmpty());
		assertEquals(entityC.getReferencedList().size(), c.getReferencedList().size());
		for (OWLClassA a : entityC.getReferencedList()) {
			final OWLClassA resA = em.find(OWLClassA.class, a.getUri(), ctx);
			assertNotNull(resA);
			assertEquals(a.getStringAttribute(), resA.getStringAttribute());
			assertTrue(c.getReferencedList().contains(resA));
		}
	}

	public void persistReferencedListNoCascade(EntityManager em, URI ctx) {
		entityC.setReferencedList(Generators.createReferencedList(5));
		em.getTransaction().begin();
		em.persist(entityC, ctx);
		em.getTransaction().commit();
		fail("This line should not have been reached.");
	}

	public void persistSimpleAndReferencedList(EntityManager em, URI ctx) {
		entityC.setReferencedList(Generators.createReferencedList(5));
		entityC.setSimpleList(Generators.createSimpleList(5));
		em.getTransaction().begin();
		em.persist(entityC, ctx);
		for (OWLClassA a : entityC.getSimpleList()) {
			em.persist(a, ctx);
		}
		for (OWLClassA a : entityC.getReferencedList()) {
			em.persist(a, ctx);
		}
		em.getTransaction().commit();

		final OWLClassC c = em.find(OWLClassC.class, entityC.getUri(), ctx);
		assertNotNull(c);
		assertNotNull(c.getSimpleList());
		assertEquals(entityC.getSimpleList().size(), c.getSimpleList().size());
		assertNotNull(c.getReferencedList());
		assertEquals(entityC.getReferencedList().size(), c.getReferencedList().size());
		for (OWLClassA a : entityC.getSimpleList()) {
			final OWLClassA resA = em.find(OWLClassA.class, a.getUri(), ctx);
			assertNotNull(resA);
			assertTrue(c.getSimpleList().contains(resA));
		}
		for (OWLClassA a : entityC.getReferencedList()) {
			final OWLClassA resA = em.find(OWLClassA.class, a.getUri(), ctx);
			assertNotNull(resA);
			assertTrue(c.getReferencedList().contains(resA));
		}
	}

	public void persistProperties(EntityManager em, URI ctx) {
		final Map<String, Set<String>> props = new HashMap<>(3);
		props.put("http://krizik.felk.cvut.cz/ontologies/jopa/attributes#propertyOne", Collections
				.singleton("http://krizik.felk.cvut.cz/ontologies/jopa/tests/Individial10"));
		props.put("http://krizik.felk.cvut.cz/ontologies/jopa/attributes#propertyTwo", Collections
				.singleton("http://krizik.felk.cvut.cz/ontologies/jopa/tests/SomeEntity"));
		props.put("http://krizik.felk.cvut.cz/ontologies/jopa/attributes#propertyThree",
				Collections.singleton("http://krizik.felk.cvut.cz/ontologies/jopa/tests/entityG"));
		final Map<String, Set<String>> expected = new HashMap<>(3);
		expected.putAll(props);
		entityB.setProperties(props);
		em.getTransaction().begin();
		em.persist(entityB, ctx);
		em.getTransaction().commit();
		em.clear();

		final OWLClassB res = em.find(OWLClassB.class, entityB.getUri(), ctx);
		assertNotNull(res);
		assertEquals(entityB.getStringAttribute(), res.getStringAttribute());
		assertNotNull(res.getProperties());
		assertFalse(res.getProperties().isEmpty());
		assertEquals(expected.size(), res.getProperties().size());
		for (Entry<String, Set<String>> e : expected.entrySet()) {
			assertTrue(res.getProperties().containsKey(e.getKey()));
			final Set<String> s = e.getValue();
			final Set<String> resS = res.getProperties().get(e.getKey());
			assertNotNull(resS);
			assertEquals(1, resS.size());
			assertEquals(s.iterator().next(), resS.iterator().next());
		}
	}

	public void persistPropertiesEmpty(EntityManager em, URI ctx) {
		entityB.setProperties(Collections.<String, Set<String>> emptyMap());
		em.getTransaction().begin();
		em.persist(entityB, ctx);
		assertTrue(em.contains(entityB));
		em.getTransaction().commit();
		em.clear();

		final OWLClassB b = em.find(OWLClassB.class, entityB.getUri(), ctx);
		assertNotNull(b);
		assertEquals(entityB.getUri(), b.getUri());
		assertEquals(entityB.getStringAttribute(), b.getStringAttribute());
		assertNull(b.getProperties());
	}
}