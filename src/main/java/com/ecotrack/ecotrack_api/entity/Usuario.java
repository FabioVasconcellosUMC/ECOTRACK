package com.ecotrack.ecotrack_api.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "usuario")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Usuario implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 500)
    private String nome;

    @Column(columnDefinition = "TEXT")
    private String email;

    @JsonIgnore
    @Column(name = "email_hash", unique = true, length = 100)
    private String emailHash;

    @JsonIgnore
    private String senha;

    @Enumerated(EnumType.STRING)
    private Perfil perfil;

    @ManyToOne
    @JoinColumn(name = "empresa_id")
    private Empresa empresa;

    private boolean ativo = true;

    private LocalDateTime criadoEm = LocalDateTime.now();

    private LocalDateTime excluidoEm;

    @JsonIgnore
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + perfil.name()));
    }

    @JsonIgnore
    @Override public String getPassword() { return senha; }

    @Override public String getUsername() { return emailHash != null ? emailHash : email; }

    @JsonIgnore
    @Override public boolean isAccountNonExpired() { return true; }

    @JsonIgnore
    @Override public boolean isAccountNonLocked() { return true; }

    @JsonIgnore
    @Override public boolean isCredentialsNonExpired() { return true; }

    @Override public boolean isEnabled() { return ativo; }
}
