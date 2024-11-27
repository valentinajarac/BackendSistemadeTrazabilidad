package com.trazafrutas.service;

import com.trazafrutas.dto.MonthlyStats;
import com.trazafrutas.exception.EntityNotFoundException;
import com.trazafrutas.model.*;
import com.trazafrutas.repository.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RemissionService {
    private static final Logger logger = LoggerFactory.getLogger(RemissionService.class);

    private final RemissionRepository remissionRepository;
    private final FarmRepository farmRepository;
    private final CropRepository cropRepository;
    private final ClientRepository clientRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public List<Remission> getAllRemissions() {
        return remissionRepository.findAll();
    }

    public List<Remission> getRemissionsByUserId(Long userId) {
        return remissionRepository.findByUserId(userId);
    }

    public Remission getRemissionById(Long id) {
        return remissionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Remisión no encontrada"));
    }

    @Transactional
    public Remission createRemission(Remission remission) {
        validateRemissionData(remission);

        Farm farm = farmRepository.findById(remission.getFarm().getId())
                .orElseThrow(() -> new EntityNotFoundException("Finca no encontrada"));

        if (!farm.getUser().getId().equals(remission.getUser().getId())) {
            throw new IllegalArgumentException("La finca no pertenece al usuario");
        }

        Crop crop = cropRepository.findById(remission.getCrop().getId())
                .orElseThrow(() -> new EntityNotFoundException("Cultivo no encontrado"));

        if (!crop.getFarm().getId().equals(farm.getId())) {
            throw new IllegalArgumentException("El cultivo no pertenece a la finca seleccionada");
        }

        if (remission.getProducto() != crop.getProducto()) {
            throw new IllegalArgumentException("El producto de la remisión debe coincidir con el del cultivo");
        }

        if (!clientRepository.existsById(remission.getClient().getId())) {
            throw new EntityNotFoundException("Cliente no encontrado");
        }

        double totalKilos = remission.getCanastillasEnviadas() * remission.getKilosPromedio();

        Query query = entityManager.createNativeQuery(
                "INSERT INTO remissions (canastillas_enviadas, client_id, crop_id, farm_id, " +
                        "fecha_despacho, kilos_promedio, producto, total_kilos, user_id) VALUES " +
                        "(:canastillas, :clientId, :cropId, :farmId, :fechaDespacho, :kilosPromedio, " +
                        "CAST(:producto AS product_type), :totalKilos, :userId) " +
                        "RETURNING *", Remission.class);

        query.setParameter("canastillas", remission.getCanastillasEnviadas());
        query.setParameter("clientId", remission.getClient().getId());
        query.setParameter("cropId", remission.getCrop().getId());
        query.setParameter("farmId", remission.getFarm().getId());
        query.setParameter("fechaDespacho", remission.getFechaDespacho());
        query.setParameter("kilosPromedio", remission.getKilosPromedio());
        query.setParameter("producto", remission.getProducto().name());
        query.setParameter("totalKilos", totalKilos);
        query.setParameter("userId", remission.getUser().getId());

        Remission savedRemission = (Remission) query.getSingleResult();
        logger.debug("Remisión creada exitosamente: ID {}", savedRemission.getId());

        return savedRemission;
    }

    @Transactional
    public Remission updateRemission(Long id, Remission updatedRemission) {
        Remission existingRemission = getRemissionById(id);
        StringBuilder queryBuilder = new StringBuilder("UPDATE remissions SET ");
        StringBuilder setClause = new StringBuilder();
        boolean needsUpdate = false;

        // Validar y actualizar fecha de despacho
        if (updatedRemission.getFechaDespacho() != null) {
            setClause.append("fecha_despacho = :fechaDespacho, ");
            needsUpdate = true;
        }

        // Validar y actualizar canastillas enviadas
        if (updatedRemission.getCanastillasEnviadas() != null) {
            if (updatedRemission.getCanastillasEnviadas() <= 0) {
                throw new IllegalArgumentException("El número de canastillas debe ser mayor a 0");
            }
            setClause.append("canastillas_enviadas = :canastillas, ");
            needsUpdate = true;
        }

        // Validar y actualizar kilos promedio
        if (updatedRemission.getKilosPromedio() != null) {
            if (updatedRemission.getKilosPromedio() <= 0) {
                throw new IllegalArgumentException("Los kilos promedio deben ser mayor a 0");
            }
            setClause.append("kilos_promedio = :kilosPromedio, ");
            needsUpdate = true;
        }

        // Validar y actualizar producto
        if (updatedRemission.getProducto() != null) {
            if (updatedRemission.getProducto() != existingRemission.getCrop().getProducto()) {
                throw new IllegalArgumentException("El producto debe coincidir con el del cultivo");
            }
            setClause.append("producto = CAST(:producto AS product_type), ");
            needsUpdate = true;
        }

        // Validar y actualizar finca
        if (updatedRemission.getFarm() != null) {
            Farm farm = farmRepository.findById(updatedRemission.getFarm().getId())
                    .orElseThrow(() -> new EntityNotFoundException("Finca no encontrada"));

            if (!farm.getUser().getId().equals(existingRemission.getUser().getId())) {
                throw new IllegalArgumentException("La finca no pertenece al usuario");
            }
            setClause.append("farm_id = :farmId, ");
            needsUpdate = true;
        }

        // Validar y actualizar cultivo
        if (updatedRemission.getCrop() != null) {
            Crop crop = cropRepository.findById(updatedRemission.getCrop().getId())
                    .orElseThrow(() -> new EntityNotFoundException("Cultivo no encontrado"));

            if (!crop.getFarm().getId().equals(existingRemission.getFarm().getId())) {
                throw new IllegalArgumentException("El cultivo no pertenece a la finca seleccionada");
            }

            if (updatedRemission.getProducto() != null && updatedRemission.getProducto() != crop.getProducto()) {
                throw new IllegalArgumentException("El producto debe coincidir con el del cultivo");
            }

            setClause.append("crop_id = :cropId, ");
            needsUpdate = true;
        }

        // Validar y actualizar cliente
        if (updatedRemission.getClient() != null) {
            if (!clientRepository.existsById(updatedRemission.getClient().getId())) {
                throw new EntityNotFoundException("Cliente no encontrado");
            }
            setClause.append("client_id = :clientId, ");
            needsUpdate = true;
        }

        if (needsUpdate) {
            // Calcular nuevo total de kilos si es necesario
            if (updatedRemission.getCanastillasEnviadas() != null || updatedRemission.getKilosPromedio() != null) {
                double canastillas = updatedRemission.getCanastillasEnviadas() != null ?
                        updatedRemission.getCanastillasEnviadas() : existingRemission.getCanastillasEnviadas();
                double kilosPromedio = updatedRemission.getKilosPromedio() != null ?
                        updatedRemission.getKilosPromedio() : existingRemission.getKilosPromedio();
                setClause.append("total_kilos = :totalKilos, ");
            }

            // Remover la última coma y espacio
            setClause.setLength(setClause.length() - 2);
            queryBuilder.append(setClause).append(" WHERE id = :id RETURNING *");

            Query query = entityManager.createNativeQuery(queryBuilder.toString(), Remission.class);

            // Establecer parámetros
            if (updatedRemission.getFechaDespacho() != null) {
                query.setParameter("fechaDespacho", updatedRemission.getFechaDespacho());
            }
            if (updatedRemission.getCanastillasEnviadas() != null) {
                query.setParameter("canastillas", updatedRemission.getCanastillasEnviadas());
            }
            if (updatedRemission.getKilosPromedio() != null) {
                query.setParameter("kilosPromedio", updatedRemission.getKilosPromedio());
            }
            if (updatedRemission.getProducto() != null) {
                query.setParameter("producto", updatedRemission.getProducto().name());
            }
            if (updatedRemission.getFarm() != null) {
                query.setParameter("farmId", updatedRemission.getFarm().getId());
            }
            if (updatedRemission.getCrop() != null) {
                query.setParameter("cropId", updatedRemission.getCrop().getId());
            }
            if (updatedRemission.getClient() != null) {
                query.setParameter("clientId", updatedRemission.getClient().getId());
            }
            if (updatedRemission.getCanastillasEnviadas() != null || updatedRemission.getKilosPromedio() != null) {
                double canastillas = updatedRemission.getCanastillasEnviadas() != null ?
                        updatedRemission.getCanastillasEnviadas() : existingRemission.getCanastillasEnviadas();
                double kilosPromedio = updatedRemission.getKilosPromedio() != null ?
                        updatedRemission.getKilosPromedio() : existingRemission.getKilosPromedio();
                query.setParameter("totalKilos", canastillas * kilosPromedio);
            }

            query.setParameter("id", id);

            Remission savedRemission = (Remission) query.getSingleResult();
            logger.debug("Remisión actualizada exitosamente: ID {}", savedRemission.getId());
            return savedRemission;
        }

        return existingRemission;
    }

    @Transactional
    public void deleteRemission(Long id) {
        if (!remissionRepository.existsById(id)) {
            throw new EntityNotFoundException("Remisión no encontrada");
        }
        remissionRepository.deleteById(id);
        logger.debug("Remisión eliminada exitosamente: ID {}", id);
    }

    public List<MonthlyStats> getMonthlySummary() {
        return remissionRepository.getMonthlySummary();
    }

    public List<MonthlyStats> getMonthlySummaryByUserId(Long userId) {
        return remissionRepository.getMonthlySummaryByUserId(userId);
    }

    private void validateRemissionData(Remission remission) {
        StringBuilder errors = new StringBuilder();

        if (remission.getFechaDespacho() == null) {
            errors.append("La fecha de despacho es requerida. ");
        }

        if (remission.getCanastillasEnviadas() == null) {
            errors.append("El número de canastillas es requerido. ");
        } else if (remission.getCanastillasEnviadas() <= 0) {
            errors.append("El número de canastillas debe ser mayor a 0. ");
        }

        if (remission.getKilosPromedio() == null) {
            errors.append("Los kilos promedio son requeridos. ");
        } else if (remission.getKilosPromedio() <= 0) {
            errors.append("Los kilos promedio deben ser mayor a 0. ");
        }

        if (remission.getProducto() == null) {
            errors.append("El producto es requerido. ");
        }

        if (remission.getFarm() == null || remission.getFarm().getId() == null) {
            errors.append("La finca es requerida. ");
        }

        if (remission.getCrop() == null || remission.getCrop().getId() == null) {
            errors.append("El cultivo es requerido. ");
        }

        if (remission.getClient() == null || remission.getClient().getId() == null) {
            errors.append("El cliente es requerido. ");
        }

        if (remission.getUser() == null) {
            errors.append("El usuario es requerido. ");
        }

        if (errors.length() > 0) {
            throw new IllegalArgumentException(errors.toString().trim());
        }
    }
}

