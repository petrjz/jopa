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

import cz.cvut.kbss.jopa.model.annotations.OWLClass;
import cz.cvut.kbss.jopa.model.annotations.OWLDataProperty;
import cz.cvut.kbss.jopa.vocabulary.DC;

import java.util.Set;

@OWLClass(iri = Vocabulary.C_OwlInterfaceAnMethods)
public interface OWLInterfaceAnMethods {
    @OWLDataProperty(iri = Vocabulary.p_m_unannotated_name)
    void setName(String name);

    @OWLDataProperty(iri = DC.Terms.TITLE)
    Set<String> getTitles();

    String getName();
}
