package com.trazafrutas.dto;

import com.trazafrutas.model.User;
import com.trazafrutas.model.enums.Role;
import com.trazafrutas.model.enums.UserStatus;
import com.trazafrutas.model.enums.Certification;
import lombok.Data;

import java.util.Set;
import java.util.HashSet;

@Data
public class UserDTO {
    private Long id;
    private String cedula;
    private String nombreCompleto;
    private String codigoTrazabilidad;
    private String municipio;
    private String telefono;
    private String usuario;
    private String password;
    private Role role;
    private UserStatus status;
    private Set<Certification> certifications = new HashSet<>();

    public User toEntity() {
        User user = new User();
        user.setId(this.id);
        user.setCedula(this.cedula);
        user.setNombreCompleto(this.nombreCompleto);
        user.setCodigoTrazabilidad(this.codigoTrazabilidad);
        user.setMunicipio(this.municipio);
        user.setTelefono(this.telefono);
        user.setUsuario(this.usuario);
        if (this.password != null) {
            user.setPassword(this.password);
        }
        user.setRole(this.role);
        user.setStatus(this.status);
        user.setCertifications(this.certifications);
        return user;
    }

    public static UserDTO fromEntity(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setCedula(user.getCedula());
        dto.setNombreCompleto(user.getNombreCompleto());
        dto.setCodigoTrazabilidad(user.getCodigoTrazabilidad());
        dto.setMunicipio(user.getMunicipio());
        dto.setTelefono(user.getTelefono());
        dto.setUsuario(user.getUsuario());
        dto.setRole(user.getRole());
        dto.setStatus(user.getStatus());
        dto.setCertifications(user.getCertifications());
        return dto;
    }
}