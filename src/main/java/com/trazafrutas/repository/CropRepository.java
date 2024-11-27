package com.trazafrutas.repository;

import com.trazafrutas.model.Crop;
import com.trazafrutas.model.enums.ProductType;
import com.trazafrutas.model.enums.CropStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface CropRepository extends JpaRepository<Crop, Long> {
    List<Crop> findByUserId(Long userId);
    List<Crop> findByFarmId(Long farmId);

    long countByUserId(Long userId);

    @Query("SELECT COALESCE(SUM(c.hectareas), 0) FROM Crop c WHERE c.farm.id = :farmId")
    double sumHectareasByFarmId(@Param("farmId") Long farmId);

    // Métodos para contar cultivos por producto
    @Query(value = "SELECT COUNT(*) FROM crops WHERE CAST(producto AS text) = ?1",
            nativeQuery = true)
    long countByProducto(String producto);

    // Métodos para contar cultivos por estado
    @Query(value = "SELECT COUNT(*) FROM crops WHERE CAST(estado AS text) = ?1",
            nativeQuery = true)
    long countByEstado(String estado);

    @Query(value = "SELECT COUNT(DISTINCT c.user_id) FROM crops c WHERE CAST(c.producto AS text) = ?1",
            nativeQuery = true)
    long countProducersByProducto(String producto);

    // Consultas que cargan relaciones específicas
    @Query("SELECT c FROM Crop c JOIN FETCH c.farm WHERE c.user.id = :userId")
    List<Crop> findByUserIdWithFarm(@Param("userId") Long userId);

    @Query("SELECT c FROM Crop c JOIN FETCH c.farm WHERE c.farm.id = :farmId")
    List<Crop> findByFarmIdWithFarm(@Param("farmId") Long farmId);

    @Query("SELECT c FROM Crop c JOIN FETCH c.farm WHERE c.id = :id")
    Optional<Crop> findByIdWithFarm(@Param("id") Long id);
}