package com.ecotrack.ecotrack_api.config;

import com.ecotrack.ecotrack_api.entity.Empresa;
import com.ecotrack.ecotrack_api.entity.Usuario;
import com.ecotrack.ecotrack_api.repository.EmpresaRepository;
import com.ecotrack.ecotrack_api.repository.UsuarioRepository;
import com.ecotrack.ecotrack_api.service.DadosPessoaisCriptografiaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Slf4j
@Component
@Profile("!test")
@RequiredArgsConstructor
public class DadosSensiveisBackfillRunner implements ApplicationRunner {

    private final UsuarioRepository usuarioRepository;
    private final EmpresaRepository empresaRepository;
    private final DadosPessoaisCriptografiaService criptografiaService;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        List<Usuario> usuarios = usuarioRepository.findAll();
        List<Usuario> usuariosAlterados = usuarios.stream()
                .filter(this::protegerUsuario)
                .toList();
        usuarioRepository.saveAllAndFlush(usuariosAlterados);

        List<Empresa> empresas = empresaRepository.findAll();
        List<Empresa> empresasAlteradas = empresas.stream()
                .filter(this::protegerEmpresa)
                .toList();
        empresaRepository.saveAllAndFlush(empresasAlteradas);

        log.info("Backfill LGPD concluido: {} usuarios e {} empresas protegidos",
                usuariosAlterados.size(), empresasAlteradas.size());
    }

    private boolean protegerUsuario(Usuario usuario) {
        boolean alterado = false;
        String emailNormalizado = criptografiaService.normalizarEmail(criptografiaService.descriptografar(usuario.getEmail()));

        if (!usuario.isAtivo()) {
            boolean precisaProtegerDados = precisaCriptografar(usuario.getNome()) || precisaCriptografar(usuario.getEmail());
            if (usuario.getEmailHash() != null) {
                usuario.setEmailHash(null);
                alterado = true;
            }
            usuario.setNome(criptografiaService.criptografar(usuario.getNome()));
            usuario.setEmail(criptografiaService.criptografar(emailNormalizado));
            return alterado || precisaProtegerDados;
        }

        if (usuario.getEmailHash() == null && emailNormalizado != null) {
            String emailHash = criptografiaService.hashBusca(emailNormalizado);
            boolean hashDisponivel = usuarioRepository.findByEmailHash(emailHash)
                    .map(usuarioExistente -> Objects.equals(usuarioExistente.getId(), usuario.getId()))
                    .orElse(true);

            if (hashDisponivel) {
                usuario.setEmailHash(emailHash);
                alterado = true;
            }
        }

        alterado = alterado || precisaCriptografar(usuario.getNome()) || precisaCriptografar(usuario.getEmail());
        usuario.setNome(criptografiaService.criptografar(usuario.getNome()));
        usuario.setEmail(criptografiaService.criptografar(emailNormalizado));
        return alterado;
    }

    private boolean protegerEmpresa(Empresa empresa) {
        boolean alterado = false;
        String cnpjNormalizado = criptografiaService.normalizarCnpj(criptografiaService.descriptografar(empresa.getCnpj()));
        String emailNormalizado = criptografiaService.normalizarEmail(criptografiaService.descriptografar(empresa.getEmail()));

        if (empresa.getCnpjHash() == null && cnpjNormalizado != null) {
            empresa.setCnpjHash(criptografiaService.hashBusca(cnpjNormalizado));
            alterado = true;
        }

        if (empresa.getEmailHash() == null && emailNormalizado != null) {
            empresa.setEmailHash(criptografiaService.hashBusca(emailNormalizado));
            alterado = true;
        }

        alterado = alterado
                || precisaCriptografar(empresa.getCnpj())
                || precisaCriptografar(empresa.getEmail())
                || precisaCriptografar(empresa.getTelefone())
                || precisaCriptografar(empresa.getEndereco());

        empresa.setCnpj(criptografiaService.criptografar(cnpjNormalizado));
        empresa.setEmail(criptografiaService.criptografar(emailNormalizado));
        empresa.setTelefone(criptografiaService.criptografar(criptografiaService.descriptografar(empresa.getTelefone())));
        empresa.setEndereco(criptografiaService.criptografar(criptografiaService.descriptografar(empresa.getEndereco())));
        return alterado;
    }

    private boolean precisaCriptografar(String valor) {
        return valor != null && !valor.startsWith("enc:");
    }
}
