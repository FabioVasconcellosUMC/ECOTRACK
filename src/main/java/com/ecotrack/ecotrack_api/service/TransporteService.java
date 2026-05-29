package com.ecotrack.ecotrack_api.service;

import com.ecotrack.ecotrack_api.entity.Empresa;
import com.ecotrack.ecotrack_api.entity.HistoricoLote;
import com.ecotrack.ecotrack_api.entity.Lote;
import com.ecotrack.ecotrack_api.entity.StatusLote;
import com.ecotrack.ecotrack_api.entity.StatusTransporte;
import com.ecotrack.ecotrack_api.entity.Transporte;
import com.ecotrack.ecotrack_api.exception.RecursoNaoEncontradoException;
import com.ecotrack.ecotrack_api.exception.RegraNegocioException;
import com.ecotrack.ecotrack_api.repository.EmpresaRepository;
import com.ecotrack.ecotrack_api.repository.HistoricoLoteRepository;
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
    private final EmpresaRepository empresaRepository;
    private final HistoricoLoteRepository historicoLoteRepository;
    private final EmailService emailService;

    public List<Transporte> listar() {
        return transporteRepository.findAll();
    }

    public Transporte buscarPorId(Long id) {
        return transporteRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Transporte não encontrado"));
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
        StatusTransporte statusAnterior = transporte.getStatus();

        validarAlteracaoStatus(statusAnterior, novoStatus);

        transporte.setStatus(novoStatus);
        transporte.setObservacao(observacao);

        aplicarImpactoNoLote(transporte, statusAnterior, novoStatus, observacao);

        return transporteRepository.save(transporte);
    }

    public List<Transporte> buscarPorLote(Long loteId) {
        return transporteRepository.findByLoteId(loteId);
    }

    private void validarAlteracaoStatus(StatusTransporte statusAnterior, StatusTransporte novoStatus) {
        if (statusAnterior == StatusTransporte.CONCLUIDO || statusAnterior == StatusTransporte.CANCELADO) {
            throw new RegraNegocioException("Transporte já está em status final");
        }

        if (statusAnterior == novoStatus) {
            throw new RegraNegocioException("Transporte já está no status informado");
        }
    }

    private void aplicarImpactoNoLote(Transporte transporte, StatusTransporte statusAnterior,
                                      StatusTransporte novoStatus, String observacao) {
        if (novoStatus == StatusTransporte.EM_TRANSITO) {
            transporte.setDataColeta(LocalDateTime.now());
            atualizarStatusLote(transporte, StatusLote.EM_TRANSITO, observacao);
        }

        if (novoStatus == StatusTransporte.CONCLUIDO) {
            transporte.setDataEntrega(LocalDateTime.now());
            atualizarStatusLote(transporte, StatusLote.DESCARTADO, observacao);
        }

        if (novoStatus == StatusTransporte.CANCELADO && statusAnterior == StatusTransporte.EM_TRANSITO) {
            atualizarStatusLote(transporte, StatusLote.AGUARDANDO_COLETA, observacao);
        }
    }

    private void atualizarStatusLote(Transporte transporte, StatusLote novoStatus, String observacao) {
        Lote lote = transporte.getLote();
        StatusLote statusAnterior = lote.getStatus();

        if (statusAnterior == novoStatus) {
            return;
        }

        lote.setStatus(novoStatus);
        loteRepository.save(lote);
        registrarHistoricoLote(transporte, lote, statusAnterior, novoStatus, observacao);
    }

    private void registrarHistoricoLote(Transporte transporte, Lote lote, StatusLote statusAnterior,
                                        StatusLote statusNovo, String observacao) {
        HistoricoLote historico = new HistoricoLote();
        historico.setLote(lote);
        historico.setStatusAnterior(statusAnterior);
        historico.setStatusNovo(statusNovo);
        historico.setUsuario(lote.getCriadoPor());
        historico.setObservacao(montarObservacaoHistorico(transporte, observacao));
        historico.setDataHora(LocalDateTime.now());
        historicoLoteRepository.save(historico);
    }

    private String montarObservacaoHistorico(Transporte transporte, String observacao) {
        if (observacao != null && !observacao.isBlank()) {
            return observacao;
        }

        return "Status do lote atualizado automaticamente pelo transporte #" + transporte.getId();
    }

    private Lote buscarLote(Transporte transporte) {
        if (transporte.getLote() == null || transporte.getLote().getId() == null) {
            throw new RegraNegocioException("Lote é obrigatório para criar transporte");
        }

        return loteRepository.findById(transporte.getLote().getId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Lote não encontrado"));
    }

    private Empresa buscarEmpresa(Empresa empresa, String mensagemErro) {
        if (empresa == null || empresa.getId() == null) {
            throw new RegraNegocioException(mensagemErro);
        }

        return empresaRepository.findById(empresa.getId())
                .orElseThrow(() -> new RecursoNaoEncontradoException(mensagemErro));
    }

    private void validarLoteDisponivel(Lote lote) {
        if (lote.getStatus() != StatusLote.AGUARDANDO_COLETA) {
            throw new RegraNegocioException("Lote não está disponível para transporte");
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
