package com.pinnacle.backend.model;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long memId;

    @Column(length = 50, nullable = false)
    private String userName;

    @Column(nullable = false)
    private String pwd; // Ensure this is hashed before storage

    @Column(nullable = false)
    private Boolean status; // Active/inactive status

    @Column(nullable = false)
    private Instant createdAt;

    private String ipLogin; // Can be nullable for signup

    @Column(length = 255, unique = true)
    private String apiKey; // Ensure this is securely generated

    private Instant lastLogin; // Track last login time
}
