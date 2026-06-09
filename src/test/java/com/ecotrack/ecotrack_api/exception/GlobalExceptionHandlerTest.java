package com.ecotrack.ecotrack_api.exception;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleNotFoundRetorna404() {
        ResponseEntity<Map<String, String>> response = handler.handleNotFound(
                new RecursoNaoEncontradoException("Empresa não encontrada")
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).containsEntry("erro", "Empresa não encontrada");
    }

    @Test
    void handleBusinessRuleRetorna400() {
        ResponseEntity<Map<String, String>> response = handler.handleBusinessRule(
                new RegraNegocioException("Lote não está disponível para transporte")
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).containsEntry("erro", "Lote não está disponível para transporte");
    }

    @Test
    void handleDataIntegrityRetornaMensagemSegura() {
        ResponseEntity<Map<String, String>> response = handler.handleDataIntegrity(
                new DataIntegrityViolationException("constraint violation")
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).containsEntry("erro", "Dados ja cadastrados ou violam uma regra de integridade");
    }

    @Test
    void handleBadCredentialsRetorna401() {
        ResponseEntity<Map<String, String>> response = handler.handleBadCredentials(
                new BadCredentialsException("bad credentials")
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).containsEntry("erro", "E-mail ou senha invalidos");
    }

    @Test
    void handleAuthenticationRetorna401ParaFalhasInternasDeAutenticacao() {
        ResponseEntity<Map<String, String>> response = handler.handleAuthentication(
                new InternalAuthenticationServiceException("usuario nao encontrado")
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).containsEntry("erro", "E-mail ou senha invalidos");
    }

    @Test
    void handleRuntimeRetornaMensagemGenerica() {
        ResponseEntity<Map<String, String>> response = handler.handleRuntime(
                new RuntimeException("detalhe interno sensivel")
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).containsEntry("erro", "Erro interno ao processar a requisicao");
    }
}
