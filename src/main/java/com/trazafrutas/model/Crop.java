package com.trazafrutas.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import com.trazafrutas.model.enums.ProductType;
import com.trazafrutas.model.enums.CropStatus;

@Data
@Entity
@Table(name = "crops")
public class Crop {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_plants")
    private Integer numeroPlants;

    @Column(name = "fecha_siembra", nullable = false)
    private LocalDate fechaSiembra;

    @Column(name = "hectareas")
    private Double hectareas;

    @Enumerated(EnumType.STRING)
    @Column(name = "producto")
    private ProductType producto;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado")
    private CropStatus estado;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "farm_id")
    private Farm farm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
}
