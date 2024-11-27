package com.trazafrutas.dto;

import com.trazafrutas.model.Remission;
import com.trazafrutas.model.enums.ProductType;
import lombok.Data;
import java.time.LocalDate;

@Data
public class RemissionDTO {
    private Long id;
    private LocalDate fechaDespacho;
    private Integer canastillasEnviadas;
    private Double kilosPromedio;
    private Double totalKilos;
    private ProductType producto;
    private Long userId;
    private String nombreProductor; // Agregado
    private Long farmId;
    private String farmNombre;
    private Long cropId;
    private String cropDescripcion;
    private Long clientId;
    private String clientNombre;

    public static RemissionDTO fromEntity(Remission remission) {
        RemissionDTO dto = new RemissionDTO();
        dto.setId(remission.getId());
        dto.setFechaDespacho(remission.getFechaDespacho());
        dto.setCanastillasEnviadas(remission.getCanastillasEnviadas());
        dto.setKilosPromedio(remission.getKilosPromedio());
        dto.setTotalKilos(remission.getTotalKilos());
        dto.setProducto(remission.getProducto());
        dto.setUserId(remission.getUser().getId());
        dto.setNombreProductor(remission.getUser().getNombreCompleto()); 
        dto.setFarmId(remission.getFarm().getId());
        dto.setFarmNombre(remission.getFarm().getNombre());
        dto.setCropId(remission.getCrop().getId());
        dto.setCropDescripcion(remission.getCrop().getProducto() + " - " + remission.getCrop().getEstado());
        dto.setClientId(remission.getClient().getId());
        dto.setClientNombre(remission.getClient().getNombre());
        return dto;
    }
}
