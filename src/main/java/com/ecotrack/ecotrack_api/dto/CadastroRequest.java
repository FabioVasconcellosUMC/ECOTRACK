package com.ecotrack.ecotrack_api.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CadastroRequest(
        @NotBlank(message = "Nome e obrigatorio")
        @Size(max = 150, message = "Nome deve ter no maximo 150 caracteres")
        @Pattern(regexp = "^[^<>]*$", message = "Nome nao pode conter tags HTML ou scripts")
        String nome,

        @NotBlank(message = "E-mail e obrigatorio")
        @Email(message = "E-mail invalido")
        @Size(max = 150, message = "E-mail deve ter no maximo 150 caracteres")
        String email,

        @NotBlank(message = "Senha e obrigatoria")
        @Size(min = 6, max = 72, message = "Senha deve ter entre 6 e 72 caracteres")
        String senha,

        @NotBlank(message = "Perfil e obrigatorio")
        String perfil,

        @AssertTrue(message = "E necessario aceitar os termos de uso")
        boolean aceitouTermosUso
) {
        public CadastroRequest {
                nome = nome == null ? null : nome.trim();
                email = email == null ? null : email.trim();
                perfil = perfil == null ? null : perfil.trim();
        }
}
