package com.trazafrutas.repository;

import com.trazafrutas.model.Farm;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FarmRepository extends JpaRepository<Farm, Long> {
    List<Farm> findByUserId(Long userId);
    boolean existsByNombreAndUserId(String nombre, Long userId);
    long countByUserId(Long userId);
}