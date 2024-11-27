package com.trazafrutas.dto.admin;

import com.trazafrutas.model.Remission;
import com.trazafrutas.model.enums.ProductType;
import lombok.Data;
import java.time.LocalDate;

@Data
public class AdminRemissionDTO {
    private Long id;
    private String productor;
    private LocalDate fechaDespacho;
    private Integer canastillasEnviadas;
    private Double kilosPromedio;
    private Double totalKilos;
    private ProductType producto;
    private String fincaNombre;
    private String clienteNombre;

    public static AdminRemissionDTO fromEntity(Remission remission) {
        AdminRemissionDTO dto = new AdminRemissionDTO();
        dto.setId(remission.getId());
        dto.setProductor(remission.getUser() != null ? remission.getUser().getNombreCompleto() : "N/A");
        dto.setFechaDespacho(remission.getFechaDespacho());
        dto.setCanastillasEnviadas(remission.getCanastillasEnviadas());
        dto.setKilosPromedio(remission.getKilosPromedio());
        dto.setTotalKilos(remission.getTotalKilos());
        dto.setProducto(remission.getProducto());
        dto.setFincaNombre(remission.getFarm() != null ? remission.getFarm().getNombre() : "N/A");
        dto.setClienteNombre(remission.getClient() != null ? remission.getClient().getNombre() : "N/A");
        return dto;
    }
}

