package com.ecotrack.ecotrack_api.service;

import com.ecotrack.ecotrack_api.entity.Empresa;
import com.ecotrack.ecotrack_api.entity.Lote;
import com.ecotrack.ecotrack_api.entity.StatusLote;
import com.ecotrack.ecotrack_api.entity.StatusTransporte;
import com.ecotrack.ecotrack_api.entity.Transporte;
import com.ecotrack.ecotrack_api.exception.RegraNegocioException;
import com.ecotrack.ecotrack_api.repository.LoteRepository;
import com.ecotrack.ecotrack_api.repository.TransporteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransporteServiceTest {

    @Mock
    private TransporteRepository transporteRepository;

    @Mock
    private LoteRepository loteRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private TransporteService transporteService;

    @Test
    void criarDefineStatusPendenteEEnviaEmailParaTransportadora() {
        Lote lote = loteDisponivel();
        Empresa transportadora = new Empresa();
        transportadora.setEmail("coleta@ecomove.com");
        transportadora.setRazaoSocial("EcoMove Transportes");

        Transporte transporte = new Transporte();
        transporte.setLote(referenciaLote(1L));
        transporte.setTransportadora(transportadora);

        when(loteRepository.findById(1L)).thenReturn(Optional.of(lote));
        when(transporteRepository.save(any(Transporte.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Transporte criado = transporteService.criar(transporte);

        assertThat(criado.getStatus()).isEqualTo(StatusTransporte.PENDENTE);
        assertThat(criado.getCriadoEm()).isNotNull();
        assertThat(criado.getLote()).isSameAs(lote);
        verify(emailService).enviarNotificacaoTransporte(
                "coleta@ecomove.com",
                "EcoMove Transportes",
                1L,
                "Lote de plastico"
        );
    }

    @Test
    void criarExigeLoteInformado() {
        Transporte transporte = new Transporte();

        assertThatThrownBy(() -> transporteService.criar(transporte))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("Lote e obrigatorio para criar transporte");
    }

    @Test
    void criarImpedeTransporteParaLoteIndisponivel() {
        Lote lote = loteDisponivel();
        lote.setStatus(StatusLote.EM_TRANSITO);

        Transporte transporte = new Transporte();
        transporte.setLote(referenciaLote(1L));
        when(loteRepository.findById(1L)).thenReturn(Optional.of(lote));

        assertThatThrownBy(() -> transporteService.criar(transporte))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("Lote nao esta disponivel para transporte");
        verifyNoInteractions(emailService);
    }

    @Test
    void alterarStatusParaEmTransitoAtualizaLoteEDataColeta() {
        Transporte transporte = transportePendente();
        when(transporteRepository.findById(1L)).thenReturn(Optional.of(transporte));
        when(transporteRepository.save(transporte)).thenReturn(transporte);

        Transporte atualizado = transporteService.alterarStatus(1L, StatusTransporte.EM_TRANSITO, "Saiu para coleta");

        assertThat(atualizado.getStatus()).isEqualTo(StatusTransporte.EM_TRANSITO);
        assertThat(atualizado.getDataColeta()).isNotNull();
        assertThat(atualizado.getObservacao()).isEqualTo("Saiu para coleta");
        assertThat(atualizado.getLote().getStatus()).isEqualTo(StatusLote.EM_TRANSITO);
        verify(loteRepository).save(atualizado.getLote());
    }

    @Test
    void alterarStatusImpedeMudancaQuandoTransporteEstaFinalizado() {
        Transporte transporte = transportePendente();
        transporte.setStatus(StatusTransporte.CONCLUIDO);
        when(transporteRepository.findById(1L)).thenReturn(Optional.of(transporte));

        assertThatThrownBy(() -> transporteService.alterarStatus(1L, StatusTransporte.EM_TRANSITO, null))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("Transporte ja esta em status final");
    }

    @Test
    void buscarPorLoteRetornaTransportesDoRepositorio() {
        Transporte transporte = new Transporte();
        when(transporteRepository.findByLoteId(1L)).thenReturn(List.of(transporte));

        List<Transporte> transportes = transporteService.buscarPorLote(1L);

        assertThat(transportes).containsExactly(transporte);
    }

    private Lote loteDisponivel() {
        Lote lote = new Lote();
        lote.setId(1L);
        lote.setDescricao("Lote de plastico");
        lote.setStatus(StatusLote.AGUARDANDO_COLETA);
        return lote;
    }

    private Lote referenciaLote(Long id) {
        Lote lote = new Lote();
        lote.setId(id);
        return lote;
    }

    private Transporte transportePendente() {
        Transporte transporte = new Transporte();
        transporte.setStatus(StatusTransporte.PENDENTE);
        transporte.setLote(loteDisponivel());
        return transporte;
    }
}
