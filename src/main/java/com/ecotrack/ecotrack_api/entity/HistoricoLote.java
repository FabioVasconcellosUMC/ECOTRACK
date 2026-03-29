package com.ecotrack.ecotrack_api.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "historico_lote")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HistoricoLote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "lote_id")
    private Lote lote;

    @Enumerated(EnumType.STRING)
    private StatusLote statusAnterior;

    @Enumerated(EnumType.STRING)
    private StatusLote statusNovo;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    private String observacao;

    private LocalDateTime dataHora = LocalDateTime.now();
}