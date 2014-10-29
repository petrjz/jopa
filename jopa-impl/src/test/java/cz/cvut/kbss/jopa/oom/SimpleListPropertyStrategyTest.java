package cz.cvut.kbss.jopa.oom;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import cz.cvut.kbss.jopa.model.annotations.OWLObjectProperty;
import cz.cvut.kbss.jopa.model.annotations.Sequence;
import cz.cvut.kbss.jopa.model.descriptors.Descriptor;
import cz.cvut.kbss.jopa.model.descriptors.EntityDescriptor;
import cz.cvut.kbss.jopa.model.metamodel.EntityType;
import cz.cvut.kbss.jopa.model.metamodel.Identifier;
import cz.cvut.kbss.jopa.model.metamodel.ListAttribute;
import cz.cvut.kbss.jopa.test.OWLClassA;
import cz.cvut.kbss.jopa.test.OWLClassC;
import cz.cvut.kbss.jopa.test.utils.TestEnvironmentUtils;
import cz.cvut.kbss.ontodriver_new.descriptors.SimpleListDescriptor;
import cz.cvut.kbss.ontodriver_new.descriptors.SimpleListValueDescriptor;
import cz.cvut.kbss.ontodriver_new.model.Assertion;
import cz.cvut.kbss.ontodriver_new.model.Axiom;
import cz.cvut.kbss.ontodriver_new.model.AxiomImpl;
import cz.cvut.kbss.ontodriver_new.model.NamedResource;
import cz.cvut.kbss.ontodriver_new.model.Value;

public class SimpleListPropertyStrategyTest {

	private static final URI PK = URI.create("http://krizik.felk.cvut.cz/ontologies/jopa/entityC");

	@Mock
	private EntityType<OWLClassC> etC;

	@Mock
	private ListAttribute simpleList;

	@Mock
	private ListAttribute refList;

	@Mock
	private Identifier idC;

	@Mock
	private EntityType<OWLClassA> etA;

	@Mock
	private Identifier idA;

	@Mock
	private EntityMappingHelper mapperMock;

	@Mock
	private CascadeResolver cascadeResolverMock;

	private Descriptor descriptor;

	private SimpleListPropertyStrategy<OWLClassC> strategy;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		TestEnvironmentUtils.initOWLClassCMocks(etC, simpleList, refList, idC);
		when(mapperMock.getEntityType(OWLClassA.class)).thenReturn(etA);
		when(etA.getIdentifier()).thenReturn(idA);
		when(idA.getJavaField()).thenReturn(OWLClassA.class.getDeclaredField("uri"));

		this.descriptor = new EntityDescriptor();
		this.strategy = new SimpleListPropertyStrategy<>(etC, simpleList, descriptor, mapperMock);
		strategy.setCascadeResolver(cascadeResolverMock);
	}

	@Test
	public void addsValueFromAxiomAndVerifiesCorrectDescriptorWasCreated() {
		final Axiom<URI> ax = new AxiomImpl<>(NamedResource.create(PK),
				Assertion.createObjectPropertyAssertion(simpleList.getIRI().toURI(), false),
				new Value<>(URI.create("http://someSequence.org")));
		final Collection<Axiom<?>> axioms = Collections.emptyList();
		when(mapperMock.loadSimpleList(any(SimpleListDescriptor.class))).thenReturn(axioms);

		strategy.addValueFromAxiom(ax);
		final ArgumentCaptor<SimpleListDescriptor> captor = ArgumentCaptor
				.forClass(SimpleListDescriptor.class);
		verify(mapperMock).loadSimpleList(captor.capture());
		final SimpleListDescriptor res = captor.getValue();
		assertEquals(PK, res.getListOwner().getIdentifier());
		assertEquals(simpleList.getIRI().toURI(), res.getListProperty().getIdentifier());
		assertEquals(simpleList.getOWLObjectPropertyHasNextIRI().toURI(), res.getNextNode()
				.getIdentifier());
		assertNull(res.getContext());
	}

	@Test
	public void buildsInstanceFieldFromAxioms() throws Exception {
		final Axiom<URI> ax = new AxiomImpl<>(NamedResource.create(PK),
				Assertion.createObjectPropertyAssertion(simpleList.getIRI().toURI(), false),
				new Value<>(URI.create("http://someSequence.org")));
		final Collection<Axiom<?>> axioms = new ArrayList<>();
		final List<OWLClassA> entitiesA = new ArrayList<>();
		URI previous = PK;
		for (int i = 0; i < 5; i++) {
			final URI uri = URI.create("http://entity" + i);
			final Axiom<URI> a = new AxiomImpl<>(NamedResource.create(previous),
					Assertion.createObjectPropertyAssertion(simpleList
							.getOWLObjectPropertyHasNextIRI().toURI(), false), new Value<>(uri));
			axioms.add(a);
			final OWLClassA entityA = new OWLClassA();
			entityA.setUri(uri);
			entitiesA.add(entityA);
			when(mapperMock.getEntityFromCacheOrOntology(OWLClassA.class, uri, descriptor))
					.thenReturn(entityA);
			previous = uri;
		}
		when(mapperMock.loadSimpleList(any(SimpleListDescriptor.class))).thenReturn(axioms);

		strategy.addValueFromAxiom(ax);
		final OWLClassC instance = new OWLClassC();
		instance.setUri(PK);
		strategy.buildInstanceFieldValue(instance);
		assertEquals(entitiesA.size(), instance.getSimpleList().size());
		for (OWLClassA a : entitiesA) {
			assertTrue(instance.getSimpleList().contains(a));
		}
	}

	@Test
	public void extractsListValuesForSave() throws Exception {
		final OWLClassC c = new OWLClassC();
		c.setUri(PK);
		c.setSimpleList(generateList());
		final AxiomValueGatherer builder = new AxiomValueGatherer(NamedResource.create(PK), null);
		strategy.extractAttributeValuesFromInstance(c, builder);
		final List<SimpleListValueDescriptor> descriptors = OOMTestUtils
				.getSimpleListValueDescriptors(builder);
		assertEquals(1, descriptors.size());
		final SimpleListValueDescriptor res = descriptors.get(0);
		assertEquals(PK, res.getListOwner().getIdentifier());
		final Field simpleListField = OWLClassC.getSimpleListField();
		assertEquals(simpleListField.getAnnotation(OWLObjectProperty.class).iri(), res
				.getListProperty().getIdentifier().toString());
		assertEquals(simpleListField.getAnnotation(Sequence.class).ObjectPropertyHasNextIRI(), res
				.getNextNode().getIdentifier().toString());
		assertEquals(c.getSimpleList().size(), res.getValues().size());
		for (int i = 0; i < c.getSimpleList().size(); i++) {
			assertEquals(c.getSimpleList().get(i).getUri(), res.getValues().get(i).getIdentifier());
		}
		verify(cascadeResolverMock, times(c.getSimpleList().size())).resolveFieldCascading(
				eq(simpleList), any(Object.class), eq((URI) null));
	}

	@Test
	public void extractsListValuesForSaveListIsEmpty() throws Exception {
		final OWLClassC c = new OWLClassC();
		c.setUri(PK);
		c.setSimpleList(new ArrayList<OWLClassA>());
		final AxiomValueGatherer builder = new AxiomValueGatherer(NamedResource.create(PK), null);
		strategy.extractAttributeValuesFromInstance(c, builder);
		final List<SimpleListValueDescriptor> descriptors = OOMTestUtils
				.getSimpleListValueDescriptors(builder);
		assertEquals(1, descriptors.size());
		final SimpleListValueDescriptor res = descriptors.get(0);
		assertEquals(PK, res.getListOwner().getIdentifier());
		final Field simpleListField = OWLClassC.getSimpleListField();
		assertEquals(simpleListField.getAnnotation(OWLObjectProperty.class).iri(), res
				.getListProperty().getIdentifier().toString());
		assertEquals(simpleListField.getAnnotation(Sequence.class).ObjectPropertyHasNextIRI(), res
				.getNextNode().getIdentifier().toString());
		assertTrue(res.getValues().isEmpty());
		verify(cascadeResolverMock, never()).resolveFieldCascading(
				eq(simpleList), any(Object.class), eq((URI) null));
	}

	@Test
	public void extractsListValuesForSaveListIsNull() throws Exception {
		final OWLClassC c = new OWLClassC();
		c.setUri(PK);
		c.setSimpleList(null);
		final AxiomValueGatherer builder = new AxiomValueGatherer(NamedResource.create(PK), null);
		strategy.extractAttributeValuesFromInstance(c, builder);
		final List<SimpleListValueDescriptor> descriptors = OOMTestUtils
				.getSimpleListValueDescriptors(builder);
		assertEquals(1, descriptors.size());
		final SimpleListValueDescriptor res = descriptors.get(0);
		assertEquals(PK, res.getListOwner().getIdentifier());
		final Field simpleListField = OWLClassC.getSimpleListField();
		assertEquals(simpleListField.getAnnotation(OWLObjectProperty.class).iri(), res
				.getListProperty().getIdentifier().toString());
		assertEquals(simpleListField.getAnnotation(Sequence.class).ObjectPropertyHasNextIRI(), res
				.getNextNode().getIdentifier().toString());
		assertTrue(res.getValues().isEmpty());
		verify(cascadeResolverMock, never()).resolveFieldCascading(
				eq(simpleList), any(Object.class), eq((URI) null));
	}

	private static List<OWLClassA> generateList() {
		final List<OWLClassA> lst = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			final OWLClassA a = new OWLClassA();
			a.setUri(URI.create("http://krizik.felk.cvut.cz/ontologies/jopa/entityA_" + i));
			lst.add(a);
		}
		return lst;
	}
}
