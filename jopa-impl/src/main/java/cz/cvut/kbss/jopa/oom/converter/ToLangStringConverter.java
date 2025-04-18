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
package cz.cvut.kbss.jopa.oom.converter;

import cz.cvut.kbss.ontodriver.model.LangString;

/**
 * Converts language tagged and language-less values to {@link LangString} attributes.
 * <p>
 * This is for the rare case that {@link LangString} is used as attribute type instead of the recommended {@link
 * cz.cvut.kbss.jopa.model.MultilingualString}.
 */
public class ToLangStringConverter implements ConverterWrapper<LangString, Object> {
    @Override
    public Object convertToAxiomValue(LangString value) {
        return value;
    }

    @Override
    public LangString convertToAttribute(Object value) {
        assert value != null;
        assert supportsAxiomValueType(value.getClass());
        return value instanceof LangString ? (LangString) value : new LangString(value.toString());
    }

    @Override
    public boolean supportsAxiomValueType(Class<?> type) {
        return String.class.equals(type) || LangString.class.isAssignableFrom(type);
    }
}
