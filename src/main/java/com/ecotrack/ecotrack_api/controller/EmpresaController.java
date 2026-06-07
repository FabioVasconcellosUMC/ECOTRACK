package com.ecotrack.ecotrack_api.controller;

import com.ecotrack.ecotrack_api.entity.Empresa;
import com.ecotrack.ecotrack_api.service.EmpresaService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/empresas")
@RequiredArgsConstructor
@Validated
public class EmpresaController {

    private final EmpresaService empresaService;

    @GetMapping
    public Object listar(@RequestParam(required = false, name = "q")
                         @Size(max = 100, message = "Busca deve ter no maximo 100 caracteres")
                         String termoBusca,
                         @RequestParam(required = false)
                         @Min(value = 1, message = "Limite deve ser maior que zero")
                         @Max(value = 100, message = "Limite deve ser no maximo 100")
                         Integer limit,
                         @RequestParam(required = false)
                         @Min(value = 0, message = "Pagina nao pode ser negativa")
                         @Max(value = 10000, message = "Pagina muito alta")
                         Integer page) {
        if (page != null) {
            return empresaService.listarPagina(termoBusca, page, limit);
        }

        return empresaService.listar(termoBusca, limit);
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
