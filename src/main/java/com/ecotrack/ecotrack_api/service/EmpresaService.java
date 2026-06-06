package com.ecotrack.ecotrack_api.service;

import com.ecotrack.ecotrack_api.entity.Empresa;
import com.ecotrack.ecotrack_api.exception.RecursoNaoEncontradoException;
import com.ecotrack.ecotrack_api.exception.RegraNegocioException;
import com.ecotrack.ecotrack_api.repository.EmpresaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmpresaService {

    private static final String SEM_TAGS = "^[^<>]*$";
    private static final String CNPJ_VALIDO = "^[0-9./-]+$";
    private static final String TELEFONE_VALIDO = "^[0-9()+\\-\\s.]*$";
    private static final String EMAIL_VALIDO = "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$";

    private final EmpresaRepository empresaRepository;
    private final DadosPessoaisCriptografiaService criptografiaService;

    public List<Empresa> listar() {
        return empresaRepository.findAll().stream()
                .map(this::descriptografarDadosSensiveis)
                .toList();
    }

    public Empresa buscarPorId(Long id) {
        return descriptografarDadosSensiveis(empresaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Empresa não encontrada")));
    }

    public Empresa buscarPorPublicId(UUID publicId) {
        return descriptografarDadosSensiveis(empresaRepository.findByPublicId(publicId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Empresa não encontrada")));
    }

    public Empresa salvar(Empresa empresa) {
        validarDadosAbertos(empresa);
        prepararDadosSensiveis(empresa);
        Empresa salva = empresaRepository.save(empresa);
        return descriptografarDadosSensiveis(salva);
    }

    public void deletar(Long id) {
        if (!empresaRepository.existsById(id)) {
            throw new RecursoNaoEncontradoException("Empresa não encontrada");
        }

        empresaRepository.deleteById(id);
    }

    public void deletarPorPublicId(UUID publicId) {
        Empresa empresa = empresaRepository.findByPublicId(publicId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Empresa não encontrada"));
        empresaRepository.delete(empresa);
    }

    private void prepararDadosSensiveis(Empresa empresa) {
        String cnpjNormalizado = criptografiaService.normalizarCnpj(empresa.getCnpj());
        String cnpjHash = criptografiaService.hashBusca(cnpjNormalizado);

        if (cnpjHash != null && empresaRepository.existsByCnpjHash(cnpjHash)) {
            throw new RegraNegocioException("CNPJ ja cadastrado");
        }

        empresa.setCnpjHash(cnpjHash);
        empresa.setEmailHash(criptografiaService.hashBusca(criptografiaService.normalizarEmail(empresa.getEmail())));
        empresa.setCnpj(criptografiaService.criptografar(cnpjNormalizado));
        empresa.setEmail(criptografiaService.criptografar(criptografiaService.normalizarEmail(empresa.getEmail())));
        empresa.setTelefone(criptografiaService.criptografar(empresa.getTelefone()));
        empresa.setEndereco(criptografiaService.criptografar(empresa.getEndereco()));
    }

    private void validarDadosAbertos(Empresa empresa) {
        validarObrigatorio(empresa.getCnpj(), "CNPJ e obrigatorio");
        validarTamanho(empresa.getCnpj(), 18, "CNPJ deve ter no maximo 18 caracteres");
        validarPadrao(empresa.getCnpj(), CNPJ_VALIDO, "CNPJ deve conter apenas numeros, pontos, barra e hifen");
        validarTamanho(empresa.getEndereco(), 300, "Endereco deve ter no maximo 300 caracteres");
        validarPadrao(empresa.getEndereco(), SEM_TAGS, "Endereco nao pode conter tags HTML ou scripts");
        validarTamanho(empresa.getEmail(), 150, "E-mail deve ter no maximo 150 caracteres");
        validarPadrao(empresa.getEmail(), EMAIL_VALIDO, "E-mail invalido");
        validarTamanho(empresa.getTelefone(), 20, "Telefone deve ter no maximo 20 caracteres");
        validarPadrao(empresa.getTelefone(), TELEFONE_VALIDO, "Telefone deve conter apenas numeros e caracteres telefonicos");
    }

    private void validarObrigatorio(String valor, String mensagem) {
        if (valor == null || valor.isBlank()) {
            throw new RegraNegocioException(mensagem);
        }
    }

    private void validarTamanho(String valor, int tamanhoMaximo, String mensagem) {
        if (valor != null && valor.length() > tamanhoMaximo) {
            throw new RegraNegocioException(mensagem);
        }
    }

    private void validarPadrao(String valor, String padrao, String mensagem) {
        if (valor != null && !valor.matches(padrao)) {
            throw new RegraNegocioException(mensagem);
        }
    }

    private Empresa descriptografarDadosSensiveis(Empresa empresa) {
        empresa.setCnpj(criptografiaService.descriptografar(empresa.getCnpj()));
        empresa.setEmail(criptografiaService.descriptografar(empresa.getEmail()));
        empresa.setTelefone(criptografiaService.descriptografar(empresa.getTelefone()));
        empresa.setEndereco(criptografiaService.descriptografar(empresa.getEndereco()));
        return empresa;
    }
}
