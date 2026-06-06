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
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

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
    private final DadosPessoaisCriptografiaService criptografiaService;

    @Transactional(readOnly = true)
    public List<Transporte> listar() {
        return listar(null, null);
    }

    @Transactional(readOnly = true)
    public List<Transporte> listar(String termoBusca, Integer limite) {
        Integer limiteConsulta = limiteNormalizado(limite);
        if (limiteConsulta == null) {
            return transporteRepository.findAll().stream()
                    .map(this::descriptografarEmpresas)
                    .toList();
        }

        PageRequest pageRequest = PageRequest.of(0, limiteConsulta);
        if (termoBusca != null && !termoBusca.isBlank()) {
            return transporteRepository.buscarPorTexto(termoBusca.trim(), pageRequest).stream()
                    .map(this::descriptografarEmpresas)
                    .toList();
        }

        return transporteRepository.findAllByOrderByCriadoEmDesc(pageRequest).stream()
                .map(this::descriptografarEmpresas)
                .toList();
    }

    @Transactional(readOnly = true)
    public Transporte buscarPorId(Long id) {
        return descriptografarEmpresas(buscarPorIdInterno(id));
    }

    @Transactional(readOnly = true)
    public Transporte buscarPorPublicId(UUID publicId) {
        return descriptografarEmpresas(buscarPorPublicIdInterno(publicId));
    }

    public Transporte criar(Transporte transporte) {
        Lote lote = buscarLote(transporte);
        validarLoteDisponivel(lote);

        Empresa transportadora = buscarEmpresa(transporte.getTransportadora(), "Transportadora nao encontrada");
        Empresa receptora = buscarEmpresa(transporte.getReceptora(), "Receptora nao encontrada");

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
        return alterarStatus(buscarPorIdInterno(id), novoStatus, observacao);
    }

    public Transporte alterarStatus(UUID publicId, StatusTransporte novoStatus, String observacao) {
        return alterarStatus(buscarPorPublicIdInterno(publicId), novoStatus, observacao);
    }

    public Transporte confirmarRecebimentoFinal(Long id, String observacao) {
        return confirmarRecebimentoFinal(buscarPorIdInterno(id), observacao);
    }

    public Transporte confirmarRecebimentoFinal(UUID publicId, String observacao) {
        return confirmarRecebimentoFinal(buscarPorPublicIdInterno(publicId), observacao);
    }

    @Transactional(readOnly = true)
    public List<Transporte> buscarPorLote(Long loteId) {
        return transporteRepository.findByLoteId(loteId);
    }

    @Transactional(readOnly = true)
    public List<Transporte> buscarPorLotePublicId(UUID lotePublicId) {
        return transporteRepository.findByLote_PublicId(lotePublicId);
    }

    private Transporte alterarStatus(Transporte transporte, StatusTransporte novoStatus, String observacao) {
        StatusTransporte statusAnterior = transporte.getStatus();

        validarAlteracaoStatus(statusAnterior, novoStatus);

        transporte.setStatus(novoStatus);
        transporte.setObservacao(observacao);

        aplicarImpactoNoLote(transporte, statusAnterior, novoStatus, observacao);

        return transporteRepository.save(transporte);
    }

    private Transporte buscarPorIdInterno(Long id) {
        return transporteRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Transporte não encontrado"));
    }

    private Transporte buscarPorPublicIdInterno(UUID publicId) {
        return transporteRepository.findByPublicId(publicId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Transporte não encontrado"));
    }

    private Transporte confirmarRecebimentoFinal(Transporte transporte, String observacao) {
        validarRecebimentoFinal(transporte);

        StatusTransporte statusAnterior = transporte.getStatus();
        transporte.setStatus(StatusTransporte.CONCLUIDO);
        transporte.setDataEntrega(LocalDateTime.now());
        transporte.setObservacao(observacao);

        aplicarImpactoNoLote(transporte, statusAnterior, StatusTransporte.CONCLUIDO,
                montarObservacaoRecebimentoFinal(observacao));

        return transporteRepository.save(transporte);
    }

    private void validarAlteracaoStatus(StatusTransporte statusAnterior, StatusTransporte novoStatus) {
        if (novoStatus == StatusTransporte.CONCLUIDO) {
            throw new RegraNegocioException("Recebimento final deve ser confirmado pela empresa receptora");
        }

        if (statusAnterior == StatusTransporte.CONCLUIDO || statusAnterior == StatusTransporte.CANCELADO) {
            throw new RegraNegocioException("Transporte já está em status final");
        }

        if (statusAnterior == novoStatus) {
            throw new RegraNegocioException("Transporte já está no status informado");
        }
    }

    private void validarRecebimentoFinal(Transporte transporte) {
        if (transporte.getStatus() == StatusTransporte.CONCLUIDO || transporte.getStatus() == StatusTransporte.CANCELADO) {
            throw new RegraNegocioException("Transporte ja esta em status final");
        }

        if (transporte.getStatus() != StatusTransporte.EM_TRANSITO) {
            throw new RegraNegocioException("Recebimento final so pode ser confirmado com transporte em transito");
        }
    }

    private String montarObservacaoRecebimentoFinal(String observacao) {
        if (observacao != null && !observacao.isBlank()) {
            return "Recebimento final confirmado pela receptora. " + observacao;
        }

        return "Recebimento final confirmado pela receptora";
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

    private Integer limiteNormalizado(Integer limite) {
        if (limite == null || limite <= 0) {
            return null;
        }

        return Math.min(limite, 100);
    }

    private String montarObservacaoHistorico(Transporte transporte, String observacao) {
        if (observacao != null && !observacao.isBlank()) {
            return observacao;
        }

        return "Status do lote atualizado automaticamente pelo transporte #" + transporte.getId();
    }

    private Lote buscarLote(Transporte transporte) {
        if (transporte.getLote() == null) {
            throw new RegraNegocioException("Lote e obrigatorio para criar transporte");
        }

        if (transporte.getLote().getId() != null) {
            return loteRepository.findById(transporte.getLote().getId())
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Lote não encontrado"));
        }

        if (transporte.getLote().getPublicId() == null) {
            throw new RegraNegocioException("Lote e obrigatorio para criar transporte");
        }

        return loteRepository.findByPublicId(transporte.getLote().getPublicId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Lote não encontrado"));
    }

    private Empresa buscarEmpresa(Empresa empresa, String mensagemErro) {
        if (empresa == null) {
            throw new RegraNegocioException(mensagemErro);
        }

        if (empresa.getId() != null) {
            return empresaRepository.findById(empresa.getId())
                    .orElseThrow(() -> new RecursoNaoEncontradoException(mensagemErro));
        }

        if (empresa.getPublicId() == null) {
            throw new RegraNegocioException(mensagemErro);
        }

        return empresaRepository.findByPublicId(empresa.getPublicId())
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
            log.warn("E-mail da transportadora nao cadastrado para o transporte {}", transporte.getId());
            return;
        }

        try {
            log.info("Preparando notificacao do transporte {} para {}", transporte.getId(), transportadora.getEmail());
            emailService.enviarNotificacaoTransporte(
                    criptografiaService.descriptografar(transportadora.getEmail()),
                    transportadora.getRazaoSocial(),
                    lote.getId(),
                    lote.getDescricao()
            );
        } catch (Exception e) {
            log.warn("Erro ao enviar e-mail de notificacao: {}", e.getMessage());
        }
    }

    private Transporte descriptografarEmpresas(Transporte transporte) {
        descriptografarEmpresa(transporte.getTransportadora());
        descriptografarEmpresa(transporte.getReceptora());
        if (transporte.getLote() != null) {
            descriptografarEmpresa(transporte.getLote().getEmpresaGeradora());
        }
        return transporte;
    }

    private void descriptografarEmpresa(Empresa empresa) {
        if (empresa == null) {
            return;
        }

        empresa.setCnpj(criptografiaService.descriptografar(empresa.getCnpj()));
        empresa.setEmail(criptografiaService.descriptografar(empresa.getEmail()));
        empresa.setTelefone(criptografiaService.descriptografar(empresa.getTelefone()));
        empresa.setEndereco(criptografiaService.descriptografar(empresa.getEndereco()));
    }
}
