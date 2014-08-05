package cz.cvut.kbss.jopa.sessions;

import java.net.URI;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import cz.cvut.kbss.jopa.accessors.StorageAccessor;
import cz.cvut.kbss.jopa.accessors.StorageAccessorImpl;
import cz.cvut.kbss.jopa.exceptions.OWLPersistenceException;
import cz.cvut.kbss.jopa.model.metamodel.EntityType;
import cz.cvut.kbss.jopa.model.metamodel.Metamodel;
import cz.cvut.kbss.jopa.model.metamodel.Type;
import cz.cvut.kbss.jopa.owlapi.AbstractEntityManager;
import cz.cvut.kbss.jopa.owlapi.OWLAPIPersistenceProperties;
import cz.cvut.kbss.jopa.transactions.EntityTransaction;
import cz.cvut.kbss.ontodriver.Connection;
import cz.cvut.kbss.ontodriver.OntologyStorageProperties;
import cz.cvut.kbss.ontodriver.exceptions.OntoDriverException;

/**
 * The ServerSession is the primary interface for accessing the ontology. </p>
 * 
 * It manages an accessor object, which performs the queries. NOTE: In the
 * future there should be a pool of accessors, since we will be dealing with
 * parallel access from many clients.
 * 
 * @author kidney
 * 
 */
public class ServerSession extends AbstractSession {

	private final Metamodel metamodel;
	private final Set<Class<?>> managedClasses;

	private CacheManager liveObjectCache;
	private StorageAccessor storageAccessor;

	private Map<EntityTransaction, AbstractEntityManager> runningTransactions;
	private Map<Object, UnitOfWorkImpl> activePersistenceContexts;
	private Map<UnitOfWorkImpl, Set<Object>> uowsToEntities;

	protected ServerSession() {
		this.metamodel = null;
		this.managedClasses = null;
	}

	public ServerSession(OntologyStorageProperties storageProperties,
			Map<String, String> properties, Metamodel metamodel) {
		this.metamodel = metamodel;
		this.managedClasses = processTypes(metamodel.getEntities());
		initialize(storageProperties, properties, metamodel);
	}

	/**
	 * Process the entity types and extract simple Java classes from them.
	 * 
	 * @param entities
	 *            Set of managed entity types.
	 * @return Set of managed entity classes.
	 */
	private Set<Class<?>> processTypes(Set<EntityType<?>> entities) {
		Set<Class<?>> types = new HashSet<Class<?>>(entities.size());
		for (Type<?> t : entities) {
			types.add(t.getJavaType());
		}
		return types;
	}

	/**
	 * Initializes this ServerSession. This in particular means initialization
	 * of the ontology accessor and live object cache.
	 * 
	 * @param properties
	 *            Map of setup properties.
	 * @param metamodel
	 *            Metamodel of the managed classes and their attributes.
	 * @param factory
	 *            Factory for creating ontology accessors.
	 */
	private void initialize(OntologyStorageProperties storageProperties,
			Map<String, String> properties, Metamodel metamodel) {
		assert properties != null;
		assert metamodel != null;
		this.runningTransactions = new WeakHashMap<>();
		this.activePersistenceContexts = new WeakHashMap<>();
		this.uowsToEntities = new WeakHashMap<>();
		this.storageAccessor = new StorageAccessorImpl(metamodel, this, storageProperties,
				properties);
		String cache = properties.get(OWLAPIPersistenceProperties.CACHE_PROPERTY);
		if (cache == null || cache.equals("on")) {
			this.liveObjectCache = new CacheManagerImpl(properties);
			liveObjectCache.setInferredClasses(metamodel.getInferredClasses());
		} else {
			this.liveObjectCache = new DisabledCacheManager();
		}
	}

	protected Connection acquireConnection() {
		return storageAccessor.acquireConnection();
	}

	@Override
	public UnitOfWork acquireUnitOfWork() {
		return new UnitOfWorkImpl(this);
	}

	public CacheManager getLiveObjectCache() {
		return liveObjectCache;
	}

	public boolean transactionStarted(EntityTransaction t, AbstractEntityManager em) {
		if (!t.isActive() || t.isRollbackOnly()) {
			return false;
		}
		runningTransactions.put(t, em);
		return true;
	}

	public void transactionFinished(EntityTransaction t) {
		if (t == null) {
			return;
		}
		AbstractEntityManager em = runningTransactions.remove(t);
		if (em == null) {
			return;
		}
		UnitOfWorkImpl uow = (UnitOfWorkImpl) em.getCurrentPersistenceContext();
		if (uow != null && uow.hasChanges()) {
			getLiveObjectCache().clearInferredObjects();
		}
		removePersistenceContext(uow);
	}

	/**
	 * Close the server session and all connections to the underlying data
	 * source.
	 */
	public void close() {
		if (!runningTransactions.isEmpty()) {
			LOG.warning("There are still transactions running. Marking them for rollback.");
			for (EntityTransaction t : runningTransactions.keySet()) {
				if (t.isActive()) {
					t.setRollbackOnly();
				}
			}
		}
		if (storageAccessor != null && storageAccessor.isOpen()) {
			try {
				storageAccessor.close();
			} catch (OntoDriverException e) {
				throw new OWLPersistenceException(e);
			}
		}
	}

	@Override
	public void removeObjectFromCache(Object object, URI context) {
		// do nothing
	}

	public Set<Class<?>> getManagedTypes() {
		return this.managedClasses;
	}

	@Override
	protected Metamodel getMetamodel() {
		return metamodel;
	}

	/**
	 * Register the specified entity as managed in the specified
	 * {@code UnitOfWork}. </p>
	 * 
	 * Registering loaded entities with their owning {@code UnitOfWork} is
	 * highly recommended, since it speeds up persistence context lookup when
	 * entity attributes are modified.
	 * 
	 * @param entity
	 *            The entity to register
	 * @param uow
	 *            Persistence context of the specified entity
	 */
	protected synchronized void registerEntityWithPersistenceContext(Object entity,
			UnitOfWorkImpl uow) {
		assert entity != null;
		assert uow != null;

		activePersistenceContexts.put(entity, uow);
		if (!uowsToEntities.containsKey(uow)) {
			uowsToEntities.put(uow, new HashSet<Object>());
		}
		uowsToEntities.get(uow).add(entity);
	}

	/**
	 * Get persistence context for the specified entity. </p>
	 * 
	 * @param entity
	 *            The entity
	 * @return Persistence context of the specified entity or null, if it cannot
	 *         be found
	 */
	public synchronized UnitOfWorkImpl getPersistenceContext(Object entity) {
		if (entity == null) {
			return null;
		}
		final UnitOfWorkImpl uow = activePersistenceContexts.get(entity);
		return uow;
	}

	/**
	 * Remove the specified {@code UnitOfWork} from the list of currently active
	 * persistence contexts. </p>
	 * 
	 * Also remove all the objects associated with this persistence context.
	 * 
	 * @param uow
	 *            The persistence context to remove
	 */
	private void removePersistenceContext(UnitOfWorkImpl uow) {
		if (uowsToEntities.containsKey(uow)) {
			for (Object entity : uowsToEntities.get(uow)) {
				activePersistenceContexts.remove(entity);
			}
		}
		uowsToEntities.remove(uow);
	}
}
