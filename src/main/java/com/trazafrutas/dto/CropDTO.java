package com.trazafrutas.dto;

import com.trazafrutas.model.Crop;
import com.trazafrutas.model.enums.ProductType;
import com.trazafrutas.model.enums.CropStatus;
import lombok.Data;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Data
public class CropDTO {
    private Long id;
    private Integer numeroPlants;
    private Double hectareas;
    private String fechaSiembra;
    private ProductType producto;
    private CropStatus estado;
    private Long farmId;
    private String farmNombre;
    private Long userId;
    private String nombreProductor;

    public static CropDTO fromEntity(Crop crop) {
        CropDTO dto = new CropDTO();
        dto.setId(crop.getId());
        dto.setNumeroPlants(crop.getNumeroPlants());
        dto.setHectareas(crop.getHectareas());
        // Formatear la fecha como string en formato ISO
        dto.setFechaSiembra(crop.getFechaSiembra().toString());
        dto.setProducto(crop.getProducto());
        dto.setEstado(crop.getEstado());
        dto.setFarmId(crop.getFarm().getId());
        dto.setFarmNombre(crop.getFarm().getNombre());
        dto.setUserId(crop.getUser().getId());
        dto.setNombreProductor(crop.getUser().getNombreCompleto());
        return dto;
    }
}