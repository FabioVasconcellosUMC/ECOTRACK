package com.ecotrack.ecotrack_api.controller;

import com.ecotrack.ecotrack_api.dto.CadastroRequest;
import com.ecotrack.ecotrack_api.dto.LoginRequest;
import com.ecotrack.ecotrack_api.entity.Perfil;
import com.ecotrack.ecotrack_api.entity.Usuario;
import com.ecotrack.ecotrack_api.exception.RegraNegocioException;
import com.ecotrack.ecotrack_api.repository.UsuarioRepository;
import com.ecotrack.ecotrack_api.security.JwtService;
import com.ecotrack.ecotrack_api.service.DadosPessoaisCriptografiaService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
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

    @Mock
    private DadosPessoaisCriptografiaService criptografiaService;

    @InjectMocks
    private AuthController authController;

    @Test
    void cadastroCriaUsuarioComSenhaCriptografadaEDadosSensiveisProtegidos() {
        CadastroRequest request = new CadastroRequest("Fabio", "fabio@email.com", "123456", "GERADORA");
        prepararEmailDisponivel();
        when(criptografiaService.criptografar("Fabio")).thenReturn("enc:nome");
        when(criptografiaService.criptografar("fabio@email.com")).thenReturn("enc:email");
        when(passwordEncoder.encode("123456")).thenReturn("senha-hash");

        ResponseEntity<Map<String, String>> response = authController.cadastro(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).containsEntry("mensagem", "Usuario cadastrado com sucesso");

        ArgumentCaptor<Usuario> usuarioCaptor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(usuarioCaptor.capture());

        Usuario usuarioSalvo = usuarioCaptor.getValue();
        assertThat(usuarioSalvo.getNome()).isEqualTo("enc:nome");
        assertThat(usuarioSalvo.getEmail()).isEqualTo("enc:email");
        assertThat(usuarioSalvo.getEmailHash()).isEqualTo("hash-email");
        assertThat(usuarioSalvo.getSenha()).isEqualTo("senha-hash");
        assertThat(usuarioSalvo.getPerfil()).isEqualTo(Perfil.GERADORA);
    }

    @Test
    void cadastroRejeitaEmailJaCadastrado() {
        CadastroRequest request = new CadastroRequest("Fabio", "fabio@email.com", "123456", "GERADORA");
        prepararHashEmail();
        Usuario usuarioAtivo = new Usuario();
        usuarioAtivo.setAtivo(true);
        when(usuarioRepository.findByEmailHash("hash-email")).thenReturn(Optional.of(usuarioAtivo));

        assertThatThrownBy(() -> authController.cadastro(request))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("E-mail ja cadastrado");

        verify(usuarioRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void cadastroRejeitaEmailDeUsuarioExcluidoLogicamenteComMensagemClara() {
        CadastroRequest request = new CadastroRequest("Fabio Novo", "fabio@email.com", "123456", "GERADORA");
        prepararHashEmail();
        Usuario usuarioInativo = new Usuario();
        usuarioInativo.setAtivo(false);
        usuarioInativo.setEmailHash("hash-email");
        when(usuarioRepository.findByEmailHash("hash-email")).thenReturn(Optional.of(usuarioInativo));

        assertThatThrownBy(() -> authController.cadastro(request))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("Conta excluida. Entre em contato com o administrador para solicitar orientacao.");

        verify(usuarioRepository, never()).save(org.mockito.ArgumentMatchers.any(Usuario.class));
    }

    @Test
    void cadastroRejeitaPerfilInvalido() {
        CadastroRequest request = new CadastroRequest("Fabio", "fabio@email.com", "123456", "INVALIDO");
        prepararEmailDisponivel();

        assertThatThrownBy(() -> authController.cadastro(request))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("Perfil invalido");
    }

    @Test
    void cadastroRejeitaPerfilAdminNoCadastroPublico() {
        CadastroRequest request = new CadastroRequest("Fabio", "fabio@email.com", "123456", "ADMIN");
        prepararEmailDisponivel();

        assertThatThrownBy(() -> authController.cadastro(request))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("Cadastro de administrador nao e permitido");

        verify(usuarioRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void loginConverteFalhaDeAutenticacaoEmCredenciaisInvalidas() {
        LoginRequest request = new LoginRequest("naoexiste@email.com", "senhaerrada");
        doThrow(new RuntimeException("detalhe interno"))
                .when(authenticationManager).authenticate(any());

        assertThatThrownBy(() -> authController.login(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("E-mail ou senha invalidos");

        verify(userDetailsService, never()).loadUserByUsername(any());
    }

    private void prepararEmailDisponivel() {
        prepararHashEmail();
        when(usuarioRepository.findByEmailHash("hash-email")).thenReturn(Optional.empty());
        when(usuarioRepository.findByEmailAndAtivoTrue("fabio@email.com")).thenReturn(Optional.empty());
    }

    private void prepararHashEmail() {
        when(criptografiaService.normalizarEmail("fabio@email.com")).thenReturn("fabio@email.com");
        when(criptografiaService.hashBusca("fabio@email.com")).thenReturn("hash-email");
    }
}
