package com.ecotrack.ecotrack_api.repository;

import com.ecotrack.ecotrack_api.entity.StatusLote;
import com.ecotrack.ecotrack_api.entity.StatusTransporte;
import com.ecotrack.ecotrack_api.entity.Transporte;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransporteRepository extends JpaRepository<Transporte, Long> {
    List<Transporte> findByLoteId(Long loteId);
    List<Transporte> findByLote_PublicId(UUID lotePublicId);
    List<Transporte> findByTransportadoraId(Long transportadoraId);
    Optional<Transporte> findByPublicId(UUID publicId);
    boolean existsByLoteIdAndTransportadoraId(Long loteId, Long transportadoraId);
    boolean existsByLoteIdAndReceptoraId(Long loteId, Long receptoraId);
    boolean existsByLoteIdAndStatusIn(Long loteId, Collection<StatusTransporte> statuses);
    List<Transporte> findAllByOrderByCriadoEmDesc(Pageable pageable);
    List<Transporte> findByLote_EmpresaGeradoraIdOrderByCriadoEmDesc(Long empresaId, Pageable pageable);
    List<Transporte> findByTransportadoraIdOrderByCriadoEmDesc(Long empresaId, Pageable pageable);
    List<Transporte> findByReceptoraIdOrderByCriadoEmDesc(Long empresaId, Pageable pageable);
    long countByStatus(StatusTransporte status);
    long countByLote_EmpresaGeradoraId(Long empresaId);
    long countByTransportadoraId(Long empresaId);
    long countByReceptoraId(Long empresaId);
    long countByLote_EmpresaGeradoraIdAndStatus(Long empresaId, StatusTransporte status);
    long countByTransportadoraIdAndStatus(Long empresaId, StatusTransporte status);
    long countByReceptoraIdAndStatus(Long empresaId, StatusTransporte status);

    @Query("""
            SELECT COUNT(t) FROM Transporte t
            JOIN t.lote l
            JOIN t.transportadora tr
            JOIN t.receptora re
            WHERE LOWER(t.responsavel) LIKE LOWER(CONCAT('%', :q, '%'))
               OR LOWER(l.descricao) LIKE LOWER(CONCAT('%', :q, '%'))
               OR LOWER(tr.razaoSocial) LIKE LOWER(CONCAT('%', :q, '%'))
               OR LOWER(re.razaoSocial) LIKE LOWER(CONCAT('%', :q, '%'))
            """)
    long countPorTexto(@Param("q") String q);

    @Query("""
            SELECT COUNT(t) FROM Transporte t
            JOIN t.lote l
            JOIN t.transportadora tr
            JOIN t.receptora re
            WHERE t.status = :status
              AND (LOWER(t.responsavel) LIKE LOWER(CONCAT('%', :q, '%'))
                   OR LOWER(l.descricao) LIKE LOWER(CONCAT('%', :q, '%'))
                   OR LOWER(tr.razaoSocial) LIKE LOWER(CONCAT('%', :q, '%'))
                   OR LOWER(re.razaoSocial) LIKE LOWER(CONCAT('%', :q, '%')))
            """)
    long countPorTextoEStatus(@Param("q") String q, @Param("status") StatusTransporte status);

    @Query("""
            SELECT COUNT(t) FROM Transporte t
            JOIN t.lote l
            JOIN t.transportadora tr
            JOIN t.receptora re
            WHERE l.empresaGeradora.id = :empresaId
              AND (LOWER(t.responsavel) LIKE LOWER(CONCAT('%', :q, '%'))
                   OR LOWER(l.descricao) LIKE LOWER(CONCAT('%', :q, '%'))
                   OR LOWER(tr.razaoSocial) LIKE LOWER(CONCAT('%', :q, '%'))
                   OR LOWER(re.razaoSocial) LIKE LOWER(CONCAT('%', :q, '%')))
            """)
    long countPorTextoGeradora(@Param("empresaId") Long empresaId, @Param("q") String q);

    @Query("""
            SELECT COUNT(t) FROM Transporte t
            JOIN t.lote l
            JOIN t.transportadora tr
            JOIN t.receptora re
            WHERE tr.id = :empresaId
              AND (LOWER(t.responsavel) LIKE LOWER(CONCAT('%', :q, '%'))
                   OR LOWER(l.descricao) LIKE LOWER(CONCAT('%', :q, '%'))
                   OR LOWER(tr.razaoSocial) LIKE LOWER(CONCAT('%', :q, '%'))
                   OR LOWER(re.razaoSocial) LIKE LOWER(CONCAT('%', :q, '%')))
            """)
    long countPorTextoTransportadora(@Param("empresaId") Long empresaId, @Param("q") String q);

    @Query("""
            SELECT COUNT(t) FROM Transporte t
            JOIN t.lote l
            JOIN t.transportadora tr
            JOIN t.receptora re
            WHERE re.id = :empresaId
              AND (LOWER(t.responsavel) LIKE LOWER(CONCAT('%', :q, '%'))
                   OR LOWER(l.descricao) LIKE LOWER(CONCAT('%', :q, '%'))
                   OR LOWER(tr.razaoSocial) LIKE LOWER(CONCAT('%', :q, '%'))
                   OR LOWER(re.razaoSocial) LIKE LOWER(CONCAT('%', :q, '%')))
            """)
    long countPorTextoReceptora(@Param("empresaId") Long empresaId, @Param("q") String q);

    @Query("""
            SELECT COUNT(t) FROM Transporte t
            JOIN t.lote l
            JOIN t.transportadora tr
            JOIN t.receptora re
            WHERE l.empresaGeradora.id = :empresaId
              AND t.status = :status
              AND (LOWER(t.responsavel) LIKE LOWER(CONCAT('%', :q, '%'))
                   OR LOWER(l.descricao) LIKE LOWER(CONCAT('%', :q, '%'))
                   OR LOWER(tr.razaoSocial) LIKE LOWER(CONCAT('%', :q, '%'))
                   OR LOWER(re.razaoSocial) LIKE LOWER(CONCAT('%', :q, '%')))
            """)
    long countPorTextoGeradoraEStatus(@Param("empresaId") Long empresaId, @Param("q") String q, @Param("status") StatusTransporte status);

    @Query("""
            SELECT COUNT(t) FROM Transporte t
            JOIN t.lote l
            JOIN t.transportadora tr
            JOIN t.receptora re
            WHERE tr.id = :empresaId
              AND t.status = :status
              AND (LOWER(t.responsavel) LIKE LOWER(CONCAT('%', :q, '%'))
                   OR LOWER(l.descricao) LIKE LOWER(CONCAT('%', :q, '%'))
                   OR LOWER(tr.razaoSocial) LIKE LOWER(CONCAT('%', :q, '%'))
                   OR LOWER(re.razaoSocial) LIKE LOWER(CONCAT('%', :q, '%')))
            """)
    long countPorTextoTransportadoraEStatus(@Param("empresaId") Long empresaId, @Param("q") String q, @Param("status") StatusTransporte status);

    @Query("""
            SELECT COUNT(t) FROM Transporte t
            JOIN t.lote l
            JOIN t.transportadora tr
            JOIN t.receptora re
            WHERE re.id = :empresaId
              AND t.status = :status
              AND (LOWER(t.responsavel) LIKE LOWER(CONCAT('%', :q, '%'))
                   OR LOWER(l.descricao) LIKE LOWER(CONCAT('%', :q, '%'))
                   OR LOWER(tr.razaoSocial) LIKE LOWER(CONCAT('%', :q, '%'))
                   OR LOWER(re.razaoSocial) LIKE LOWER(CONCAT('%', :q, '%')))
            """)
    long countPorTextoReceptoraEStatus(@Param("empresaId") Long empresaId, @Param("q") String q, @Param("status") StatusTransporte status);

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
            SELECT t FROM Transporte t
            JOIN t.lote l
            JOIN t.transportadora tr
            JOIN t.receptora re
            WHERE l.empresaGeradora.id = :empresaId
              AND (LOWER(t.responsavel) LIKE LOWER(CONCAT('%', :q, '%'))
                   OR LOWER(l.descricao) LIKE LOWER(CONCAT('%', :q, '%'))
                   OR LOWER(tr.razaoSocial) LIKE LOWER(CONCAT('%', :q, '%'))
                   OR LOWER(re.razaoSocial) LIKE LOWER(CONCAT('%', :q, '%')))
            ORDER BY t.criadoEm DESC
            """)
    List<Transporte> buscarPorTextoGeradora(@Param("empresaId") Long empresaId, @Param("q") String q, Pageable pageable);

    @Query("""
            SELECT t FROM Transporte t
            JOIN t.lote l
            JOIN t.transportadora tr
            JOIN t.receptora re
            WHERE tr.id = :empresaId
              AND (LOWER(t.responsavel) LIKE LOWER(CONCAT('%', :q, '%'))
                   OR LOWER(l.descricao) LIKE LOWER(CONCAT('%', :q, '%'))
                   OR LOWER(tr.razaoSocial) LIKE LOWER(CONCAT('%', :q, '%'))
                   OR LOWER(re.razaoSocial) LIKE LOWER(CONCAT('%', :q, '%')))
            ORDER BY t.criadoEm DESC
            """)
    List<Transporte> buscarPorTextoTransportadora(@Param("empresaId") Long empresaId, @Param("q") String q, Pageable pageable);

    @Query("""
            SELECT t FROM Transporte t
            JOIN t.lote l
            JOIN t.transportadora tr
            JOIN t.receptora re
            WHERE re.id = :empresaId
              AND (LOWER(t.responsavel) LIKE LOWER(CONCAT('%', :q, '%'))
                   OR LOWER(l.descricao) LIKE LOWER(CONCAT('%', :q, '%'))
                   OR LOWER(tr.razaoSocial) LIKE LOWER(CONCAT('%', :q, '%'))
                   OR LOWER(re.razaoSocial) LIKE LOWER(CONCAT('%', :q, '%')))
            ORDER BY t.criadoEm DESC
            """)
    List<Transporte> buscarPorTextoReceptora(@Param("empresaId") Long empresaId, @Param("q") String q, Pageable pageable);

    @Query("SELECT COUNT(DISTINCT t.lote.id) FROM Transporte t WHERE t.transportadora.id = :empresaId")
    long countLotesPorTransportadora(@Param("empresaId") Long empresaId);

    @Query("SELECT COUNT(DISTINCT t.lote.id) FROM Transporte t WHERE t.receptora.id = :empresaId")
    long countLotesPorReceptora(@Param("empresaId") Long empresaId);

    @Query("SELECT COUNT(DISTINCT t.lote.id) FROM Transporte t WHERE t.transportadora.id = :empresaId AND t.lote.status = :status")
    long countLotesPorTransportadoraEStatus(@Param("empresaId") Long empresaId, @Param("status") StatusLote status);

    @Query("SELECT COUNT(DISTINCT t.lote.id) FROM Transporte t WHERE t.receptora.id = :empresaId AND t.lote.status = :status")
    long countLotesPorReceptoraEStatus(@Param("empresaId") Long empresaId, @Param("status") StatusLote status);

    @Query("""
            SELECT t.transportadora.publicId, t.transportadora.razaoSocial, COUNT(t)
            FROM Transporte t
            GROUP BY t.transportadora.publicId, t.transportadora.razaoSocial
            ORDER BY COUNT(t) DESC
            """)
    List<Object[]> rankingTransportadoras(Pageable pageable);

    @Query("""
            SELECT t.transportadora.publicId, t.transportadora.razaoSocial, COUNT(t)
            FROM Transporte t
            WHERE t.transportadora.id = :empresaId
            GROUP BY t.transportadora.publicId, t.transportadora.razaoSocial
            ORDER BY COUNT(t) DESC
            """)
    List<Object[]> rankingTransportadoraPropria(@Param("empresaId") Long empresaId, Pageable pageable);

    @Query("""
            SELECT t.transportadora.publicId, t.transportadora.razaoSocial, COUNT(t)
            FROM Transporte t
            WHERE t.lote.empresaGeradora.id = :empresaId
            GROUP BY t.transportadora.publicId, t.transportadora.razaoSocial
            ORDER BY COUNT(t) DESC
            """)
    List<Object[]> rankingTransportadorasPorGeradora(@Param("empresaId") Long empresaId, Pageable pageable);

    @Query("""
            SELECT t.transportadora.publicId, t.transportadora.razaoSocial, COUNT(t)
            FROM Transporte t
            WHERE t.receptora.id = :empresaId
            GROUP BY t.transportadora.publicId, t.transportadora.razaoSocial
            ORDER BY COUNT(t) DESC
            """)
    List<Object[]> rankingTransportadorasPorReceptora(@Param("empresaId") Long empresaId, Pageable pageable);

    @Query("""
            SELECT t.lote.empresaGeradora.publicId, t.lote.empresaGeradora.razaoSocial, COUNT(DISTINCT t.lote)
            FROM Transporte t
            WHERE t.transportadora.id = :empresaId
            GROUP BY t.lote.empresaGeradora.publicId, t.lote.empresaGeradora.razaoSocial
            ORDER BY COUNT(DISTINCT t.lote) DESC
            """)
    List<Object[]> rankingGeradorasPorTransportadora(@Param("empresaId") Long empresaId, Pageable pageable);

    @Query("""
            SELECT t.lote.empresaGeradora.publicId, t.lote.empresaGeradora.razaoSocial, COUNT(DISTINCT t.lote)
            FROM Transporte t
            WHERE t.receptora.id = :empresaId
            GROUP BY t.lote.empresaGeradora.publicId, t.lote.empresaGeradora.razaoSocial
            ORDER BY COUNT(DISTINCT t.lote) DESC
            """)
    List<Object[]> rankingGeradorasPorReceptora(@Param("empresaId") Long empresaId, Pageable pageable);
}
