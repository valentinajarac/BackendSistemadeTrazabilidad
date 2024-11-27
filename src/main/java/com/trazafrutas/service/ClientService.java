package com.trazafrutas.service;

import com.trazafrutas.exception.EntityNotFoundException;
import com.trazafrutas.model.Client;
import com.trazafrutas.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClientService {
    private final ClientRepository clientRepository;

    public List<Client> getAllClients() {
        return clientRepository.findAll();
    }

    public Client getClientById(Long id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cliente no encontrado con ID: " + id));
    }

    @Transactional
    public Client createClient(Client client) {
        validateClientData(client);

        // Verificar duplicados
        if (clientRepository.existsByNit(client.getNit())) {
            throw new IllegalArgumentException("Ya existe un cliente con el NIT: " + client.getNit());
        }
        if (clientRepository.existsByFloid(client.getFloid())) {
            throw new IllegalArgumentException("Ya existe un cliente con el FLO ID: " + client.getFloid());
        }
        if (clientRepository.existsByEmail(client.getEmail())) {
            throw new IllegalArgumentException("Ya existe un cliente con el email: " + client.getEmail());
        }

        return clientRepository.save(client);
    }

    @Transactional
    public Client updateClient(Long id, Client updatedClient) {
        Client existingClient = getClientById(id);

        // Validar y actualizar NIT
        if (updatedClient.getNit() != null) {
            if (!existingClient.getNit().equals(updatedClient.getNit()) &&
                    clientRepository.existsByNit(updatedClient.getNit())) {
                throw new IllegalArgumentException("Ya existe un cliente con el NIT: " + updatedClient.getNit());
            }
            existingClient.setNit(updatedClient.getNit());
        }

        // Validar y actualizar nombre
        if (updatedClient.getNombre() != null) {
            if (updatedClient.getNombre().trim().isEmpty()) {
                throw new IllegalArgumentException("El nombre no puede estar vacío");
            }
            existingClient.setNombre(updatedClient.getNombre());
        }

        // Validar y actualizar FLO ID
        if (updatedClient.getFloid() != null) {
            if (!existingClient.getFloid().equals(updatedClient.getFloid()) &&
                    clientRepository.existsByFloid(updatedClient.getFloid())) {
                throw new IllegalArgumentException("Ya existe un cliente con el FLO ID: " + updatedClient.getFloid());
            }
            if (!updatedClient.getFloid().matches("\\d{4}")) {
                throw new IllegalArgumentException("El FLO ID debe ser un número de 4 dígitos");
            }
            existingClient.setFloid(updatedClient.getFloid());
        }

        // Validar y actualizar dirección
        if (updatedClient.getDireccion() != null) {
            if (updatedClient.getDireccion().trim().isEmpty()) {
                throw new IllegalArgumentException("La dirección no puede estar vacía");
            }
            existingClient.setDireccion(updatedClient.getDireccion());
        }

        // Validar y actualizar teléfono
        if (updatedClient.getTelefono() != null) {
            if (!updatedClient.getTelefono().matches("\\d{7,10}")) {
                throw new IllegalArgumentException("El teléfono debe tener entre 7 y 10 dígitos");
            }
            existingClient.setTelefono(updatedClient.getTelefono());
        }

        // Validar y actualizar email
        if (updatedClient.getEmail() != null) {
            if (!existingClient.getEmail().equals(updatedClient.getEmail()) &&
                    clientRepository.existsByEmail(updatedClient.getEmail())) {
                throw new IllegalArgumentException("Ya existe un cliente con el email: " + updatedClient.getEmail());
            }
            if (!updatedClient.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                throw new IllegalArgumentException("El email no tiene un formato válido");
            }
            existingClient.setEmail(updatedClient.getEmail());
        }

        return clientRepository.save(existingClient);
    }

    @Transactional
    public void deleteClient(Long id) {
        if (!clientRepository.existsById(id)) {
            throw new EntityNotFoundException("Cliente no encontrado con ID: " + id);
        }
        clientRepository.deleteById(id);
    }

    private void validateClientData(Client client) {
        StringBuilder errors = new StringBuilder();

        if (client.getNit() == null || client.getNit().trim().isEmpty()) {
            errors.append("El NIT es requerido. ");
        }

        if (client.getNombre() == null || client.getNombre().trim().isEmpty()) {
            errors.append("El nombre es requerido. ");
        }

        if (client.getFloid() == null || client.getFloid().trim().isEmpty()) {
            errors.append("El FLO ID es requerido. ");
        } else if (!client.getFloid().matches("\\d{4}")) {
            errors.append("El FLO ID debe ser un número de 4 dígitos. ");
        }

        if (client.getDireccion() == null || client.getDireccion().trim().isEmpty()) {
            errors.append("La dirección es requerida. ");
        }

        if (client.getTelefono() == null || client.getTelefono().trim().isEmpty()) {
            errors.append("El teléfono es requerido. ");
        } else if (!client.getTelefono().matches("\\d{7,10}")) {
            errors.append("El teléfono debe tener entre 7 y 10 dígitos. ");
        }

        if (client.getEmail() == null || client.getEmail().trim().isEmpty()) {
            errors.append("El email es requerido. ");
        } else if (!client.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            errors.append("El email no tiene un formato válido. ");
        }

        if (errors.length() > 0) {
            throw new IllegalArgumentException(errors.toString().trim());
        }
    }
}