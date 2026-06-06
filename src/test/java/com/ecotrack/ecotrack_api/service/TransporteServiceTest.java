package com.ecotrack.ecotrack_api.service;

import com.ecotrack.ecotrack_api.entity.Empresa;
import com.ecotrack.ecotrack_api.entity.HistoricoLote;
import com.ecotrack.ecotrack_api.entity.Lote;
import com.ecotrack.ecotrack_api.entity.StatusLote;
import com.ecotrack.ecotrack_api.entity.StatusTransporte;
import com.ecotrack.ecotrack_api.entity.Transporte;
import com.ecotrack.ecotrack_api.exception.RegraNegocioException;
import com.ecotrack.ecotrack_api.repository.EmpresaRepository;
import com.ecotrack.ecotrack_api.repository.HistoricoLoteRepository;
import com.ecotrack.ecotrack_api.repository.LoteRepository;
import com.ecotrack.ecotrack_api.repository.TransporteRepository;
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
class TransporteServiceTest {

    @Mock
    private TransporteRepository transporteRepository;

    @Mock
    private LoteRepository loteRepository;

    @Mock
    private EmpresaRepository empresaRepository;

    @Mock
    private HistoricoLoteRepository historicoLoteRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private DadosPessoaisCriptografiaService criptografiaService;

    @InjectMocks
    private TransporteService transporteService;

    @Test
    void criarDefineStatusPendenteEEnviaEmailParaTransportadora() {
        Lote lote = lote(1L, StatusLote.AGUARDANDO_COLETA);
        lote.setDescricao("Resíduo químico");
        Empresa transportadora = empresa(2L, "Transportadora Eco", "transporte@email.com");
        Empresa receptora = empresa(3L, "Receptora Eco", "receptora@email.com");
        Transporte transporte = transporteComReferencias(lote.getId(), transportadora.getId(), receptora.getId());

        when(loteRepository.findById(1L)).thenReturn(Optional.of(lote));
        when(empresaRepository.findById(2L)).thenReturn(Optional.of(transportadora));
        when(empresaRepository.findById(3L)).thenReturn(Optional.of(receptora));
        when(transporteRepository.save(any(Transporte.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(criptografiaService.descriptografar("transporte@email.com")).thenReturn("transporte@email.com");

        Transporte resultado = transporteService.criar(transporte);

        assertThat(resultado.getStatus()).isEqualTo(StatusTransporte.PENDENTE);
        assertThat(resultado.getLote()).isSameAs(lote);
        assertThat(resultado.getTransportadora()).isSameAs(transportadora);
        assertThat(resultado.getReceptora()).isSameAs(receptora);
        assertThat(resultado.getCriadoEm()).isNotNull();

        verify(emailService).enviarNotificacaoTransporte(
                "transporte@email.com",
                "Transportadora Eco",
                1L,
                "Resíduo químico"
        );
    }

    @Test
    void criarRejeitaLoteIndisponivel() {
        Lote lote = lote(1L, StatusLote.EM_TRANSITO);
        Transporte transporte = transporteComReferencias(1L, 2L, 3L);
        when(loteRepository.findById(1L)).thenReturn(Optional.of(lote));

        assertThatThrownBy(() -> transporteService.criar(transporte))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("Lote não está disponível para transporte");

        verify(transporteRepository, never()).save(any(Transporte.class));
    }

    @Test
    void alterarStatusParaEmTransitoAtualizaLoteEHistorico() {
        Lote lote = lote(1L, StatusLote.AGUARDANDO_COLETA);
        Transporte transporte = new Transporte();
        transporte.setId(10L);
        transporte.setLote(lote);
        transporte.setStatus(StatusTransporte.PENDENTE);
        when(transporteRepository.findById(10L)).thenReturn(Optional.of(transporte));
        when(transporteRepository.save(transporte)).thenReturn(transporte);

        Transporte resultado = transporteService.alterarStatus(10L, StatusTransporte.EM_TRANSITO, "coleta iniciada");

        assertThat(resultado.getStatus()).isEqualTo(StatusTransporte.EM_TRANSITO);
        assertThat(resultado.getDataColeta()).isNotNull();
        assertThat(lote.getStatus()).isEqualTo(StatusLote.EM_TRANSITO);
        verify(loteRepository).save(lote);

        ArgumentCaptor<HistoricoLote> historicoCaptor = ArgumentCaptor.forClass(HistoricoLote.class);
        verify(historicoLoteRepository).save(historicoCaptor.capture());
        assertThat(historicoCaptor.getValue().getStatusAnterior()).isEqualTo(StatusLote.AGUARDANDO_COLETA);
        assertThat(historicoCaptor.getValue().getStatusNovo()).isEqualTo(StatusLote.EM_TRANSITO);
        assertThat(historicoCaptor.getValue().getObservacao()).isEqualTo("coleta iniciada");
    }

    @Test
    void alterarStatusCanceladoVoltaLoteParaAguardandoColetaQuandoEstavaEmTransito() {
        Lote lote = lote(1L, StatusLote.EM_TRANSITO);
        Transporte transporte = new Transporte();
        transporte.setId(10L);
        transporte.setLote(lote);
        transporte.setStatus(StatusTransporte.EM_TRANSITO);
        when(transporteRepository.findById(10L)).thenReturn(Optional.of(transporte));
        when(transporteRepository.save(transporte)).thenReturn(transporte);

        Transporte resultado = transporteService.alterarStatus(10L, StatusTransporte.CANCELADO, "motorista cancelou");

        assertThat(resultado.getStatus()).isEqualTo(StatusTransporte.CANCELADO);
        assertThat(lote.getStatus()).isEqualTo(StatusLote.AGUARDANDO_COLETA);
        verify(loteRepository).save(lote);
        verify(historicoLoteRepository).save(any(HistoricoLote.class));
    }

    @Test
    void alterarStatusRejeitaConclusaoSemConfirmacaoDaReceptora() {
        Transporte transporte = new Transporte();
        transporte.setId(10L);
        transporte.setStatus(StatusTransporte.EM_TRANSITO);
        when(transporteRepository.findById(10L)).thenReturn(Optional.of(transporte));

        assertThatThrownBy(() -> transporteService.alterarStatus(10L, StatusTransporte.CONCLUIDO, null))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("Recebimento final deve ser confirmado pela empresa receptora");

        verify(transporteRepository, never()).save(any(Transporte.class));
    }

    @Test
    void confirmarRecebimentoFinalConcluiTransporteEDescartaLote() {
        Lote lote = lote(1L, StatusLote.EM_TRANSITO);
        Transporte transporte = new Transporte();
        transporte.setId(10L);
        transporte.setLote(lote);
        transporte.setStatus(StatusTransporte.EM_TRANSITO);
        when(transporteRepository.findById(10L)).thenReturn(Optional.of(transporte));
        when(transporteRepository.save(transporte)).thenReturn(transporte);

        Transporte resultado = transporteService.confirmarRecebimentoFinal(10L, "residuo recebido no destino");

        assertThat(resultado.getStatus()).isEqualTo(StatusTransporte.CONCLUIDO);
        assertThat(resultado.getDataEntrega()).isNotNull();
        assertThat(lote.getStatus()).isEqualTo(StatusLote.DESCARTADO);
        verify(loteRepository).save(lote);

        ArgumentCaptor<HistoricoLote> historicoCaptor = ArgumentCaptor.forClass(HistoricoLote.class);
        verify(historicoLoteRepository).save(historicoCaptor.capture());
        assertThat(historicoCaptor.getValue().getStatusAnterior()).isEqualTo(StatusLote.EM_TRANSITO);
        assertThat(historicoCaptor.getValue().getStatusNovo()).isEqualTo(StatusLote.DESCARTADO);
        assertThat(historicoCaptor.getValue().getObservacao())
                .isEqualTo("Recebimento final confirmado pela receptora. residuo recebido no destino");
    }

    @Test
    void confirmarRecebimentoFinalExigeTransporteEmTransito() {
        Transporte transporte = new Transporte();
        transporte.setId(10L);
        transporte.setStatus(StatusTransporte.PENDENTE);
        when(transporteRepository.findById(10L)).thenReturn(Optional.of(transporte));

        assertThatThrownBy(() -> transporteService.confirmarRecebimentoFinal(10L, null))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("Recebimento final so pode ser confirmado com transporte em transito");

        verify(transporteRepository, never()).save(any(Transporte.class));
    }

    @Test
    void alterarStatusRejeitaTransporteFinalizado() {
        Transporte transporte = new Transporte();
        transporte.setId(10L);
        transporte.setStatus(StatusTransporte.CONCLUIDO);
        when(transporteRepository.findById(10L)).thenReturn(Optional.of(transporte));

        assertThatThrownBy(() -> transporteService.alterarStatus(10L, StatusTransporte.CANCELADO, null))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("Transporte já está em status final");

        verify(transporteRepository, never()).save(any(Transporte.class));
    }

    private Transporte transporteComReferencias(Long loteId, Long transportadoraId, Long receptoraId) {
        Transporte transporte = new Transporte();
        transporte.setLote(lote(loteId, null));
        transporte.setTransportadora(empresa(transportadoraId, null, null));
        transporte.setReceptora(empresa(receptoraId, null, null));
        return transporte;
    }

    private Lote lote(Long id, StatusLote status) {
        Lote lote = new Lote();
        lote.setId(id);
        lote.setStatus(status);
        return lote;
    }

    private Empresa empresa(Long id, String razaoSocial, String email) {
        Empresa empresa = new Empresa();
        empresa.setId(id);
        empresa.setRazaoSocial(razaoSocial);
        empresa.setEmail(email);
        return empresa;
    }
}
