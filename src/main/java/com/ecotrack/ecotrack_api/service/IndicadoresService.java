package com.ecotrack.ecotrack_api.service;

import com.ecotrack.ecotrack_api.dto.DashboardResumoResponse;
import com.ecotrack.ecotrack_api.dto.LotesPorMesResponse;
import com.ecotrack.ecotrack_api.dto.RankingEmpresaResponse;
import com.ecotrack.ecotrack_api.dto.RelatoriosResumoResponse;
import com.ecotrack.ecotrack_api.dto.TotalPorCategoriaResponse;
import com.ecotrack.ecotrack_api.entity.Empresa;
import com.ecotrack.ecotrack_api.entity.Perfil;
import com.ecotrack.ecotrack_api.entity.StatusLote;
import com.ecotrack.ecotrack_api.entity.StatusTransporte;
import com.ecotrack.ecotrack_api.entity.TipoEmpresa;
import com.ecotrack.ecotrack_api.entity.Usuario;
import com.ecotrack.ecotrack_api.repository.EmpresaRepository;
import com.ecotrack.ecotrack_api.repository.LoteRepository;
import com.ecotrack.ecotrack_api.repository.TransporteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class IndicadoresService {

    private static final int MESES_DASHBOARD = 6;
    private static final int LIMITE_RANKING = 5;

    private final EmpresaRepository empresaRepository;
    private final LoteRepository loteRepository;
    private final TransporteRepository transporteRepository;
    private final EscopoUsuarioService escopoUsuarioService;

    @Transactional(readOnly = true)
    public DashboardResumoResponse resumoDashboard() {
        Usuario usuario = escopoUsuarioService.usuarioAutenticado();
        if (!escopoUsuarioService.isAdmin(usuario)) {
            Empresa empresa = escopoUsuarioService.empresaVinculada(usuario);
            if (empresa == null) {
                return resumoDashboardVazio();
            }

            return resumoDashboardEmpresa(usuario.getPerfil(), empresa.getId());
        }

        return new DashboardResumoResponse(
                empresaRepository.count(),
                loteRepository.count(),
                transporteRepository.countByStatus(StatusTransporte.EM_TRANSITO),
                totalToneladas(),
                lotesPorMes()
        );
    }

    @Transactional(readOnly = true)
    public RelatoriosResumoResponse resumoRelatorios() {
        Usuario usuario = escopoUsuarioService.usuarioAutenticado();
        if (!escopoUsuarioService.isAdmin(usuario)) {
            Empresa empresa = escopoUsuarioService.empresaVinculada(usuario);
            if (empresa == null) {
                return resumoRelatoriosVazio();
            }

            return resumoRelatoriosEmpresa(usuario.getPerfil(), empresa);
        }

        return new RelatoriosResumoResponse(
                empresaRepository.count(),
                loteRepository.count(),
                transporteRepository.count(),
                transporteRepository.countByStatus(StatusTransporte.CONCLUIDO),
                transporteRepository.countByStatus(StatusTransporte.PENDENTE)
                        + transporteRepository.countByStatus(StatusTransporte.EM_TRANSITO),
                totalToneladas(),
                empresasPorTipo(),
                lotesPorStatus(),
                rankingTransportadoras(),
                rankingGeradoras()
        );
    }

    private DashboardResumoResponse resumoDashboardEmpresa(Perfil perfil, Long empresaId) {
        return new DashboardResumoResponse(
                1,
                totalLotes(perfil, empresaId),
                totalTransportesPorStatus(perfil, empresaId, StatusTransporte.EM_TRANSITO),
                totalToneladas(perfil, empresaId),
                lotesPorMes(perfil, empresaId)
        );
    }

    private RelatoriosResumoResponse resumoRelatoriosEmpresa(Perfil perfil, Empresa empresa) {
        Long empresaId = empresa.getId();
        return new RelatoriosResumoResponse(
                1,
                totalLotes(perfil, empresaId),
                totalTransportes(perfil, empresaId),
                totalTransportesPorStatus(perfil, empresaId, StatusTransporte.CONCLUIDO),
                totalTransportesPorStatus(perfil, empresaId, StatusTransporte.PENDENTE)
                        + totalTransportesPorStatus(perfil, empresaId, StatusTransporte.EM_TRANSITO),
                totalToneladas(perfil, empresaId),
                List.of(new TotalPorCategoriaResponse(empresa.getTipo().name(), 1)),
                lotesPorStatus(perfil, empresaId),
                rankingTransportadoras(perfil, empresaId),
                rankingGeradoras(perfil, empresaId)
        );
    }

    private DashboardResumoResponse resumoDashboardVazio() {
        return new DashboardResumoResponse(0, 0, 0, BigDecimal.ZERO, List.of());
    }

    private RelatoriosResumoResponse resumoRelatoriosVazio() {
        return new RelatoriosResumoResponse(0, 0, 0, 0, 0, BigDecimal.ZERO, List.of(), List.of(), List.of(), List.of());
    }

    private List<TotalPorCategoriaResponse> empresasPorTipo() {
        return List.of(
                new TotalPorCategoriaResponse(TipoEmpresa.GERADORA.name(), empresaRepository.countByTipo(TipoEmpresa.GERADORA)),
                new TotalPorCategoriaResponse(TipoEmpresa.TRANSPORTADORA.name(), empresaRepository.countByTipo(TipoEmpresa.TRANSPORTADORA)),
                new TotalPorCategoriaResponse(TipoEmpresa.RECEPTORA.name(), empresaRepository.countByTipo(TipoEmpresa.RECEPTORA))
        );
    }

    private List<TotalPorCategoriaResponse> lotesPorStatus() {
        return List.of(
                new TotalPorCategoriaResponse(StatusLote.AGUARDANDO_COLETA.name(), loteRepository.countByStatus(StatusLote.AGUARDANDO_COLETA)),
                new TotalPorCategoriaResponse(StatusLote.EM_TRANSITO.name(), loteRepository.countByStatus(StatusLote.EM_TRANSITO)),
                new TotalPorCategoriaResponse(StatusLote.DESCARTADO.name(), loteRepository.countByStatus(StatusLote.DESCARTADO)),
                new TotalPorCategoriaResponse(StatusLote.CANCELADO.name(), loteRepository.countByStatus(StatusLote.CANCELADO))
        );
    }

    private List<TotalPorCategoriaResponse> lotesPorStatus(Perfil perfil, Long empresaId) {
        return List.of(
                new TotalPorCategoriaResponse(StatusLote.AGUARDANDO_COLETA.name(), totalLotesPorStatus(perfil, empresaId, StatusLote.AGUARDANDO_COLETA)),
                new TotalPorCategoriaResponse(StatusLote.EM_TRANSITO.name(), totalLotesPorStatus(perfil, empresaId, StatusLote.EM_TRANSITO)),
                new TotalPorCategoriaResponse(StatusLote.DESCARTADO.name(), totalLotesPorStatus(perfil, empresaId, StatusLote.DESCARTADO)),
                new TotalPorCategoriaResponse(StatusLote.CANCELADO.name(), totalLotesPorStatus(perfil, empresaId, StatusLote.CANCELADO))
        );
    }

    private BigDecimal totalToneladas() {
        return loteRepository.somarQuantidadePorUnidade().stream()
                .map(this::converterParaTonelada)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal totalToneladas(Perfil perfil, Long empresaId) {
        List<Object[]> totais = switch (perfil) {
            case GERADORA -> loteRepository.somarQuantidadePorUnidadeGeradora(empresaId);
            case TRANSPORTADORA -> loteRepository.somarQuantidadePorUnidadeTransportadora(empresaId);
            case RECEPTORA -> loteRepository.somarQuantidadePorUnidadeReceptora(empresaId);
            default -> List.of();
        };

        return totais.stream()
                .map(this::converterParaTonelada)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal converterParaTonelada(Object[] linha) {
        String unidade = String.valueOf(linha[0]);
        BigDecimal quantidade = (BigDecimal) linha[1];

        if ("TON".equals(unidade)) {
            return quantidade;
        }

        if ("KG".equals(unidade)) {
            return quantidade.divide(BigDecimal.valueOf(1000));
        }

        return BigDecimal.ZERO;
    }

    private List<LotesPorMesResponse> lotesPorMes() {
        LocalDate inicio = LocalDate.now()
                .minusMonths(MESES_DASHBOARD - 1L)
                .withDayOfMonth(1);

        return loteRepository.contarPorMesDesde(inicio.atStartOfDay()).stream()
                .map(linha -> new LotesPorMesResponse(
                        ((Number) linha[0]).intValue(),
                        ((Number) linha[1]).intValue(),
                        ((Number) linha[2]).longValue()
                ))
                .toList();
    }

    private List<LotesPorMesResponse> lotesPorMes(Perfil perfil, Long empresaId) {
        LocalDate inicio = LocalDate.now()
                .minusMonths(MESES_DASHBOARD - 1L)
                .withDayOfMonth(1);

        List<Object[]> linhas = switch (perfil) {
            case GERADORA -> loteRepository.contarPorMesDesdeGeradora(inicio.atStartOfDay(), empresaId);
            case TRANSPORTADORA -> loteRepository.contarPorMesDesdeTransportadora(inicio.atStartOfDay(), empresaId);
            case RECEPTORA -> loteRepository.contarPorMesDesdeReceptora(inicio.atStartOfDay(), empresaId);
            default -> List.of();
        };

        return linhas.stream()
                .map(linha -> new LotesPorMesResponse(
                        ((Number) linha[0]).intValue(),
                        ((Number) linha[1]).intValue(),
                        ((Number) linha[2]).longValue()
                ))
                .toList();
    }

    private List<RankingEmpresaResponse> rankingTransportadoras() {
        return transporteRepository.rankingTransportadoras(PageRequest.of(0, LIMITE_RANKING)).stream()
                .map(this::rankingEmpresa)
                .toList();
    }

    private List<RankingEmpresaResponse> rankingTransportadoras(Perfil perfil, Long empresaId) {
        PageRequest limite = PageRequest.of(0, LIMITE_RANKING);
        List<Object[]> linhas = switch (perfil) {
            case GERADORA -> transporteRepository.rankingTransportadorasPorGeradora(empresaId, limite);
            case TRANSPORTADORA -> transporteRepository.rankingTransportadoraPropria(empresaId, limite);
            case RECEPTORA -> transporteRepository.rankingTransportadorasPorReceptora(empresaId, limite);
            default -> List.of();
        };

        return linhas.stream().map(this::rankingEmpresa).toList();
    }

    private List<RankingEmpresaResponse> rankingGeradoras() {
        return loteRepository.rankingGeradoras(PageRequest.of(0, LIMITE_RANKING)).stream()
                .map(this::rankingEmpresa)
                .toList();
    }

    private List<RankingEmpresaResponse> rankingGeradoras(Perfil perfil, Long empresaId) {
        PageRequest limite = PageRequest.of(0, LIMITE_RANKING);
        List<Object[]> linhas = switch (perfil) {
            case GERADORA -> loteRepository.rankingGeradoraPropria(empresaId, limite);
            case TRANSPORTADORA -> transporteRepository.rankingGeradorasPorTransportadora(empresaId, limite);
            case RECEPTORA -> transporteRepository.rankingGeradorasPorReceptora(empresaId, limite);
            default -> List.of();
        };

        return linhas.stream().map(this::rankingEmpresa).toList();
    }

    private long totalLotes(Perfil perfil, Long empresaId) {
        return switch (perfil) {
            case GERADORA -> loteRepository.countByEmpresaGeradoraId(empresaId);
            case TRANSPORTADORA -> transporteRepository.countLotesPorTransportadora(empresaId);
            case RECEPTORA -> transporteRepository.countLotesPorReceptora(empresaId);
            default -> 0;
        };
    }

    private long totalLotesPorStatus(Perfil perfil, Long empresaId, StatusLote status) {
        if (perfil == Perfil.GERADORA) {
            return loteRepository.countByEmpresaGeradoraIdAndStatus(empresaId, status);
        }

        return lotesPorStatus(perfil, empresaId, status);
    }

    private long lotesPorStatus(Perfil perfil, Long empresaId, StatusLote status) {
        return switch (perfil) {
            case TRANSPORTADORA -> transporteRepository.countLotesPorTransportadoraEStatus(empresaId, status);
            case RECEPTORA -> transporteRepository.countLotesPorReceptoraEStatus(empresaId, status);
            default -> 0;
        };
    }

    private long totalTransportes(Perfil perfil, Long empresaId) {
        return switch (perfil) {
            case GERADORA -> transporteRepository.countByLote_EmpresaGeradoraId(empresaId);
            case TRANSPORTADORA -> transporteRepository.countByTransportadoraId(empresaId);
            case RECEPTORA -> transporteRepository.countByReceptoraId(empresaId);
            default -> 0;
        };
    }

    private long totalTransportesPorStatus(Perfil perfil, Long empresaId, StatusTransporte status) {
        return switch (perfil) {
            case GERADORA -> transporteRepository.countByLote_EmpresaGeradoraIdAndStatus(empresaId, status);
            case TRANSPORTADORA -> transporteRepository.countByTransportadoraIdAndStatus(empresaId, status);
            case RECEPTORA -> transporteRepository.countByReceptoraIdAndStatus(empresaId, status);
            default -> 0;
        };
    }

    private RankingEmpresaResponse rankingEmpresa(Object[] linha) {
        return new RankingEmpresaResponse(
                (UUID) linha[0],
                (String) linha[1],
                ((Number) linha[2]).longValue()
        );
    }
}
