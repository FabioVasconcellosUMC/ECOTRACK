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

    @Query("""
            SELECT l FROM Lote l
            JOIN FETCH l.empresaGeradora
            WHERE LOWER(l.descricao) LIKE LOWER(CONCAT('%', :q, '%'))
               OR LOWER(l.tipoResiduo) LIKE LOWER(CONCAT('%', :q, '%'))
            ORDER BY l.criadoEm DESC
            """)
    List<Lote> buscarPorTexto(@Param("q") String q, Pageable pageable);

    @Query("""
            SELECT UPPER(l.unidade), COALESCE(SUM(l.quantidade), 0)
            FROM Lote l
            GROUP BY UPPER(l.unidade)
            """)
    List<Object[]> somarQuantidadePorUnidade();

    @Query("""
            SELECT YEAR(l.criadoEm), MONTH(l.criadoEm), COUNT(l)
            FROM Lote l
            WHERE l.criadoEm >= :inicio
            GROUP BY YEAR(l.criadoEm), MONTH(l.criadoEm)
            """)
    List<Object[]> contarPorMesDesde(@Param("inicio") java.time.LocalDateTime inicio);

    @Query("""
            SELECT l.empresaGeradora.publicId, l.empresaGeradora.razaoSocial, COUNT(l)
            FROM Lote l
            GROUP BY l.empresaGeradora.publicId, l.empresaGeradora.razaoSocial
            ORDER BY COUNT(l) DESC
            """)
    List<Object[]> rankingGeradoras(Pageable pageable);
}
