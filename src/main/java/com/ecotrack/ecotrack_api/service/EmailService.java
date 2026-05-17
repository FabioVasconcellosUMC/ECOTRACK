package com.ecotrack.ecotrack_api.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private static final URI RESEND_EMAILS_ENDPOINT = URI.create("https://api.resend.com/emails");
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(20);

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Value("${resend.api.key:}")
    private String resendApiKey;

    @Value("${resend.from.email:EcoTrack <onboarding@resend.dev>}")
    private String remetente;

    public void enviarNotificacaoTransporte(String destinatario, String nomeTransportadora, Long loteId, String descricaoLote) {
        validarConfiguracao();

        String assunto = "EcoTrack — Novo lote atribuído para coleta";
        String texto = montarMensagem(nomeTransportadora, loteId, descricaoLote);
        String corpo = montarCorpoRequisicao(destinatario, assunto, texto);

        HttpRequest request = HttpRequest.newBuilder(RESEND_EMAILS_ENDPOINT)
                .timeout(REQUEST_TIMEOUT)
                .header("Authorization", "Bearer " + resendApiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(corpo))
                .build();

        log.info("Tentando enviar e-mail via Resend para {} usando remetente {}", destinatario, remetente);

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            validarRespostaResend(response);
            log.info("E-mail de transporte enviado via Resend para {}. Resposta: {}", destinatario, response.body());
        } catch (IOException e) {
            throw new RuntimeException("Falha de conexão com a API do Resend", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Envio de e-mail interrompido", e);
        }
    }

    private void validarConfiguracao() {
        if (resendApiKey == null || resendApiKey.isBlank()) {
            throw new RuntimeException("RESEND_API_KEY não configurada");
        }
    }

    private String montarMensagem(String nomeTransportadora, Long loteId, String descricaoLote) {
        return "Olá, " + nomeTransportadora + "!\n\n" +
                "Um novo lote foi atribuído à sua transportadora no sistema EcoTrack.\n\n" +
                "Detalhes do lote:\n" +
                "ID: #" + loteId + "\n" +
                "Descrição: " + descricaoLote + "\n\n" +
                "Acesse o sistema para mais informações:\n" +
                "https://ecotrack-khaki.vercel.app\n\n" +
                "Atenciosamente,\n" +
                "Equipe EcoTrack";
    }

    private String montarCorpoRequisicao(String destinatario, String assunto, String texto) {
        try {
            return objectMapper.writeValueAsString(Map.of(
                    "from", remetente,
                    "to", destinatario,
                    "subject", assunto,
                    "text", texto
            ));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Erro ao montar requisição de e-mail", e);
        }
    }

    private void validarRespostaResend(HttpResponse<String> response) {
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new RuntimeException(
                    "Resend retornou status " + response.statusCode() + ": " + response.body()
            );
        }
    }
}
