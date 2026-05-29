package com.ecotrack.ecotrack_api.controller;

import com.ecotrack.ecotrack_api.dto.CadastroRequest;
import com.ecotrack.ecotrack_api.entity.Perfil;
import com.ecotrack.ecotrack_api.entity.Usuario;
import com.ecotrack.ecotrack_api.exception.RegraNegocioException;
import com.ecotrack.ecotrack_api.repository.UsuarioRepository;
import com.ecotrack.ecotrack_api.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthController authController;

    @Test
    void cadastroCriaUsuarioComSenhaCriptografada() {
        CadastroRequest request = new CadastroRequest("Fabio", "fabio@email.com", "123456", "ADMIN");
        when(usuarioRepository.findByEmail("fabio@email.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("123456")).thenReturn("senha-hash");

        ResponseEntity<Map<String, String>> response = authController.cadastro(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).containsEntry("mensagem", "Usuário cadastrado com sucesso");

        ArgumentCaptor<Usuario> usuarioCaptor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(usuarioCaptor.capture());

        Usuario usuarioSalvo = usuarioCaptor.getValue();
        assertThat(usuarioSalvo.getNome()).isEqualTo("Fabio");
        assertThat(usuarioSalvo.getEmail()).isEqualTo("fabio@email.com");
        assertThat(usuarioSalvo.getSenha()).isEqualTo("senha-hash");
        assertThat(usuarioSalvo.getPerfil()).isEqualTo(Perfil.ADMIN);
    }

    @Test
    void cadastroRejeitaEmailJaCadastrado() {
        CadastroRequest request = new CadastroRequest("Fabio", "fabio@email.com", "123456", "ADMIN");
        when(usuarioRepository.findByEmail("fabio@email.com")).thenReturn(Optional.of(new Usuario()));

        assertThatThrownBy(() -> authController.cadastro(request))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("E-mail já cadastrado");

        verify(usuarioRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void cadastroRejeitaPerfilInvalido() {
        CadastroRequest request = new CadastroRequest("Fabio", "fabio@email.com", "123456", "INVALIDO");
        when(usuarioRepository.findByEmail("fabio@email.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authController.cadastro(request))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("Perfil inválido");
    }
}
