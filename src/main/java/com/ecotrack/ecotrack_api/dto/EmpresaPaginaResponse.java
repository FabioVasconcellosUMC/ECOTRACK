package com.ecotrack.ecotrack_api.dto;

import com.ecotrack.ecotrack_api.entity.Empresa;

import java.util.List;

public record EmpresaPaginaResponse(
        List<Empresa> itens,
        int page,
        int limit,
        boolean hasNext,
        long total,
        long totalGeradoras,
        long totalTransportadoras,
        long totalReceptoras
) {
}
