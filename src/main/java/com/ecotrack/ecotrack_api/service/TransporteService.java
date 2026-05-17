package com.ecotrack.ecotrack_api.service;

import com.ecotrack.ecotrack_api.entity.*;
import com.ecotrack.ecotrack_api.repository.EmpresaRepository;
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
    private final EmpresaRepository empresaRepository;
    private final EmailService emailService;

    public List<Transporte> listar() {
        return transporteRepository.findAll();
    }

    public Transporte buscarPorId(Long id) {
        return transporteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transporte não encontrado"));
    }

    public Transporte criar(Transporte transporte) {
        Lote lote = buscarLote(transporte);
        validarLoteDisponivel(lote);

        Empresa transportadora = buscarEmpresa(transporte.getTransportadora(), "Transportadora não encontrada");
        Empresa receptora = buscarEmpresa(transporte.getReceptora(), "Receptora não encontrada");

        transporte.setLote(lote);
        transporte.setTransportadora(transportadora);
        transporte.setReceptora(receptora);
        transporte.setStatus(StatusTransporte.PENDENTE);
        transporte.setCriadoEm(LocalDateTime.now());

        Transporte salvo = transporteRepository.save(transporte);
        log.info("Transporte {} criado para a transportadora {}", salvo.getId(), transportadora.getRazaoSocial());
        enviarEmailParaTransportadora(salvo, lote);

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

    private Lote buscarLote(Transporte transporte) {
        if (transporte.getLote() == null || transporte.getLote().getId() == null) {
            throw new RuntimeException("Lote é obrigatório para criar transporte");
        }

        return loteRepository.findById(transporte.getLote().getId())
                .orElseThrow(() -> new RuntimeException("Lote não encontrado"));
    }

    private Empresa buscarEmpresa(Empresa empresa, String mensagemErro) {
        if (empresa == null || empresa.getId() == null) {
            throw new RuntimeException(mensagemErro);
        }

        return empresaRepository.findById(empresa.getId())
                .orElseThrow(() -> new RuntimeException(mensagemErro));
    }

    private void validarLoteDisponivel(Lote lote) {
        if (lote.getStatus() != StatusLote.AGUARDANDO_COLETA) {
            throw new RuntimeException("Lote não está disponível para transporte");
        }
    }

    private void enviarEmailParaTransportadora(Transporte transporte, Lote lote) {
        Empresa transportadora = transporte.getTransportadora();

        if (transportadora == null || transportadora.getEmail() == null || transportadora.getEmail().isBlank()) {
            log.warn("E-mail da transportadora não cadastrado para o transporte {}", transporte.getId());
            return;
        }

        try {
            log.info("Preparando notificação do transporte {} para {}", transporte.getId(), transportadora.getEmail());
            emailService.enviarNotificacaoTransporte(
                    transportadora.getEmail(),
                    transportadora.getRazaoSocial(),
                    lote.getId(),
                    lote.getDescricao()
            );
        } catch (Exception e) {
            log.warn("Erro ao enviar e-mail de notificação: {}", e.getMessage());
        }
    }
}
