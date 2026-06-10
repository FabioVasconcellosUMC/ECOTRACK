package com.ecotrack.ecotrack_api.repository;

import com.ecotrack.ecotrack_api.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmail(String email);
    Optional<Usuario> findByEmailHash(String emailHash);
    Optional<Usuario> findByEmailAndAtivoTrue(String email);
    Optional<Usuario> findByEmailHashAndAtivoTrue(String emailHash);
    Optional<Usuario> findByPublicId(UUID publicId);
}
