package com.pinnacle.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pinnacle.backend.model.ClientModel;

@Repository
public interface ClientRepository extends JpaRepository<ClientModel, Long> {
    public boolean existsByUserName(String userName);

    public ClientModel findByUserName(String userName);
}
