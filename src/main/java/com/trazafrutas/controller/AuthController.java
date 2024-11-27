package com.trazafrutas.controller;

import com.trazafrutas.dto.AuthRequest;
import com.trazafrutas.dto.AuthResponse;
import com.trazafrutas.dto.ApiResponse;
import com.trazafrutas.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("authController") // Nombre único del bean
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(@RequestBody AuthRequest request) {
        AuthResponse authResponse = authService.authenticate(request);
        ApiResponse response = new ApiResponse(true, "Login exitoso", authResponse);
        return ResponseEntity.ok(response);
    }
}
