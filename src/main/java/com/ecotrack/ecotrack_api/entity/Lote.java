package com.ecotrack.ecotrack_api.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "lote")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Lote {

    private static final String SEM_TAGS = "^[^<>]*$";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Long id;

    @Column(name = "public_id", nullable = false, unique = true, updatable = false)
    private UUID publicId = UUID.randomUUID();

    @NotBlank(message = "Descricao e obrigatoria")
    @Size(max = 1000, message = "Descricao deve ter no maximo 1000 caracteres")
    @Pattern(regexp = SEM_TAGS, message = "Descricao nao pode conter tags HTML ou scripts")
    private String descricao;

    @NotBlank(message = "Tipo de residuo e obrigatorio")
    @Size(max = 100, message = "Tipo de residuo deve ter no maximo 100 caracteres")
    @Pattern(regexp = SEM_TAGS, message = "Tipo de residuo nao pode conter tags HTML ou scripts")
    private String tipoResiduo;

    @NotNull(message = "Quantidade e obrigatoria")
    @DecimalMin(value = "0.01", message = "Quantidade deve ser maior que zero")
    @Digits(integer = 8, fraction = 2, message = "Quantidade deve ter no maximo 8 digitos inteiros e 2 decimais")
    private BigDecimal quantidade;

    @NotBlank(message = "Unidade e obrigatoria")
    @Size(max = 20, message = "Unidade deve ter no maximo 20 caracteres")
    @Pattern(regexp = "^[A-Za-z0-9/% .-]+$", message = "Unidade contem caracteres invalidos")
    private String unidade;

    @Enumerated(EnumType.STRING)
    private StatusLote status = StatusLote.AGUARDANDO_COLETA;

    @NotNull(message = "Empresa geradora e obrigatoria")
    @ManyToOne
    @JoinColumn(name = "empresa_geradora_id")
    private Empresa empresaGeradora;

    @ManyToOne
    @JoinColumn(name = "criado_por")
    private Usuario criadoPor;

    private LocalDateTime criadoEm = LocalDateTime.now();
}
