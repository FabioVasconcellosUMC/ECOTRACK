package com.ecotrack.ecotrack_api.repository;

import com.ecotrack.ecotrack_api.entity.Lote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import java.util.UUID;

public interface LoteRepository extends JpaRepository<Lote, Long> {

    @Query("SELECT l FROM Lote l JOIN FETCH l.empresaGeradora WHERE l.id = :id")
    Optional<Lote> findByIdWithEmpresa(@Param("id") Long id);

    @Query("SELECT l FROM Lote l JOIN FETCH l.empresaGeradora WHERE l.publicId = :publicId")
    Optional<Lote> findByPublicIdWithEmpresa(@Param("publicId") UUID publicId);

    Optional<Lote> findByPublicId(UUID publicId);
}
