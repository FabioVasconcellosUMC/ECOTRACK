package com.ecotrack.ecotrack_api.service;

import com.ecotrack.ecotrack_api.entity.Empresa;
import com.ecotrack.ecotrack_api.entity.Perfil;
import com.ecotrack.ecotrack_api.entity.TipoEmpresa;
import com.ecotrack.ecotrack_api.entity.Usuario;
import com.ecotrack.ecotrack_api.exception.RecursoNaoEncontradoException;
import com.ecotrack.ecotrack_api.exception.RegraNegocioException;
import com.ecotrack.ecotrack_api.repository.EmpresaRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmpresaServiceTest {

    @Mock
    private EmpresaRepository empresaRepository;

    @Mock
    private DadosPessoaisCriptografiaService criptografiaService;

    @InjectMocks
    private EmpresaService empresaService;

    @AfterEach
    void limparContextoSeguranca() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void buscarPorIdRetornaEmpresaEncontradaComDadosLegiveis() {
        Empresa empresa = new Empresa();
        empresa.setId(1L);
        empresa.setRazaoSocial("Transportadora Eco");
        empresa.setCnpj("enc:cnpj");
        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));
        when(criptografiaService.descriptografar(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Empresa resultado = empresaService.buscarPorId(1L);

        assertThat(resultado).isSameAs(empresa);
    }

    @Test
    void buscarPorIdLancaExcecaoQuandoNaoEncontrar() {
        when(empresaRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> empresaService.buscarPorId(1L))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage("Empresa não encontrada");
    }

    @Test
    void deletarRemoveEmpresaExistente() {
        when(empresaRepository.existsById(1L)).thenReturn(true);

        empresaService.deletar(1L);

        verify(empresaRepository).deleteById(1L);
    }

    @Test
    void deletarRejeitaEmpresaInexistente() {
        when(empresaRepository.existsById(1L)).thenReturn(false);

        assertThatThrownBy(() -> empresaService.deletar(1L))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage("Empresa não encontrada");

        verify(empresaRepository, never()).deleteById(1L);
    }

    @Test
    void salvarRejeitaDadosSensiveisInvalidosAntesDeCriptografar() {
        Empresa empresa = new Empresa();
        empresa.setRazaoSocial("Empresa Eco");
        empresa.setCnpj("12.345<script>");

        assertThatThrownBy(() -> empresaService.salvar(empresa))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("CNPJ deve conter apenas numeros, pontos, barra e hifen");

        verify(empresaRepository, never()).save(any());
    }

    @Test
    void salvarPermiteTransportadoraCadastrarEmpresaTransportadora() {
        autenticarComo(Perfil.TRANSPORTADORA);
        Empresa empresa = empresaValida(TipoEmpresa.TRANSPORTADORA);
        prepararCriptografia(empresa);
        when(empresaRepository.save(empresa)).thenReturn(empresa);

        Empresa resultado = empresaService.salvar(empresa);

        assertThat(resultado).isSameAs(empresa);
        verify(empresaRepository).save(empresa);
    }

    @Test
    void salvarRejeitaTransportadoraCadastrarEmpresaGeradora() {
        autenticarComo(Perfil.TRANSPORTADORA);
        Empresa empresa = empresaValida(TipoEmpresa.GERADORA);

        assertThatThrownBy(() -> empresaService.salvar(empresa))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("Seu perfil permite cadastrar apenas empresa do tipo TRANSPORTADORA");

        verify(empresaRepository, never()).save(any());
    }

    @Test
    void salvarPermiteReceptoraCadastrarEmpresaReceptora() {
        autenticarComo(Perfil.RECEPTORA);
        Empresa empresa = empresaValida(TipoEmpresa.RECEPTORA);
        prepararCriptografia(empresa);
        when(empresaRepository.save(empresa)).thenReturn(empresa);

        Empresa resultado = empresaService.salvar(empresa);

        assertThat(resultado).isSameAs(empresa);
        verify(empresaRepository).save(empresa);
    }

    private void autenticarComo(Perfil perfil) {
        Usuario usuario = new Usuario();
        usuario.setPerfil(perfil);
        TestingAuthenticationToken authentication = new TestingAuthenticationToken(usuario, null, "ROLE_" + perfil.name());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private Empresa empresaValida(TipoEmpresa tipo) {
        Empresa empresa = new Empresa();
        empresa.setRazaoSocial("Empresa Eco");
        empresa.setTipo(tipo);
        empresa.setCnpj("42.591.651/0001-43");
        empresa.setEmail("empresa@email.com");
        empresa.setTelefone("(11) 98722-3232");
        empresa.setEndereco("Amazonas, 253, Barueri, SP");
        return empresa;
    }

    private void prepararCriptografia(Empresa empresa) {
        when(criptografiaService.normalizarCnpj(empresa.getCnpj())).thenReturn("42591651000143");
        when(criptografiaService.normalizarEmail(empresa.getEmail())).thenReturn("empresa@email.com");
        when(criptografiaService.hashBusca("42591651000143")).thenReturn("hash-cnpj");
        when(criptografiaService.hashBusca("empresa@email.com")).thenReturn("hash-email");
        when(criptografiaService.criptografar(any())).thenAnswer(invocation -> "enc:" + invocation.getArgument(0));
        when(criptografiaService.descriptografar(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }
}
