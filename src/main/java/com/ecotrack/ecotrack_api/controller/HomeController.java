package com.ecotrack.ecotrack_api.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "EcoTrack API está online! Utilize os endpoints de /auth para autenticação.";
    }
}