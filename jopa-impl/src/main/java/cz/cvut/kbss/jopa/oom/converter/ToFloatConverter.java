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
package cz.cvut.kbss.jopa.oom.converter;

public class ToFloatConverter implements ConverterWrapper<Float, Object> {

    @Override
    public Object convertToAxiomValue(Float value) {
        return value;
    }

    @Override
    public Float convertToAttribute(Object value) {
        assert value != null;
        assert supportsAxiomValueType(value.getClass());
        return ((Number) value).floatValue();
    }

    @Override
    public boolean supportsAxiomValueType(Class<?> type) {
        return Float.class.isAssignableFrom(type) || Long.class.isAssignableFrom(type) || Integer.class
                .isAssignableFrom(type) || Short.class.isAssignableFrom(type) || Byte.class.isAssignableFrom(type);
    }
}
