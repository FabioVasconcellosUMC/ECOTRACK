package com.ecotrack.ecotrack_api.controller;

import com.ecotrack.ecotrack_api.entity.Transporte;
import com.ecotrack.ecotrack_api.entity.StatusTransporte;
import com.ecotrack.ecotrack_api.service.TransporteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/transportes")
@RequiredArgsConstructor
public class TransporteController {

    private final TransporteService transporteService;

    @GetMapping
    public ResponseEntity<List<Transporte>> listar() {
        return ResponseEntity.ok(transporteService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Transporte> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(transporteService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<Transporte> criar(@RequestBody Transporte transporte) {
        return ResponseEntity.status(201).body(transporteService.criar(transporte));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Transporte> alterarStatus(
            @PathVariable Long id,
            @RequestParam StatusTransporte novoStatus,
            @RequestParam(required = false) String observacao) {
        return ResponseEntity.ok(transporteService.alterarStatus(id, novoStatus, observacao));
    }

    @GetMapping("/lote/{loteId}")
    public ResponseEntity<List<Transporte>> buscarPorLote(@PathVariable Long loteId) {
        return ResponseEntity.ok(transporteService.buscarPorLote(loteId));
    }
}