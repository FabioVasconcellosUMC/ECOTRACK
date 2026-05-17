package com.ecotrack.ecotrack_api.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String remetente;

    public void enviarNotificacaoTransporte(String destinatario, String nomeTransportadora, Long loteId, String descricaoLote) {
        SimpleMailMessage msg = new SimpleMailMessage();

        if (remetente != null && !remetente.isBlank()) {
            msg.setFrom(remetente);
        }

        msg.setTo(destinatario);
        msg.setSubject("EcoTrack — Novo lote atribuído para coleta");
        msg.setText(
                "Olá, " + nomeTransportadora + "!\n\n" +
                        "Um novo lote foi atribuído à sua transportadora no sistema EcoTrack.\n\n" +
                        "Detalhes do lote:\n" +
                        "ID: #" + loteId + "\n" +
                        "Descrição: " + descricaoLote + "\n\n" +
                        "Acesse o sistema para mais informações:\n" +
                        "https://ecotrack-khaki.vercel.app\n\n" +
                        "Atenciosamente,\n" +
                        "Equipe EcoTrack"
        );

        log.info("Tentando enviar e-mail de transporte para {} usando remetente {}", destinatario, remetente);
        mailSender.send(msg);
        log.info("E-mail de transporte enviado com sucesso para {}", destinatario);
    }
}
