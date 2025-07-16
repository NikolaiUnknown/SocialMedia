package com.media.socialmedia.Services;

import com.media.socialmedia.util.Caches;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.function.Supplier;

@Service
public class CacheService {

    private final CacheManager manager;

    @Autowired
    public CacheService(CacheManager manager) {
        this.manager = manager;
    }
    @SuppressWarnings("unchecked")
    public <T> T getCacheFrom(Caches cacheName, Long key, Supplier<T> request) throws RedisConnectionFailureException{
        Cache cache = manager.getCache(cacheName.name());
        if (cache == null) throw new RedisConnectionFailureException("Cannot connect to redis");
        Cache.ValueWrapper response = cache.get(key);
        if (response != null){
            return (T) response.get();
        }
        T value = request.get();
        cache.put(key, value);
        return value;
    }

    public void updateInCache(Caches cacheName, Long key, Set<?> set) throws RedisConnectionFailureException{
        Cache cache = manager.getCache(cacheName.name());
        if (cache != null){
            cache.put(key,set);
        }
        else throw new RedisConnectionFailureException("Cannot connect to redis");
    }

    public void evictFromCache(Caches cacheName, Long key)throws RedisConnectionFailureException {
        Cache cache = manager.getCache(cacheName.name());
        if (cache != null){
            cache.evict(key);
        }
        else throw new RedisConnectionFailureException("Cannot connect to redis");
    }
}
