package com.ecotrack.ecotrack_api.controller;

import com.ecotrack.ecotrack_api.dto.RelatoriosResumoResponse;
import com.ecotrack.ecotrack_api.service.IndicadoresService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/relatorios")
@RequiredArgsConstructor
public class RelatoriosController {

    private final IndicadoresService indicadoresService;

    @GetMapping("/resumo")
    public ResponseEntity<RelatoriosResumoResponse> resumo() {
        return ResponseEntity.ok(indicadoresService.resumoRelatorios());
    }
}
