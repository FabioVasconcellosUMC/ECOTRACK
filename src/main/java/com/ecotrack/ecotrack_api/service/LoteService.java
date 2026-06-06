package com.ecotrack.ecotrack_api.service;

import com.ecotrack.ecotrack_api.entity.Empresa;
import com.ecotrack.ecotrack_api.entity.HistoricoLote;
import com.ecotrack.ecotrack_api.entity.Lote;
import com.ecotrack.ecotrack_api.entity.StatusLote;
import com.ecotrack.ecotrack_api.entity.Usuario;
import com.ecotrack.ecotrack_api.exception.RecursoNaoEncontradoException;
import com.ecotrack.ecotrack_api.exception.RegraNegocioException;
import com.ecotrack.ecotrack_api.repository.EmpresaRepository;
import com.ecotrack.ecotrack_api.repository.HistoricoLoteRepository;
import com.ecotrack.ecotrack_api.repository.LoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class LoteService {

    private static final String OBSERVACAO_CRIACAO = "Lote criado";

    private final LoteRepository loteRepository;
    private final HistoricoLoteRepository historicoLoteRepository;
    private final EmpresaRepository empresaRepository;
    private final DadosPessoaisCriptografiaService criptografiaService;

    public Lote criar(Lote lote, Usuario usuario) {
        lote.setEmpresaGeradora(buscarEmpresaGeradora(lote));
        prepararNovoLote(lote, usuario);
        Lote salvo = loteRepository.save(lote);

        registrarHistorico(salvo, null, StatusLote.AGUARDANDO_COLETA, usuario, OBSERVACAO_CRIACAO);
        return salvo;
    }

    @Transactional(readOnly = true)
    public List<Lote> listarTodos() {
        return loteRepository.findAll().stream()
                .map(this::descriptografarEmpresaGeradora)
                .toList();
    }

    @Transactional(readOnly = true)
    public Lote buscarPorId(Long id) {
        return descriptografarEmpresaGeradora(buscarPorIdInterno(id));
    }

    @Transactional(readOnly = true)
    public Lote buscarPorPublicId(UUID publicId) {
        return descriptografarEmpresaGeradora(buscarPorPublicIdInterno(publicId));
    }

    public Lote alterarStatus(Long id, StatusLote novoStatus, String observacao, Usuario usuario) {
        return alterarStatus(buscarPorIdInterno(id), novoStatus, observacao, usuario);
    }

    public Lote alterarStatus(UUID publicId, StatusLote novoStatus, String observacao, Usuario usuario) {
        return alterarStatus(buscarPorPublicIdInterno(publicId), novoStatus, observacao, usuario);
    }

    @Transactional(readOnly = true)
    public List<HistoricoLote> buscarHistorico(Long loteId) {
        return historicoLoteRepository.findByLoteIdOrderByDataHoraDesc(loteId);
    }

    @Transactional(readOnly = true)
    public List<HistoricoLote> buscarHistoricoPorPublicId(UUID publicId) {
        Lote lote = buscarPorPublicId(publicId);
        return buscarHistorico(lote.getId());
    }

    private Lote alterarStatus(Lote lote, StatusLote novoStatus, String observacao, Usuario usuario) {
        validarTransicao(lote.getStatus(), novoStatus);

        StatusLote statusAnterior = lote.getStatus();
        lote.setStatus(novoStatus);
        loteRepository.save(lote);

        registrarHistorico(lote, statusAnterior, novoStatus, usuario, observacao);
        return lote;
    }

    private Lote buscarPorIdInterno(Long id) {
        return loteRepository.findByIdWithEmpresa(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Lote não encontrado"));
    }

    private Lote buscarPorPublicIdInterno(UUID publicId) {
        return loteRepository.findByPublicIdWithEmpresa(publicId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Lote não encontrado"));
    }

    private void prepararNovoLote(Lote lote, Usuario usuario) {
        lote.setCriadoPor(usuario);
        lote.setStatus(StatusLote.AGUARDANDO_COLETA);
        lote.setCriadoEm(LocalDateTime.now());
    }

    private void validarTransicao(StatusLote atual, StatusLote novo) {
        if (atual == novo) {
            throw new RegraNegocioException("Lote já está com o status informado");
        }

        if (atual == StatusLote.DESCARTADO || atual == StatusLote.CANCELADO) {
            throw new RegraNegocioException("Lote já está em status final e não pode ser alterado");
        }
    }

    private void registrarHistorico(Lote lote, StatusLote anterior, StatusLote novo,
                                    Usuario usuario, String observacao) {
        HistoricoLote historico = new HistoricoLote();
        historico.setLote(lote);
        historico.setStatusAnterior(anterior);
        historico.setStatusNovo(novo);
        historico.setUsuario(usuario);
        historico.setObservacao(observacao);
        historico.setDataHora(LocalDateTime.now());
        historicoLoteRepository.save(historico);
    }

    private Empresa buscarEmpresaGeradora(Lote lote) {
        if (lote.getEmpresaGeradora() == null) {
            throw new RegraNegocioException("Empresa geradora e obrigatoria para criar lote");
        }

        if (lote.getEmpresaGeradora().getId() != null) {
            return empresaRepository.findById(lote.getEmpresaGeradora().getId())
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Empresa geradora nao encontrada"));
        }

        if (lote.getEmpresaGeradora().getPublicId() == null) {
            throw new RegraNegocioException("Empresa geradora e obrigatoria para criar lote");
        }

        return empresaRepository.findByPublicId(lote.getEmpresaGeradora().getPublicId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Empresa geradora nao encontrada"));
    }

    private Lote descriptografarEmpresaGeradora(Lote lote) {
        if (lote.getEmpresaGeradora() != null) {
            Empresa empresa = lote.getEmpresaGeradora();
            empresa.setCnpj(criptografiaService.descriptografar(empresa.getCnpj()));
            empresa.setEmail(criptografiaService.descriptografar(empresa.getEmail()));
            empresa.setTelefone(criptografiaService.descriptografar(empresa.getTelefone()));
            empresa.setEndereco(criptografiaService.descriptografar(empresa.getEndereco()));
        }
        return lote;
    }
}
