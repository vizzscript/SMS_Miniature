package com.pinnacle.backend.model;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FinalModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;

    @ManyToOne
    @JoinColumn(name = "memId", referencedColumnName = "memId")
    private ClientModel client;

    @Column(nullable = false)
    private String respId;

    @Column(nullable = false)
    private Long mobileNo;

    @Column(nullable = false)
    private String sender;

    @Column(nullable = false)
    private String message;

    @Column(nullable = false)
    private Instant reqDateTime;
}
