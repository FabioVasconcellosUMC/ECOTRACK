package com.ecotrack.ecotrack_api.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "empresa")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Empresa {

    private static final String SEM_TAGS = "^[^<>]*$";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Long id;

    @Column(name = "public_id", nullable = false, unique = true, updatable = false)
    private UUID publicId = UUID.randomUUID();

    @NotBlank(message = "Razao social e obrigatoria")
    @Size(max = 200, message = "Razao social deve ter no maximo 200 caracteres")
    @Pattern(regexp = SEM_TAGS, message = "Razao social nao pode conter tags HTML ou scripts")
    private String razaoSocial;

    @Column(columnDefinition = "TEXT")
    private String cnpj;

    @Column(name = "cnpj_hash", unique = true, length = 100)
    private String cnpjHash;

    @NotNull(message = "Tipo da empresa e obrigatorio")
    @Enumerated(EnumType.STRING)
    private TipoEmpresa tipo;

    @Column(columnDefinition = "TEXT")
    private String endereco;

    @Column(columnDefinition = "TEXT")
    private String email;

    @Column(name = "email_hash", length = 100)
    private String emailHash;

    @Column(columnDefinition = "TEXT")
    private String telefone;

    private boolean ativa = true;
    private LocalDateTime criadoEm = LocalDateTime.now();
}
