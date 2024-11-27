package com.trazafrutas.controller;

import com.trazafrutas.dto.ApiResponse;
import com.trazafrutas.model.User;
import com.trazafrutas.model.enums.Role;
import com.trazafrutas.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.trazafrutas.dto.UserDTO;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;

    private ResponseEntity<?> checkAdminRole(User user) {
        if (user.getRole() != Role.ADMIN) {
            return ResponseEntity.status(403)
                    .body(new ApiResponse(false, "Solo los administradores pueden gestionar usuarios"));
        }
        return null;
    }

    @GetMapping
    public ResponseEntity<?> getAllUsers(@AuthenticationPrincipal User user) {
        ResponseEntity<?> roleCheck = checkAdminRole(user);
        if (roleCheck != null) return roleCheck;

        try {
            List<User> users = userService.getAllUsers();
            return ResponseEntity.ok(new ApiResponse(true, "Usuarios obtenidos exitosamente", users));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse(false, "Error al obtener los usuarios: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id, @AuthenticationPrincipal User user) {
        ResponseEntity<?> roleCheck = checkAdminRole(user);
        if (roleCheck != null) return roleCheck;

        try {
            User foundUser = userService.getUserById(id);
            foundUser.setPassword(null);
            return ResponseEntity.ok(new ApiResponse(true, "Usuario encontrado", foundUser));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody UserDTO userDTO, @AuthenticationPrincipal User user) {
        ResponseEntity<?> roleCheck = checkAdminRole(user);
        if (roleCheck != null) return roleCheck;

        try {
            logger.debug("Creando nuevo usuario: {}", userDTO.getUsuario());
            if (userDTO.getPassword() == null || userDTO.getPassword().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse(false, "La contraseña es requerida"));
            }

            User createdUser = userService.createUser(userDTO.toEntity());
            createdUser.setPassword(null); // No devolver la contraseña en la respuesta

            logger.debug("Usuario creado exitosamente: {}", createdUser.getUsuario());
            return ResponseEntity.ok(new ApiResponse(true, "Usuario creado exitosamente", createdUser));
        } catch (IllegalArgumentException e) {
            logger.error("Error al crear usuario: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, e.getMessage()));
        } catch (Exception e) {
            logger.error("Error inesperado al crear usuario: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse(false, "Error al crear el usuario: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(
            @PathVariable Long id,
            @RequestBody UserDTO userDTO,
            @AuthenticationPrincipal User user) {
        ResponseEntity<?> roleCheck = checkAdminRole(user);
        if (roleCheck != null) return roleCheck;

        try {
            logger.debug("Actualizando usuario ID {}: {}", id, userDTO.getUsuario());
            User updatedUser = userService.updateUser(id, userDTO.toEntity());
            updatedUser.setPassword(null);

            logger.debug("Usuario actualizado exitosamente: {}", updatedUser.getUsuario());
            return ResponseEntity.ok(new ApiResponse(true, "Usuario actualizado exitosamente", updatedUser));
        } catch (Exception e) {
            logger.error("Error al actualizar usuario: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse(false, "Error al actualizar el usuario: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id, @AuthenticationPrincipal User user) {
        ResponseEntity<?> roleCheck = checkAdminRole(user);
        if (roleCheck != null) return roleCheck;

        try {
            if (id == 1) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse(false, "No se puede eliminar al administrador principal"));
            }

            userService.deleteUser(id);
            return ResponseEntity.ok(new ApiResponse(true, "Usuario eliminado exitosamente"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }
}