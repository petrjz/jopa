package cz.cvut.kbss.jopa.test.integration.jena;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import cz.cvut.kbss.jopa.exceptions.OWLInferredAttributeModifiedException;
import cz.cvut.kbss.jopa.model.EntityManager;
import cz.cvut.kbss.jopa.owlapi.OWLAPIPersistenceProperties;
import cz.cvut.kbss.jopa.test.TestEnvironment;
import cz.cvut.kbss.jopa.test.integration.runners.UpdateOperationsRunner;
import cz.cvut.kbss.jopa.test.utils.JenaTDBStorageConfig;
import cz.cvut.kbss.jopa.test.utils.StorageConfig;
import cz.cvut.kbss.ontodriver.OntoDriverProperties;
import cz.cvut.kbss.ontodriver.impl.jena.DriverCachingJenaFactory;

public class TestUpdateOperationsTDB {

	private static final Logger LOG = Logger.getLogger(TestUpdateOperationsTDB.class.getName());

	private static final StorageConfig storage = initStorage();
	private static final Map<String, String> properties = initProperties();

	private static UpdateOperationsRunner runner;

	private EntityManager em;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		runner = new UpdateOperationsRunner(LOG);
	}

	@After
	public void tearDown() throws Exception {
		if (em.isOpen()) {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
			em.getEntityManagerFactory().close();
		}
		runner.initBeforeTest();
	}

	@Test
	public void testMergeSet() throws Exception {
		em = TestEnvironment.getPersistenceConnector("JenaTDBMergeSet", storage, false, properties);
		runner.mergeSet(em, context());
	}

	@Test
	public void testUpdateDataLeaveLazy() throws Exception {
		em = TestEnvironment.getPersistenceConnector("JenaTDBUpdateDataProperty", storage, false,
				properties);
		runner.updateDataPropertyKeepLazyEmpty(em, context());
	}

	@Test
	public void testUpdateDataPropertySetNull() {
		em = TestEnvironment.getPersistenceConnector("JenaTDBUpdateDataPropertyToNull", storage,
				true, properties);
		runner.updateDataPropertySetNull(em, context());
	}

	@Test
	public void testUpdateReference() {
		em = TestEnvironment.getPersistenceConnector("JenaTDBUpdateReference", storage, true,
				properties);
		runner.updateReference(em, context());
	}

	@Test
	public void testMergeDetachedWithChanges() {
		em = TestEnvironment.getPersistenceConnector("JenaTDBUpdateDetached", storage, true,
				properties);
		runner.mergeDetachedWithChanges(em, context());
	}

	@Test
	public void testMergeDetachedCascade() {
		em = TestEnvironment.getPersistenceConnector("JenaTDBUpdateCascade", storage, true,
				properties);
		runner.mergeDetachedCascade(em, context());
	}

	@Test
	public void testRemoveFromSimpleList() {
		em = TestEnvironment.getPersistenceConnector("JenaTDBUpdateRemoveFromSimpleList", storage,
				true, properties);
		runner.removeFromSimpleList(em, context());
	}

	@Test
	public void testAddToSimpleList() {
		em = TestEnvironment.getPersistenceConnector("JenaTDBUpdateAddToSimpleList", storage, true,
				properties);
		runner.addToSimpleList(em, context());
	}

	@Test
	public void testClearSimpleList() {
		em = TestEnvironment.getPersistenceConnector("JenaTDBUpdateClearSimpleList", storage, true,
				properties);
		runner.clearSimpleList(em, context());
	}

	@Test
	public void testReplaceSimpleList() {
		em = TestEnvironment.getPersistenceConnector("JenaTDBUpdateReplaceSimpleList", storage,
				true, properties);
		runner.replaceSimpleList(em, context());
	}

	@Test
	public void testRemoveFromReferencedList() {
		em = TestEnvironment.getPersistenceConnector("JenaTDBUpdateRemoveFromReferencedList",
				storage, true, properties);
		runner.removeFromReferencedList(em, context());
	}

	@Test
	public void testAddToReferencedList() {
		em = TestEnvironment.getPersistenceConnector("JenaTDBUpdateAddToReferencedList", storage,
				true, properties);
		runner.addToReferencedList(em, context());
	}

	@Test
	public void testClearReferencedList() {
		em = TestEnvironment.getPersistenceConnector("JenaTDBUpdateClearReferencedList", storage,
				true, properties);
		runner.clearReferencedList(em, context());
	}

	@Test
	public void testReplaceReferencedList() {
		em = TestEnvironment.getPersistenceConnector("JenaTDBUpdateReplaceReferencedList", storage,
				true, properties);
		runner.replaceReferencedList(em, context());
	}

	@Test
	public void testAddNewToProperties() {
		em = TestEnvironment.getPersistenceConnector("JenaTDBUpdateAddNewToProperties", storage,
				false, properties);
		runner.addNewToProperties(em, context());
	}

	@Test
	public void testAddPropertyValue() {
		em = TestEnvironment.getPersistenceConnector("JenaTDBUpdateAddPropertyValue", storage,
				false, properties);
		runner.addPropertyValue(em, context());
	}

	@Test(expected = OWLInferredAttributeModifiedException.class)
	public void testModifyInferredAttribute() {
		em = TestEnvironment.getPersistenceConnector("JenaTDBModifyInferredAttribute", storage,
				false, properties);
		runner.modifyInferredAttribute(em, context());
	}

	private URI context() {
		// OWLAPI storages don't use contexts
		return null;
	}

	private static StorageConfig initStorage() {
		return new JenaTDBStorageConfig();
	}

	private static Map<String, String> initProperties() {
		final Map<String, String> map = new HashMap<>();
		map.put(OntoDriverProperties.USE_TRANSACTIONAL_ONTOLOGY, Boolean.TRUE.toString());
		map.put(OntoDriverProperties.JENA_DRIVER_FACTORY, DriverCachingJenaFactory.class.getName());
		map.put(OWLAPIPersistenceProperties.LANG, "en");
		return map;
	}
}