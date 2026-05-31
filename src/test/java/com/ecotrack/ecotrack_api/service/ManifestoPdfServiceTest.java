package com.ecotrack.ecotrack_api.service;

import com.ecotrack.ecotrack_api.entity.Empresa;
import com.ecotrack.ecotrack_api.entity.Lote;
import com.ecotrack.ecotrack_api.entity.StatusLote;
import com.ecotrack.ecotrack_api.entity.StatusTransporte;
import com.ecotrack.ecotrack_api.entity.TipoEmpresa;
import com.ecotrack.ecotrack_api.entity.Transporte;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ManifestoPdfServiceTest {

    @Mock
    private TransporteService transporteService;

    @InjectMocks
    private ManifestoPdfService manifestoPdfService;

    @Test
    void gerarManifestoRetornaPdfDoTransporte() {
        Transporte transporte = transporteCompleto();
        when(transporteService.buscarPorId(10L)).thenReturn(transporte);

        byte[] pdf = manifestoPdfService.gerarManifesto(10L);

        assertThat(pdf).isNotEmpty();
        assertThat(new String(pdf, 0, 4, StandardCharsets.US_ASCII)).isEqualTo("%PDF");
        verify(transporteService).buscarPorId(10L);
    }

    @Test
    void nomeArquivoUsaIdentificadorDoTransporte() {
        assertThat(manifestoPdfService.nomeArquivo(10L)).isEqualTo("manifesto-transporte-10.pdf");
    }

    private Transporte transporteCompleto() {
        Empresa geradora = empresa(1L, "Geradora Eco", TipoEmpresa.GERADORA);
        Empresa transportadora = empresa(2L, "Transportadora Eco", TipoEmpresa.TRANSPORTADORA);
        Empresa receptora = empresa(3L, "Receptora Eco", TipoEmpresa.RECEPTORA);

        Lote lote = new Lote();
        lote.setId(20L);
        lote.setDescricao("Residuo quimico controlado");
        lote.setTipoResiduo("Quimico");
        lote.setQuantidade(new BigDecimal("480"));
        lote.setUnidade("KG");
        lote.setStatus(StatusLote.EM_TRANSITO);
        lote.setEmpresaGeradora(geradora);

        Transporte transporte = new Transporte();
        transporte.setId(10L);
        transporte.setLote(lote);
        transporte.setTransportadora(transportadora);
        transporte.setReceptora(receptora);
        transporte.setResponsavel("Fabio Vasconcellos");
        transporte.setStatus(StatusTransporte.EM_TRANSITO);
        transporte.setCriadoEm(LocalDateTime.of(2026, 5, 31, 10, 0));
        transporte.setDataColeta(LocalDateTime.of(2026, 5, 31, 11, 30));
        transporte.setObservacao("Coleta iniciada");
        return transporte;
    }

    private Empresa empresa(Long id, String razaoSocial, TipoEmpresa tipo) {
        Empresa empresa = new Empresa();
        empresa.setId(id);
        empresa.setRazaoSocial(razaoSocial);
        empresa.setTipo(tipo);
        empresa.setCnpj("12345678000199");
        empresa.setEmail("contato@ecotrack.com");
        empresa.setTelefone("(11) 99999-0000");
        empresa.setEndereco("Rua Eco, 100");
        return empresa;
    }
}
