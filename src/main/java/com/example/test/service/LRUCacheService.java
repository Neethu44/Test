package com.example.test.service;

import com.example.test.entity.CacheEntity;
import com.example.test.repository.LRUCacheRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LRUCacheService {

    private final LRUCacheRepository repository;

    @Value("${cache.capacity}")
    private int capacity;

    // Fast in-memory storage synced with the database
    private final Map<String, String> memCache = new ConcurrentHashMap<>();

    public LRUCacheService(LRUCacheRepository repository) {
        this.repository = repository;
    }

    /**
     * 1. Restore state from database on startup.
     * Re-populates the in-memory map so reads work immediately after restart.
     */
    @PostConstruct
    public void init() {
        repository.findAll().forEach(entry ->
                memCache.put(entry.getKey(), entry.getValue())
        );
    }

    /**
     * 2. Get item.
     * Serves fast value from memory, updates the DB timestamp for LRU persistence.
     */
    @Transactional
    public String get(String key) {
        if (!memCache.containsKey(key)) {
            return null;
        }

        // FIX: Changed save() to saveAndFlush() to force the timestamp update on disk instantly
        repository.findById(key).ifPresent(entry -> {
            entry.setLastAccessed(LocalDateTime.now());
            repository.saveAndFlush(entry);
        });

        return memCache.get(key);
    }

    /**
     * 3. Put item (Add/Update).
     * Saves to memory and DB. Evicts the least recently used item if full.
     */
    @Transactional
    public void put(String key, String value) {
        // If it's a new key and we are at capacity, evict the oldest from DB and memory
        if (memCache.size() >= capacity && !memCache.containsKey(key)) {
            CacheEntity oldest = repository.findFirstByOrderByLastAccessedAsc();
            if (oldest != null) {
                memCache.remove(oldest.getKey());
                repository.delete(oldest);
                repository.flush(); // FIX: Force immediate disk removal before writing new entry
            }
        }

        // Save to memory
        memCache.put(key, value);

        // FIX: Changed save() to saveAndFlush() to commit the insertion right now
        repository.saveAndFlush(new CacheEntity(key, value, LocalDateTime.now()));
    }

    /**
     * 4. Delete by ID.
     * Removals are instantly synchronized between the memory map and database table.
     */
    @Transactional
    public boolean delete(String key) {
        if (!memCache.containsKey(key)) {
            return false;
        }

        // Remove from memory cache
        memCache.remove(key);

        // Remove from database
        repository.deleteById(key);
        return true;
    }

    /**
     * 5. Delete all.
     * Completely purges all data from memory and hard wipes the database table.
     */
    @Transactional
    public void deleteAll() {
        // Clear memory cache
        memCache.clear();

        // Clear database table
        repository.deleteAll();
    }

    @Transactional(readOnly = true)
    public List<CacheEntity> getAllMruFirst() {
        return repository.findAllByOrderByLastAccessedDesc();
    }
}