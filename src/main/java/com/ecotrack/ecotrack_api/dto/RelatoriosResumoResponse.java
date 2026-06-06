package com.ecotrack.ecotrack_api.dto;

import java.math.BigDecimal;
import java.util.List;

public record RelatoriosResumoResponse(
        long totalEmpresas,
        long totalLotes,
        long totalTransportes,
        long transportesConcluidos,
        long transportesEmAndamento,
        BigDecimal totalToneladas,
        List<TotalPorCategoriaResponse> empresasPorTipo,
        List<TotalPorCategoriaResponse> lotesPorStatus,
        List<RankingEmpresaResponse> rankingTransportadoras,
        List<RankingEmpresaResponse> rankingGeradoras
) {
}
