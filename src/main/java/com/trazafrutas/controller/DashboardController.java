package com.trazafrutas.controller;

import com.trazafrutas.dto.ApiResponse;
import com.trazafrutas.dto.DashboardStats;
import com.trazafrutas.model.User;
import com.trazafrutas.model.enums.Role;
import com.trazafrutas.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {
    private final DashboardService dashboardService;

    @GetMapping("/stats")
    public ResponseEntity<?> getStats(@AuthenticationPrincipal User user) {
        try {
            if (user.getRole() != Role.ADMIN) {
                return ResponseEntity.status(403)
                        .body(new ApiResponse(false, "Solo los administradores pueden ver estas estadísticas"));
            }

            DashboardStats stats = dashboardService.getAdminStats();
            return ResponseEntity.ok(new ApiResponse(true, "Estadísticas obtenidas exitosamente", stats));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse(false, "Error al obtener estadísticas: " + e.getMessage()));
        }
    }

    @GetMapping("/monthly")
    public ResponseEntity<?> getMonthlyStats(@AuthenticationPrincipal User user) {
        try {
            if (user.getRole() != Role.ADMIN) {
                return ResponseEntity.status(403)
                        .body(new ApiResponse(false, "Solo los administradores pueden ver estas estadísticas"));
            }

            var monthlyStats = dashboardService.getMonthlyStats();
            return ResponseEntity.ok(new ApiResponse(true, "Estadísticas mensuales obtenidas exitosamente", monthlyStats));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse(false, "Error al obtener estadísticas mensuales: " + e.getMessage()));
        }
    }
}