package com.ecotrack.ecotrack_api.service;

import com.ecotrack.ecotrack_api.entity.Empresa;
import com.ecotrack.ecotrack_api.exception.RecursoNaoEncontradoException;
import com.ecotrack.ecotrack_api.exception.RegraNegocioException;
import com.ecotrack.ecotrack_api.repository.EmpresaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
class EmpresaServiceTest {

    @Mock
    private EmpresaRepository empresaRepository;

    @Mock
    private DadosPessoaisCriptografiaService criptografiaService;

    @InjectMocks
    private EmpresaService empresaService;

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
}
