package com.example.test.repository;

import com.example.test.entity.CacheEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LRUCacheRepository extends JpaRepository<CacheEntity, String> {
    CacheEntity findFirstByOrderByLastAccessedAsc();

    List<CacheEntity> findAllByOrderByLastAccessedDesc();
}
