package com.trazafrutas.repository;

import com.trazafrutas.model.Remission;
import com.trazafrutas.dto.MonthlyStats;
import com.trazafrutas.dto.CurrentMonthStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface RemissionRepository extends JpaRepository<Remission, Long> {
    List<Remission> findByUserId(Long userId);

    @Query("SELECT COUNT(r) FROM Remission r WHERE r.user.id = :userId")
    Long countByUserId(@Param("userId") Long userId);

    @Query("SELECT COALESCE(SUM(r.totalKilos), 0.0) FROM Remission r WHERE r.user.id = :userId")
    Double sumTotalKilosByUserId(@Param("userId") Long userId);

    @Query("""
        SELECT new com.trazafrutas.dto.MonthlyStats(
            date_trunc('month', r.fechaDespacho),
            SUM(CASE WHEN r.producto = 'UCHUVA' THEN r.totalKilos ELSE 0 END),
            SUM(CASE WHEN r.producto = 'GULUPA' THEN r.totalKilos ELSE 0 END)
        )
        FROM Remission r
        WHERE r.fechaDespacho >= subtract_months(CURRENT_DATE, 11)
        GROUP BY date_trunc('month', r.fechaDespacho)
        ORDER BY date_trunc('month', r.fechaDespacho) DESC
    """)
    List<MonthlyStats> getMonthlySummary();

    @Query("""
        SELECT new com.trazafrutas.dto.MonthlyStats(
            date_trunc('month', r.fechaDespacho),
            SUM(CASE WHEN r.producto = 'UCHUVA' THEN r.totalKilos ELSE 0 END),
            SUM(CASE WHEN r.producto = 'GULUPA' THEN r.totalKilos ELSE 0 END)
        )
        FROM Remission r
        WHERE r.user.id = :userId
        AND r.fechaDespacho >= subtract_months(CURRENT_DATE, 11)
        GROUP BY date_trunc('month', r.fechaDespacho)
        ORDER BY date_trunc('month', r.fechaDespacho) DESC
    """)
    List<MonthlyStats> getMonthlySummaryByUserId(@Param("userId") Long userId);

    @Query("""
        SELECT new com.trazafrutas.dto.CurrentMonthStats(
            COUNT(r),
            SUM(CASE WHEN r.producto = 'UCHUVA' THEN r.totalKilos ELSE 0 END),
            SUM(CASE WHEN r.producto = 'GULUPA' THEN r.totalKilos ELSE 0 END)
        )
        FROM Remission r
        WHERE year(r.fechaDespacho) = year(CURRENT_DATE)
        AND month(r.fechaDespacho) = month(CURRENT_DATE)
    """)
    CurrentMonthStats getCurrentMonthStats();

    @Query("""
        SELECT new com.trazafrutas.dto.CurrentMonthStats(
            COUNT(r),
            SUM(CASE WHEN r.producto = 'UCHUVA' THEN r.totalKilos ELSE 0 END),
            SUM(CASE WHEN r.producto = 'GULUPA' THEN r.totalKilos ELSE 0 END)
        )
        FROM Remission r
        WHERE r.user.id = :userId
        AND year(r.fechaDespacho) = year(CURRENT_DATE)
        AND month(r.fechaDespacho) = month(CURRENT_DATE)
    """)
    CurrentMonthStats getCurrentMonthStatsByUserId(@Param("userId") Long userId);
}