package com.pinnacle.backend.repository;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Repository
public class CustomFinalRepository {
    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public void loadFileToDatabase(String filePath) {
        String sql = "LOAD DATA LOCAL INFILE '" + filePath + "' " +
                "INTO TABLE final_model " +
                "FIELDS TERMINATED BY ',' " +
                "LINES TERMINATED BY '\n' " +
                "(sender, mobile_no, message)";

        entityManager.createNativeQuery(sql).executeUpdate();
    }

}
