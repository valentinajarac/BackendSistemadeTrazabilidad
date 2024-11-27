package com.trazafrutas.dto;

import com.trazafrutas.model.Farm;
import lombok.Data;

@Data
public class FarmDTO {
    private Long id;
    private String nombre;
    private Double hectareas;
    private String municipio;
    private String vereda;
    private Long userId;


    public static FarmDTO fromEntity(Farm farm) {
        FarmDTO dto = new FarmDTO();
        dto.setId(farm.getId());
        dto.setNombre(farm.getNombre());
        dto.setHectareas(farm.getHectareas());
        dto.setMunicipio(farm.getMunicipio());
        dto.setVereda(farm.getVereda());
        dto.setUserId(farm.getUser().getId());
        return dto;
    }
}