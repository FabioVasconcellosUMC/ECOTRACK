package com.ecotrack.ecotrack_api.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleNotFoundRetornaStatus404() {
        ResponseEntity<Map<String, String>> response =
                handler.handleNotFound(new RecursoNaoEncontradoException("Lote nao encontrado"));

        assertThat(response.getStatusCode().value()).isEqualTo(404);
        assertThat(response.getBody()).containsEntry("erro", "Lote nao encontrado");
    }

    @Test
    void handleBusinessRuleRetornaStatus400() {
        ResponseEntity<Map<String, String>> response =
                handler.handleBusinessRule(new RegraNegocioException("Regra invalida"));

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).containsEntry("erro", "Regra invalida");
    }

    @Test
    void handleBadCredentialsRetornaStatus401() {
        ResponseEntity<Map<String, String>> response = handler.handleBadCredentials(null);

        assertThat(response.getStatusCode().value()).isEqualTo(401);
        assertThat(response.getBody()).containsEntry("erro", "Email ou senha invalidos");
    }
}
