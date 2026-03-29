package com.ecotrack.ecotrack_api.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "lote")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Lote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String descricao;
    private String tipoResiduo;
    private BigDecimal quantidade;
    private String unidade;

    @Enumerated(EnumType.STRING)
    private StatusLote status = StatusLote.AGUARDANDO_COLETA;

    @ManyToOne
    @JoinColumn(name = "empresa_geradora_id")
    private Empresa empresaGeradora;

    @ManyToOne
    @JoinColumn(name = "criado_por")
    private Usuario criadoPor;

    private LocalDateTime criadoEm = LocalDateTime.now();
}