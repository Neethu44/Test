package com.example.test.controller;

import com.example.test.entity.CacheEntity;
import com.example.test.service.LRUCacheService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cache")
public class LRUCacheController {
    private final LRUCacheService lruCacheService;

    public LRUCacheController(LRUCacheService lruCacheService){
        this.lruCacheService = lruCacheService;
    }

    @PostMapping
    public ResponseEntity<String> put(@RequestParam String key, @RequestParam String value) {
        lruCacheService.put(key, value);
        return ResponseEntity.ok("Saved key: " + key);
    }

    @GetMapping("/{key}")
    public ResponseEntity<String> get(@PathVariable String key) {
        String value = lruCacheService.get(key);
        if (value == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(value);
    }

    @DeleteMapping("/{key}")
    public ResponseEntity<String> delete(@PathVariable String key) {
        boolean deleted = lruCacheService.delete(key);
        if (!deleted) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok("Deleted key: " + key);
    }

    // Clear the entire cache
    @DeleteMapping
    public ResponseEntity<String> deleteAll() {
        lruCacheService.deleteAll();
        return ResponseEntity.ok("Cleared all cache entries.");
    }

    // Get all items in MRU order
    @GetMapping("/all")
    public ResponseEntity<List<CacheEntity>> getAll() {
        List<CacheEntity> entries = lruCacheService.getAllMruFirst();
        return ResponseEntity.ok(entries);
    }
}
