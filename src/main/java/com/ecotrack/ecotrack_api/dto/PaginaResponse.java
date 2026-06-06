package com.ecotrack.ecotrack_api.dto;

import java.util.List;
import java.util.Map;

public record PaginaResponse<T>(
        List<T> itens,
        int page,
        int limit,
        boolean hasNext,
        long total,
        Map<String, Long> totalPorStatus
) {
    public PaginaResponse(List<T> itens, int page, int limit, boolean hasNext) {
        this(itens, page, limit, hasNext, itens.size(), Map.of());
    }
}
