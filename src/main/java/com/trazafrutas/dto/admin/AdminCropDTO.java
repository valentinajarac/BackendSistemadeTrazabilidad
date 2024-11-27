package com.trazafrutas.dto.admin;

import com.trazafrutas.model.Crop;
import com.trazafrutas.model.enums.ProductType;
import com.trazafrutas.model.enums.CropStatus;
import lombok.Data;
import java.time.LocalDate;

@Data
public class AdminCropDTO {
    private Long id;
    private String productor;
    private String farmNombre;
    private Integer numeroPlants;
    private Double hectareas;
    private String fechaSiembra;
    private ProductType producto;
    private CropStatus estado;

    public static AdminCropDTO fromEntity(Crop crop) {
        AdminCropDTO dto = new AdminCropDTO();
        dto.setId(crop.getId());
        dto.setProductor(crop.getUser().getNombreCompleto());
        dto.setFarmNombre(crop.getFarm().getNombre());
        dto.setNumeroPlants(crop.getNumeroPlants());
        dto.setHectareas(crop.getHectareas());
        dto.setFechaSiembra(crop.getFechaSiembra().toString());
        dto.setProducto(crop.getProducto());
        dto.setEstado(crop.getEstado());
        return dto;
    }
}

