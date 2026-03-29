package com.ecotrack.ecotrack_api.repository;

import com.ecotrack.ecotrack_api.entity.HistoricoLote;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface HistoricoLoteRepository extends JpaRepository<HistoricoLote, Long> {
    List<HistoricoLote> findByLoteIdOrderByDataHoraDesc(Long loteId);
}