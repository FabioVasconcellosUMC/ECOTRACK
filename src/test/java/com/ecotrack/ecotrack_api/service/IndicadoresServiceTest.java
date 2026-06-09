package com.ecotrack.ecotrack_api.service;

import com.ecotrack.ecotrack_api.dto.DashboardResumoResponse;
import com.ecotrack.ecotrack_api.dto.RelatoriosResumoResponse;
import com.ecotrack.ecotrack_api.entity.Empresa;
import com.ecotrack.ecotrack_api.entity.Perfil;
import com.ecotrack.ecotrack_api.entity.StatusLote;
import com.ecotrack.ecotrack_api.entity.StatusTransporte;
import com.ecotrack.ecotrack_api.entity.TipoEmpresa;
import com.ecotrack.ecotrack_api.entity.Usuario;
import com.ecotrack.ecotrack_api.repository.EmpresaRepository;
import com.ecotrack.ecotrack_api.repository.LoteRepository;
import com.ecotrack.ecotrack_api.repository.TransporteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IndicadoresServiceTest {

    @Mock
    private EmpresaRepository empresaRepository;

    @Mock
    private LoteRepository loteRepository;

    @Mock
    private TransporteRepository transporteRepository;

    @Mock
    private EscopoUsuarioService escopoUsuarioService;

    @InjectMocks
    private IndicadoresService indicadoresService;

    @Test
    void resumoDashboardAdminConsolidaIndicadoresGerais() {
        Usuario admin = usuario(Perfil.ADMIN);
        when(escopoUsuarioService.usuarioAutenticado()).thenReturn(admin);
        when(escopoUsuarioService.isAdmin(admin)).thenReturn(true);
        when(empresaRepository.count()).thenReturn(12L);
        when(loteRepository.count()).thenReturn(30L);
        when(transporteRepository.countByStatus(StatusTransporte.EM_TRANSITO)).thenReturn(4L);
        when(loteRepository.somarQuantidadePorUnidade()).thenReturn(List.<Object[]>of(
                new Object[]{"TON", BigDecimal.valueOf(2)},
                new Object[]{"KG", BigDecimal.valueOf(500)},
                new Object[]{"L", BigDecimal.valueOf(999)}
        ));
        when(loteRepository.contarPorMesDesde(any())).thenReturn(List.<Object[]>of(new Object[]{2026, 6, 9L}));

        DashboardResumoResponse resumo = indicadoresService.resumoDashboard();

        assertThat(resumo.totalEmpresas()).isEqualTo(12);
        assertThat(resumo.totalLotes()).isEqualTo(30);
        assertThat(resumo.totalEmTransito()).isEqualTo(4);
        assertThat(resumo.totalToneladas()).isEqualByComparingTo("2.5");
        assertThat(resumo.lotesPorMes()).hasSize(1);
        assertThat(resumo.lotesPorMes().get(0).total()).isEqualTo(9);
    }

    @Test
    void resumoRelatoriosAdminMontaDistribuicoesRankingsETotais() {
        Usuario admin = usuario(Perfil.ADMIN);
        UUID transportadoraId = UUID.randomUUID();
        UUID geradoraId = UUID.randomUUID();
        when(escopoUsuarioService.usuarioAutenticado()).thenReturn(admin);
        when(escopoUsuarioService.isAdmin(admin)).thenReturn(true);
        when(empresaRepository.count()).thenReturn(10L);
        when(loteRepository.count()).thenReturn(20L);
        when(transporteRepository.count()).thenReturn(15L);
        when(transporteRepository.countByStatus(StatusTransporte.CONCLUIDO)).thenReturn(6L);
        when(transporteRepository.countByStatus(StatusTransporte.PENDENTE)).thenReturn(2L);
        when(transporteRepository.countByStatus(StatusTransporte.ACEITO)).thenReturn(3L);
        when(transporteRepository.countByStatus(StatusTransporte.EM_TRANSITO)).thenReturn(4L);
        when(loteRepository.somarQuantidadePorUnidade()).thenReturn(List.<Object[]>of(
                new Object[]{"TON", BigDecimal.ONE},
                new Object[]{"KG", BigDecimal.valueOf(750)}
        ));
        when(empresaRepository.countByTipo(TipoEmpresa.GERADORA)).thenReturn(4L);
        when(empresaRepository.countByTipo(TipoEmpresa.TRANSPORTADORA)).thenReturn(3L);
        when(empresaRepository.countByTipo(TipoEmpresa.RECEPTORA)).thenReturn(3L);
        when(loteRepository.countByStatus(StatusLote.AGUARDANDO_COLETA)).thenReturn(8L);
        when(loteRepository.countByStatus(StatusLote.EM_TRANSITO)).thenReturn(5L);
        when(loteRepository.countByStatus(StatusLote.DESCARTADO)).thenReturn(6L);
        when(loteRepository.countByStatus(StatusLote.CANCELADO)).thenReturn(1L);
        when(transporteRepository.rankingTransportadoras(any(Pageable.class)))
                .thenReturn(List.<Object[]>of(new Object[]{transportadoraId, "Transporte Alfa", 7L}));
        when(loteRepository.rankingGeradoras(any(Pageable.class)))
                .thenReturn(List.<Object[]>of(new Object[]{geradoraId, "Geradora Beta", 9L}));

        RelatoriosResumoResponse resumo = indicadoresService.resumoRelatorios();

        assertThat(resumo.totalEmpresas()).isEqualTo(10);
        assertThat(resumo.totalLotes()).isEqualTo(20);
        assertThat(resumo.totalTransportes()).isEqualTo(15);
        assertThat(resumo.transportesConcluidos()).isEqualTo(6);
        assertThat(resumo.transportesEmAndamento()).isEqualTo(9);
        assertThat(resumo.totalToneladas()).isEqualByComparingTo("1.75");
        assertThat(resumo.empresasPorTipo()).extracting("total").containsExactly(4L, 3L, 3L);
        assertThat(resumo.lotesPorStatus()).extracting("total").containsExactly(8L, 5L, 6L, 1L);
        assertThat(resumo.rankingTransportadoras().get(0).id()).isEqualTo(transportadoraId);
        assertThat(resumo.rankingGeradoras().get(0).id()).isEqualTo(geradoraId);
    }

    @Test
    void resumoDashboardUsuarioSemEmpresaVinculadaRetornaVazio() {
        Usuario geradora = usuario(Perfil.GERADORA);
        when(escopoUsuarioService.usuarioAutenticado()).thenReturn(geradora);
        when(escopoUsuarioService.isAdmin(geradora)).thenReturn(false);
        when(escopoUsuarioService.empresaVinculada(geradora)).thenReturn(null);

        DashboardResumoResponse resumo = indicadoresService.resumoDashboard();

        assertThat(resumo.totalEmpresas()).isZero();
        assertThat(resumo.totalLotes()).isZero();
        assertThat(resumo.totalEmTransito()).isZero();
        assertThat(resumo.totalToneladas()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(resumo.lotesPorMes()).isEmpty();
    }

    @Test
    void resumoRelatoriosUsuarioSemEmpresaVinculadaRetornaVazio() {
        Usuario transportadora = usuario(Perfil.TRANSPORTADORA);
        when(escopoUsuarioService.usuarioAutenticado()).thenReturn(transportadora);
        when(escopoUsuarioService.isAdmin(transportadora)).thenReturn(false);
        when(escopoUsuarioService.empresaVinculada(transportadora)).thenReturn(null);

        RelatoriosResumoResponse resumo = indicadoresService.resumoRelatorios();

        assertThat(resumo.totalEmpresas()).isZero();
        assertThat(resumo.totalLotes()).isZero();
        assertThat(resumo.totalTransportes()).isZero();
        assertThat(resumo.empresasPorTipo()).isEmpty();
        assertThat(resumo.lotesPorStatus()).isEmpty();
    }

    @Test
    void resumoRelatoriosGeradoraMostraSomenteDadosDaEmpresaVinculada() {
        Empresa empresa = empresa(77L, TipoEmpresa.GERADORA);
        Usuario usuario = usuario(Perfil.GERADORA);
        UUID rankingId = UUID.randomUUID();
        when(escopoUsuarioService.usuarioAutenticado()).thenReturn(usuario);
        when(escopoUsuarioService.isAdmin(usuario)).thenReturn(false);
        when(escopoUsuarioService.empresaVinculada(usuario)).thenReturn(empresa);
        when(loteRepository.countByEmpresaGeradoraId(77L)).thenReturn(11L);
        when(transporteRepository.countByLote_EmpresaGeradoraId(77L)).thenReturn(8L);
        when(transporteRepository.countByLote_EmpresaGeradoraIdAndStatus(77L, StatusTransporte.CONCLUIDO)).thenReturn(5L);
        when(transporteRepository.countByLote_EmpresaGeradoraIdAndStatus(77L, StatusTransporte.PENDENTE)).thenReturn(1L);
        when(transporteRepository.countByLote_EmpresaGeradoraIdAndStatus(77L, StatusTransporte.ACEITO)).thenReturn(2L);
        when(transporteRepository.countByLote_EmpresaGeradoraIdAndStatus(77L, StatusTransporte.EM_TRANSITO)).thenReturn(3L);
        when(loteRepository.somarQuantidadePorUnidadeGeradora(77L))
                .thenReturn(List.<Object[]>of(new Object[]{"KG", BigDecimal.valueOf(1200)}));
        when(loteRepository.countByEmpresaGeradoraIdAndStatus(77L, StatusLote.AGUARDANDO_COLETA)).thenReturn(4L);
        when(loteRepository.countByEmpresaGeradoraIdAndStatus(77L, StatusLote.EM_TRANSITO)).thenReturn(3L);
        when(loteRepository.countByEmpresaGeradoraIdAndStatus(77L, StatusLote.DESCARTADO)).thenReturn(2L);
        when(loteRepository.countByEmpresaGeradoraIdAndStatus(77L, StatusLote.CANCELADO)).thenReturn(1L);
        when(transporteRepository.rankingTransportadorasPorGeradora(any(), any(Pageable.class)))
                .thenReturn(List.<Object[]>of(new Object[]{rankingId, "Transportadora Parceira", 6L}));
        when(loteRepository.rankingGeradoraPropria(any(), any(Pageable.class)))
                .thenReturn(List.<Object[]>of(new Object[]{empresa.getPublicId(), "Geradora Eco", 11L}));

        RelatoriosResumoResponse resumo = indicadoresService.resumoRelatorios();

        assertThat(resumo.totalEmpresas()).isEqualTo(1);
        assertThat(resumo.totalLotes()).isEqualTo(11);
        assertThat(resumo.totalTransportes()).isEqualTo(8);
        assertThat(resumo.transportesConcluidos()).isEqualTo(5);
        assertThat(resumo.transportesEmAndamento()).isEqualTo(6);
        assertThat(resumo.totalToneladas()).isEqualByComparingTo("1.2");
        assertThat(resumo.empresasPorTipo().get(0).categoria()).isEqualTo("GERADORA");
        assertThat(resumo.lotesPorStatus()).extracting("total").containsExactly(4L, 3L, 2L, 1L);
        assertThat(resumo.rankingTransportadoras()).hasSize(1);
        assertThat(resumo.rankingGeradoras()).hasSize(1);
    }

    @Test
    void resumoDashboardTransportadoraUsaEscopoDaEmpresaVinculada() {
        Empresa empresa = empresa(88L, TipoEmpresa.TRANSPORTADORA);
        Usuario usuario = usuario(Perfil.TRANSPORTADORA);
        when(escopoUsuarioService.usuarioAutenticado()).thenReturn(usuario);
        when(escopoUsuarioService.isAdmin(usuario)).thenReturn(false);
        when(escopoUsuarioService.empresaVinculada(usuario)).thenReturn(empresa);
        when(transporteRepository.countLotesPorTransportadora(88L)).thenReturn(14L);
        when(transporteRepository.countByTransportadoraIdAndStatus(88L, StatusTransporte.EM_TRANSITO)).thenReturn(2L);
        when(loteRepository.somarQuantidadePorUnidadeTransportadora(88L))
                .thenReturn(List.<Object[]>of(new Object[]{"TON", BigDecimal.valueOf(3)}));
        when(loteRepository.contarPorMesDesdeTransportadora(any(), any()))
                .thenReturn(List.<Object[]>of(new Object[]{2026, 5, 4L}));

        DashboardResumoResponse resumo = indicadoresService.resumoDashboard();

        assertThat(resumo.totalEmpresas()).isEqualTo(1);
        assertThat(resumo.totalLotes()).isEqualTo(14);
        assertThat(resumo.totalEmTransito()).isEqualTo(2);
        assertThat(resumo.totalToneladas()).isEqualByComparingTo("3");
        assertThat(resumo.lotesPorMes().get(0).mes()).isEqualTo(5);
    }

    @Test
    void resumoDashboardReceptoraUsaEscopoDaEmpresaVinculada() {
        Empresa empresa = empresa(99L, TipoEmpresa.RECEPTORA);
        Usuario usuario = usuario(Perfil.RECEPTORA);
        when(escopoUsuarioService.usuarioAutenticado()).thenReturn(usuario);
        when(escopoUsuarioService.isAdmin(usuario)).thenReturn(false);
        when(escopoUsuarioService.empresaVinculada(usuario)).thenReturn(empresa);
        when(transporteRepository.countLotesPorReceptora(99L)).thenReturn(6L);
        when(transporteRepository.countByReceptoraIdAndStatus(99L, StatusTransporte.EM_TRANSITO)).thenReturn(1L);
        when(loteRepository.somarQuantidadePorUnidadeReceptora(99L))
                .thenReturn(List.<Object[]>of(new Object[]{"KG", BigDecimal.valueOf(2500)}));
        when(loteRepository.contarPorMesDesdeReceptora(any(), any()))
                .thenReturn(List.<Object[]>of(new Object[]{2026, 4, 2L}));

        DashboardResumoResponse resumo = indicadoresService.resumoDashboard();

        assertThat(resumo.totalEmpresas()).isEqualTo(1);
        assertThat(resumo.totalLotes()).isEqualTo(6);
        assertThat(resumo.totalEmTransito()).isEqualTo(1);
        assertThat(resumo.totalToneladas()).isEqualByComparingTo("2.5");
        assertThat(resumo.lotesPorMes().get(0).mes()).isEqualTo(4);
    }

    private Usuario usuario(Perfil perfil) {
        Usuario usuario = new Usuario();
        usuario.setPerfil(perfil);
        return usuario;
    }

    private Empresa empresa(Long id, TipoEmpresa tipo) {
        Empresa empresa = new Empresa();
        empresa.setId(id);
        empresa.setPublicId(UUID.randomUUID());
        empresa.setRazaoSocial("Empresa Eco");
        empresa.setTipo(tipo);
        return empresa;
    }
}

