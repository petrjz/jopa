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
package cz.cvut.kbss.jopa.test;

import cz.cvut.kbss.jopa.model.annotations.Id;
import cz.cvut.kbss.jopa.model.annotations.OWLClass;
import cz.cvut.kbss.jopa.model.annotations.OWLDataProperty;
import cz.cvut.kbss.jopa.model.annotations.Sparql;

import java.net.URI;
import java.util.Set;

@OWLClass(iri = Vocabulary.C_OwlClassWithQueryAttr3)
public class OWLClassWithQueryAttr3 implements HasUri {

    private static final String QUERY = "SELECT ?pluralAttribute\n" +
            "WHERE {?this <" + Vocabulary.P_HAS_SIMPLE_LIST + "> ?pluralAttribute}";

    @Id
    private URI uri;

    @OWLDataProperty(iri = Vocabulary.P_HAS_SIMPLE_LIST, simpleLiteral = true)
    private Set<String> pluralAttribute;

    @Sparql(query = QUERY, enableReferencingAttributes = false)
    private Set<String> pluralQueryAttribute;

    public OWLClassWithQueryAttr3() {
    }

    public OWLClassWithQueryAttr3(URI uri) {
        this.uri = uri;
    }

    public void setUri(URI uri) {
        this.uri = uri;
    }

    @Override
    public URI getUri() {
        return uri;
    }

    public Set<String> getPluralAttribute() {
        return pluralAttribute;
    }

    public void setPluralAttribute(Set<String> pluralAttribute) {
        this.pluralAttribute = pluralAttribute;
    }

    public Set<String> getPluralQueryAttribute() {
        return pluralQueryAttribute;
    }

    public void setPluralQueryAttribute(Set<String> pluralQueryAttribute) {
        this.pluralQueryAttribute = pluralQueryAttribute;
    }

    public static String getSparqlQuery() {
        return QUERY;
    }

    @Override
    public String toString() {
        String out = "OWLClassWithQueryAttr: uri = " + uri;
        out += ", pluralAttribute = " + pluralAttribute;
        return out;
    }
}
