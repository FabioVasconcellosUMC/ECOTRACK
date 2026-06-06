package com.ecotrack.ecotrack_api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank(message = "E-mail e obrigatorio")
        @Email(message = "E-mail invalido")
        @Size(max = 150, message = "E-mail deve ter no maximo 150 caracteres")
        String email,

        @NotBlank(message = "Senha e obrigatoria")
        @Size(max = 72, message = "Senha deve ter no maximo 72 caracteres")
        String senha
) {}
