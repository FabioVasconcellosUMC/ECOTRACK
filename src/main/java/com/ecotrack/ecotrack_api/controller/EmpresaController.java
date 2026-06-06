package com.ecotrack.ecotrack_api.controller;

import com.ecotrack.ecotrack_api.entity.Empresa;
import com.ecotrack.ecotrack_api.service.EmpresaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/empresas")
@RequiredArgsConstructor
public class EmpresaController {

    private final EmpresaService empresaService;

    @GetMapping
    public List<Empresa> listar() {
        return empresaService.listar();
    }

    @GetMapping("/{publicId}")
    public ResponseEntity<Empresa> buscarPorPublicId(@PathVariable UUID publicId) {
        return ResponseEntity.ok(empresaService.buscarPorPublicId(publicId));
    }

    @PostMapping
    public ResponseEntity<Empresa> salvar(@Valid @RequestBody Empresa empresa) {
        return ResponseEntity.ok(empresaService.salvar(empresa));
    }

    @DeleteMapping("/{publicId}")
    public ResponseEntity<Void> deletar(@PathVariable UUID publicId) {
        empresaService.deletarPorPublicId(publicId);
        return ResponseEntity.noContent().build();
    }
}
