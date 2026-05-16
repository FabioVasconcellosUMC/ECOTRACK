package com.ecotrack.ecotrack_api.controller;

import com.ecotrack.ecotrack_api.dto.CadastroRequest;
import com.ecotrack.ecotrack_api.dto.LoginRequest;
import com.ecotrack.ecotrack_api.dto.LoginResponse;
import com.ecotrack.ecotrack_api.entity.Perfil;
import com.ecotrack.ecotrack_api.entity.Usuario;
import com.ecotrack.ecotrack_api.exception.RegraNegocioException;
import com.ecotrack.ecotrack_api.repository.UsuarioRepository;
import com.ecotrack.ecotrack_api.security.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
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

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.senha())
        );

        Usuario usuario = (Usuario) userDetailsService.loadUserByUsername(request.email());
        String token = jwtService.gerarToken(usuario);

        return ResponseEntity.ok(new LoginResponse(token, usuario.getNome(), usuario.getPerfil().name()));
    }

    @PostMapping("/cadastro")
    public ResponseEntity<Map<String, String>> cadastro(@Valid @RequestBody CadastroRequest request) {
        validarEmailDisponivel(request.email());

        Usuario usuario = new Usuario();
        usuario.setNome(request.nome());
        usuario.setEmail(request.email());
        usuario.setSenha(passwordEncoder.encode(request.senha()));
        usuario.setPerfil(converterPerfil(request.perfil()));
        usuarioRepository.save(usuario);

        return ResponseEntity.status(201).body(Map.of("mensagem", "Usuario cadastrado com sucesso"));
    }

    private void validarEmailDisponivel(String email) {
        if (usuarioRepository.findByEmail(email).isPresent()) {
            throw new RegraNegocioException("Email ja cadastrado");
        }
    }

    private Perfil converterPerfil(String perfil) {
        try {
            return Perfil.valueOf(perfil.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RegraNegocioException("Perfil invalido");
        }
    }
}
