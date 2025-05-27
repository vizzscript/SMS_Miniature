package com.pinnacle.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// import com.pinnacle.backend.model.ClientModel;
import com.pinnacle.backend.model.FinalModel;
import java.util.List;


@Repository
public interface FinalRepository extends JpaRepository<FinalModel, Long> {
    List<FinalModel> findByClient_MemId(Long memId);
}
