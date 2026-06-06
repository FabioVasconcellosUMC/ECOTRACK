package com.ecotrack.ecotrack_api.config;

import com.ecotrack.ecotrack_api.entity.Empresa;
import com.ecotrack.ecotrack_api.entity.Usuario;
import com.ecotrack.ecotrack_api.repository.EmpresaRepository;
import com.ecotrack.ecotrack_api.repository.UsuarioRepository;
import com.ecotrack.ecotrack_api.service.DadosPessoaisCriptografiaService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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
        usuarioRepository.findAll().forEach(this::protegerUsuario);
        empresaRepository.findAll().forEach(this::protegerEmpresa);
    }

    private void protegerUsuario(Usuario usuario) {
        String emailNormalizado = criptografiaService.normalizarEmail(criptografiaService.descriptografar(usuario.getEmail()));
        if (usuario.getEmailHash() == null && emailNormalizado != null) {
            usuario.setEmailHash(criptografiaService.hashBusca(emailNormalizado));
        }

        usuario.setNome(criptografiaService.criptografar(usuario.getNome()));
        usuario.setEmail(criptografiaService.criptografar(emailNormalizado));
    }

    private void protegerEmpresa(Empresa empresa) {
        String cnpjNormalizado = criptografiaService.normalizarCnpj(criptografiaService.descriptografar(empresa.getCnpj()));
        String emailNormalizado = criptografiaService.normalizarEmail(criptografiaService.descriptografar(empresa.getEmail()));

        if (empresa.getCnpjHash() == null && cnpjNormalizado != null) {
            empresa.setCnpjHash(criptografiaService.hashBusca(cnpjNormalizado));
        }

        if (empresa.getEmailHash() == null && emailNormalizado != null) {
            empresa.setEmailHash(criptografiaService.hashBusca(emailNormalizado));
        }

        empresa.setCnpj(criptografiaService.criptografar(cnpjNormalizado));
        empresa.setEmail(criptografiaService.criptografar(emailNormalizado));
        empresa.setTelefone(criptografiaService.criptografar(criptografiaService.descriptografar(empresa.getTelefone())));
        empresa.setEndereco(criptografiaService.criptografar(criptografiaService.descriptografar(empresa.getEndereco())));
    }
}
