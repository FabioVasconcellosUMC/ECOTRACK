package com.ecotrack.ecotrack_api.service;

import com.ecotrack.ecotrack_api.entity.Perfil;
import com.ecotrack.ecotrack_api.entity.Usuario;
import com.ecotrack.ecotrack_api.exception.RegraNegocioException;
import com.ecotrack.ecotrack_api.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private DadosPessoaisCriptografiaService criptografiaService;

    @InjectMocks
    private UsuarioService usuarioService;

    @Test
    void excluirLogicamenteInativaUsuarioECriptografaDadosPessoais() {
        Usuario usuario = criarUsuarioAtivo();
        String sufixo = usuario.getPublicId().toString().substring(0, 8);
        when(usuarioRepository.findById(10L)).thenReturn(Optional.of(usuario));
        when(criptografiaService.criptografar("Usuario excluido " + sufixo)).thenReturn("nome-anonimo");
        when(criptografiaService.criptografar("usuario-removido-" + sufixo + "@anonimo.local")).thenReturn("email-anonimo");
        when(criptografiaService.criptografar("senha-removida-" + sufixo)).thenReturn("senha-anonima");

        usuarioService.excluirLogicamente(usuario);

        assertThat(usuario.isAtivo()).isFalse();
        assertThat(usuario.getNome()).isEqualTo("nome-anonimo");
        assertThat(usuario.getEmail()).isEqualTo("email-anonimo");
        assertThat(usuario.getSenha()).isEqualTo("senha-anonima");
        assertThat(usuario.getEmailHash()).isNull();
        assertThat(usuario.getExcluidoEm()).isNotNull();
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void excluirLogicamenteRejeitaUsuarioJaExcluido() {
        Usuario usuario = criarUsuarioAtivo();
        usuario.setAtivo(false);
        when(usuarioRepository.findById(10L)).thenReturn(Optional.of(usuario));

        assertThatThrownBy(() -> usuarioService.excluirLogicamente(usuario))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("Usuario ja esta excluido");

        verify(usuarioRepository, never()).save(usuario);
    }

    @Test
    void excluirLogicamenteRejeitaContaAdministradora() {
        Usuario usuario = criarUsuarioAtivo();
        usuario.setPerfil(Perfil.ADMIN);
        when(usuarioRepository.findById(10L)).thenReturn(Optional.of(usuario));

        assertThatThrownBy(() -> usuarioService.excluirLogicamente(usuario))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("A conta administradora principal nao pode ser excluida por esta tela");

        assertThat(usuario.isAtivo()).isTrue();
        verify(usuarioRepository, never()).save(usuario);
    }

    @Test
    void excluirPorPublicIdInativaUsuarioNaoAdmin() {
        UUID publicId = UUID.randomUUID();
        Usuario usuario = criarUsuarioAtivo();
        usuario.setPublicId(publicId);
        String sufixo = publicId.toString().substring(0, 8);
        when(usuarioRepository.findByPublicId(publicId)).thenReturn(Optional.of(usuario));
        when(criptografiaService.criptografar("Usuario excluido " + sufixo)).thenReturn("nome-anonimo");
        when(criptografiaService.criptografar("usuario-removido-" + sufixo + "@anonimo.local")).thenReturn("email-anonimo");
        when(criptografiaService.criptografar("senha-removida-" + sufixo)).thenReturn("senha-anonima");

        usuarioService.excluirPorPublicId(publicId);

        assertThat(usuario.isAtivo()).isFalse();
        assertThat(usuario.getEmailHash()).isNull();
        assertThat(usuario.getExcluidoEm()).isNotNull();
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void excluirPorPublicIdRejeitaContaAdmin() {
        UUID publicId = UUID.randomUUID();
        Usuario usuario = criarUsuarioAtivo();
        usuario.setPublicId(publicId);
        usuario.setPerfil(Perfil.ADMIN);
        when(usuarioRepository.findByPublicId(publicId)).thenReturn(Optional.of(usuario));

        assertThatThrownBy(() -> usuarioService.excluirPorPublicId(publicId))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("Contas administradoras nao podem ser excluidas");

        assertThat(usuario.isAtivo()).isTrue();
        verify(usuarioRepository, never()).save(usuario);
    }

    private Usuario criarUsuarioAtivo() {
        Usuario usuario = new Usuario();
        usuario.setId(10L);
        usuario.setPublicId(UUID.randomUUID());
        usuario.setNome("Fabio");
        usuario.setEmail("fabio@email.com");
        usuario.setEmailHash("hash-email");
        usuario.setSenha("senha-hash");
        usuario.setPerfil(Perfil.GERADORA);
        usuario.setAtivo(true);
        return usuario;
    }
}
