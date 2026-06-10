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
        validarEmailDisponivel(request.email());
        Perfil perfil = converterPerfil(request.perfil());
        validarPerfilCadastroPublico(perfil);

        Usuario usuario = new Usuario();
        usuario.setNome(criptografiaService.criptografar(request.nome()));
        usuario.setEmail(criptografiaService.criptografar(criptografiaService.normalizarEmail(request.email())));
        usuario.setEmailHash(hashEmail(request.email()));
        usuario.setSenha(passwordEncoder.encode(request.senha()));
        usuario.setPerfil(perfil);
        usuarioRepository.save(usuario);

        return ResponseEntity.status(201).body(Map.of("mensagem", "Usuario cadastrado com sucesso"));
    }

    private void validarEmailDisponivel(String email) {
        String emailNormalizado = criptografiaService.normalizarEmail(email);
        if (usuarioRepository.findByEmailHashAndAtivoTrue(hashEmail(email)).isPresent()
                || usuarioRepository.findByEmailAndAtivoTrue(emailNormalizado).isPresent()) {
            throw new RegraNegocioException("E-mail ja cadastrado");
        }
    }

    private String hashEmail(String email) {
        return criptografiaService.hashBusca(criptografiaService.normalizarEmail(email));
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
