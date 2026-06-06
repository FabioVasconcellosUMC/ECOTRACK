package com.ecotrack.ecotrack_api.dto;

import java.math.BigDecimal;
import java.util.List;

public record DashboardResumoResponse(
        long totalEmpresas,
        long totalLotes,
        long totalEmTransito,
        BigDecimal totalToneladas,
        List<LotesPorMesResponse> lotesPorMes
) {
}
