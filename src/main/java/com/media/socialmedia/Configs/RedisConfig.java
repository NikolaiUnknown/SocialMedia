package com.media.socialmedia.Configs;

import com.media.socialmedia.util.Caches;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
public class RedisConfig {
    @Value("${spring.data.redis.dataLifetime}")
    private int dataLifetime;
    @Value("${spring.data.redis.setsLifetime}")
    private int setsLifetime;

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory){
        RedisCacheConfiguration config = RedisCacheConfiguration
                .defaultCacheConfig()
                .serializeKeysWith(
                        RedisSerializationContext
                                .SerializationPair
                                .fromSerializer(new StringRedisSerializer())
                )
                .serializeValuesWith(
                        RedisSerializationContext
                                .SerializationPair
                                .fromSerializer(new GenericJackson2JsonRedisSerializer())
                );
        Map<String, RedisCacheConfiguration> configurationMap = new HashMap<>();
        configurationMap.put(Caches.USERS.name(), config.entryTtl(Duration.ofMillis(dataLifetime)));
        configurationMap.put(Caches.POST.name(), config.entryTtl(Duration.ofMillis(dataLifetime)));
        configurationMap.put(Caches.FRIENDS.name(), config.entryTtl(Duration.ofMillis(setsLifetime)));
        configurationMap.put(Caches.FRIENDS_OF.name(), config.entryTtl(Duration.ofMillis(setsLifetime)));
        configurationMap.put(Caches.BLACKLIST.name(), config.entryTtl(Duration.ofMillis(setsLifetime)));
        configurationMap.put(Caches.INVITES.name(), config.entryTtl(Duration.ofMillis(setsLifetime)));
        configurationMap.put(Caches.INVITES_OF.name(), config.entryTtl(Duration.ofMillis(setsLifetime)));
        configurationMap.put(Caches.POSTS.name(), config.entryTtl(Duration.ofMillis(setsLifetime)));
        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config)
                .withInitialCacheConfigurations(configurationMap)
                .build();
    }
    @Bean
    public ModelMapper mapper(){
        return new ModelMapper();
    }
}
