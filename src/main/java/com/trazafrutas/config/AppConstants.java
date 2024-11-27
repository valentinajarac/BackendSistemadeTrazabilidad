package com.trazafrutas.config;

public class AppConstants {
    // Admin user constants
    public static final String ADMIN_USERNAME = "admin";
    public static final String ADMIN_PASSWORD = "admin123";
    public static final String ADMIN_CEDULA = "1234567890";
    public static final String ADMIN_NOMBRE = "Administrador Sistema";
    public static final String ADMIN_CODIGO = "ADMIN001";
    public static final String ADMIN_MUNICIPIO = "Bogotá";
    public static final String ADMIN_TELEFONO = "3001234567";

    // Password hash for admin123 (used in migrations)
    public static final String ADMIN_PASSWORD_HASH = "$2a$10$DaWzKFk1h3Vk8rKHGxvCp.LZpUk2qkKxY5Jy9H4ZEqD4kKvqFLtcO";

    private AppConstants() {
        // Private constructor to prevent instantiation
    }
}