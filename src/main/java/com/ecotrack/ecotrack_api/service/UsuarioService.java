package com.ecotrack.ecotrack_api.service;

import com.ecotrack.ecotrack_api.entity.Usuario;
import com.ecotrack.ecotrack_api.exception.RecursoNaoEncontradoException;
import com.ecotrack.ecotrack_api.exception.RegraNegocioException;
import com.ecotrack.ecotrack_api.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final DadosPessoaisCriptografiaService criptografiaService;

    @Transactional
    public void excluirLogicamente(Usuario usuarioAutenticado) {
        Usuario usuario = usuarioRepository.findById(usuarioAutenticado.getId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado"));

        if (!usuario.isAtivo()) {
            throw new RegraNegocioException("Usuário já está excluído");
        }

        usuario.setNome(criptografiaService.criptografar(valorSeguro(usuario.getNome())));
        usuario.setEmail(criptografiaService.criptografar(valorSeguro(usuario.getEmail())));
        usuario.setSenha(criptografiaService.criptografar(valorSeguro(usuario.getSenha())));
        usuario.setEmailHash(null);
        usuario.setAtivo(false);
        usuario.setExcluidoEm(LocalDateTime.now());

        usuarioRepository.save(usuario);
    }

    private String valorSeguro(String valor) {
        return valor == null ? "" : valor;
    }
}
