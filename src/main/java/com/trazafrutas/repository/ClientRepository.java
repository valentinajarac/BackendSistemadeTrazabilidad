package com.trazafrutas.repository;

import com.trazafrutas.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientRepository extends JpaRepository<Client, Long> {
    boolean existsByNit(String nit);
    boolean existsByFloid(String floid);
    boolean existsByEmail(String email);
}