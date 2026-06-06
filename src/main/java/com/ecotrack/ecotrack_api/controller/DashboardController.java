package com.ecotrack.ecotrack_api.controller;

import com.ecotrack.ecotrack_api.dto.DashboardResumoResponse;
import com.ecotrack.ecotrack_api.service.IndicadoresService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final IndicadoresService indicadoresService;

    @GetMapping("/resumo")
    public ResponseEntity<DashboardResumoResponse> resumo() {
        return ResponseEntity.ok(indicadoresService.resumoDashboard());
    }
}
