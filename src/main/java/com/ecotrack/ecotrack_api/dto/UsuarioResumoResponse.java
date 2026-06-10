package com.ecotrack.ecotrack_api.dto;

import com.ecotrack.ecotrack_api.entity.Perfil;

import java.time.LocalDateTime;
import java.util.UUID;

public record UsuarioResumoResponse(
        UUID publicId,
        String nome,
        String emailMascarado,
        Perfil perfil,
        String empresa,
        boolean ativo,
        boolean termosUsoAceitos,
        LocalDateTime termosUsoAceitosEm,
        LocalDateTime criadoEm
) {
}
