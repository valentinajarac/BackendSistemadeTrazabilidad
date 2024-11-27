package com.trazafrutas.service;

import com.trazafrutas.dto.DashboardStats;
import com.trazafrutas.dto.MonthlyStats;
import com.trazafrutas.dto.CurrentMonthStats;
import com.trazafrutas.model.enums.Role;
import com.trazafrutas.model.enums.UserStatus;
import com.trazafrutas.model.enums.ProductType;
import com.trazafrutas.model.enums.CropStatus;
import com.trazafrutas.model.enums.Certification;
import com.trazafrutas.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {
    private final UserRepository userRepository;
    private final FarmRepository farmRepository;
    private final CropRepository cropRepository;
    private final RemissionRepository remissionRepository;

    public DashboardStats getAdminStats() {
        DashboardStats stats = new DashboardStats();

        // Estadísticas de productores
        stats.setProductoresActivos(userRepository.countByRoleAndStatus(
                Role.PRODUCER.name(), UserStatus.ACTIVO.name()));
        stats.setProductoresInactivos(userRepository.countByRoleAndStatus(
                Role.PRODUCER.name(), UserStatus.INACTIVO.name()));
        stats.setTotalProductores(stats.getProductoresActivos() + stats.getProductoresInactivos());

        // Estadísticas de productores por producto
        stats.setProductoresUchuva(cropRepository.countProducersByProducto(ProductType.UCHUVA.name()));
        stats.setProductoresGulupa(cropRepository.countProducersByProducto(ProductType.GULUPA.name()));

        // Estadísticas de certificaciones
        stats.setProductoresFairtrade(userRepository.countByRoleAndCertification(
                Role.PRODUCER.name(), Certification.FAIRTRADE_USA.name()));
        stats.setProductoresGlobalGap(userRepository.countByRoleAndCertification(
                Role.PRODUCER.name(), Certification.GLOBAL_GAP.name()));
        stats.setProductoresIca(userRepository.countByRoleAndCertification(
                Role.PRODUCER.name(), Certification.ICA.name()));
        stats.setProductoresSinCertificacion(userRepository.countByRoleAndCertification(
                Role.PRODUCER.name(), Certification.NINGUNA.name()));

        // Productores con al menos una certificación
        String[] certifications = {
                Certification.FAIRTRADE_USA.name(),
                Certification.GLOBAL_GAP.name(),
                Certification.ICA.name()
        };
        stats.setProductoresConCertificacion(
                userRepository.countDistinctByRoleAndCertificationsIn(
                        Role.PRODUCER.name(), certifications));

        // Estadísticas de fincas y cultivos
        stats.setTotalFincas(farmRepository.count());
        stats.setTotalCultivos(cropRepository.count());
        stats.setCultivosProduccion(cropRepository.countByEstado(CropStatus.PRODUCCION.name()));
        stats.setCultivosVegetacion(cropRepository.countByEstado(CropStatus.VEGETACION.name()));
        stats.setCultivosUchuva(cropRepository.countByProducto(ProductType.UCHUVA.name()));
        stats.setCultivosGulupa(cropRepository.countByProducto(ProductType.GULUPA.name()));

        // Estadísticas del mes actual
        CurrentMonthStats monthlyStats = remissionRepository.getCurrentMonthStats();
        if (monthlyStats != null) {
            stats.setDespachosMes(monthlyStats.getDespachos());
            stats.setKilosUchuvaMes(monthlyStats.getKilosUchuva());
            stats.setKilosGulupaMes(monthlyStats.getKilosGulupa());
        } else {
            stats.setDespachosMes(0L);
            stats.setKilosUchuvaMes(0.0);
            stats.setKilosGulupaMes(0.0);
        }

        // Distribución de productores por municipio
        List<DashboardStats.ProduccionPorMunicipio> produccionesPorMunicipio = new ArrayList<>();
        List<Object[]> resultados = userRepository.countProductoresPorMunicipio();

        for (Object[] resultado : resultados) {
            String municipio = (String) resultado[0];
            Long cantidad = ((Number) resultado[1]).longValue();
            produccionesPorMunicipio.add(new DashboardStats.ProduccionPorMunicipio(municipio, cantidad));
        }

        stats.setProduccionesPorMunicipio(produccionesPorMunicipio);

        return stats;
    }

    public List<MonthlyStats> getMonthlyStats() {
        return remissionRepository.getMonthlySummary();
    }
}
