package com.trazafrutas.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;
import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyStats {
    private LocalDate mes;
    private Double kilosUchuva;
    private Double kilosGulupa;

    // Constructor adicional para manejar resultados de consultas SQL
    public MonthlyStats(Timestamp mes, Double kilosUchuva, Double kilosGulupa) {
        this.mes = mes != null ? mes.toLocalDateTime().toLocalDate() : null;
        this.kilosUchuva = kilosUchuva;
        this.kilosGulupa = kilosGulupa;
    }

    // Getters con valores por defecto para evitar NullPointerException
    public Double getKilosUchuva() {
        return kilosUchuva != null ? kilosUchuva : 0.0;
    }

    public Double getKilosGulupa() {
        return kilosGulupa != null ? kilosGulupa : 0.0;
    }

    public LocalDate getMes() {
        return mes;
    }

    public void setMes(LocalDate mes) {
        this.mes = mes;
    }

    public void setKilosUchuva(Double kilosUchuva) {
        this.kilosUchuva = kilosUchuva;
    }

    public void setKilosGulupa(Double kilosGulupa) {
        this.kilosGulupa = kilosGulupa;
    }
}
