package com.trazafrutas.security;

import com.trazafrutas.dto.AuthRequest;
import com.trazafrutas.dto.AuthResponse;
import com.trazafrutas.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("securityAuthController") // Nombre único del bean
@RequestMapping("/api/security/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        AuthResponse authResponse = authService.authenticate(request);
        return ResponseEntity.ok(authResponse);
    }
}
