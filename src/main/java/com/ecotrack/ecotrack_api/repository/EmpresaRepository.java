package com.ecotrack.ecotrack_api.repository;

import com.ecotrack.ecotrack_api.entity.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface EmpresaRepository extends JpaRepository<Empresa, Long> {
    boolean existsByCnpj(String cnpj);
    boolean existsByCnpjHash(String cnpjHash);
    Optional<Empresa> findByPublicId(UUID publicId);
}
