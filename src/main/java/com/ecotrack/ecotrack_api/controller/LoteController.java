package com.ecotrack.ecotrack_api.controller;

import com.ecotrack.ecotrack_api.entity.HistoricoLote;
import com.ecotrack.ecotrack_api.entity.Lote;
import com.ecotrack.ecotrack_api.entity.StatusLote;
import com.ecotrack.ecotrack_api.entity.Usuario;
import com.ecotrack.ecotrack_api.service.LoteService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/lotes")
@RequiredArgsConstructor
@Validated
public class LoteController {

    private final LoteService loteService;

    @PostMapping
    public ResponseEntity<Lote> criar(@Valid @RequestBody Lote lote,
                                      @AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.status(201).body(loteService.criar(lote, usuario));
    }

    @GetMapping
    public ResponseEntity<?> listar(@RequestParam(required = false, name = "q") String termoBusca,
                                    @RequestParam(required = false) Integer limit,
                                    @RequestParam(required = false) Integer page) {
        if (page != null) {
            return ResponseEntity.ok(loteService.listarPagina(termoBusca, page, limit));
        }

        return ResponseEntity.ok(loteService.listarTodos(termoBusca, limit));
    }

    @GetMapping("/{publicId}")
    public ResponseEntity<Lote> buscar(@PathVariable UUID publicId) {
        return ResponseEntity.ok(loteService.buscarPorPublicId(publicId));
    }

    @PatchMapping("/{publicId}/status")
    public ResponseEntity<Lote> alterarStatus(@PathVariable UUID publicId,
                                              @RequestParam StatusLote novoStatus,
                                              @RequestParam(required = false)
                                              @Size(max = 1000, message = "Observacao deve ter no maximo 1000 caracteres")
                                              @Pattern(regexp = "^[^<>]*$", message = "Observacao nao pode conter tags HTML ou scripts")
                                              String observacao,
                                              @AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(loteService.alterarStatus(publicId, novoStatus, observacao, usuario));
    }

    @GetMapping("/{publicId}/historico")
    public ResponseEntity<List<HistoricoLote>> historico(@PathVariable UUID publicId) {
        return ResponseEntity.ok(loteService.buscarHistoricoPorPublicId(publicId));
    }
}
