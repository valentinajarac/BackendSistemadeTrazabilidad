package com.trazafrutas.service;

import com.trazafrutas.exception.EntityNotFoundException;
import com.trazafrutas.model.User;
import com.trazafrutas.model.enums.Certification;
import com.trazafrutas.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @PersistenceContext
    private EntityManager entityManager;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con ID: " + id));
    }

    @Transactional
    public User createUser(User user) {
        validateUserData(user);
        logger.debug("Validando datos de nuevo usuario: {}", user.getUsuario());

        if (userRepository.existsByUsuario(user.getUsuario())) {
            throw new IllegalArgumentException("Ya existe un usuario con el nombre de usuario: " + user.getUsuario());
        }
        if (userRepository.existsByCedula(user.getCedula())) {
            throw new IllegalArgumentException("Ya existe un usuario con la cédula: " + user.getCedula());
        }
        if (userRepository.existsByCodigoTrazabilidad(user.getCodigoTrazabilidad())) {
            throw new IllegalArgumentException("Ya existe un usuario con el código de trazabilidad: " + user.getCodigoTrazabilidad());
        }

        String encodedPassword = passwordEncoder.encode(user.getPassword());

        Query query = entityManager.createNativeQuery(
                "INSERT INTO users (cedula, codigo_trazabilidad, municipio, nombre_completo, " +
                        "password, role, user_status, telefono, usuario) VALUES " +
                        "(:cedula, :codigoTrazabilidad, :municipio, :nombreCompleto, " +
                        ":password, CAST(:role AS role_type), CAST(:userStatus AS user_status), :telefono, :usuario) " +
                        "RETURNING *", User.class);

        query.setParameter("cedula", user.getCedula());
        query.setParameter("codigoTrazabilidad", user.getCodigoTrazabilidad());
        query.setParameter("municipio", user.getMunicipio());
        query.setParameter("nombreCompleto", user.getNombreCompleto());
        query.setParameter("password", encodedPassword);
        query.setParameter("role", user.getRole().name());
        query.setParameter("userStatus", user.getStatus().name());
        query.setParameter("telefono", user.getTelefono());
        query.setParameter("usuario", user.getUsuario());

        User savedUser = (User) query.getSingleResult();

        if (user.getCertifications() != null && !user.getCertifications().isEmpty()) {
            for (Certification cert : user.getCertifications()) {
                entityManager.createNativeQuery(
                                "INSERT INTO user_certifications (user_id, certification) " +
                                        "VALUES (:userId, :certValue::text::certification_type)")
                        .setParameter("userId", savedUser.getId())
                        .setParameter("certValue", cert.name())
                        .executeUpdate();
            }
            savedUser.setCertifications(user.getCertifications());
        }

        return savedUser;
    }

    @Transactional
    public User updateUser(Long id, User updatedUser) {
        User existingUser = getUserById(id);
        logger.debug("Actualizando usuario: {}", existingUser.getUsuario());

        // Validaciones de datos
        if (updatedUser.getCedula() != null) {
            if (!existingUser.getCedula().equals(updatedUser.getCedula()) &&
                    userRepository.existsByCedula(updatedUser.getCedula())) {
                throw new IllegalArgumentException("Ya existe un usuario con la cédula: " + updatedUser.getCedula());
            }
            if (!updatedUser.getCedula().matches("\\d{7,10}")) {
                throw new IllegalArgumentException("La cédula debe tener entre 7 y 10 dígitos");
            }
        }

        if (updatedUser.getCodigoTrazabilidad() != null) {
            if (!existingUser.getCodigoTrazabilidad().equals(updatedUser.getCodigoTrazabilidad()) &&
                    userRepository.existsByCodigoTrazabilidad(updatedUser.getCodigoTrazabilidad())) {
                throw new IllegalArgumentException("Ya existe un usuario con el código de trazabilidad: " +
                        updatedUser.getCodigoTrazabilidad());
            }
        }

        if (updatedUser.getUsuario() != null) {
            if (!existingUser.getUsuario().equals(updatedUser.getUsuario()) &&
                    userRepository.existsByUsuario(updatedUser.getUsuario())) {
                throw new IllegalArgumentException("Ya existe un usuario con el nombre de usuario: " +
                        updatedUser.getUsuario());
            }
            if (updatedUser.getUsuario().length() < 4) {
                throw new IllegalArgumentException("El nombre de usuario debe tener al menos 4 caracteres");
            }
        }

        if (updatedUser.getTelefono() != null && !updatedUser.getTelefono().matches("\\d{10}")) {
            throw new IllegalArgumentException("El teléfono debe tener 10 dígitos");
        }

        if (updatedUser.getPassword() != null && !updatedUser.getPassword().trim().isEmpty() &&
                updatedUser.getPassword().length() < 6) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 6 caracteres");
        }

        // Construir la consulta de actualización
        StringBuilder queryBuilder = new StringBuilder("UPDATE users SET ");
        StringBuilder setClause = new StringBuilder();

        // Construir dinámicamente la cláusula SET
        if (updatedUser.getCedula() != null) {
            setClause.append("cedula = :cedula, ");
        }
        if (updatedUser.getCodigoTrazabilidad() != null) {
            setClause.append("codigo_trazabilidad = :codigoTrazabilidad, ");
        }
        if (updatedUser.getMunicipio() != null) {
            setClause.append("municipio = :municipio, ");
        }
        if (updatedUser.getNombreCompleto() != null) {
            setClause.append("nombre_completo = :nombreCompleto, ");
        }
        if (updatedUser.getTelefono() != null) {
            setClause.append("telefono = :telefono, ");
        }
        if (updatedUser.getUsuario() != null) {
            setClause.append("usuario = :usuario, ");
        }
        if (updatedUser.getPassword() != null && !updatedUser.getPassword().trim().isEmpty()) {
            setClause.append("password = :password, ");
        }
        if (updatedUser.getRole() != null) {
            setClause.append("role = CAST(:role AS role_type), ");
        }
        if (updatedUser.getStatus() != null) {
            setClause.append("user_status = CAST(:userStatus AS user_status), ");
        }

        // Remover la última coma y espacio si existen
        if (setClause.length() > 0) {
            setClause.setLength(setClause.length() - 2);
            queryBuilder.append(setClause);
            queryBuilder.append(" WHERE id = :id RETURNING *");

            Query query = entityManager.createNativeQuery(queryBuilder.toString(), User.class);

            // Establecer parámetros solo si están presentes
            if (updatedUser.getCedula() != null) {
                query.setParameter("cedula", updatedUser.getCedula());
            }
            if (updatedUser.getCodigoTrazabilidad() != null) {
                query.setParameter("codigoTrazabilidad", updatedUser.getCodigoTrazabilidad());
            }
            if (updatedUser.getMunicipio() != null) {
                query.setParameter("municipio", updatedUser.getMunicipio());
            }
            if (updatedUser.getNombreCompleto() != null) {
                query.setParameter("nombreCompleto", updatedUser.getNombreCompleto());
            }
            if (updatedUser.getTelefono() != null) {
                query.setParameter("telefono", updatedUser.getTelefono());
            }
            if (updatedUser.getUsuario() != null) {
                query.setParameter("usuario", updatedUser.getUsuario());
            }
            if (updatedUser.getPassword() != null && !updatedUser.getPassword().trim().isEmpty()) {
                query.setParameter("password", passwordEncoder.encode(updatedUser.getPassword()));
            }
            if (updatedUser.getRole() != null) {
                query.setParameter("role", updatedUser.getRole().name());
            }
            if (updatedUser.getStatus() != null) {
                query.setParameter("userStatus", updatedUser.getStatus().name());
            }

            query.setParameter("id", id);

            User savedUser = (User) query.getSingleResult();

            // Actualizar certificaciones si se proporcionan
            if (updatedUser.getCertifications() != null) {
                savedUser.setCertifications(updatedUser.getCertifications());
                savedUser = userRepository.save(savedUser);
            }

            logger.debug("Usuario actualizado exitosamente: {}", savedUser.getUsuario());
            return savedUser;
        }

        // Si no hay campos para actualizar, devolver el usuario existente
        return existingUser;
    }

    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new EntityNotFoundException("Usuario no encontrado con ID: " + id);
        }
        userRepository.deleteById(id);
        logger.debug("Usuario eliminado exitosamente, ID: {}", id);
    }

    private void validateUserData(User user) {
        StringBuilder errors = new StringBuilder();

        if (user.getCedula() == null || user.getCedula().trim().isEmpty()) {
            errors.append("La cédula es requerida. ");
        } else if (!user.getCedula().matches("\\d{7,10}")) {
            errors.append("La cédula debe tener entre 7 y 10 dígitos. ");
        }

        if (user.getNombreCompleto() == null || user.getNombreCompleto().trim().isEmpty()) {
            errors.append("El nombre completo es requerido. ");
        }

        if (user.getCodigoTrazabilidad() == null || user.getCodigoTrazabilidad().trim().isEmpty()) {
            errors.append("El código de trazabilidad es requerido. ");
        }

        if (user.getMunicipio() == null || user.getMunicipio().trim().isEmpty()) {
            errors.append("El municipio es requerido. ");
        }

        if (user.getTelefono() == null || user.getTelefono().trim().isEmpty()) {
            errors.append("El teléfono es requerido. ");
        } else if (!user.getTelefono().matches("\\d{10}")) {
            errors.append("El teléfono debe tener 10 dígitos. ");
        }

        if (user.getUsuario() == null || user.getUsuario().trim().isEmpty()) {
            errors.append("El nombre de usuario es requerido. ");
        } else if (user.getUsuario().length() < 4) {
            errors.append("El nombre de usuario debe tener al menos 4 caracteres. ");
        }

        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            errors.append("La contraseña es requerida. ");
        } else if (user.getPassword().length() < 6) {
            errors.append("La contraseña debe tener al menos 6 caracteres. ");
        }

        if (user.getRole() == null) {
            errors.append("El rol es requerido. ");
        }

        if (errors.length() > 0) {
            throw new IllegalArgumentException(errors.toString().trim());
        }
    }
}