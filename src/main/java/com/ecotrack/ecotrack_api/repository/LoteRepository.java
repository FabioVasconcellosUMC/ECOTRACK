package com.ecotrack.ecotrack_api.repository;

import com.ecotrack.ecotrack_api.entity.Lote;
import com.ecotrack.ecotrack_api.entity.StatusLote;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LoteRepository extends JpaRepository<Lote, Long> {

    @Query("SELECT l FROM Lote l JOIN FETCH l.empresaGeradora WHERE l.id = :id")
    Optional<Lote> findByIdWithEmpresa(@Param("id") Long id);

    @Query("SELECT l FROM Lote l JOIN FETCH l.empresaGeradora WHERE l.publicId = :publicId")
    Optional<Lote> findByPublicIdWithEmpresa(@Param("publicId") UUID publicId);

    Optional<Lote> findByPublicId(UUID publicId);
    long countByStatus(StatusLote status);

    @Query("""
            SELECT l FROM Lote l
            JOIN FETCH l.empresaGeradora
            ORDER BY l.criadoEm DESC
            """)
    List<Lote> findRecentes(Pageable pageable);
    List<Lote> findByEmpresaGeradoraIdOrderByCriadoEmDesc(Long empresaId, Pageable pageable);
    long countByEmpresaGeradoraId(Long empresaId);
    long countByEmpresaGeradoraIdAndStatus(Long empresaId, StatusLote status);

    @Query("""
            SELECT l FROM Lote l
            JOIN FETCH l.empresaGeradora
            WHERE LOWER(l.descricao) LIKE LOWER(CONCAT('%', :q, '%'))
               OR LOWER(l.tipoResiduo) LIKE LOWER(CONCAT('%', :q, '%'))
            ORDER BY l.criadoEm DESC
            """)
    List<Lote> buscarPorTexto(@Param("q") String q, Pageable pageable);

    @Query("""
            SELECT DISTINCT l FROM Lote l
            JOIN FETCH l.empresaGeradora
            JOIN Transporte t ON t.lote = l
            WHERE t.transportadora.id = :empresaId
            ORDER BY l.criadoEm DESC
            """)
    List<Lote> findRecentesPorTransportadora(@Param("empresaId") Long empresaId, Pageable pageable);

    @Query("""
            SELECT DISTINCT l FROM Lote l
            JOIN FETCH l.empresaGeradora
            JOIN Transporte t ON t.lote = l
            WHERE t.receptora.id = :empresaId
            ORDER BY l.criadoEm DESC
            """)
    List<Lote> findRecentesPorReceptora(@Param("empresaId") Long empresaId, Pageable pageable);

    @Query("""
            SELECT l FROM Lote l
            JOIN FETCH l.empresaGeradora
            WHERE l.empresaGeradora.id = :empresaId
              AND (LOWER(l.descricao) LIKE LOWER(CONCAT('%', :q, '%'))
                   OR LOWER(l.tipoResiduo) LIKE LOWER(CONCAT('%', :q, '%')))
            ORDER BY l.criadoEm DESC
            """)
    List<Lote> buscarPorTextoGeradora(@Param("empresaId") Long empresaId, @Param("q") String q, Pageable pageable);

    @Query("""
            SELECT DISTINCT l FROM Lote l
            JOIN FETCH l.empresaGeradora
            JOIN Transporte t ON t.lote = l
            WHERE t.transportadora.id = :empresaId
              AND (LOWER(l.descricao) LIKE LOWER(CONCAT('%', :q, '%'))
                   OR LOWER(l.tipoResiduo) LIKE LOWER(CONCAT('%', :q, '%')))
            ORDER BY l.criadoEm DESC
            """)
    List<Lote> buscarPorTextoTransportadora(@Param("empresaId") Long empresaId, @Param("q") String q, Pageable pageable);

    @Query("""
            SELECT DISTINCT l FROM Lote l
            JOIN FETCH l.empresaGeradora
            JOIN Transporte t ON t.lote = l
            WHERE t.receptora.id = :empresaId
              AND (LOWER(l.descricao) LIKE LOWER(CONCAT('%', :q, '%'))
                   OR LOWER(l.tipoResiduo) LIKE LOWER(CONCAT('%', :q, '%')))
            ORDER BY l.criadoEm DESC
            """)
    List<Lote> buscarPorTextoReceptora(@Param("empresaId") Long empresaId, @Param("q") String q, Pageable pageable);

    @Query("""
            SELECT UPPER(l.unidade), COALESCE(SUM(l.quantidade), 0)
            FROM Lote l
            GROUP BY UPPER(l.unidade)
            """)
    List<Object[]> somarQuantidadePorUnidade();

    @Query("""
            SELECT UPPER(l.unidade), COALESCE(SUM(l.quantidade), 0)
            FROM Lote l
            WHERE l.empresaGeradora.id = :empresaId
            GROUP BY UPPER(l.unidade)
            """)
    List<Object[]> somarQuantidadePorUnidadeGeradora(@Param("empresaId") Long empresaId);

    @Query("""
            SELECT UPPER(l.unidade), COALESCE(SUM(l.quantidade), 0)
            FROM Lote l
            JOIN Transporte t ON t.lote = l
            WHERE t.transportadora.id = :empresaId
            GROUP BY UPPER(l.unidade)
            """)
    List<Object[]> somarQuantidadePorUnidadeTransportadora(@Param("empresaId") Long empresaId);

    @Query("""
            SELECT UPPER(l.unidade), COALESCE(SUM(l.quantidade), 0)
            FROM Lote l
            JOIN Transporte t ON t.lote = l
            WHERE t.receptora.id = :empresaId
            GROUP BY UPPER(l.unidade)
            """)
    List<Object[]> somarQuantidadePorUnidadeReceptora(@Param("empresaId") Long empresaId);

    @Query("""
            SELECT YEAR(l.criadoEm), MONTH(l.criadoEm), COUNT(l)
            FROM Lote l
            WHERE l.criadoEm >= :inicio
            GROUP BY YEAR(l.criadoEm), MONTH(l.criadoEm)
            """)
    List<Object[]> contarPorMesDesde(@Param("inicio") java.time.LocalDateTime inicio);

    @Query("""
            SELECT YEAR(l.criadoEm), MONTH(l.criadoEm), COUNT(l)
            FROM Lote l
            WHERE l.criadoEm >= :inicio
              AND l.empresaGeradora.id = :empresaId
            GROUP BY YEAR(l.criadoEm), MONTH(l.criadoEm)
            """)
    List<Object[]> contarPorMesDesdeGeradora(@Param("inicio") java.time.LocalDateTime inicio, @Param("empresaId") Long empresaId);

    @Query("""
            SELECT YEAR(l.criadoEm), MONTH(l.criadoEm), COUNT(DISTINCT l)
            FROM Lote l
            JOIN Transporte t ON t.lote = l
            WHERE l.criadoEm >= :inicio
              AND t.transportadora.id = :empresaId
            GROUP BY YEAR(l.criadoEm), MONTH(l.criadoEm)
            """)
    List<Object[]> contarPorMesDesdeTransportadora(@Param("inicio") java.time.LocalDateTime inicio, @Param("empresaId") Long empresaId);

    @Query("""
            SELECT YEAR(l.criadoEm), MONTH(l.criadoEm), COUNT(DISTINCT l)
            FROM Lote l
            JOIN Transporte t ON t.lote = l
            WHERE l.criadoEm >= :inicio
              AND t.receptora.id = :empresaId
            GROUP BY YEAR(l.criadoEm), MONTH(l.criadoEm)
            """)
    List<Object[]> contarPorMesDesdeReceptora(@Param("inicio") java.time.LocalDateTime inicio, @Param("empresaId") Long empresaId);

    @Query("""
            SELECT l.empresaGeradora.publicId, l.empresaGeradora.razaoSocial, COUNT(l)
            FROM Lote l
            GROUP BY l.empresaGeradora.publicId, l.empresaGeradora.razaoSocial
            ORDER BY COUNT(l) DESC
            """)
    List<Object[]> rankingGeradoras(Pageable pageable);

    @Query("""
            SELECT l.empresaGeradora.publicId, l.empresaGeradora.razaoSocial, COUNT(l)
            FROM Lote l
            WHERE l.empresaGeradora.id = :empresaId
            GROUP BY l.empresaGeradora.publicId, l.empresaGeradora.razaoSocial
            ORDER BY COUNT(l) DESC
            """)
    List<Object[]> rankingGeradoraPropria(@Param("empresaId") Long empresaId, Pageable pageable);
}
