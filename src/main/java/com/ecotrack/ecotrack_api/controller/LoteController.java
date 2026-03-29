package com.ecotrack.ecotrack_api.controller;

import com.ecotrack.ecotrack_api.entity.HistoricoLote;
import com.ecotrack.ecotrack_api.entity.Lote;
import com.ecotrack.ecotrack_api.entity.StatusLote;
import com.ecotrack.ecotrack_api.entity.Usuario;
import com.ecotrack.ecotrack_api.service.LoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/lotes")
@RequiredArgsConstructor
public class LoteController {

    private final LoteService loteService;

    @PostMapping
    public ResponseEntity<Lote> criar(@RequestBody Lote lote,
                                      @AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.status(201).body(loteService.criar(lote, usuario));
    }

    @GetMapping
    public ResponseEntity<List<Lote>> listar() {
        return ResponseEntity.ok(loteService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Lote> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(loteService.buscarPorId(id));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Lote> alterarStatus(@PathVariable Long id,
                                              @RequestParam StatusLote novoStatus,
                                              @RequestParam(required = false) String observacao,
                                              @AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(loteService.alterarStatus(id, novoStatus, observacao, usuario));
    }

    @GetMapping("/{id}/historico")
    public ResponseEntity<List<HistoricoLote>> historico(@PathVariable Long id) {
        return ResponseEntity.ok(loteService.buscarHistorico(id));
    }
}