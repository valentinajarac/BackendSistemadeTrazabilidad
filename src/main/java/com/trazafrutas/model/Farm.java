package com.trazafrutas.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "farms")
public class Farm {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre")
    private String nombre;

    @Column(name = "hectareas")
    private Double hectareas;

    @Column(name = "municipio")
    private String municipio;

    @Column(name = "vereda")
    private String vereda;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
}