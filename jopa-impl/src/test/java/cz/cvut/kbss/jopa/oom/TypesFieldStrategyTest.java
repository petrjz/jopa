/*
 * JOPA
 * Copyright (C) 2025 Czech Technical University in Prague
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
package cz.cvut.kbss.jopa.oom;

import cz.cvut.kbss.jopa.environment.OWLClassA;
import cz.cvut.kbss.jopa.environment.OWLClassP;
import cz.cvut.kbss.jopa.environment.utils.Generators;
import cz.cvut.kbss.jopa.environment.utils.MetamodelMocks;
import cz.cvut.kbss.jopa.model.descriptors.EntityDescriptor;
import cz.cvut.kbss.jopa.model.metamodel.EntityType;
import cz.cvut.kbss.jopa.model.metamodel.TypesSpecification;
import cz.cvut.kbss.ontodriver.model.Assertion;
import cz.cvut.kbss.ontodriver.model.Axiom;
import cz.cvut.kbss.ontodriver.model.AxiomImpl;
import cz.cvut.kbss.ontodriver.model.NamedResource;
import cz.cvut.kbss.ontodriver.model.Value;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class TypesFieldStrategyTest {

    private static final URI IDENTIFIER = Generators.createIndividualIdentifier();

    @Mock
    private EntityMappingHelper mapperMock;

    private AxiomValueGatherer gatherer;

    private MetamodelMocks mocks;

    private OWLClassA entityA;

    @BeforeEach
    public void setUp() throws Exception {
        this.mocks = new MetamodelMocks();
        this.entityA = new OWLClassA();
        entityA.setUri(IDENTIFIER);


        this.gatherer = new AxiomValueGatherer(NamedResource.create(IDENTIFIER), null);
        gatherer.addValue(Assertion.createClassAssertion(false), new Value<>(IDENTIFIER), null);
    }

    private <T> TypesFieldStrategy<T> strategy(EntityType<T> et, TypesSpecification<T, ?> typesSpec) {
        EntityDescriptor descriptor = new EntityDescriptor();
        return new TypesFieldStrategy<>(et, typesSpec,
                                        descriptor.getAttributeDescriptor(typesSpec), mapperMock);
    }

    @Test
    public void extractsTypesForPersist() throws Exception {
        final TypesFieldStrategy<OWLClassA> strategy =
                strategy(mocks.forOwlClassA().entityType(), mocks.forOwlClassA().typesSpec());
        final int count = 5;
        entityA.setTypes(Generators.generateTypes(count));
        when(mapperMock.getOriginalInstance(entityA)).thenReturn(null);

        strategy.buildAxiomValuesFromInstance(entityA, gatherer);

        final Set<URI> toAdd = OOMTestUtils.getTypesToAdd(gatherer);
        assertEquals(count, toAdd.size());
        assertEquals(entityA.getTypes(), toAdd.stream().map(URI::toString).collect(Collectors.toSet()));
        assertThat(OOMTestUtils.getTypesToRemove(gatherer), empty());
    }

    @Test
    public void extractsAddedTypes() throws Exception {
        final TypesFieldStrategy<OWLClassA> strategy =
                strategy(mocks.forOwlClassA().entityType(), mocks.forOwlClassA().typesSpec());
        final int count = 5;
        entityA.setTypes(Generators.generateTypes(count));
        final OWLClassA original = createOriginal();
        when(mapperMock.getOriginalInstance(entityA)).thenReturn(original);
        final Set<String> addedTypes = new HashSet<>(Arrays.asList(Generators.createIndividualIdentifier().toString(),
                                                                   Generators.createIndividualIdentifier().toString()));
        entityA.getTypes().addAll(addedTypes);

        strategy.buildAxiomValuesFromInstance(entityA, gatherer);

        final Set<URI> toAdd = OOMTestUtils.getTypesToAdd(gatherer);
        assertEquals(addedTypes.size(), toAdd.size());
        assertEquals(addedTypes, toAdd.stream().map(URI::toString).collect(Collectors.toSet()));
        assertTrue(OOMTestUtils.getTypesToRemove(gatherer).isEmpty());
    }

    private OWLClassA createOriginal() {
        final OWLClassA a = new OWLClassA();
        a.setUri(IDENTIFIER);
        if (entityA.getTypes() != null) {
            a.setTypes(new HashSet<>(entityA.getTypes()));
        }
        a.setStringAttribute(a.getStringAttribute());
        return a;
    }

    @Test
    public void extractsRemovedTypes() throws Exception {
        final TypesFieldStrategy<OWLClassA> strategy =
                strategy(mocks.forOwlClassA().entityType(), mocks.forOwlClassA().typesSpec());
        final int count = 5;
        entityA.setTypes(Generators.generateTypes(count));
        final OWLClassA original = createOriginal();
        when(mapperMock.getOriginalInstance(entityA)).thenReturn(original);
        final Set<String> removedTypes = removeRandomTypes();
        assertFalse(removedTypes.isEmpty());
        assertFalse(entityA.getTypes().isEmpty());

        strategy.buildAxiomValuesFromInstance(entityA, gatherer);

        final Set<URI> toRemove = OOMTestUtils.getTypesToRemove(gatherer);
        assertFalse(toRemove.isEmpty());
        assertEquals(removedTypes, toRemove.stream().map(URI::toString).collect(Collectors.toSet()));
        assertTrue(OOMTestUtils.getTypesToAdd(gatherer).isEmpty());
    }

    private Set<String> removeRandomTypes() {
        final Set<String> removedTypes = new HashSet<>();
        int i = 0;
        final Iterator<String> it = entityA.getTypes().iterator();
        while (it.hasNext()) {
            if (i % 2 != 0) {
                removedTypes.add(it.next());
                it.remove();
            } else {
                it.next();
            }
            i++;
        }
        return removedTypes;
    }

    @Test
    public void extractsRemovedTypesWhenValueIsNull() throws Exception {
        final TypesFieldStrategy<OWLClassA> strategy =
                strategy(mocks.forOwlClassA().entityType(), mocks.forOwlClassA().typesSpec());
        final int count = 5;
        entityA.setTypes(Generators.generateTypes(count));
        final OWLClassA original = createOriginal();
        when(mapperMock.getOriginalInstance(entityA)).thenReturn(original);
        entityA.setTypes(null);

        strategy.buildAxiomValuesFromInstance(entityA, gatherer);

        final Set<URI> toRemove = OOMTestUtils.getTypesToRemove(gatherer);
        assertFalse(toRemove.isEmpty());
        assertEquals(toRemove.stream().map(URI::toString).collect(Collectors.toSet()), original.getTypes());
        assertThat(OOMTestUtils.getTypesToAdd(gatherer), empty());
    }

    @Test
    public void extractsAddedAndRemovedTypes() throws Exception {
        final TypesFieldStrategy<OWLClassA> strategy =
                strategy(mocks.forOwlClassA().entityType(), mocks.forOwlClassA().typesSpec());
        final int count = 5;
        entityA.setTypes(Generators.generateTypes(count));
        final OWLClassA original = createOriginal();
        when(mapperMock.getOriginalInstance(entityA)).thenReturn(original);
        final Set<String> removedTypes = removeRandomTypes();
        final Set<String> addedTypes = new HashSet<>(Arrays.asList(Generators.createIndividualIdentifier().toString(),
                                                                   Generators.createIndividualIdentifier().toString()));
        entityA.getTypes().addAll(addedTypes);

        strategy.buildAxiomValuesFromInstance(entityA, gatherer);

        final Set<URI> toRemove = OOMTestUtils.getTypesToRemove(gatherer);
        assertFalse(toRemove.isEmpty());
        assertEquals(removedTypes, toRemove.stream().map(URI::toString).collect(Collectors.toSet()));
        final Set<URI> toAdd = OOMTestUtils.getTypesToAdd(gatherer);
        assertEquals(addedTypes.size(), toAdd.size());
        assertEquals(addedTypes, toAdd.stream().map(URI::toString).collect(Collectors.toSet()));
    }

    @Test
    public void extractsNothingWhenThereAreNoTypesAndNoOriginal() throws Exception {
        final TypesFieldStrategy<OWLClassA> strategy =
                strategy(mocks.forOwlClassA().entityType(), mocks.forOwlClassA().typesSpec());
        when(mapperMock.getOriginalInstance(entityA)).thenReturn(null);
        strategy.buildAxiomValuesFromInstance(entityA, gatherer);

        assertThat(OOMTestUtils.getTypesToRemove(gatherer), empty());
        assertThat(OOMTestUtils.getTypesToAdd(gatherer), empty());
    }

    @Test
    public void extractsNothingWhenOriginalTypesAndCurrentTypesAreNull() throws Exception {
        final TypesFieldStrategy<OWLClassA> strategy =
                strategy(mocks.forOwlClassA().entityType(), mocks.forOwlClassA().typesSpec());
        final OWLClassA original = createOriginal();
        when(mapperMock.getOriginalInstance(entityA)).thenReturn(original);

        strategy.buildAxiomValuesFromInstance(entityA, gatherer);

        assertThat(OOMTestUtils.getTypesToRemove(gatherer), empty());
        assertThat(OOMTestUtils.getTypesToAdd(gatherer), empty());
    }

    @Test
    public void extractsTypesToAddWhenOriginalTypesAreNull() throws Exception {
        final TypesFieldStrategy<OWLClassA> strategy =
                strategy(mocks.forOwlClassA().entityType(), mocks.forOwlClassA().typesSpec());
        final int count = 5;
        entityA.setTypes(Generators.generateTypes(count));
        final OWLClassA original = createOriginal();
        original.setTypes(null);
        when(mapperMock.getOriginalInstance(entityA)).thenReturn(original);
        strategy.buildAxiomValuesFromInstance(entityA, gatherer);

        final Set<URI> toAdd = OOMTestUtils.getTypesToAdd(gatherer);
        assertEquals(count, toAdd.size());
    }

    @Test
    public void extractsTypesForPersistFromUriTypes() throws Exception {
        final TypesFieldStrategy<OWLClassP> strategy =
                strategy(mocks.forOwlClassP().entityType(), mocks.forOwlClassP().types());
        final OWLClassP p = new OWLClassP();
        p.setTypes(Generators.generateUriTypes(Generators.DEFAULT_SIZE));

        strategy.buildAxiomValuesFromInstance(p, gatherer);
        final Set<URI> toPersist = OOMTestUtils.getTypesToAdd(gatherer);
        assertEquals(p.getTypes(), toPersist);
    }

    @Test
    public void buildsStringBasedTypesFieldValueFromAxioms() {
        final TypesFieldStrategy<OWLClassA> strategy =
                strategy(mocks.forOwlClassA().entityType(), mocks.forOwlClassA().typesSpec());
        final List<Axiom<URI>> axioms = generateClassAssertionAxioms(OWLClassA.getClassIri());
        axioms.forEach(strategy::addAxiomValue);

        final OWLClassA a = new OWLClassA();
        strategy.buildInstanceFieldValue(a);
        final URI classUri = URI.create(OWLClassA.getClassIri());
        assertEquals(axioms.size() - 1, a.getTypes().size());
        axioms.stream().filter(ax -> !ax.getValue().getValue().equals(classUri))
              .forEach(ax -> assertTrue(a.getTypes().contains(ax.getValue().stringValue())));
    }

    private List<Axiom<URI>> generateClassAssertionAxioms(String javaClassIri) {
        final List<Axiom<URI>> axioms = new ArrayList<>();
        final NamedResource subject = NamedResource.create(IDENTIFIER);
        axioms.add(
                new AxiomImpl<>(subject, Assertion.createClassAssertion(false), new Value<>(URI.create(javaClassIri))));
        final Set<URI> types = Generators.generateUriTypes(Generators.DEFAULT_SIZE);
        types.forEach(u -> axioms.add(new AxiomImpl<>(subject, Assertion.createClassAssertion(false), new Value<>(u))));
        return axioms;
    }

    @Test
    public void buildsUriBasedTypesFieldValueFromAxioms() {
        final TypesFieldStrategy<OWLClassP> strategy =
                strategy(mocks.forOwlClassP().entityType(), mocks.forOwlClassP().types());
        final List<Axiom<URI>> axioms = generateClassAssertionAxioms(OWLClassP.getClassIri());
        axioms.forEach(strategy::addAxiomValue);

        final OWLClassP p = new OWLClassP();
        strategy.buildInstanceFieldValue(p);
        final URI classUri = URI.create(OWLClassP.getClassIri());
        assertEquals(axioms.size() - 1, p.getTypes().size());
        axioms.stream().filter(ax -> !ax.getValue().getValue().equals(classUri))
              .forEach(ax -> assertTrue(p.getTypes().contains(ax.getValue().getValue())));
    }

    @Test
    public void buildInstanceFieldSetsFieldValueToEmptySetWhenNoAxiomsAreLoaded() {
        final TypesFieldStrategy<OWLClassP> strategy =
                strategy(mocks.forOwlClassP().entityType(), mocks.forOwlClassP().types());
        final List<Axiom<URI>> axioms = Collections.singletonList(
                new AxiomImpl<>(NamedResource.create(IDENTIFIER), Assertion.createClassAssertion(false),
                                new Value<>(URI.create(OWLClassP.getClassIri()))));
        axioms.forEach(strategy::addAxiomValue);

        final OWLClassP p = new OWLClassP();
        assertNull(p.getTypes());
        strategy.buildInstanceFieldValue(p);
        assertNotNull(p.getTypes());
        assertTrue(p.getTypes().isEmpty());
    }

    @Test
    void buildAxiomsFromInstanceReturnsAxiomsCorrespondingToAttributeValue() {
        final TypesFieldStrategy<OWLClassA> sut =
                strategy(mocks.forOwlClassA().entityType(), mocks.forOwlClassA().typesSpec());
        final int count = 5;
        entityA.setTypes(Generators.generateTypes(count));

        final Set<Axiom<?>> result = sut.buildAxiomsFromInstance(entityA);
        assertEquals(entityA.getTypes().size(), result.size());
        final Assertion assertion = Assertion.createClassAssertion(false);
        entityA.getTypes().forEach(t -> assertThat(result, hasItem(new AxiomImpl<>(NamedResource.create(IDENTIFIER),
                                                                                   assertion,
                                                                                   new Value<>(URI.create(t))))));
    }

    @Test
    void buildAxiomsFromInstanceReturnsEmptySetForEmptyTypes() {
        entityA.setTypes(Collections.emptySet());
        final TypesFieldStrategy<OWLClassA> sut =
                strategy(mocks.forOwlClassA().entityType(), mocks.forOwlClassA().typesSpec());
        final Set<Axiom<?>> result = sut.buildAxiomsFromInstance(entityA);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
