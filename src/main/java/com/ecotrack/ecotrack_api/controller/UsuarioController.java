package com.ecotrack.ecotrack_api.controller;

import com.ecotrack.ecotrack_api.entity.Usuario;
import com.ecotrack.ecotrack_api.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @DeleteMapping("/me")
    public ResponseEntity<Map<String, String>> excluirMinhaConta(@AuthenticationPrincipal Usuario usuario) {
        usuarioService.excluirLogicamente(usuario);
        return ResponseEntity.ok(Map.of("mensagem", "Usuário excluído com sucesso"));
    }
}
