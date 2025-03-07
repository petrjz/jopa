/*
 * JOPA
 * Copyright (C) 2024 Czech Technical University in Prague
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.
 */
package cz.cvut.kbss.jopa.sessions;

import cz.cvut.kbss.jopa.model.EntityState;
import cz.cvut.kbss.jopa.model.LoadState;
import cz.cvut.kbss.jopa.model.descriptors.Descriptor;
import cz.cvut.kbss.jopa.model.lifecycle.PostLoadInvoker;
import cz.cvut.kbss.jopa.model.metamodel.EntityType;
import cz.cvut.kbss.jopa.model.metamodel.FieldSpecification;
import cz.cvut.kbss.jopa.proxy.lazy.LazyLoadingProxyFactory;
import cz.cvut.kbss.jopa.sessions.cache.DisabledCacheManager;
import cz.cvut.kbss.jopa.sessions.change.ObjectChangeSet;
import cz.cvut.kbss.jopa.sessions.descriptor.LoadStateDescriptor;
import cz.cvut.kbss.jopa.sessions.descriptor.LoadStateDescriptorFactory;
import cz.cvut.kbss.jopa.sessions.util.CloneRegistrationDescriptor;
import cz.cvut.kbss.jopa.sessions.util.LoadingParameters;
import cz.cvut.kbss.jopa.utils.Configuration;
import cz.cvut.kbss.jopa.sessions.cache.CacheManager;
import cz.cvut.kbss.jopa.sessions.change.ChangeRecord;
import cz.cvut.kbss.jopa.utils.EntityPropertiesUtils;

import java.lang.reflect.Field;
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static cz.cvut.kbss.jopa.exceptions.OWLEntityExistsException.individualAlreadyManaged;
import static cz.cvut.kbss.jopa.utils.EntityPropertiesUtils.getValueAsURI;

public class ReadOnlyUnitOfWork extends AbstractUnitOfWork {

    final Map<Object, Object> keysToOriginals = new HashMap<>();
    final Set<Object> originalMapping = new HashSet<>();

    private final LazyLoadingProxyFactory lazyLoaderFactory;

    private static final CacheManager disabledCache = new DisabledCacheManager();

    ReadOnlyUnitOfWork(AbstractSession parent, Configuration configuration) {
        super(parent, configuration);
        this.lazyLoaderFactory = new LazyLoadingProxyFactory(this);
    }

    // TODO: remove
    private void debugPrint() {
        final String ANSI_RESET =  "\u001B[0m";
        final String ANSI_YELLOW = "\u001B[33m";
        final String ANSI_BLUE = "\u001B[34m";
        final String ANSI_ORANGE = "\u001B[38;5;208m";

        LOG.info(ANSI_YELLOW + "-------------------------------------------------------------" + ANSI_RESET);

        // these should be empty
        System.out.println("cloneMapping");
        System.out.println(this.cloneMapping);
        System.out.println("cloneToOriginals");
        System.out.println(this.cloneToOriginals);
        System.out.println("keysToClones");
        System.out.println(this.keysToClones);

        // print originals
        System.out.println();
        System.out.println(ANSI_ORANGE + "originalMapping" + ANSI_RESET + "{");
        originalMapping.forEach(o -> System.out.println("   " + o));
        System.out.println("}");
        System.out.println();

        // print keys -> originals
        System.out.println();
        System.out.println(ANSI_ORANGE + "keysToOriginals" + ANSI_RESET + "{");
        keysToOriginals.forEach((k, v) -> System.out.printf("%-15s : %s%n", k, v));
        System.out.println("}");
        System.out.println();

        // print referenceProxies - comment out (referenceProxies is private field)
//        System.out.println();
//        System.out.println(ANSI_ORANGE + "referenceProxies" + ANSI_RESET + "{");
//        referenceProxies.forEach((k, v) -> System.out.printf("%-15s : %s%n", k, v));
//        System.out.println("}");
//        System.out.println();
        LOG.info(ANSI_YELLOW + "-------------------------------------------------------------" + ANSI_RESET);
    }

    @Override
    public void clear() {
        debugPrint();
        LOG.trace("Clearing read-only UOW.");

        // TODO: try not to call super.clear() (there are unnecessary clears on hash sets for clones
        super.clear();
        keysToOriginals.clear();
        originalMapping.clear();
    }

    @Override
    void detachAllManagedInstances() {
        originalMapping.forEach(super::removeLazyLoadingProxies);
    }

    @Override
    public void commit() {
        LOG.trace("UnitOfWork commit started.");
        if (!isActive()) {
            throw new IllegalStateException("Cannot commit inactive Unit of Work!");
        }
        this.clear();
        LOG.trace("UnitOfWork commit finished.");
    }


    protected <T> T readObjectInternal(Class<T> cls, Object identifier, Descriptor descriptor) {
        assert cls != null;
        assert identifier != null;
        assert descriptor != null;

        // check managed objects
        T result = readManagedObject(cls, identifier, descriptor);
        if (result != null) {
            LOG.trace("L1 CACHE hit: Object with identifier {}.", identifier);
            return result;
        }

        // check repository
        LoadingParameters<T> params = new LoadingParameters<>(cls, getValueAsURI(identifier), descriptor);
        result = storage.find(params);
        // TODO: Post load here? yes

        LOG.trace("Read object {} with identifier {}.", result, identifier);

        final Object original = registerExistingObject(result, descriptor);
        return cls.cast(original);
    }

    @Override
    <T> T readManagedObject(Class<T> cls, Object identifier, Descriptor descriptor) {
        // simply return the managed object or null
        return getManagedOriginal(cls, identifier, descriptor);
    }

    @Override
    public <T> T getManagedOriginal(Class<T> cls, Object identifier, Descriptor descriptor) {
        if (!this.keysToOriginals.containsKey(identifier)) { return null; }

        final Object original = keysToOriginals.get(identifier);
        if (!cls.isAssignableFrom(original.getClass())) {
            throw individualAlreadyManaged(identifier);
        }

        return isInRepository(descriptor, original) ? cls.cast(original) : null;
    }

    @Override
    public void unregisterObject(Object object) {
        if (object == null) { return; }

        originalMapping.remove(object);
        keysToOriginals.remove(super.getIdentifier(object));

        super.removeLazyLoadingProxies(object);
        super.unregisterEntityFromOntologyContext(object);
    }

    @Override
    public Object registerExistingObject(Object entity, Descriptor descriptor) {
        LOG.trace("Registering object {}", entity);
        if (entity == null) { return null; }

        if (containsOriginal(entity)) {
            LOG.trace("Original object {} already registered", entity);
            return entity;
        }
        registerOriginal(entity, descriptor);

        // TODO: post load listeners are triggered after registration?
        List.of(new PostLoadInvoker(getMetamodel())).forEach(c -> c.accept(entity));
        return entity;
    }

    private void registerOriginal(Object original, Descriptor descriptor) {
        originalMapping.add(original);
        final Object identifier = super.getIdentifier(original);
        keysToOriginals.put(identifier, original);

        super.registerEntityWithOntologyContext(original, descriptor);
        // TODO: why does this work: (maybe this should not be handled) vzdy bude nastaven
        if (isEntityType(original.getClass()) && !super.getLoadStateRegistry().contains(original)) {
            super.getLoadStateRegistry().put(
                original,
                LoadStateDescriptorFactory.createAllUnknown(original, (EntityType<? super Object>) getMetamodel().entity(original.getClass())));
        }
        processEntityFields(original);
    }

    private void processEntityFields(Object original) {
        // inject lazy loading proxies and process relationships
        final Class<?> originalClass = original.getClass();
        final EntityType<?> et = getMetamodel().entity(originalClass);
        final LoadStateDescriptor<Object> loadState = super.getLoadStateRegistry().get(original);

        for (FieldSpecification<?, ?> fs : et.getFieldSpecifications()) {
            if (fs == et.getIdentifier()) { continue; }   // Already cloned

            final Field f = fs.getJavaField();
            final Object fieldValue = EntityPropertiesUtils.getFieldValue(f, original);
            Object newValue = null;

            if (loadState.isLoaded(fs) == LoadState.NOT_LOADED) {
                newValue = lazyLoaderFactory.createProxy(original, (FieldSpecification<? super Object, ?>) fs);
            } else if (fieldValue == null) {
                continue;
            } else {
                final Class<?> fieldValueClass = fieldValue.getClass();

                if (IndirectWrapperHelper.requiresIndirectWrapper(fieldValue)) {
                    // register objects if possible
                    // TODO: process field correctly (Collection, MultilingualString, Map)
                    if (fieldValue instanceof Iterable) {
                        ((Iterable<?>) fieldValue).forEach(obj -> {
                            if (!super.isEntityType(obj.getClass())) { return; }

                            final Descriptor entityDescriptor = super.getDescriptor(original);
                            final Descriptor fieldDescriptor = super.getFieldDescriptor(original, f, entityDescriptor);
                            registerExistingObject(obj, fieldDescriptor);
                        });
                    }
                    newValue = fieldValue;
                } else if (super.isEntityType(fieldValueClass)) {
                    final Descriptor entityDescriptor = super.getDescriptor(original);
                    final Descriptor fieldDescriptor = super.getFieldDescriptor(original, f, entityDescriptor);
                    newValue = registerExistingObject(fieldValue, fieldDescriptor);
                } else {
                    // We assume that the value is immutable
                    newValue = fieldValue;
                }
            }
            EntityPropertiesUtils.setFieldValue(f, original, newValue);
        }
    }

    @Override
    public Object getOriginal(Object original) {
        // this method does not make sense in this context, this uow does not handle clones
        // simply return the original object
        // TODO: change javadoc
        return original;
    }

    @Override
    boolean containsOriginal(Object entity) {
        assert entity != null;
        return originalMapping.contains(entity);
    }

    @Override
    public <T> Object loadEntityField(T entity, FieldSpecification<? super T, ?> fieldSpec) {
        Objects.requireNonNull(entity);
        Objects.requireNonNull(fieldSpec);
        final Field field = fieldSpec.getJavaField();
        assert field.getDeclaringClass().isAssignableFrom(entity.getClass());

        final Descriptor entityDescriptor = super.getDescriptor(entity);
        final LoadStateDescriptor<?> loadStateDescriptor = loadStateRegistry.get(entity);
        if (loadStateDescriptor.isLoaded(fieldSpec) == LoadState.LOADED) {
            return EntityPropertiesUtils.getFieldValue(field, entity);
        }

        storage.loadFieldValue(entity, fieldSpec, entityDescriptor);
        final Object orig = EntityPropertiesUtils.getFieldValue(field, entity);
        final Object entityOriginal = getOriginal(entity);
        if (entityOriginal != null) {
            EntityPropertiesUtils.setFieldValue(field, entityOriginal, orig);
        }
        final Descriptor fieldDescriptor = super.getFieldDescriptor(entity, field, entityDescriptor);

        if (super.isEntityType(field.getType())) {
            // Single entity
            registerExistingObject(orig, fieldDescriptor);
        } else {
            // Collection or Map
            // TODO: Collection, map or multilingual string,
            // check if the type is entityType
            // this functionality will basically be the same as in processEntityFields
            for (Object o : (Iterable<?>) orig) {
                registerExistingObject(o, fieldDescriptor);
            }
        }

        loadStateDescriptor.setLoaded((FieldSpecification) fieldSpec, LoadState.LOADED);
        return orig;
    }

    @Override
    public boolean isObjectManaged(Object entity) {
        Objects.requireNonNull(entity);
        return this.originalMapping.contains(entity);
    }

    @Override
    public EntityState getState(Object entity) {
        Objects.requireNonNull(entity);
        return originalMapping.contains(entity) ? EntityState.MANAGED : EntityState.NOT_MANAGED;
    }

    @Override
    public EntityState getState(Object entity, Descriptor descriptor) {
        Objects.requireNonNull(entity);
        Objects.requireNonNull(descriptor);

         return originalMapping.contains(entity) && super.isInRepository(descriptor, entity)
                 ? EntityState.MANAGED
                 : EntityState.NOT_MANAGED;
    }

    @Override
    public <T> T getReference(Class<T> cls, Object identifier, Descriptor descriptor) {
        return super.readObject(cls, identifier, descriptor);
    }

    ////////////////////////////////////////////////CACHE METHODS///////////////////////////////////////////////////////
    @Override
    public CacheManager getLiveObjectCache() {
        // either return new instance of disabled cache or
        // return static instance
        return disabledCache;
    }

    private void evictPossiblyUpdatedReferencesFromCache() {
        throw new UnsupportedOperationException("Method not implemented.");
    }

    @Override
    public void putObjectIntoCache(Object identifier, Object entity, Descriptor descriptor) {
        throw new UnsupportedOperationException("putObjectIntoCache: Method not implemented.");
    }

    @Override
    public void removeObjectFromCache(Object toRemove, URI context) {
        throw new UnsupportedOperationException("removeObjectFromCache: Method not implemented.");
    }

    // this is only used in on commit and change tracking uow -> could be deleted
    @Override
    void preventCachingIfReferenceIsNotLoaded(ChangeRecord changeRecord) {
        throw new UnsupportedOperationException("Method not implemented.");
    }

    //////////////////////////////////////THESE METHODS SHOULD NOT BE SUPPORTED/////////////////////////////////////////
    @Override
    public boolean isObjectNew(Object entity) {
        throw new UnsupportedOperationException("Method not implemented.");
    }

    @Override
    public <T> T mergeDetached(T entity, Descriptor descriptor) {
        throw new UnsupportedOperationException("Method not implemented.");
    }

    @Override
    public <T> T mergeDetachedInternal(T entity, Descriptor descriptor) {
        throw new UnsupportedOperationException("Method not implemented.");
    }

    @Override
    protected <T> T getInstanceForMerge(URI identifier, EntityType<T> et, Descriptor descriptor) {
        throw new UnsupportedOperationException("Method not implemented.");
    }

    @Override
    protected void evictAfterMerge(EntityType<?> et, URI identifier, Descriptor descriptor) {
        throw new UnsupportedOperationException("evictAfterMerge: Method not implemented.");
    }

    @Override
    public <T> void refreshObject(T object) {
        throw new UnsupportedOperationException("Method not implemented.");
    }

    @Override
    public Object createIndirectCollection(Object collection, Object owner, Field field) {
        throw new UnsupportedOperationException("createIndirectCollection: Method not implemented.");
    }

    @Override
    public Object registerExistingObject(Object entity, CloneRegistrationDescriptor registrationDescriptor) {
        throw new UnsupportedOperationException("registerExistingObject: Method not implemented.");
    }

    protected static ObjectChangeSet copyChangeSet(ObjectChangeSet changeSet, Object original, Object clone,
                                                   Descriptor descriptor) {
        throw new UnsupportedOperationException("copyChangeSet: Method not implemented.");
    }

    @Override
    protected ObjectChangeSet processInferredValueChanges(ObjectChangeSet changeSet) {
        throw new UnsupportedOperationException("Method not implemented.");
    }

    @Override
    void validateIntegrityConstraints() {
        throw new UnsupportedOperationException("Method not implemented.");
    }

    @Override
    void calculateChanges() {
        throw new UnsupportedOperationException("Method not implemented.");
    }

    @Override
    public void commitToStorage() {
        throw new UnsupportedOperationException("Method not implemented.");
    }

    @Override
    void persistNewObjects() {
        throw new UnsupportedOperationException("Method not implemented.");
    }

    @Override
    void registerClone(Object clone, Object original, Descriptor descriptor) {
        throw new UnsupportedOperationException("Method not implemented.");
    }

    @Override
    public void registerOriginalForNewClone(Object clone, Object original) {
        throw new UnsupportedOperationException("Method not implemented.");
    }

    @Override
    public void writeUncommittedChanges() {
        throw new UnsupportedOperationException("writeUncommitedChanges: Method not implemented.");
    }

    @Override
    public Object getCloneForOriginal(Object original) {
        throw new UnsupportedOperationException("Method not implemented.");
    }

    @Override
    public boolean hasChanges() {
        throw new UnsupportedOperationException("Method not implemented.");
    }

    @Override
    void setHasChanges() {
        throw new UnsupportedOperationException("Method not implemented.");
    }

    @Override
    public void restoreRemovedObject(Object entity) {
        throw new UnsupportedOperationException("restoreRemovedObject: Method not implemented.");
    }

    @Override
    public boolean isFlushingChanges() {
        throw new UnsupportedOperationException("isFlushingChanges: Method not implemented.");
    }

    @Override
    public void attributeChanged(Object entity, Field f) {
        throw new UnsupportedOperationException("attributedChanged: Method not implemented.");
    }

    @Override
    public void attributeChanged(Object entity, FieldSpecification<?, ?> fieldSpec) {
        throw new UnsupportedOperationException("attributeChanged: Method not implemented.");
    }

    @Override
    protected void markCloneForDeletion(Object entity, Object identifier) {
        throw new UnsupportedOperationException("markCloneForDeletion: Method not implemented.");
    }

    @Override
    public void registerNewObject(Object entity, Descriptor descriptor) {
        throw new UnsupportedOperationException("registerNewObject: Method not implemented.");
    }

    @Override
    public void removeObject(Object object) {
        throw new UnsupportedOperationException("removeObject: Method not implemented.");
    }
}
