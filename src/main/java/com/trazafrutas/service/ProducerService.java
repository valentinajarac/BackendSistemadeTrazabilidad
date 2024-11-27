package com.trazafrutas.service;

import com.trazafrutas.dto.MonthlyStats;
import com.trazafrutas.dto.CurrentMonthStats;
import com.trazafrutas.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProducerService {
    private static final Logger logger = LoggerFactory.getLogger(ProducerService.class);

    private final FarmRepository farmRepository;
    private final CropRepository cropRepository;
    private final RemissionRepository remissionRepository;

    public Map<String, Object> getProducerStats(Long userId) {
        logger.debug("Obteniendo estadísticas para el productor ID: {}", userId);
        Map<String, Object> stats = new HashMap<>();

        try {
            // Contar fincas del productor
            long totalFincas = farmRepository.countByUserId(userId);
            stats.put("totalFincas", totalFincas);
            logger.debug("Total fincas: {}", totalFincas);

            // Contar cultivos del productor
            long totalCultivos = cropRepository.countByUserId(userId);
            stats.put("totalCultivos", totalCultivos);
            logger.debug("Total cultivos: {}", totalCultivos);

            // Obtener total de remisiones y kilos de todos los tiempos
            Long totalRemisiones = remissionRepository.countByUserId(userId);
            Double totalKilos = remissionRepository.sumTotalKilosByUserId(userId);

            stats.put("totalRemisiones", totalRemisiones != null ? totalRemisiones : 0L);
            stats.put("kilosTotales", totalKilos != null ? totalKilos : 0.0);

            logger.debug("Total remisiones: {}, Total kilos: {}", totalRemisiones, totalKilos);

        } catch (Exception e) {
            logger.error("Error al obtener estadísticas del productor: {}", e.getMessage());
            throw e;
        }

        return stats;
    }

    public List<MonthlyStats> getMonthlyStats(Long userId) {
        logger.debug("Obteniendo estadísticas mensuales para el productor ID: {}", userId);
        return remissionRepository.getMonthlySummaryByUserId(userId);
    }
}