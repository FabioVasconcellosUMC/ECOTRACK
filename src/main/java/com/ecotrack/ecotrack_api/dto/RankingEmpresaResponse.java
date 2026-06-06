package com.ecotrack.ecotrack_api.dto;

import java.util.UUID;

public record RankingEmpresaResponse(
        UUID id,
        String razaoSocial,
        long total
) {
}
