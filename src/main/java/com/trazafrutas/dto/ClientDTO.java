package com.trazafrutas.dto;

import lombok.Data;

@Data
public class ClientDTO {
    private Long id;
    private String nit;
    private String nombre;
    private String floid;
    private String direccion;
    private String telefono;
    private String email;
}