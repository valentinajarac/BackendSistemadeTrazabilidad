package com.trazafrutas.service;

import com.trazafrutas.exception.EntityNotFoundException;
import com.trazafrutas.model.Crop;
import com.trazafrutas.model.Farm;
import com.trazafrutas.model.enums.CropStatus;
import com.trazafrutas.repository.CropRepository;
import com.trazafrutas.repository.FarmRepository;
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
public class CropService {
    private static final Logger logger = LoggerFactory.getLogger(CropService.class);

    private final CropRepository cropRepository;
    private final FarmRepository farmRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public List<Crop> getAllCrops() {
        return cropRepository.findAll();
    }

    public List<Crop> getCropsByUserId(Long userId) {
        return cropRepository.findByUserIdWithFarm(userId);
    }

    public List<Crop> getCropsByFarmId(Long farmId, Long userId) {
        Farm farm = farmRepository.findById(farmId)
                .orElseThrow(() -> new EntityNotFoundException("Finca no encontrada"));

        if (!farm.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("La finca no pertenece al usuario");
        }

        return cropRepository.findByFarmIdWithFarm(farmId);
    }

    public Crop getCropById(Long id) {
        return cropRepository.findByIdWithFarm(id)
                .orElseThrow(() -> new EntityNotFoundException("Cultivo no encontrado"));
    }

    @Transactional
    public Crop createCrop(Crop crop) {
        validateCropData(crop);

        Farm farm = farmRepository.findById(crop.getFarm().getId())
                .orElseThrow(() -> new EntityNotFoundException("Finca no encontrada"));

        if (!farm.getUser().getId().equals(crop.getUser().getId())) {
            throw new IllegalArgumentException("La finca no pertenece al usuario");
        }

        double hectareasUsadas = cropRepository.sumHectareasByFarmId(farm.getId());
        double hectareasDisponibles = farm.getHectareas() - hectareasUsadas;

        if (crop.getHectareas() > hectareasDisponibles) {
            throw new IllegalArgumentException(
                    String.format("La finca solo tiene %.2f hectáreas disponibles de %.2f hectáreas totales",
                            hectareasDisponibles, farm.getHectareas()));
        }

        Query query = entityManager.createNativeQuery(
                "INSERT INTO crops (farm_id, user_id, numero_plants, hectareas, " +
                        "fecha_siembra, producto, estado) VALUES " +
                        "(:farmId, :userId, :numeroPlants, :hectareas, :fechaSiembra, " +
                        "CAST(:producto AS product_type), CAST(:estado AS crop_status)) " +
                        "RETURNING *", Crop.class);

        query.setParameter("farmId", farm.getId());
        query.setParameter("userId", crop.getUser().getId());
        query.setParameter("numeroPlants", crop.getNumeroPlants());
        query.setParameter("hectareas", crop.getHectareas());
        query.setParameter("fechaSiembra", crop.getFechaSiembra());
        query.setParameter("producto", crop.getProducto().name());
        query.setParameter("estado", CropStatus.VEGETACION.name());

        return (Crop) query.getSingleResult();
    }

    @Transactional
    public Crop updateCrop(Long id, Crop updatedCrop) {
        Crop existingCrop = getCropById(id);
        boolean needsUpdate = false;

        StringBuilder queryBuilder = new StringBuilder("UPDATE crops SET ");
        StringBuilder setClause = new StringBuilder();

        if (updatedCrop.getNumeroPlants() != null && updatedCrop.getNumeroPlants() > 0) {
            setClause.append("numero_plants = :numeroPlants, ");
            needsUpdate = true;
        }

        if (updatedCrop.getHectareas() != null && updatedCrop.getHectareas() > 0) {
            Farm farm = existingCrop.getFarm();
            if (updatedCrop.getFarm() != null && !updatedCrop.getFarm().getId().equals(farm.getId())) {
                farm = farmRepository.findById(updatedCrop.getFarm().getId())
                        .orElseThrow(() -> new EntityNotFoundException("Finca no encontrada"));

                if (!farm.getUser().getId().equals(existingCrop.getUser().getId())) {
                    throw new IllegalArgumentException("La finca no pertenece al usuario");
                }
            }

            double hectareasUsadas = cropRepository.sumHectareasByFarmId(farm.getId());
            double hectareasActuales = existingCrop.getHectareas();
            double hectareasDisponibles = farm.getHectareas() - (hectareasUsadas - hectareasActuales);

            if (updatedCrop.getHectareas() > hectareasDisponibles) {
                throw new IllegalArgumentException(
                        String.format("La finca solo tiene %.2f hectáreas disponibles de %.2f hectáreas totales",
                                hectareasDisponibles, farm.getHectareas()));
            }

            setClause.append("hectareas = :hectareas, ");
            needsUpdate = true;
        }

        if (updatedCrop.getProducto() != null) {
            setClause.append("producto = CAST(:producto AS product_type), ");
            needsUpdate = true;
        }

        if (updatedCrop.getFechaSiembra() != null) {
            setClause.append("fecha_siembra = :fechaSiembra, ");
            needsUpdate = true;
        }

        if (updatedCrop.getFarm() != null) {
            Farm farm = farmRepository.findById(updatedCrop.getFarm().getId())
                    .orElseThrow(() -> new EntityNotFoundException("Finca no encontrada"));

            if (!farm.getUser().getId().equals(existingCrop.getUser().getId())) {
                throw new IllegalArgumentException("La finca no pertenece al usuario");
            }
            setClause.append("farm_id = :farmId, ");
            needsUpdate = true;
        }

        if (needsUpdate) {
            // Remover la última coma y espacio
            setClause.setLength(setClause.length() - 2);
            queryBuilder.append(setClause).append(" WHERE id = :id RETURNING *");

            Query query = entityManager.createNativeQuery(queryBuilder.toString(), Crop.class);

            // Establecer parámetros solo si están presentes
            if (updatedCrop.getNumeroPlants() != null) {
                query.setParameter("numeroPlants", updatedCrop.getNumeroPlants());
            }
            if (updatedCrop.getHectareas() != null) {
                query.setParameter("hectareas", updatedCrop.getHectareas());
            }
            if (updatedCrop.getProducto() != null) {
                query.setParameter("producto", updatedCrop.getProducto().name());
            }
            if (updatedCrop.getFechaSiembra() != null) {
                query.setParameter("fechaSiembra", updatedCrop.getFechaSiembra());
            }
            if (updatedCrop.getFarm() != null) {
                query.setParameter("farmId", updatedCrop.getFarm().getId());
            }

            query.setParameter("id", id);

            return (Crop) query.getSingleResult();
        }

        return existingCrop;
    }

    @Transactional
    public void deleteCrop(Long id) {
        if (!cropRepository.existsById(id)) {
            throw new EntityNotFoundException("Cultivo no encontrado");
        }
        cropRepository.deleteById(id);
    }

    private void validateCropData(Crop crop) {
        StringBuilder errors = new StringBuilder();

        if (crop.getNumeroPlants() == null) {
            errors.append("El número de plantas es requerido. ");
        } else if (crop.getNumeroPlants() <= 0) {
            errors.append("El número de plantas debe ser mayor a 0. ");
        }

        if (crop.getHectareas() == null) {
            errors.append("Las hectáreas son requeridas. ");
        } else if (crop.getHectareas() <= 0) {
            errors.append("Las hectáreas deben ser mayor a 0. ");
        }

        if (crop.getProducto() == null) {
            errors.append("El producto es requerido. ");
        }

        if (crop.getFechaSiembra() == null) {
            errors.append("La fecha de siembra es requerida. ");
        }

        if (crop.getFarm() == null || crop.getFarm().getId() == null) {
            errors.append("La finca es requerida. ");
        }

        if (crop.getUser() == null) {
            errors.append("El usuario es requerido. ");
        }

        if (errors.length() > 0) {
            throw new IllegalArgumentException(errors.toString().trim());
        }
    }
}