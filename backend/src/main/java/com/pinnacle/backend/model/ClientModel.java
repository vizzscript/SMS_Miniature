package com.pinnacle.backend.model;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long memId;

    @Getter
    @Column(length = 50, nullable = false)
    private String userName;

    @Column(nullable = false)
    private String pwd; // Ensure this is hashed before storage

    @Column(nullable = false)
    private Boolean status; // For active/inactive status

    @Column(nullable = false)
    private Instant createdAt;

    private String ipLogin; // Nullable, no need for annotation

    @Column(unique = true)
    private String apiKey;

    private Instant lastLogin; // Nullable, no need for annotation
}