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
package cz.cvut.kbss.ontodriver.owlapi.config;

public class Constants {

    /**
     * Default language to use when an {@link cz.cvut.kbss.ontodriver.model.Assertion} does not specify a language.
     * <p>
     * The {@code null} value ensures that strings will be saved as xsd:string and loaded with any language tag (or
     * without a language tag at all).
     */
    public static final String DEFAULT_LANGUAGE = null;

    private Constants() {
        throw new AssertionError();
    }
}
