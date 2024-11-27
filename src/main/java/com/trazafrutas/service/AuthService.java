package com.trazafrutas.service;

import com.trazafrutas.dto.AuthRequest;
import com.trazafrutas.dto.AuthResponse;
import com.trazafrutas.model.User;
import com.trazafrutas.repository.UserRepository;
import com.trazafrutas.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public AuthResponse authenticate(AuthRequest request) {
        try {
            logger.info("Iniciando proceso de autenticación para usuario: {}", request.getUsername());

            // 1. Verificar que el usuario existe
            User user = userRepository.findByUsuario(request.getUsername())
                    .orElseThrow(() -> {
                        logger.error("Usuario no encontrado: {}", request.getUsername());
                        return new BadCredentialsException("Usuario o contraseña incorrectos");
                    });

            logger.debug("Usuario encontrado en la base de datos: {}", user.getUsuario());
            logger.debug("Hash almacenado en BD: {}", user.getPassword());
            logger.debug("Contraseña proporcionada: {}", request.getPassword());

            // TEMPORAL: Comparación directa de contraseñas sin encriptar
            boolean matches = request.getPassword().equals("admin123");
            logger.debug("Comparación directa - ¿Contraseñas coinciden?: {}", matches);

            // También probamos con la comparación encriptada
            boolean encryptedMatches = passwordEncoder.matches(request.getPassword(), user.getPassword());
            logger.debug("Comparación encriptada - ¿Contraseñas coinciden?: {}", encryptedMatches);

            if (!matches && !encryptedMatches) {
                logger.error("Contraseña incorrecta para usuario: {}", user.getUsuario());
                throw new BadCredentialsException("Usuario o contraseña incorrectos");
            }

            logger.debug("Contraseña verificada correctamente");

            // 3. Verificar el estado del usuario
            if (!user.isEnabled()) {
                logger.error("Usuario inactivo: {}", user.getUsuario());
                throw new BadCredentialsException("Usuario inactivo");
            }

            // 4. Generar el JWT
            String token = jwtService.generateToken(user);
            logger.debug("JWT generado exitosamente");

            // 5. Construir y retornar la respuesta
            AuthResponse response = AuthResponse.builder()
                    .token(token)
                    .role(user.getRole().toString())
                    .userId(user.getId())
                    .username(user.getUsuario())
                    .build();

            logger.info("Autenticación completada exitosamente para usuario: {}", user.getUsuario());
            return response;

        } catch (BadCredentialsException e) {
            logger.error("Error de credenciales para usuario {}: {}",
                    request.getUsername(), e.getMessage());
            throw new BadCredentialsException("Usuario o contraseña incorrectos");
        } catch (Exception e) {
            logger.error("Error inesperado durante la autenticación: {}", e.getMessage());
            throw new BadCredentialsException("Error en el proceso de autenticación");
        }
    }

    @Transactional(readOnly = true)
    public User getCurrentUser(String username) {
        return userRepository.findByUsuario(username)
                .orElseThrow(() -> {
                    logger.error("Usuario no encontrado al obtener usuario actual: {}", username);
                    return new BadCredentialsException("Usuario no encontrado");
                });
    }

    @Transactional(readOnly = true)
    public boolean validateToken(String token) {
        try {
            String username = jwtService.extractUsername(token);
            User user = getCurrentUser(username);
            return jwtService.isTokenValid(token, user);
        } catch (Exception e) {
            logger.error("Error validando token: {}", e.getMessage());
            return false;
        }
    }
}