package com.example.Alotrabong.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@MappedSuperclass
@Getter @Setter
public abstract class Auditable {
    @Column(name = "created_at")
    protected LocalDateTime createdAt;
    @Column(name = "updated_at")
    protected LocalDateTime updatedAt;
}