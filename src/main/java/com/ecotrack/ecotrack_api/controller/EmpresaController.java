package com.ecotrack.ecotrack_api.controller;

import com.ecotrack.ecotrack_api.entity.Empresa;
import com.ecotrack.ecotrack_api.service.EmpresaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/empresas")
@RequiredArgsConstructor
public class EmpresaController {

    private final EmpresaService empresaService;

    @GetMapping
    public List<Empresa> listar() {
        return empresaService.listar();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Empresa> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(empresaService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<Empresa> salvar(@RequestBody Empresa empresa) {
        return ResponseEntity.ok(empresaService.salvar(empresa));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        empresaService.deletar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/cnpj/{cnpj}")
    public ResponseEntity<Map> consultarCnpj(@PathVariable String cnpj) {
        return ResponseEntity.ok(empresaService.consultarCnpj(cnpj));
    }
}