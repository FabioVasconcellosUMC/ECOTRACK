package com.ecotrack.ecotrack_api.config;

import com.ecotrack.ecotrack_api.entity.Empresa;
import com.ecotrack.ecotrack_api.entity.Usuario;
import com.ecotrack.ecotrack_api.repository.EmpresaRepository;
import com.ecotrack.ecotrack_api.repository.UsuarioRepository;
import com.ecotrack.ecotrack_api.service.DadosPessoaisCriptografiaService;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DadosSensiveisBackfillRunnerTest {

    private final UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
    private final EmpresaRepository empresaRepository = mock(EmpresaRepository.class);
    private final DadosPessoaisCriptografiaService criptografiaService = mock(DadosPessoaisCriptografiaService.class);
    private final DadosSensiveisBackfillRunner runner =
            new DadosSensiveisBackfillRunner(usuarioRepository, empresaRepository, criptografiaService);

    @Test
    void runCriptografaDadosAntigosEmTextoClaro() {
        Usuario usuario = new Usuario();
        usuario.setNome("Administrador");
        usuario.setEmail("admin@ecotrack.com");

        Empresa empresa = new Empresa();
        empresa.setCnpj("12.345.678/0001-90");
        empresa.setEmail("empresa@email.com");
        empresa.setTelefone("(11) 99999-9999");
        empresa.setEndereco("Rua Teste, 123");

        when(usuarioRepository.findAll()).thenReturn(List.of(usuario));
        when(empresaRepository.findAll()).thenReturn(List.of(empresa));
        when(criptografiaService.descriptografar("admin@ecotrack.com")).thenReturn("admin@ecotrack.com");
        when(criptografiaService.normalizarEmail("admin@ecotrack.com")).thenReturn("admin@ecotrack.com");
        when(criptografiaService.hashBusca("admin@ecotrack.com")).thenReturn("hash-email-usuario");
        when(criptografiaService.criptografar("Administrador")).thenReturn("enc:nome");
        when(criptografiaService.criptografar("admin@ecotrack.com")).thenReturn("enc:email-usuario");

        when(criptografiaService.descriptografar("12.345.678/0001-90")).thenReturn("12.345.678/0001-90");
        when(criptografiaService.normalizarCnpj("12.345.678/0001-90")).thenReturn("12345678000190");
        when(criptografiaService.hashBusca("12345678000190")).thenReturn("hash-cnpj");
        when(criptografiaService.descriptografar("empresa@email.com")).thenReturn("empresa@email.com");
        when(criptografiaService.normalizarEmail("empresa@email.com")).thenReturn("empresa@email.com");
        when(criptografiaService.hashBusca("empresa@email.com")).thenReturn("hash-email-empresa");
        when(criptografiaService.descriptografar("(11) 99999-9999")).thenReturn("(11) 99999-9999");
        when(criptografiaService.descriptografar("Rua Teste, 123")).thenReturn("Rua Teste, 123");
        when(criptografiaService.criptografar("12345678000190")).thenReturn("enc:cnpj");
        when(criptografiaService.criptografar("empresa@email.com")).thenReturn("enc:email-empresa");
        when(criptografiaService.criptografar("(11) 99999-9999")).thenReturn("enc:telefone");
        when(criptografiaService.criptografar("Rua Teste, 123")).thenReturn("enc:endereco");

        runner.run(null);

        assertThat(usuario.getNome()).isEqualTo("enc:nome");
        assertThat(usuario.getEmail()).isEqualTo("enc:email-usuario");
        assertThat(usuario.getEmailHash()).isEqualTo("hash-email-usuario");
        assertThat(empresa.getCnpj()).isEqualTo("enc:cnpj");
        assertThat(empresa.getEmail()).isEqualTo("enc:email-empresa");
        assertThat(empresa.getTelefone()).isEqualTo("enc:telefone");
        assertThat(empresa.getEndereco()).isEqualTo("enc:endereco");
        assertThat(empresa.getCnpjHash()).isEqualTo("hash-cnpj");
        assertThat(empresa.getEmailHash()).isEqualTo("hash-email-empresa");
        verify(usuarioRepository).saveAllAndFlush(anyList());
        verify(empresaRepository).saveAllAndFlush(anyList());
    }

    @Test
    void runNaoRestauraHashQuandoOutroUsuarioJaUsaOMesmoHash() {
        Usuario usuarioInativoAntigo = new Usuario();
        usuarioInativoAntigo.setId(1L);
        usuarioInativoAntigo.setNome("Usuario Excluido");
        usuarioInativoAntigo.setEmail("enc:email-antigo");
        usuarioInativoAntigo.setEmailHash(null);

        Usuario usuarioAtivoAtual = new Usuario();
        usuarioAtivoAtual.setId(2L);
        usuarioAtivoAtual.setEmailHash("hash-repetido");

        when(usuarioRepository.findAll()).thenReturn(List.of(usuarioInativoAntigo));
        when(empresaRepository.findAll()).thenReturn(List.of());
        when(criptografiaService.descriptografar("enc:email-antigo")).thenReturn("usuario@email.com");
        when(criptografiaService.normalizarEmail("usuario@email.com")).thenReturn("usuario@email.com");
        when(criptografiaService.hashBusca("usuario@email.com")).thenReturn("hash-repetido");
        when(usuarioRepository.findByEmailHash("hash-repetido")).thenReturn(Optional.of(usuarioAtivoAtual));
        when(criptografiaService.criptografar("Usuario Excluido")).thenReturn("enc:nome");
        when(criptografiaService.criptografar("usuario@email.com")).thenReturn("enc:email");

        runner.run(null);

        assertThat(usuarioInativoAntigo.getEmailHash()).isNull();
        verify(usuarioRepository).saveAllAndFlush(anyList());
    }
}
