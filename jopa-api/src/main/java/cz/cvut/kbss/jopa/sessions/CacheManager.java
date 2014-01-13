package cz.cvut.kbss.jopa.sessions;

import java.net.URI;
import java.util.Set;

/**
 * This interface defines basic methods for accessing the shared live object
 * cache.
 * 
 * @author kidney
 * 
 */
public interface CacheManager extends Cache {

	/**
	 * Adds the specified object into the shared session cache. </p>
	 * 
	 * This method gets the primary key of the object in parameter, so it does
	 * not need to extract it itself.
	 * @param contextUri
	 *            URI of the context to which the specified entity belongs
	 * @param primaryKey
	 *            Primary key of the specified object
	 * @param entity
	 *            The object to be added into the cache
	 */
	public void add(URI contextUri, Object primaryKey, Object entity);

	/**
	 * Gets entity with the specified primary key from the cache.
	 * 
	 * @param cls
	 *            Class of the entity
	 * @param primaryKey
	 *            Primary key of the entity to search for
	 * 
	 * @return Entity with the specified primary key or null
	 */
	public <T> T get(Class<T> cls, Object primaryKey);

	/**
	 * Gets entity with the specified primary key from the cache. </p>
	 * 
	 * The entity is searched for in the context specified by {@code contextUri}
	 * . Thus all three conditions - class, primary key and context must match
	 * to return a result.
	 * @param contextUri
	 *            URI of the context
	 * @param cls
	 *            Class of the entity
	 * @param primaryKey
	 *            Primary key of the entity
	 * 
	 * @return Entity with the specified primary key or {@code null}
	 */
	public <T> T get(URI contextUri, Class<T> cls, Object primaryKey);

	/**
	 * Remove objects with inferred attributes from the cache, since there are
	 * changes in the ontology that might influence the inferred attributes.
	 */
	public void clearInferredObjects();

	/**
	 * Acquire a read lock. </p>
	 * 
	 * Read locks are non-exclusive, i. e. multiple threads can simultaneously
	 * acquire read locks and read the same data. If an exclusive lock is held
	 * (see {@link #acquireWriteLock()}) the thread blocks until the exclusive
	 * lock is released.
	 * 
	 * @return True if the lock was successfully acquired, false otherwise.
	 */
	public boolean acquireReadLock();

	/**
	 * Release the previously acquired read lock. </p>
	 * 
	 * @see #acquireReadLock()
	 */
	public void releaseReadLock();

	/**
	 * Acquire an exclusive write lock. </p>
	 * 
	 * Acquiring write lock exclusively locks the cache manager so the calling
	 * thread can modify the data in the cache. Since the write lock is
	 * exclusive, if another thread already holds a lock (no matter whether
	 * exclusive or non-exclusive), the calling thread waits until the lock is
	 * released.
	 * 
	 * @return True if the lock was successfully acquired, false otherwise.
	 */
	public boolean acquireWriteLock();

	/**
	 * Release the previously acquired write lock. </p>
	 * 
	 * @see #acquireWriteLock()
	 */
	public void releaseWriteLock();

	/**
	 * Set the inferred classes for this cache manager. </p>
	 * 
	 * Entities from inferred classes are special in that when anything in the
	 * ontology changes, they have to be evicted from the cache, since they are
	 * reasoned and their attributes may change.
	 * 
	 * @param inferredClasses
	 *            Set of inferred classes
	 */
	public void setInferredClasses(Set<Class<?>> inferredClasses);

}