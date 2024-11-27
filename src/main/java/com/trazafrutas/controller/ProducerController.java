package com.trazafrutas.controller;

import com.trazafrutas.dto.ApiResponse;
import com.trazafrutas.dto.MonthlyStats;
import com.trazafrutas.model.User;
import com.trazafrutas.model.enums.Role;
import com.trazafrutas.service.ProducerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/producer")
@RequiredArgsConstructor
public class ProducerController {
    private final ProducerService producerService;

    @GetMapping("/stats")
    public ResponseEntity<?> getStats(@AuthenticationPrincipal User user) {
        try {
            if (user.getRole() != Role.PRODUCER) {
                return ResponseEntity.status(403)
                        .body(new ApiResponse(false, "Acceso denegado"));
            }

            Map<String, Object> stats = producerService.getProducerStats(user.getId());
            return ResponseEntity.ok(new ApiResponse(true, "Estadísticas obtenidas exitosamente", stats));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse(false, "Error al obtener estadísticas: " + e.getMessage()));
        }
    }

    @GetMapping("/monthly")
    public ResponseEntity<?> getMonthlyStats(@AuthenticationPrincipal User user) {
        try {
            if (user.getRole() != Role.PRODUCER) {
                return ResponseEntity.status(403)
                        .body(new ApiResponse(false, "Acceso denegado"));
            }

            var monthlyStats = producerService.getMonthlyStats(user.getId());
            return ResponseEntity.ok(new ApiResponse(true, "Estadísticas mensuales obtenidas exitosamente", monthlyStats));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse(false, "Error al obtener estadísticas mensuales: " + e.getMessage()));
        }
    }
}