/*
 * JOPA
 * Copyright (C) 2023 Czech Technical University in Prague
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
package cz.cvut.kbss.jopa.test;

import cz.cvut.kbss.jopa.model.annotations.*;
import cz.cvut.kbss.jopa.vocabulary.DC;

import java.net.URI;
import java.util.Set;

@OWLClass(iri = Vocabulary.C_OWL_CLASS_CHILD_A)
public class OWLChildClassA implements OWLParentA, OWLParentB {

    @Id
    private URI id;

    @Types(fetchType = FetchType.EAGER)
    private Set<String> types;

    @OWLAnnotationProperty(iri = DC.Terms.SOURCE)
    private Set<String> pluralAnnotationProperty;

    @OWLDataProperty(iri = Vocabulary.P_E_STRING_ATTRIBUTE)
    private String stringAttribute;

    public URI getId() {
        return id;
    }

    public void setId(URI id) {
        this.id = id;
    }

    public Set<String> getTypes() {
        return types;
    }

    public void setTypes(Set<String> types) {
        this.types = types;
    }

    @Override
    public String getStringAttribute() {
        return stringAttribute;
    }

    @Override
    public void setStringAttribute(String stringAttribute) {
        this.stringAttribute = stringAttribute;
    }

    @Override
    public Set<String> getPluralAnnotationProperty() {
        return pluralAnnotationProperty;
    }

    @Override
    public void setPluralAnnotationProperty(Set<String> pluralAnnotationProperty) {
        this.pluralAnnotationProperty = pluralAnnotationProperty;
    }
}
