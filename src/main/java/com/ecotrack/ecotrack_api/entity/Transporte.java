package com.ecotrack.ecotrack_api.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "transporte")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transporte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "lote_id")
    private Lote lote;

    @ManyToOne
    @JoinColumn(name = "transportadora_id")
    private Empresa transportadora;

    @ManyToOne
    @JoinColumn(name = "receptora_id")
    private Empresa receptora;

    @Enumerated(EnumType.STRING)
    private StatusTransporte status = StatusTransporte.PENDENTE;

    private LocalDateTime dataColeta;
    private LocalDateTime dataEntrega;
    private String responsavel;
    private String observacao;
    private LocalDateTime criadoEm = LocalDateTime.now();
}