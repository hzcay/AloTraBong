package com.alotrabong.shared.kernel;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.Instant;

@MappedSuperclass
public abstract class BaseEntity {
    @CreationTimestamp @Column(updatable = false)
    protected Instant createdAt;
    @UpdateTimestamp
    protected Instant updatedAT;

    public Instant getCreatedAt(){ return createdAt;}
    public Instant getUpdatedAt(){ return updatedAT;}
}

