package com.pinnacle.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.pinnacle.backend.model.PrivilegeModel;

@Repository
public interface PrivilegeRepository extends JpaRepository<PrivilegeModel, Long> {

    Optional<PrivilegeModel> findByClient_MemId(Long memId);

}
