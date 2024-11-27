package com.trazafrutas.repository;

import com.trazafrutas.model.User;
import com.trazafrutas.model.enums.Role;
import com.trazafrutas.model.enums.UserStatus;
import com.trazafrutas.model.enums.Certification;
import com.trazafrutas.dto.DashboardStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsuario(String usuario);
    boolean existsByUsuario(String usuario);
    boolean existsByCedula(String cedula);
    boolean existsByCodigoTrazabilidad(String codigoTrazabilidad);

    @Query(value = "SELECT COUNT(*) FROM users WHERE CAST(role AS text) = ?1 AND CAST(user_status AS text) = ?2",
            nativeQuery = true)
    Long countByRoleAndStatus(String role, String status);

    @Query(value = "SELECT COUNT(DISTINCT u.id) FROM users u " +
            "JOIN user_certifications uc ON u.id = uc.user_id " +
            "WHERE CAST(u.role AS text) = ?1 AND uc.certification = ?2 AND u.user_status = 'ACTIVO'",
            nativeQuery = true)
    Long countByRoleAndCertification(String role, String certification);

    @Query(value = "SELECT COUNT(DISTINCT u.id) FROM users u " +
            "JOIN user_certifications uc ON u.id = uc.user_id " +
            "WHERE CAST(u.role AS text) = ?1 AND uc.certification = ANY(?2) AND u.user_status = 'ACTIVO'",
            nativeQuery = true)
    Long countDistinctByRoleAndCertificationsIn(String role, String[] certifications);

    @Query(value = "SELECT municipio, COUNT(*) as cantidad " +
            "FROM users " +
            "WHERE CAST(role AS text) = 'PRODUCER' AND user_status = 'ACTIVO' " +
            "GROUP BY municipio " +
            "ORDER BY COUNT(*) DESC",
            nativeQuery = true)
    List<Object[]> countProductoresPorMunicipio();
}