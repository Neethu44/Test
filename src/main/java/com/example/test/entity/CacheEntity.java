package com.example.test.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name="cachetest")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CacheEntity {

    @Id
    private String key;
    private String value;
    private LocalDateTime lastAccessed;
}
