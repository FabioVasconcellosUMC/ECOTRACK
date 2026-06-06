package com.ecotrack.ecotrack_api.dto;

import java.util.List;

public record PaginaResponse<T>(
        List<T> itens,
        int page,
        int limit,
        boolean hasNext
) {
}
