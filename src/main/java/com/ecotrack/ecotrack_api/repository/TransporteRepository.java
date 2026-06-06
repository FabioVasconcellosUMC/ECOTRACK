package com.ecotrack.ecotrack_api.repository;

import com.ecotrack.ecotrack_api.entity.Transporte;
import com.ecotrack.ecotrack_api.entity.StatusTransporte;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransporteRepository extends JpaRepository<Transporte, Long> {
    List<Transporte> findByLoteId(Long loteId);
    List<Transporte> findByLote_PublicId(UUID lotePublicId);
    List<Transporte> findByTransportadoraId(Long transportadoraId);
    Optional<Transporte> findByPublicId(UUID publicId);
    List<Transporte> findAllByOrderByCriadoEmDesc(Pageable pageable);
    long countByStatus(StatusTransporte status);

    @Query("""
            SELECT t FROM Transporte t
            JOIN t.lote l
            JOIN t.transportadora tr
            JOIN t.receptora re
            WHERE LOWER(t.responsavel) LIKE LOWER(CONCAT('%', :q, '%'))
               OR LOWER(l.descricao) LIKE LOWER(CONCAT('%', :q, '%'))
               OR LOWER(tr.razaoSocial) LIKE LOWER(CONCAT('%', :q, '%'))
               OR LOWER(re.razaoSocial) LIKE LOWER(CONCAT('%', :q, '%'))
            ORDER BY t.criadoEm DESC
            """)
    List<Transporte> buscarPorTexto(@Param("q") String q, Pageable pageable);

    @Query("""
            SELECT t.transportadora.publicId, t.transportadora.razaoSocial, COUNT(t)
            FROM Transporte t
            GROUP BY t.transportadora.publicId, t.transportadora.razaoSocial
            ORDER BY COUNT(t) DESC
            """)
    List<Object[]> rankingTransportadoras(Pageable pageable);
}
