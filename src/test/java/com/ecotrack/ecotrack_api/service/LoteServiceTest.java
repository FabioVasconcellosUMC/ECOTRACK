package com.ecotrack.ecotrack_api.service;

import com.ecotrack.ecotrack_api.entity.HistoricoLote;
import com.ecotrack.ecotrack_api.entity.Lote;
import com.ecotrack.ecotrack_api.entity.StatusLote;
import com.ecotrack.ecotrack_api.entity.Usuario;
import com.ecotrack.ecotrack_api.entity.Empresa;
import com.ecotrack.ecotrack_api.exception.RegraNegocioException;
import com.ecotrack.ecotrack_api.repository.EmpresaRepository;
import com.ecotrack.ecotrack_api.repository.HistoricoLoteRepository;
import com.ecotrack.ecotrack_api.repository.LoteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoteServiceTest {

    @Mock
    private LoteRepository loteRepository;

    @Mock
    private HistoricoLoteRepository historicoLoteRepository;

    @Mock
    private EmpresaRepository empresaRepository;

    @InjectMocks
    private LoteService loteService;

    @Test
    void criarDefineStatusInicialERegistraHistorico() {
        Lote lote = new Lote();
        Empresa empresaRef = new Empresa();
        empresaRef.setId(1L);
        lote.setEmpresaGeradora(empresaRef);
        Empresa empresaPersistida = new Empresa();
        empresaPersistida.setId(1L);
        Usuario usuario = new Usuario();
        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresaPersistida));
        when(loteRepository.save(lote)).thenAnswer(invocation -> invocation.getArgument(0));

        Lote resultado = loteService.criar(lote, usuario);

        assertThat(resultado.getEmpresaGeradora()).isSameAs(empresaPersistida);
        assertThat(resultado.getStatus()).isEqualTo(StatusLote.AGUARDANDO_COLETA);
        assertThat(resultado.getCriadoPor()).isSameAs(usuario);
        assertThat(resultado.getCriadoEm()).isNotNull();

        ArgumentCaptor<HistoricoLote> historicoCaptor = ArgumentCaptor.forClass(HistoricoLote.class);
        verify(historicoLoteRepository).save(historicoCaptor.capture());

        HistoricoLote historico = historicoCaptor.getValue();
        assertThat(historico.getLote()).isSameAs(lote);
        assertThat(historico.getStatusAnterior()).isNull();
        assertThat(historico.getStatusNovo()).isEqualTo(StatusLote.AGUARDANDO_COLETA);
        assertThat(historico.getUsuario()).isSameAs(usuario);
        assertThat(historico.getObservacao()).isEqualTo("Lote criado");
    }

    @Test
    void alterarStatusAtualizaLoteEHistorico() {
        Lote lote = new Lote();
        lote.setId(1L);
        lote.setStatus(StatusLote.AGUARDANDO_COLETA);
        Usuario usuario = new Usuario();
        when(loteRepository.findByIdWithEmpresa(1L)).thenReturn(Optional.of(lote));

        Lote resultado = loteService.alterarStatus(1L, StatusLote.EM_TRANSITO, "coleta iniciada", usuario);

        assertThat(resultado.getStatus()).isEqualTo(StatusLote.EM_TRANSITO);
        verify(loteRepository).save(lote);

        ArgumentCaptor<HistoricoLote> historicoCaptor = ArgumentCaptor.forClass(HistoricoLote.class);
        verify(historicoLoteRepository).save(historicoCaptor.capture());

        HistoricoLote historico = historicoCaptor.getValue();
        assertThat(historico.getStatusAnterior()).isEqualTo(StatusLote.AGUARDANDO_COLETA);
        assertThat(historico.getStatusNovo()).isEqualTo(StatusLote.EM_TRANSITO);
        assertThat(historico.getObservacao()).isEqualTo("coleta iniciada");
    }

    @Test
    void alterarStatusRejeitaStatusRepetido() {
        Lote lote = new Lote();
        lote.setId(1L);
        lote.setStatus(StatusLote.AGUARDANDO_COLETA);
        when(loteRepository.findByIdWithEmpresa(1L)).thenReturn(Optional.of(lote));

        assertThatThrownBy(() -> loteService.alterarStatus(1L, StatusLote.AGUARDANDO_COLETA, null, new Usuario()))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("Lote já está com o status informado");

        verify(loteRepository, never()).save(any(Lote.class));
    }

    @Test
    void alterarStatusRejeitaStatusFinal() {
        Lote lote = new Lote();
        lote.setId(1L);
        lote.setStatus(StatusLote.DESCARTADO);
        when(loteRepository.findByIdWithEmpresa(1L)).thenReturn(Optional.of(lote));

        assertThatThrownBy(() -> loteService.alterarStatus(1L, StatusLote.CANCELADO, null, new Usuario()))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("Lote já está em status final e não pode ser alterado");
    }
}
