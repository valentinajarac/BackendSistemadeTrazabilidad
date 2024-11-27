package com.trazafrutas.model;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.trazafrutas.model.enums.Role;
import com.trazafrutas.model.enums.UserStatus;
import com.trazafrutas.model.enums.Certification;

@Data
@Entity
@Table(name = "users")
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String cedula;

    @Column(name = "nombre_completo", nullable = false)
    private String nombreCompleto;

    @Column(name = "codigo_trazabilidad", nullable = false, unique = true)
    private String codigoTrazabilidad;

    @Column(nullable = false)
    private String municipio;

    @Column(nullable = false)
    private String telefono;

    @Column(nullable = false, unique = true)
    private String usuario;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "role_type", nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_status", columnDefinition = "user_status", nullable = false)
    private UserStatus status = UserStatus.ACTIVO;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "user_certifications",
            joinColumns = @JoinColumn(name = "user_id")
    )
    @Column(name = "certification", columnDefinition = "certification_type")
    @Enumerated(EnumType.STRING)
    private Set<Certification> certifications = new HashSet<>();

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getUsername() {
        return usuario;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return status == UserStatus.ACTIVO;
    }
}
