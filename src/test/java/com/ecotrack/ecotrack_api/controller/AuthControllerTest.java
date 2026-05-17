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
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
        CadastroRequest request = new CadastroRequest("Ana", "ana@ecotrack.com", "123456", "ADMIN");
        when(usuarioRepository.findByEmail("ana@ecotrack.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("123456")).thenReturn("senha-criptografada");

        ResponseEntity<Map<String, String>> response = authController.cadastro(request);

        assertThat(response.getStatusCode().value()).isEqualTo(201);
        assertThat(response.getBody()).containsEntry("mensagem", "Usuario cadastrado com sucesso");

        ArgumentCaptor<Usuario> usuarioCaptor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(usuarioCaptor.capture());
        assertThat(usuarioCaptor.getValue().getEmail()).isEqualTo("ana@ecotrack.com");
        assertThat(usuarioCaptor.getValue().getSenha()).isEqualTo("senha-criptografada");
        assertThat(usuarioCaptor.getValue().getPerfil()).isEqualTo(Perfil.ADMIN);
    }

    @Test
    void cadastroImpedeEmailDuplicado() {
        CadastroRequest request = new CadastroRequest("Ana", "ana@ecotrack.com", "123456", "ADMIN");
        when(usuarioRepository.findByEmail("ana@ecotrack.com")).thenReturn(Optional.of(new Usuario()));

        assertThatThrownBy(() -> authController.cadastro(request))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("Email ja cadastrado");
    }

    @Test
    void cadastroImpedePerfilInvalido() {
        CadastroRequest request = new CadastroRequest("Ana", "ana@ecotrack.com", "123456", "GESTOR");
        when(usuarioRepository.findByEmail("ana@ecotrack.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authController.cadastro(request))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("Perfil invalido");
    }
}
