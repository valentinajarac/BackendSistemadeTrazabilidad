package com.trazafrutas.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStats {
    // Estadísticas de productores
    private Long totalProductores;
    private Long productoresActivos;
    private Long productoresInactivos;

    // Estadísticas por producto
    private Long productoresUchuva;
    private Long productoresGulupa;

    // Estadísticas de certificaciones
    private Long productoresFairtrade;
    private Long productoresGlobalGap;
    private Long productoresIca;
    private Long productoresSinCertificacion;
    private Long productoresConCertificacion;

    // Estadísticas de fincas y cultivos
    private Long totalFincas;
    private Long totalCultivos;
    private Long cultivosProduccion;
    private Long cultivosVegetacion;
    private Long cultivosUchuva;
    private Long cultivosGulupa;

    // Estadísticas del mes actual
    private Long despachosMes;
    private Double kilosUchuvaMes;
    private Double kilosGulupaMes;

    // Distribución por municipio
    private List<ProduccionPorMunicipio> produccionesPorMunicipio;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProduccionPorMunicipio {
        private String municipio;
        private Long cantidad;
    }
}