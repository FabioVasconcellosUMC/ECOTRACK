package com.ecotrack.ecotrack_api.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void enviarNotificacaoTransporte(String destinatario, String nomeTransportadora, Long loteId, String descricaoLote) {
        SimpleMailMessage msg = new SimpleMailMessage();
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
        mailSender.send(msg);
    }
}