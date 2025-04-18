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
package cz.cvut.kbss.jopa.sessions.cache;

import cz.cvut.kbss.jopa.model.JOPAPersistenceProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;

/**
 * Creates second level cache based on the specified properties.
 */
public abstract class CacheFactory {

    private static final Logger LOG = LoggerFactory.getLogger(CacheFactory.class);

    private static final String LRU_CACHE = "lru";
    private static final String TTL_CACHE = "ttl";

    private CacheFactory() {
        throw new AssertionError();
    }

    /**
     * Creates new cache based on the specified properties.
     *
     * @param properties Configuration of cache
     * @return Cache implementation
     */
    public static CacheManager createCache(Map<String, String> properties) {
        Objects.requireNonNull(properties);
        final String enabledStr = properties.get(JOPAPersistenceProperties.CACHE_ENABLED);
        if (enabledStr != null && !Boolean.parseBoolean(enabledStr)) {
            LOG.debug("Second level cache is disabled.");
            return new DisabledCacheManager();
        }
        return createEnabledCache(properties);
    }

    private static CacheManager createEnabledCache(Map<String, String> properties) {
        final String cacheType = properties.getOrDefault(JOPAPersistenceProperties.CACHE_TYPE, LRU_CACHE).toLowerCase();
        return switch (cacheType) {
            case LRU_CACHE -> {
                LOG.debug("Using LRU cache.");
                yield new LruCacheManager(properties);
            }
            case TTL_CACHE -> {
                LOG.debug("Using TTL cache.");
                yield new TtlCacheManager(properties);
            }
            default -> throw new IllegalArgumentException("Invalid second level cache type " + cacheType);
        };
    }
}
