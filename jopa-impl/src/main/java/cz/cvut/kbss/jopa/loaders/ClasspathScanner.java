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
package cz.cvut.kbss.jopa.loaders;

import java.util.function.Consumer;

/**
 * Scans application classpath, allowing to find classes of significance (e.g., entity classes).
 */
public interface ClasspathScanner {

    /**
     * Registers a listener to which discovered classes will be passed for processing.
     *
     * @param listener Listener to invoke with discovered classes
     */
    void addListener(Consumer<Class<?>> listener);

    /**
     * Start class processing, looking for classes in the specified package (and its descendants).
     *
     * @param scanPackage Package to scan
     */
    void processClasses(String scanPackage);
}
