package com.ecotrack.ecotrack_api.service;

import com.ecotrack.ecotrack_api.entity.Empresa;
import com.ecotrack.ecotrack_api.entity.Lote;
import com.ecotrack.ecotrack_api.entity.StatusLote;
import com.ecotrack.ecotrack_api.entity.StatusTransporte;
import com.ecotrack.ecotrack_api.entity.Transporte;
import com.ecotrack.ecotrack_api.exception.RecursoNaoEncontradoException;
import com.ecotrack.ecotrack_api.exception.RegraNegocioException;
import com.ecotrack.ecotrack_api.repository.LoteRepository;
import com.ecotrack.ecotrack_api.repository.TransporteRepository;
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
                .orElseThrow(() -> new RecursoNaoEncontradoException("Transporte nao encontrado"));
    }

    public Transporte criar(Transporte transporte) {
        Lote lote = buscarLoteDoTransporte(transporte);
        validarLoteDisponivel(lote);

        transporte.setStatus(StatusTransporte.PENDENTE);
        transporte.setCriadoEm(LocalDateTime.now());
        transporte.setLote(lote);

        Transporte salvo = transporteRepository.save(transporte);
        notificarTransportadora(salvo, lote);

        return salvo;
    }

    public Transporte alterarStatus(Long id, StatusTransporte novoStatus, String observacao) {
        Transporte transporte = buscarPorId(id);
        validarStatusEditavel(transporte);

        aplicarNovoStatus(transporte, novoStatus, observacao);

        return transporteRepository.save(transporte);
    }

    public List<Transporte> buscarPorLote(Long loteId) {
        return transporteRepository.findByLoteId(loteId);
    }

    private Lote buscarLoteDoTransporte(Transporte transporte) {
        if (transporte.getLote() == null || transporte.getLote().getId() == null) {
            throw new RegraNegocioException("Lote e obrigatorio para criar transporte");
        }

        return loteRepository.findById(transporte.getLote().getId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Lote nao encontrado"));
    }

    private void validarLoteDisponivel(Lote lote) {
        if (lote.getStatus() != StatusLote.AGUARDANDO_COLETA) {
            throw new RegraNegocioException("Lote nao esta disponivel para transporte");
        }
    }

    private void notificarTransportadora(Transporte transporte, Lote lote) {
        Empresa transportadora = transporte.getTransportadora();

        if (transportadora == null || transportadora.getEmail() == null) {
            return;
        }

        try {
            emailService.enviarNotificacaoTransporte(
                    transportadora.getEmail(),
                    transportadora.getRazaoSocial(),
                    lote.getId(),
                    lote.getDescricao()
            );
        } catch (Exception e) {
            log.warn("Erro ao enviar email de notificacao: {}", e.getMessage());
        }
    }

    private void validarStatusEditavel(Transporte transporte) {
        if (statusFinal(transporte.getStatus())) {
            throw new RegraNegocioException("Transporte ja esta em status final");
        }
    }

    private boolean statusFinal(StatusTransporte status) {
        return status == StatusTransporte.CONCLUIDO || status == StatusTransporte.CANCELADO;
    }

    private void aplicarNovoStatus(Transporte transporte, StatusTransporte novoStatus, String observacao) {
        transporte.setStatus(novoStatus);
        transporte.setObservacao(observacao);

        if (novoStatus == StatusTransporte.EM_TRANSITO) {
            iniciarTransporte(transporte);
        }

        if (novoStatus == StatusTransporte.CONCLUIDO) {
            concluirTransporte(transporte);
        }
    }

    private void iniciarTransporte(Transporte transporte) {
        transporte.setDataColeta(LocalDateTime.now());
        atualizarStatusLote(transporte.getLote(), StatusLote.EM_TRANSITO);
    }

    private void concluirTransporte(Transporte transporte) {
        transporte.setDataEntrega(LocalDateTime.now());
        atualizarStatusLote(transporte.getLote(), StatusLote.DESCARTADO);
    }

    private void atualizarStatusLote(Lote lote, StatusLote status) {
        lote.setStatus(status);
        loteRepository.save(lote);
    }
}
