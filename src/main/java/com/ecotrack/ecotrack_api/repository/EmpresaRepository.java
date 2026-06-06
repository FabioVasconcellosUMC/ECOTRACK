package com.ecotrack.ecotrack_api.repository;

import com.ecotrack.ecotrack_api.entity.Empresa;
import com.ecotrack.ecotrack_api.entity.TipoEmpresa;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EmpresaRepository extends JpaRepository<Empresa, Long> {
    boolean existsByCnpj(String cnpj);
    boolean existsByCnpjHash(String cnpjHash);
    Optional<Empresa> findByPublicId(UUID publicId);
    List<Empresa> findAllByOrderByCriadoEmDesc(Pageable pageable);
    List<Empresa> findByRazaoSocialContainingIgnoreCaseOrderByCriadoEmDesc(String razaoSocial, Pageable pageable);
    long countByTipo(TipoEmpresa tipo);
}
