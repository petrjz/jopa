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
package cz.cvut.kbss.jopa.utils;

import cz.cvut.kbss.jopa.model.JOPAPersistenceProperties;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Specifies how changes to managed objects are tracked in transactions.
 */
public enum ChangeTrackingMode {

    /**
     * Tracks changes immediately as they are made and propagates them to a transaction snapshot of the repository
     * managed by the underlying OntoDriver.
     */
    IMMEDIATE,
    /**
     * Calculates changes on commit, propagating them immediately to the underlying repository.
     */
    ON_COMMIT;

    /**
     * Resolves change tracking mode from the specified configuration.
     *
     * @param configuration Configuration to resolve change tracking mode from
     * @return Resolved {@code ChangeTrackingMode}
     */
    public static ChangeTrackingMode resolve(Configuration configuration) {
        Objects.requireNonNull(configuration);
        final String configValue = configuration.get(JOPAPersistenceProperties.CHANGE_TRACKING_MODE);
        final Optional<ChangeTrackingMode> result = Stream.of(values())
                                                          .filter(m -> m.toString().equalsIgnoreCase(configValue))
                                                          .findAny();
        return result.orElseGet(() -> {
            // For RDF4J driver, no other mode makes sense as the driver does not support transactional snapshots
            if ("cz.cvut.kbss.ontodriver.rdf4j.Rdf4jDataSource".equals(configuration.get(JOPAPersistenceProperties.DATA_SOURCE_CLASS))) {
                return ON_COMMIT;
            }
            // Default legacy mode
            return IMMEDIATE;
        });
    }
}
