package com.ecotrack.ecotrack_api.repository;

import com.ecotrack.ecotrack_api.entity.Transporte;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransporteRepository extends JpaRepository<Transporte, Long> {
    List<Transporte> findByLoteId(Long loteId);
    List<Transporte> findByLote_PublicId(UUID lotePublicId);
    List<Transporte> findByTransportadoraId(Long transportadoraId);
    Optional<Transporte> findByPublicId(UUID publicId);
}
