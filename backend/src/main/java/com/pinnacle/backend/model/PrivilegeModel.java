package com.pinnacle.backend.model;

import java.util.List;

import com.pinnacle.backend.config.PrivilegesConverter;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
// import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrivilegeModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long prevId;

    @ManyToOne
    @JoinColumn(name = "memId", referencedColumnName = "memId")
    private ClientModel client;

    @Column(columnDefinition = "JSON", nullable = false)
    @Convert(converter = PrivilegesConverter.class)
    private List<String> privileges;
}
