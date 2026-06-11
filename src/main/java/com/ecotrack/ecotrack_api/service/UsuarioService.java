package com.ecotrack.ecotrack_api.service;

import com.ecotrack.ecotrack_api.dto.UsuarioResumoResponse;
import com.ecotrack.ecotrack_api.entity.Perfil;
import com.ecotrack.ecotrack_api.entity.Usuario;
import com.ecotrack.ecotrack_api.exception.RecursoNaoEncontradoException;
import com.ecotrack.ecotrack_api.exception.RegraNegocioException;
import com.ecotrack.ecotrack_api.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final DadosPessoaisCriptografiaService criptografiaService;

    @Transactional(readOnly = true)
    public List<UsuarioResumoResponse> listarAtivos() {
        return usuarioRepository.findAll().stream()
                .filter(Usuario::isAtivo)
                .sorted(Comparator.comparing(Usuario::getCriadoEm).reversed())
                .map(this::toResumo)
                .toList();
    }

    @Transactional
    public void excluirLogicamente(Usuario usuarioAutenticado) {
        Usuario usuario = usuarioRepository.findById(usuarioAutenticado.getId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuario nao encontrado"));

        excluirUsuario(usuario, "A conta administradora principal nao pode ser excluida por esta tela");
    }

    @Transactional
    public void excluirPorPublicId(UUID publicId) {
        Usuario usuario = usuarioRepository.findByPublicId(publicId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuario nao encontrado"));

        excluirUsuario(usuario, "Contas administradoras nao podem ser excluidas");
    }

    private void excluirUsuario(Usuario usuario, String mensagemAdmin) {
        if (!usuario.isAtivo()) {
            throw new RegraNegocioException("Usuario ja esta excluido");
        }

        if (usuario.getPerfil() == Perfil.ADMIN) {
            throw new RegraNegocioException(mensagemAdmin);
        }

        String sufixo = usuario.getPublicId() == null
                ? String.valueOf(usuario.getId())
                : usuario.getPublicId().toString().substring(0, 8);

        usuario.setNome(criptografiaService.criptografar("Usuario excluido " + sufixo));
        usuario.setEmail(criptografiaService.criptografar("usuario-removido-" + sufixo + "@anonimo.local"));
        usuario.setEmailHash(null);
        usuario.setSenha(criptografiaService.criptografar("senha-removida-" + sufixo));
        usuario.setAtivo(false);
        usuario.setExcluidoEm(LocalDateTime.now());

        usuarioRepository.save(usuario);
    }

    private UsuarioResumoResponse toResumo(Usuario usuario) {
        String email = criptografiaService.descriptografar(usuario.getEmail());
        return new UsuarioResumoResponse(
                usuario.getPublicId(),
                criptografiaService.descriptografar(usuario.getNome()),
                mascararEmail(email),
                usuario.getPerfil(),
                usuario.getEmpresa() == null ? null : usuario.getEmpresa().getRazaoSocial(),
                usuario.isAtivo(),
                usuario.isTermosUsoAceitos(),
                usuario.getTermosUsoAceitosEm(),
                usuario.getCriadoEm()
        );
    }

    private String mascararEmail(String email) {
        if (email == null || email.isBlank() || !email.contains("@")) {
            return "E-mail protegido";
        }
        String[] partes = email.split("@", 2);
        String local = partes[0];
        String dominio = partes[1];
        String prefixo = local.length() <= 2 ? local : local.substring(0, 2);
        return prefixo + "***@" + dominio;
    }

}
