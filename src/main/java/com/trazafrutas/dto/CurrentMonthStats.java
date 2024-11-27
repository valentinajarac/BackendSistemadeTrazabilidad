package com.trazafrutas.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CurrentMonthStats {
    private Long despachos;
    private Double kilosUchuva;
    private Double kilosGulupa;

    public Long getDespachos() {
        return despachos != null ? despachos : 0L;
    }

    public Double getKilosUchuva() {
        return kilosUchuva != null ? kilosUchuva : 0.0;
    }

    public Double getKilosGulupa() {
        return kilosGulupa != null ? kilosGulupa : 0.0;
    }
}