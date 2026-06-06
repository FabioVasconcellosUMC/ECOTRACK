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

    private Empresa descriptografarDadosSensiveis(Empresa empresa) {
        empresa.setCnpj(criptografiaService.descriptografar(empresa.getCnpj()));
        empresa.setEmail(criptografiaService.descriptografar(empresa.getEmail()));
        empresa.setTelefone(criptografiaService.descriptografar(empresa.getTelefone()));
        empresa.setEndereco(criptografiaService.descriptografar(empresa.getEndereco()));
        return empresa;
    }
}
