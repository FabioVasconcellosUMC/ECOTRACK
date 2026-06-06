package com.ecotrack.ecotrack_api.security;

import com.ecotrack.ecotrack_api.entity.Usuario;
import com.ecotrack.ecotrack_api.repository.UsuarioRepository;
import com.ecotrack.ecotrack_api.service.DadosPessoaisCriptografiaService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private DadosPessoaisCriptografiaService criptografiaService;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    @Test
    void loadUserByUsernameRejeitaUsuarioInativo() {
        Usuario usuario = new Usuario();
        usuario.setEmail("enc:email");
        usuario.setEmailHash("hash-email");
        usuario.setAtivo(false);
        when(criptografiaService.normalizarEmail("fabio@email.com")).thenReturn("fabio@email.com");
        when(criptografiaService.hashBusca("fabio@email.com")).thenReturn("hash-email");
        when(usuarioRepository.findByEmailHash("hash-email")).thenReturn(Optional.of(usuario));

        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("fabio@email.com"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("Usuario nao encontrado ou inativo: fabio@email.com");
    }
}
