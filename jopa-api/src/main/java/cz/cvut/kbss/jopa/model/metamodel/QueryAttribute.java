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

import cz.cvut.kbss.jopa.NonJPA;
import cz.cvut.kbss.jopa.model.annotations.ParticipationConstraint;

/**
 * Represents an attribute of a Java type that is defined by a query.
 *
 * @param <X> The represented type that contains the attribute
 * @param <Y> The type of the represented attribute
 */
public interface QueryAttribute<X, Y> extends FieldSpecification<X, Y> {

    /**
     * Return the entire query as inserted into {@link cz.cvut.kbss.jopa.model.annotations.Sparql}.
     *
     * @return Query defining this attribute
     */
    @NonJPA
    String getQuery();

    /**
     * Whether referencing other entity attributes is enabled.
     *
     * @return {@code true} when other entity attributes may be referenced from the query
     */
    @NonJPA
    boolean enableReferencingAttributes();

    /**
     * Return the java.lang.reflect.Member for the represented attribute.
     *
     * @return corresponding java.lang.reflect.Member
     */
    java.lang.reflect.Member getJavaMember();

    /**
     * Returns participation constraints specified for this attribute.
     *
     * @return Array of participation constraints
     */
    ParticipationConstraint[] getConstraints();
}
