package com.ecotrack.ecotrack_api.service;

import com.ecotrack.ecotrack_api.entity.Empresa;
import com.ecotrack.ecotrack_api.exception.RecursoNaoEncontradoException;
import com.ecotrack.ecotrack_api.repository.EmpresaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmpresaServiceTest {

    @Mock
    private EmpresaRepository empresaRepository;

    @InjectMocks
    private EmpresaService empresaService;

    @Test
    void listarRetornaEmpresasCadastradas() {
        Empresa empresa = new Empresa();
        empresa.setRazaoSocial("EcoMove Transportes");
        when(empresaRepository.findAll()).thenReturn(List.of(empresa));

        List<Empresa> empresas = empresaService.listar();

        assertThat(empresas).containsExactly(empresa);
    }

    @Test
    void buscarPorIdRetornaEmpresaQuandoExiste() {
        Empresa empresa = new Empresa();
        empresa.setId(1L);
        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));

        Empresa encontrada = empresaService.buscarPorId(1L);

        assertThat(encontrada).isSameAs(empresa);
    }

    @Test
    void buscarPorIdLancaExcecaoQuandoEmpresaNaoExiste() {
        when(empresaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> empresaService.buscarPorId(99L))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage("Empresa nao encontrada");
    }

    @Test
    void deletarRemoveEmpresaQuandoExiste() {
        when(empresaRepository.existsById(1L)).thenReturn(true);

        empresaService.deletar(1L);

        verify(empresaRepository).deleteById(1L);
    }
}
