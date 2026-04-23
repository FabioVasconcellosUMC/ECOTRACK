package com.ecotrack.ecotrack_api.controller;

import com.ecotrack.ecotrack_api.dto.LoginRequest;
import com.ecotrack.ecotrack_api.dto.LoginResponse;
import com.ecotrack.ecotrack_api.entity.Perfil;
import com.ecotrack.ecotrack_api.entity.Usuario;
import com.ecotrack.ecotrack_api.repository.UsuarioRepository;
import com.ecotrack.ecotrack_api.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<?> cadastro(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String nome = request.get("nome");
        String senha = request.get("senha");
        String perfil = request.get("perfil");

        if (usuarioRepository.findByEmail(email).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Email já cadastrado"));
        }

        Usuario usuario = new Usuario();
        usuario.setNome(nome);
        usuario.setEmail(email);
        usuario.setSenha(passwordEncoder.encode(senha));
        usuario.setPerfil(Perfil.valueOf(perfil.toUpperCase()));
        usuarioRepository.save(usuario);

        return ResponseEntity.status(201).body(Map.of("mensagem", "Usuário cadastrado com sucesso"));
    }
}