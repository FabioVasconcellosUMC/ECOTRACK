package com.ecotrack.ecotrack_api.service;

import com.ecotrack.ecotrack_api.entity.HistoricoLote;
import com.ecotrack.ecotrack_api.entity.Lote;
import com.ecotrack.ecotrack_api.entity.StatusLote;
import com.ecotrack.ecotrack_api.entity.Usuario;
import com.ecotrack.ecotrack_api.repository.HistoricoLoteRepository;
import com.ecotrack.ecotrack_api.repository.LoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class LoteService {

    private final LoteRepository loteRepository;
    private final HistoricoLoteRepository historicoLoteRepository;

    public Lote criar(Lote lote, Usuario usuario) {
        lote.setCriadoPor(usuario);
        lote.setStatus(StatusLote.AGUARDANDO_COLETA);
        lote.setCriadoEm(LocalDateTime.now());
        Lote salvo = loteRepository.save(lote);

        registrarHistorico(salvo, null, StatusLote.AGUARDANDO_COLETA, usuario, "Lote criado");
        return salvo;
    }

    public List<Lote> listarTodos() {
        return loteRepository.findAll();
    }

    public Lote buscarPorId(Long id) {
        return loteRepository.findByIdWithEmpresa(id)
                .orElseThrow(() -> new RuntimeException("Lote não encontrado"));
    }

    public Lote alterarStatus(Long id, StatusLote novoStatus, String observacao, Usuario usuario) {
        Lote lote = buscarPorId(id);
        validarTransicao(lote.getStatus(), novoStatus);

        StatusLote statusAnterior = lote.getStatus();
        lote.setStatus(novoStatus);
        loteRepository.save(lote);

        registrarHistorico(lote, statusAnterior, novoStatus, usuario, observacao);
        return lote;
    }

    public List<HistoricoLote> buscarHistorico(Long loteId) {
        return historicoLoteRepository.findByLoteIdOrderByDataHoraDesc(loteId);
    }

    private void validarTransicao(StatusLote atual, StatusLote novo) {
        if (atual == StatusLote.DESCARTADO || atual == StatusLote.CANCELADO) {
            throw new RuntimeException("Lote já está em status final e não pode ser alterado");
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
}