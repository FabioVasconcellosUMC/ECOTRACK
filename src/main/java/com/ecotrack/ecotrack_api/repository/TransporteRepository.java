package com.ecotrack.ecotrack_api.repository;

import com.ecotrack.ecotrack_api.entity.Transporte;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TransporteRepository extends JpaRepository<Transporte, Long> {
    List<Transporte> findByLoteId(Long loteId);
    List<Transporte> findByTransportadoraId(Long transportadoraId);
}