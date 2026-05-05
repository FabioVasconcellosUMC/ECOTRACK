package com.ecotrack.ecotrack_api.service;

import com.ecotrack.ecotrack_api.entity.Empresa;
import com.ecotrack.ecotrack_api.repository.EmpresaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmpresaService {

    private final EmpresaRepository empresaRepository;
    private final RestTemplate restTemplate;

    @Value("${brasilapi.url}")
    private String brasilApiUrl;

    public List<Empresa> listar() {
        return empresaRepository.findAll();
    }

    public Empresa buscarPorId(Long id) {
        return empresaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Empresa não encontrada"));
    }

    public Empresa salvar(Empresa empresa) {
        return empresaRepository.save(empresa);
    }

    public void deletar(Long id) {
        empresaRepository.deleteById(id);
    }

    public Map consultarCnpj(String cnpj) {
        String url = brasilApiUrl + "/cnpj/v1/" + cnpj.replaceAll("[^0-9]", "");
        return restTemplate.getForObject(url, Map.class);
    }
}