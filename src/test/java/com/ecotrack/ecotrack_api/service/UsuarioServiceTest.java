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
        when(usuarioRepository.findById(10L)).thenReturn(Optional.of(usuario));
        when(criptografiaService.criptografar("Fabio")).thenReturn("nome-criptografado");
        when(criptografiaService.criptografar("fabio@email.com")).thenReturn("email-criptografado");
        when(criptografiaService.criptografar("senha-hash")).thenReturn("senha-criptografada");

        usuarioService.excluirLogicamente(usuario);

        assertThat(usuario.isAtivo()).isFalse();
        assertThat(usuario.getNome()).isEqualTo("enc:nome-criptografado");
        assertThat(usuario.getEmail()).isEqualTo("enc:10:email-criptografado");
        assertThat(usuario.getSenha()).isEqualTo("enc:senha-criptografada");
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
                .hasMessage("Usuário já está excluído");

        verify(usuarioRepository, never()).save(usuario);
    }

    private Usuario criarUsuarioAtivo() {
        Usuario usuario = new Usuario();
        usuario.setId(10L);
        usuario.setNome("Fabio");
        usuario.setEmail("fabio@email.com");
        usuario.setSenha("senha-hash");
        usuario.setPerfil(Perfil.GERADORA);
        usuario.setAtivo(true);
        return usuario;
    }
}
