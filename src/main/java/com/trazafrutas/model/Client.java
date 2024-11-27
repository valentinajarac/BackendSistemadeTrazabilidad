package com.trazafrutas.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "clients")
public class Client {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String nit;
    private String nombre;
    private String floid;
    private String direccion;
    private String telefono;
    private String email;
}