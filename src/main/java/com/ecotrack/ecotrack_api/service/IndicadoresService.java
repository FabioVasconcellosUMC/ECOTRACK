package com.ecotrack.ecotrack_api.service;

import com.ecotrack.ecotrack_api.dto.DashboardResumoResponse;
import com.ecotrack.ecotrack_api.dto.LotesPorMesResponse;
import com.ecotrack.ecotrack_api.dto.RankingEmpresaResponse;
import com.ecotrack.ecotrack_api.dto.RelatoriosResumoResponse;
import com.ecotrack.ecotrack_api.dto.TotalPorCategoriaResponse;
import com.ecotrack.ecotrack_api.entity.StatusLote;
import com.ecotrack.ecotrack_api.entity.StatusTransporte;
import com.ecotrack.ecotrack_api.entity.TipoEmpresa;
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

    @Transactional(readOnly = true)
    public DashboardResumoResponse resumoDashboard() {
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

    private BigDecimal totalToneladas() {
        return loteRepository.somarQuantidadePorUnidade().stream()
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

    private List<RankingEmpresaResponse> rankingTransportadoras() {
        return transporteRepository.rankingTransportadoras(PageRequest.of(0, LIMITE_RANKING)).stream()
                .map(this::rankingEmpresa)
                .toList();
    }

    private List<RankingEmpresaResponse> rankingGeradoras() {
        return loteRepository.rankingGeradoras(PageRequest.of(0, LIMITE_RANKING)).stream()
                .map(this::rankingEmpresa)
                .toList();
    }

    private RankingEmpresaResponse rankingEmpresa(Object[] linha) {
        return new RankingEmpresaResponse(
                (UUID) linha[0],
                (String) linha[1],
                ((Number) linha[2]).longValue()
        );
    }
}
