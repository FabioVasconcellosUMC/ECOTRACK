package com.ecotrack.ecotrack_api.controller;

import com.ecotrack.ecotrack_api.entity.StatusTransporte;
import com.ecotrack.ecotrack_api.entity.Transporte;
import com.ecotrack.ecotrack_api.service.ManifestoPdfService;
import com.ecotrack.ecotrack_api.service.TransporteService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/transportes")
@RequiredArgsConstructor
@Validated
public class TransporteController {

    private final TransporteService transporteService;
    private final ManifestoPdfService manifestoPdfService;

    @GetMapping
    public ResponseEntity<List<Transporte>> listar(@RequestParam(required = false, name = "q") String termoBusca,
                                                   @RequestParam(required = false) Integer limit) {
        return ResponseEntity.ok(transporteService.listar(termoBusca, limit));
    }

    @GetMapping("/{publicId}")
    public ResponseEntity<Transporte> buscarPorPublicId(@PathVariable UUID publicId) {
        return ResponseEntity.ok(transporteService.buscarPorPublicId(publicId));
    }

    @PostMapping
    public ResponseEntity<Transporte> criar(@Valid @RequestBody Transporte transporte) {
        return ResponseEntity.status(201).body(transporteService.criar(transporte));
    }

    @PatchMapping("/{publicId}/status")
    public ResponseEntity<Transporte> alterarStatus(
            @PathVariable UUID publicId,
            @RequestParam StatusTransporte novoStatus,
            @RequestParam(required = false)
            @Size(max = 1000, message = "Observacao deve ter no maximo 1000 caracteres")
            @Pattern(regexp = "^[^<>]*$", message = "Observacao nao pode conter tags HTML ou scripts")
            String observacao) {
        return ResponseEntity.ok(transporteService.alterarStatus(publicId, novoStatus, observacao));
    }

    @PatchMapping("/{publicId}/recebimento-final")
    public ResponseEntity<Transporte> confirmarRecebimentoFinal(
            @PathVariable UUID publicId,
            @RequestParam(required = false)
            @Size(max = 1000, message = "Observacao deve ter no maximo 1000 caracteres")
            @Pattern(regexp = "^[^<>]*$", message = "Observacao nao pode conter tags HTML ou scripts")
            String observacao) {
        return ResponseEntity.ok(transporteService.confirmarRecebimentoFinal(publicId, observacao));
    }

    @GetMapping("/lote/{lotePublicId}")
    public ResponseEntity<List<Transporte>> buscarPorLote(@PathVariable UUID lotePublicId) {
        return ResponseEntity.ok(transporteService.buscarPorLotePublicId(lotePublicId));
    }

    @GetMapping("/{publicId}/manifesto")
    public ResponseEntity<byte[]> gerarManifesto(@PathVariable UUID publicId) {
        byte[] pdf = manifestoPdfService.gerarManifesto(publicId);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename(manifestoPdfService.nomeArquivo(publicId))
                                .build()
                                .toString()
                )
                .body(pdf);
    }
}
