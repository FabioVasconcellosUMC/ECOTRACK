package com.ecotrack.ecotrack_api.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "transporte")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transporte {

    private static final String SEM_TAGS = "^[^<>]*$";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Long id;

    @Column(name = "public_id", nullable = false, unique = true, updatable = false)
    private UUID publicId = UUID.randomUUID();

    @NotNull(message = "Lote e obrigatorio")
    @ManyToOne
    @JoinColumn(name = "lote_id")
    private Lote lote;

    @NotNull(message = "Transportadora e obrigatoria")
    @ManyToOne
    @JoinColumn(name = "transportadora_id")
    private Empresa transportadora;

    @NotNull(message = "Receptora e obrigatoria")
    @ManyToOne
    @JoinColumn(name = "receptora_id")
    private Empresa receptora;

    @Enumerated(EnumType.STRING)
    private StatusTransporte status = StatusTransporte.PENDENTE;

    private LocalDateTime dataColeta;
    private LocalDateTime dataEntrega;

    @Size(max = 150, message = "Responsavel deve ter no maximo 150 caracteres")
    @Pattern(regexp = SEM_TAGS, message = "Responsavel nao pode conter tags HTML ou scripts")
    private String responsavel;

    @Size(max = 1000, message = "Observacao deve ter no maximo 1000 caracteres")
    @Pattern(regexp = SEM_TAGS, message = "Observacao nao pode conter tags HTML ou scripts")
    private String observacao;

    private LocalDateTime criadoEm = LocalDateTime.now();
}
