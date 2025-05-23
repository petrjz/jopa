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
package cz.cvut.kbss.jopa.model.metamodel;

import cz.cvut.kbss.jopa.environment.Vocabulary;
import cz.cvut.kbss.jopa.exception.InvalidFieldMappingException;
import cz.cvut.kbss.jopa.model.IRI;
import cz.cvut.kbss.jopa.model.annotations.CascadeType;
import cz.cvut.kbss.jopa.model.annotations.RDFContainerType;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class RdfContainerAttributeImplTest {

    @Test
    void getCollectionTypeReturnsListForRdfSeq() {
        final DataPropertyAttributes pa = new DataPropertyAttributes(null);
        pa.type = BasicTypeImpl.get(Integer.class);
        pa.iri = IRI.create(Vocabulary.p_p_gender);
        pa.cascadeTypes = new CascadeType[0];
        pa.persistentAttributeType = Attribute.PersistentAttributeType.DATA;

        final RdfContainerAttributeImpl<?, ?, ?> sut = (RdfContainerAttributeImpl<?, ?, ?>) RdfContainerAttributeImpl.builder(pa)
                                                                                                                     .containerType(RDFContainerType.SEQ)
                                                                                                                     .elementType(BasicTypeImpl.get(Integer.class))
                                                                                                                     .collectionType(List.class)
                                                                                                                     .build();
        assertEquals(CollectionType.LIST, sut.getCollectionType());
    }

    @Test
    void getCollectionTypeReturnsSetForRdfAlt() {
        final DataPropertyAttributes pa = new DataPropertyAttributes(null);
        pa.type = BasicTypeImpl.get(Integer.class);
        pa.iri = IRI.create(Vocabulary.p_p_gender);
        pa.cascadeTypes = new CascadeType[0];
        pa.persistentAttributeType = Attribute.PersistentAttributeType.DATA;

        final RdfContainerAttributeImpl<?, ?, ?> sut = (RdfContainerAttributeImpl<?, ?, ?>) RdfContainerAttributeImpl.builder(pa)
                                                                                                                     .containerType(RDFContainerType.ALT)
                                                                                                                     .elementType(BasicTypeImpl.get(Integer.class))
                                                                                                                     .collectionType(Set.class)
                                                                                                                     .build();
        assertEquals(CollectionType.SET, sut.getCollectionType());
    }

    @Test
    void getCollectionTypeReturnsCollectionForRdfBag() {
        final DataPropertyAttributes pa = new DataPropertyAttributes(null);
        pa.type = BasicTypeImpl.get(Integer.class);
        pa.iri = IRI.create(Vocabulary.p_p_gender);
        pa.cascadeTypes = new CascadeType[0];
        pa.persistentAttributeType = Attribute.PersistentAttributeType.DATA;

        final RdfContainerAttributeImpl<?, ?, ?> sut = (RdfContainerAttributeImpl<?, ?, ?>) RdfContainerAttributeImpl.builder(pa)
                                                                                                                     .containerType(RDFContainerType.BAG)
                                                                                                                     .elementType(BasicTypeImpl.get(Integer.class))
                                                                                                                     .collectionType(List.class)
                                                                                                                     .build();
        assertEquals(CollectionType.COLLECTION, sut.getCollectionType());
    }

    @Test
    void rdfSeqWithJavaTypeSetThrowsInvalidFieldMappingException() {
        final DataPropertyAttributes pa = new DataPropertyAttributes(null);
        pa.type = BasicTypeImpl.get(Integer.class);
        pa.iri = IRI.create(Vocabulary.p_p_gender);
        pa.cascadeTypes = new CascadeType[0];
        pa.persistentAttributeType = Attribute.PersistentAttributeType.DATA;

        assertThrows(InvalidFieldMappingException.class, () -> RdfContainerAttributeImpl.builder(pa)
                                                                                 .containerType(RDFContainerType.SEQ)
                                                                                 .elementType(BasicTypeImpl.get(Integer.class))
                                                                                 .collectionType(Set.class)
                                                                                 .build());
    }

    @Test
    void rdfAltWithJavaTypeListThrowsInvalidFieldMappingException() {
        final DataPropertyAttributes pa = new DataPropertyAttributes(null);
        pa.type = BasicTypeImpl.get(Integer.class);
        pa.iri = IRI.create(Vocabulary.p_p_gender);
        pa.cascadeTypes = new CascadeType[0];
        pa.persistentAttributeType = Attribute.PersistentAttributeType.DATA;

        assertThrows(InvalidFieldMappingException.class, () -> RdfContainerAttributeImpl.builder(pa)
                                                                                        .containerType(RDFContainerType.ALT)
                                                                                        .elementType(BasicTypeImpl.get(Integer.class))
                                                                                        .collectionType(List.class)
                                                                                        .build());
    }
}
