package com.trazafrutas.controller;

import com.trazafrutas.dto.ApiResponse;
import com.trazafrutas.dto.CropDTO;
import com.trazafrutas.dto.admin.AdminCropDTO;
import com.trazafrutas.model.Crop;
import com.trazafrutas.model.User;
import com.trazafrutas.model.enums.Role;
import com.trazafrutas.service.CropService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/crops")
@RequiredArgsConstructor
public class CropController {
    private final CropService cropService;

    @GetMapping
    public ResponseEntity<?> getMyCrops(@AuthenticationPrincipal User user) {
        if (user.getRole() != Role.PRODUCER) {
            return ResponseEntity.status(403)
                    .body(new ApiResponse(false, "Solo los productores pueden gestionar cultivos"));
        }

        try {
            List<Crop> crops = cropService.getCropsByUserId(user.getId());
            List<CropDTO> cropDTOs = crops.stream()
                    .map(CropDTO::fromEntity)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(new ApiResponse(true, "Cultivos obtenidos exitosamente", cropDTOs));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse(false, "Error al obtener los cultivos: " + e.getMessage()));
        }
    }

    @GetMapping("/farm/{farmId}")
    public ResponseEntity<?> getCropsByFarmId(
            @PathVariable Long farmId,
            @AuthenticationPrincipal User user) {
        if (user.getRole() != Role.PRODUCER) {
            return ResponseEntity.status(403)
                    .body(new ApiResponse(false, "Solo los productores pueden gestionar cultivos"));
        }

        try {
            List<Crop> crops = cropService.getCropsByFarmId(farmId, user.getId());
            List<CropDTO> cropDTOs = crops.stream()
                    .map(CropDTO::fromEntity)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(new ApiResponse(true, "Cultivos obtenidos exitosamente", cropDTOs));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(403)
                    .body(new ApiResponse(false, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse(false, "Error al obtener los cultivos: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getCropById(@PathVariable Long id, @AuthenticationPrincipal User user) {
        if (user.getRole() != Role.PRODUCER) {
            return ResponseEntity.status(403)
                    .body(new ApiResponse(false, "Solo los productores pueden gestionar cultivos"));
        }

        try {
            Crop crop = cropService.getCropById(id);
            if (!crop.getUser().getId().equals(user.getId())) {
                return ResponseEntity.status(403)
                        .body(new ApiResponse(false, "No tiene permiso para ver este cultivo"));
            }
            return ResponseEntity.ok(new ApiResponse(true, "Cultivo encontrado", CropDTO.fromEntity(crop)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<?> createCrop(@RequestBody Crop crop, @AuthenticationPrincipal User user) {
        if (user.getRole() != Role.PRODUCER) {
            return ResponseEntity.status(403)
                    .body(new ApiResponse(false, "Solo los productores pueden gestionar cultivos"));
        }

        try {
            crop.setUser(user);
            Crop newCrop = cropService.createCrop(crop);
            return ResponseEntity.ok(new ApiResponse(true, "Cultivo creado exitosamente", CropDTO.fromEntity(newCrop)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse(false, "Error al crear el cultivo: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCrop(
            @PathVariable Long id,
            @RequestBody Crop crop,
            @AuthenticationPrincipal User user) {
        if (user.getRole() != Role.PRODUCER) {
            return ResponseEntity.status(403)
                    .body(new ApiResponse(false, "Solo los productores pueden gestionar cultivos"));
        }

        try {
            Crop existingCrop = cropService.getCropById(id);
            if (!existingCrop.getUser().getId().equals(user.getId())) {
                return ResponseEntity.status(403)
                        .body(new ApiResponse(false, "No tiene permiso para modificar este cultivo"));
            }

            crop.setUser(user);
            Crop updatedCrop = cropService.updateCrop(id, crop);
            return ResponseEntity.ok(new ApiResponse(true, "Cultivo actualizado exitosamente", CropDTO.fromEntity(updatedCrop)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse(false, "Error al actualizar el cultivo: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCrop(@PathVariable Long id, @AuthenticationPrincipal User user) {
        if (user.getRole() != Role.PRODUCER) {
            return ResponseEntity.status(403)
                    .body(new ApiResponse(false, "Solo los productores pueden gestionar cultivos"));
        }

        try {
            Crop crop = cropService.getCropById(id);
            if (!crop.getUser().getId().equals(user.getId())) {
                return ResponseEntity.status(403)
                        .body(new ApiResponse(false, "No tiene permiso para eliminar este cultivo"));
            }

            cropService.deleteCrop(id);
            return ResponseEntity.ok(new ApiResponse(true, "Cultivo eliminado exitosamente"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    @GetMapping("/admin")
    public ResponseEntity<?> getAllCropsForAdmin(@AuthenticationPrincipal User user) {
        if (user.getRole() != Role.ADMIN) {
            return ResponseEntity.status(403)
                    .body(new ApiResponse(false, "Solo los administradores pueden ver todos los cultivos"));
        }

        try {
            List<Crop> crops = cropService.getAllCrops();
            List<AdminCropDTO> cropDTOs = crops.stream()
                    .map(AdminCropDTO::fromEntity)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(new ApiResponse(true, "Cultivos obtenidos exitosamente", cropDTOs));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse(false, "Error al obtener los cultivos: " + e.getMessage()));
        }
    }
}
