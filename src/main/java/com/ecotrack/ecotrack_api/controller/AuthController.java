package com.ecotrack.ecotrack_api.controller;

import com.ecotrack.ecotrack_api.dto.CadastroRequest;
import com.ecotrack.ecotrack_api.dto.LoginRequest;
import com.ecotrack.ecotrack_api.dto.LoginResponse;
import com.ecotrack.ecotrack_api.entity.Perfil;
import com.ecotrack.ecotrack_api.entity.Usuario;
import com.ecotrack.ecotrack_api.exception.RegraNegocioException;
import com.ecotrack.ecotrack_api.repository.UsuarioRepository;
import com.ecotrack.ecotrack_api.security.JwtService;
import com.ecotrack.ecotrack_api.service.DadosPessoaisCriptografiaService;
import com.ecotrack.ecotrack_api.validation.TextoSeguro;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final DadosPessoaisCriptografiaService criptografiaService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.senha())
            );
        } catch (RuntimeException e) {
            throw new BadCredentialsException("E-mail ou senha invalidos", e);
        }

        Usuario usuario = (Usuario) userDetailsService.loadUserByUsername(request.email());
        String token = jwtService.gerarToken(usuario);

        return ResponseEntity.ok(new LoginResponse(
                token,
                criptografiaService.descriptografar(usuario.getNome()),
                usuario.getPerfil().name()
        ));
    }

    @PostMapping("/cadastro")
    public ResponseEntity<Map<String, String>> cadastro(@Valid @RequestBody CadastroRequest request) {
        TextoSeguro.validar(request.nome(), "Nome");
        String emailNormalizado = criptografiaService.normalizarEmail(request.email());
        String emailHash = hashEmail(emailNormalizado);
        validarEmailDisponivel(emailNormalizado, emailHash);
        Perfil perfil = converterPerfil(request.perfil());
        validarPerfilCadastroPublico(perfil);

        Usuario usuario = new Usuario();
        usuario.setNome(criptografiaService.criptografar(request.nome()));
        usuario.setEmail(criptografiaService.criptografar(emailNormalizado));
        usuario.setEmailHash(emailHash);
        usuario.setSenha(passwordEncoder.encode(request.senha()));
        usuario.setPerfil(perfil);
        usuario.setTermosUsoAceitos(true);
        usuario.setTermosUsoAceitosEm(LocalDateTime.now());
        usuarioRepository.save(usuario);

        return ResponseEntity.status(201).body(Map.of("mensagem", "Usuario cadastrado com sucesso"));
    }

    private void validarEmailDisponivel(String emailNormalizado, String emailHash) {
        usuarioRepository.findByEmailHash(emailHash).ifPresent(usuario -> {
            if (usuario.isAtivo()) {
                throw new RegraNegocioException("E-mail ja cadastrado");
            }
            throw new RegraNegocioException("Conta excluida. Entre em contato com o administrador para solicitar orientacao.");
        });

        if (usuarioRepository.findByEmailAndAtivoTrue(emailNormalizado).isPresent()) {
            throw new RegraNegocioException("E-mail ja cadastrado");
        }
    }

    private String hashEmail(String emailNormalizado) {
        return criptografiaService.hashBusca(emailNormalizado);
    }

    private Perfil converterPerfil(String perfil) {
        try {
            return Perfil.valueOf(perfil.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RegraNegocioException("Perfil invalido");
        }
    }

    private void validarPerfilCadastroPublico(Perfil perfil) {
        if (perfil == Perfil.ADMIN) {
            throw new RegraNegocioException("Cadastro de administrador nao e permitido");
        }
    }
}
