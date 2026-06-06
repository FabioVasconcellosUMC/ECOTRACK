package com.ecotrack.ecotrack_api.dto;

public record LotesPorMesResponse(
        int ano,
        int mes,
        long total
) {
}
