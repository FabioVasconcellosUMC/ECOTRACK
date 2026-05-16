package com.ecotrack.ecotrack_api.service;

import com.ecotrack.ecotrack_api.entity.*;
import com.ecotrack.ecotrack_api.repository.TransporteRepository;
import com.ecotrack.ecotrack_api.repository.LoteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class TransporteService {

    private final TransporteRepository transporteRepository;
    private final LoteRepository loteRepository;
    private final EmailService emailService;

    public List<Transporte> listar() {
        return transporteRepository.findAll();
    }

    public Transporte buscarPorId(Long id) {
        return transporteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transporte não encontrado"));
    }

    public Transporte criar(Transporte transporte) {
        Lote lote = loteRepository.findById(transporte.getLote().getId())
                .orElseThrow(() -> new RuntimeException("Lote não encontrado"));

        if (lote.getStatus() != StatusLote.AGUARDANDO_COLETA) {
            throw new RuntimeException("Lote não está disponível para transporte");
        }

        transporte.setStatus(StatusTransporte.PENDENTE);
        transporte.setCriadoEm(LocalDateTime.now());
        Transporte salvo = transporteRepository.save(transporte);

        // Envia e-mail para a transportadora
        try {
            Empresa transportadora = salvo.getTransportadora();
            if (transportadora != null && transportadora.getEmail() != null) {
                emailService.enviarNotificacaoTransporte(
                        transportadora.getEmail(),
                        transportadora.getRazaoSocial(),
                        lote.getId(),
                        lote.getDescricao()
                );
            }
        } catch (Exception e) {
            log.warn("Erro ao enviar e-mail de notificação: {}", e.getMessage());
        }

        return salvo;
    }

    public Transporte alterarStatus(Long id, StatusTransporte novoStatus, String observacao) {
        Transporte transporte = buscarPorId(id);

        if (transporte.getStatus() == StatusTransporte.CONCLUIDO ||
                transporte.getStatus() == StatusTransporte.CANCELADO) {
            throw new RuntimeException("Transporte já está em status final");
        }

        transporte.setStatus(novoStatus);
        transporte.setObservacao(observacao);

        if (novoStatus == StatusTransporte.EM_TRANSITO) {
            transporte.setDataColeta(LocalDateTime.now());
            Lote lote = transporte.getLote();
            lote.setStatus(StatusLote.EM_TRANSITO);
            loteRepository.save(lote);
        }

        if (novoStatus == StatusTransporte.CONCLUIDO) {
            transporte.setDataEntrega(LocalDateTime.now());
            Lote lote = transporte.getLote();
            lote.setStatus(StatusLote.DESCARTADO);
            loteRepository.save(lote);
        }

        return transporteRepository.save(transporte);
    }

    public List<Transporte> buscarPorLote(Long loteId) {
        return transporteRepository.findByLoteId(loteId);
    }
}