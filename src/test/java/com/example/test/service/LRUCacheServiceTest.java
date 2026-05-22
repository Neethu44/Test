package com.example.test.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest
class LRUCacheServiceTest {

    @Autowired
    private LRUCacheService cacheService;

    @Value("${cache.capacity}")
    private int capacity;

    @BeforeEach
    void setUp() {
        // Clear all entries to ensure a completely clean state for each test
        cacheService.deleteAll();
    }

    @Test
    void get_OnMissingKey_ReturnsNull() {
        // Act: Attempt to retrieve a key that does not exist
        String result = cacheService.get("missingKey");

        // Assert: Ensure it accurately returns null
        assertNull(result);
    }

    @Test
    void put_BeyondCapacity_CorrectlyEvictsLeastRecentlyUsedEntry() throws InterruptedException {
        // Handle edge case where configuration capacity is set to 1
        if (capacity == 1) {
            cacheService.put("A", "1");
            Thread.sleep(10);
            cacheService.put("B", "2"); // Instantly evicts A

            assertNull(cacheService.get("A"), "Key A should be evicted when capacity is 1!");
            assertEquals("2", cacheService.get("B"));
            return; // Exit test early for capacity 1
        }

        // 1. Fill the cache dynamically up to its configured limit
        for (int i = 0; i < capacity; i++) {
            cacheService.put("KEY_" + i, "VAL_" + i);
            Thread.sleep(10);
        }

        // 2. Refresh the very first item ("KEY_0") to make it the most recently used (MRU)
        String value0 = cacheService.get("KEY_0");
        assertEquals("VAL_0", value0);
        Thread.sleep(10);

        // 3. Add a new item to exceed capacity and trigger eviction
        cacheService.put("NEW_ITEM", "NEW_VAL");

        // 4. Assertions: "KEY_1" is now the oldest and must be evicted
        assertNull(cacheService.get("KEY_1"), "KEY_1 should have been evicted!");
        assertEquals("VAL_0", cacheService.get("KEY_0"), "KEY_0 should have survived!");
        assertEquals("NEW_VAL", cacheService.get("NEW_ITEM"));
    }
}
