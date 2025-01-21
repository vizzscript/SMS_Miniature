package com.pinnacle.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pinnacle.backend.model.FinalModel;

@Repository
public interface FinalRepository extends JpaRepository<FinalModel, Long> {
}
