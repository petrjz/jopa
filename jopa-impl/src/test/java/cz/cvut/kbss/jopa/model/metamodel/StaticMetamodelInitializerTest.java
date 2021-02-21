package cz.cvut.kbss.jopa.model.metamodel;

import cz.cvut.kbss.jopa.environment.OWLClassA_;
import cz.cvut.kbss.jopa.environment.OWLClassB_;
import cz.cvut.kbss.jopa.environment.OWLClassC_;
import cz.cvut.kbss.jopa.environment.Vocabulary;
import cz.cvut.kbss.jopa.environment.utils.MetamodelMocks;
import cz.cvut.kbss.jopa.exception.StaticMetamodelInitializationException;
import cz.cvut.kbss.jopa.model.annotations.Id;
import cz.cvut.kbss.jopa.model.annotations.OWLClass;
import cz.cvut.kbss.jopa.model.annotations.Properties;
import cz.cvut.kbss.jopa.model.annotations.Types;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.net.URI;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class StaticMetamodelInitializerTest {

    @Mock
    private Metamodel metamodel;

    private MetamodelMocks metamodelMocks;

    private StaticMetamodelInitializer sut;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        this.metamodelMocks = new MetamodelMocks();
        metamodelMocks.setMocks(metamodel);
        this.sut = new StaticMetamodelInitializer(metamodel);
    }

    @Test
    void initializeStaticMetamodelInitializesFieldsOfSimpleEntityStaticMetamodel() {
        when(metamodel.getEntities()).thenReturn(Collections.singleton(metamodelMocks.forOwlClassA().entityType()));
        sut.initializeStaticMetamodel();
        assertEquals(metamodelMocks.forOwlClassA().identifier(), OWLClassA_.uri);
        assertEquals(metamodelMocks.forOwlClassA().stringAttribute(), OWLClassA_.stringAttribute);
        assertEquals(metamodelMocks.forOwlClassA().typesSpec(), OWLClassA_.types);
    }

    @Test
    void initializeStaticMetamodelInitializesPropertiesFieldInStaticMetamodelClass() {
        when(metamodel.getEntities()).thenReturn(Collections.singleton(metamodelMocks.forOwlClassB().entityType()));
        sut.initializeStaticMetamodel();
        assertEquals(metamodelMocks.forOwlClassB().identifier(), OWLClassB_.uri);
        assertEquals(metamodelMocks.forOwlClassB().stringAttribute(), OWLClassB_.stringAttribute);
        assertEquals(metamodelMocks.forOwlClassB().propertiesSpec(), OWLClassB_.properties);
    }

    @Test
    void initializeStaticMetamodelInitializesPluralAttributesInStaticMetamodelClass() {
        when(metamodel.getEntities()).thenReturn(Collections.singleton(metamodelMocks.forOwlClassC().entityType()));
        sut.initializeStaticMetamodel();
        assertEquals(metamodelMocks.forOwlClassC().identifier(), OWLClassC_.uri);
        assertEquals(metamodelMocks.forOwlClassC().referencedListAtt(), OWLClassC_.referencedList);
        assertEquals(metamodelMocks.forOwlClassC().simpleListAtt(), OWLClassC_.simpleList);
    }

    @Test
    void initializeStaticMetamodelThrowsStaticMetamodelInitializationExceptionWhenStaticMetamodelFieldHasNoMetamodelCounterpart() throws Exception {
        final EntityType<NoMatching> et = mock(EntityType.class);
        final Identifier id = mock(Identifier.class);
        when(id.getJavaField()).thenReturn(NoMatching.class.getDeclaredField("uri"));
        when(et.getIdentifier()).thenReturn(id);
        when(et.getDeclaredAttribute(any())).thenThrow(IllegalArgumentException.class);
        when(et.getJavaType()).thenReturn(NoMatching.class);
        when(metamodel.getEntities()).thenReturn(Collections.singleton(et));
        assertThrows(StaticMetamodelInitializationException.class, () -> sut.initializeStaticMetamodel());
    }

    @OWLClass(iri = Vocabulary.CLASS_BASE + "NoMatching")
    public static class NoMatching {

        @Id
        private URI uri;

        @Types
        private Set<String> types;

        @Properties
        private Map<String, Set<String>> properties;
    }

    @StaticMetamodel(NoMatching.class)
    public static class NoMatching_ {
        public static volatile Identifier<NoMatching, URI> uri;

        public static volatile Attribute<NoMatching, String> unknown;
    }

    @Test
    void initializeStaticMetamodelIgnoresClassWithoutMatchingStaticMetamodelAnnotation() throws Exception {
        final EntityType<NoStaticMetamodelAnnotation> et = mock(EntityType.class);
        final Identifier id = mock(Identifier.class);
        when(id.getJavaField()).thenReturn(NoStaticMetamodelAnnotation.class.getDeclaredField("uri"));
        when(et.getIdentifier()).thenReturn(id);
        when(et.getDeclaredAttribute(any())).thenThrow(IllegalArgumentException.class);
        when(et.getJavaType()).thenReturn(NoStaticMetamodelAnnotation.class);
        when(metamodel.getEntities()).thenReturn(Collections.singleton(et));
        assertNull(NoStaticMetamodelAnnotation_.uri);
        sut.initializeStaticMetamodel();
        assertNull(NoStaticMetamodelAnnotation_.uri);
    }

    @OWLClass(iri = Vocabulary.CLASS_BASE + "NoStaticMetamodelAnnotation")
    public static class NoStaticMetamodelAnnotation {
        @Id
        private URI uri;
    }

    public static class NoStaticMetamodelAnnotation_ {
        public static volatile Identifier<NoMatching, URI> uri;
    }

    @Test
    void initializeStaticMetamodelIgnoresEntityIdentifierWhenItIsInheritedFromSuperclass() throws Exception {
        final EntityType<WithInheritedIdentifier> et = mock(EntityType.class);
        final Identifier id = mock(Identifier.class);
        when(id.getJavaField()).thenReturn(NoMatching.class.getDeclaredField("uri"));
        when(et.getIdentifier()).thenReturn(id);
        when(et.getDeclaredAttribute(any())).thenThrow(IllegalArgumentException.class);
        when(et.getJavaType()).thenReturn(WithInheritedIdentifier.class);
        when(metamodel.getEntities()).thenReturn(Collections.singleton(et));
        assertThrows(StaticMetamodelInitializationException.class, () -> sut.initializeStaticMetamodel());
        assertNull(WithInheritedIdentifier_.uri);
    }

    @OWLClass(iri = Vocabulary.CLASS_BASE + "WithInheritedIdentifier")
    public static class WithInheritedIdentifier {

    }

    @StaticMetamodel(WithInheritedIdentifier.class)
    public static class WithInheritedIdentifier_ {
        public static volatile Identifier<WithInheritedIdentifier, URI> uri;
    }

    @Test
    void initializeStaticMetamodelIgnoresTypesSpecificationWhenItIsInheritedFromSuperclass() throws Exception {
        final EntityType<WithInheritedTypes> et = mock(EntityType.class);
        final Identifier id = mock(Identifier.class);
        when(id.getJavaField()).thenReturn(NoMatching.class.getDeclaredField("uri"));
        when(et.getIdentifier()).thenReturn(id);
        final TypesSpecification types = mock(TypesSpecification.class);
        when(types.getJavaField()).thenReturn(NoMatching.class.getDeclaredField("types"));
        when(et.getTypes()).thenReturn(types);
        when(et.getDeclaredAttribute(any())).thenThrow(IllegalArgumentException.class);
        when(et.getJavaType()).thenReturn(WithInheritedTypes.class);
        when(metamodel.getEntities()).thenReturn(Collections.singleton(et));
        assertThrows(StaticMetamodelInitializationException.class, () -> sut.initializeStaticMetamodel());
        assertNull(WithInheritedTypes_.types);
    }

    @OWLClass(iri = Vocabulary.CLASS_BASE + "WithInheritedTypes")
    public static class WithInheritedTypes {

    }

    @StaticMetamodel(WithInheritedTypes.class)
    public static class WithInheritedTypes_ {
        public static volatile TypesSpecification<WithInheritedTypes, String> types;
    }

    @Test
    void initializeStaticMetamodelIgnoresPropertiesSpecificationWhenItIsInheritedFromSuperclass() throws Exception {
        final EntityType<WithInheritedProperties> et = mock(EntityType.class);
        final Identifier id = mock(Identifier.class);
        when(id.getJavaField()).thenReturn(NoMatching.class.getDeclaredField("uri"));
        when(et.getIdentifier()).thenReturn(id);
        final PropertiesSpecification types = mock(PropertiesSpecification.class);
        when(types.getJavaField()).thenReturn(NoMatching.class.getDeclaredField("properties"));
        when(et.getProperties()).thenReturn(types);
        when(et.getDeclaredAttribute(any())).thenThrow(IllegalArgumentException.class);
        when(et.getJavaType()).thenReturn(WithInheritedProperties.class);
        when(metamodel.getEntities()).thenReturn(Collections.singleton(et));
        assertThrows(StaticMetamodelInitializationException.class, () -> sut.initializeStaticMetamodel());
        assertNull(WithInheritedProperties_.properties);
    }

    @OWLClass(iri = Vocabulary.CLASS_BASE + "WithInheritedProperties")
    public static class WithInheritedProperties {

    }

    @StaticMetamodel(WithInheritedProperties.class)
    public static class WithInheritedProperties_ {
        public static volatile PropertiesSpecification<WithInheritedProperties, Set<String>, String, String> properties;
    }
}