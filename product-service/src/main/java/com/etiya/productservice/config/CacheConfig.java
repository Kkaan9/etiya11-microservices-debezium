package com.etiya.productservice.config;

import java.time.Duration;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Distributed (Redis-backed) cache for product read operations. GET responses are cached here;
 * every write (add/update/delete) and every stock change applied by {@code ProductStockService}
 * evicts the affected entries so readers never observe stale data.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /** Cache for a single product, keyed by product id. */
    public static final String PRODUCTS_CACHE = "products";

    /** Cache for the full product listing (single entry, no key). */
    public static final String PRODUCTS_LIST_CACHE = "productsList";

    private static final Duration CACHE_TTL = Duration.ofMinutes(10);

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        GenericJackson2JsonRedisSerializer valueSerializer =
                new GenericJackson2JsonRedisSerializer(redisObjectMapper());

        RedisCacheConfiguration cacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(CACHE_TTL)
                .disableCachingNullValues()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(valueSerializer));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(cacheConfiguration)
                .build();
    }

    /**
     * Cached values carry their concrete type so the same "products" cache instance can be
     * reused for different response DTOs. Default typing itself is configured internally by
     * {@link GenericJackson2JsonRedisSerializer} (calling {@code activateDefaultTyping} here too
     * would fight its own type-resolution setup and corrupt round-tripping of collection values
     * such as the cached product list). Only the validator is set, restricting which classes may
     * carry embedded type info to this service's own DTOs and java.util classes.
     */
    private ObjectMapper redisObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        PolymorphicTypeValidator typeValidator = BasicPolymorphicTypeValidator.builder()
                .allowIfSubType("com.etiya.productservice.services.dtos")
                .allowIfSubType("java.util")
                .build();
        objectMapper.setPolymorphicTypeValidator(typeValidator);

        return objectMapper;
    }
}
