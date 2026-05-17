package com.ecotrack.ecotrack_api.service;

import com.ecotrack.ecotrack_api.entity.HistoricoLote;
import com.ecotrack.ecotrack_api.entity.Lote;
import com.ecotrack.ecotrack_api.entity.StatusLote;
import com.ecotrack.ecotrack_api.entity.Usuario;
import com.ecotrack.ecotrack_api.exception.RecursoNaoEncontradoException;
import com.ecotrack.ecotrack_api.exception.RegraNegocioException;
import com.ecotrack.ecotrack_api.repository.HistoricoLoteRepository;
import com.ecotrack.ecotrack_api.repository.LoteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoteServiceTest {

    @Mock
    private LoteRepository loteRepository;

    @Mock
    private HistoricoLoteRepository historicoLoteRepository;

    @InjectMocks
    private LoteService loteService;

    @Test
    void criarDefineDadosIniciaisERegistraHistorico() {
        Usuario usuario = new Usuario();
        Lote lote = new Lote();
        when(loteRepository.save(lote)).thenReturn(lote);

        Lote criado = loteService.criar(lote, usuario);

        assertThat(criado.getStatus()).isEqualTo(StatusLote.AGUARDANDO_COLETA);
        assertThat(criado.getCriadoPor()).isSameAs(usuario);
        assertThat(criado.getCriadoEm()).isNotNull();
        verify(historicoLoteRepository).save(any(HistoricoLote.class));
    }

    @Test
    void buscarPorIdLancaExcecaoQuandoLoteNaoExiste() {
        when(loteRepository.findByIdWithEmpresa(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> loteService.buscarPorId(99L))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage("Lote nao encontrado");
    }

    @Test
    void alterarStatusAtualizaLoteEHistorico() {
        Usuario usuario = new Usuario();
        Lote lote = new Lote();
        lote.setStatus(StatusLote.AGUARDANDO_COLETA);
        when(loteRepository.findByIdWithEmpresa(1L)).thenReturn(Optional.of(lote));

        loteService.alterarStatus(1L, StatusLote.EM_TRANSITO, "Coleta iniciada", usuario);

        assertThat(lote.getStatus()).isEqualTo(StatusLote.EM_TRANSITO);
        verify(loteRepository).save(lote);

        ArgumentCaptor<HistoricoLote> historicoCaptor = ArgumentCaptor.forClass(HistoricoLote.class);
        verify(historicoLoteRepository).save(historicoCaptor.capture());
        assertThat(historicoCaptor.getValue().getStatusAnterior()).isEqualTo(StatusLote.AGUARDANDO_COLETA);
        assertThat(historicoCaptor.getValue().getStatusNovo()).isEqualTo(StatusLote.EM_TRANSITO);
        assertThat(historicoCaptor.getValue().getObservacao()).isEqualTo("Coleta iniciada");
    }

    @Test
    void alterarStatusImpedeMudancaQuandoLoteEstaFinalizado() {
        Lote lote = new Lote();
        lote.setStatus(StatusLote.DESCARTADO);
        when(loteRepository.findByIdWithEmpresa(1L)).thenReturn(Optional.of(lote));

        assertThatThrownBy(() -> loteService.alterarStatus(1L, StatusLote.EM_TRANSITO, null, new Usuario()))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("Lote ja esta em status final e nao pode ser alterado");
    }

    @Test
    void buscarHistoricoRetornaRegistrosOrdenadosDoRepositorio() {
        HistoricoLote historico = new HistoricoLote();
        when(historicoLoteRepository.findByLoteIdOrderByDataHoraDesc(1L)).thenReturn(List.of(historico));

        List<HistoricoLote> resultado = loteService.buscarHistorico(1L);

        assertThat(resultado).containsExactly(historico);
    }
}
