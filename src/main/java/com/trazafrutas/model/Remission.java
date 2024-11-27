package com.trazafrutas.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import com.trazafrutas.model.enums.ProductType;

@Data
@Entity
@Table(name = "remissions")
public class Remission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fecha_despacho")
    private LocalDate fechaDespacho;

    @Column(name = "canastillas_enviadas")
    private Integer canastillasEnviadas;

    @Column(name = "kilos_promedio")
    private Double kilosPromedio;

    @Column(name = "total_kilos")
    private Double totalKilos;

    @Enumerated(EnumType.STRING)
    private ProductType producto;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "farm_id")
    private Farm farm;

    @ManyToOne
    @JoinColumn(name = "crop_id")
    private Crop crop;

    @ManyToOne
    @JoinColumn(name = "client_id")
    private Client client;
}