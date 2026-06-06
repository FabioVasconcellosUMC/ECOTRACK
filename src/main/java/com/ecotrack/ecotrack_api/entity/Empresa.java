package com.ecotrack.ecotrack_api.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
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

    @NotBlank(message = "CNPJ e obrigatorio")
    @Size(max = 18, message = "CNPJ deve ter no maximo 18 caracteres")
    @Pattern(regexp = "^[0-9./-]+$", message = "CNPJ deve conter apenas numeros, pontos, barra e hifen")
    @Column(unique = true)
    private String cnpj;

    @NotNull(message = "Tipo da empresa e obrigatorio")
    @Enumerated(EnumType.STRING)
    private TipoEmpresa tipo;

    @Size(max = 300, message = "Endereco deve ter no maximo 300 caracteres")
    @Pattern(regexp = SEM_TAGS, message = "Endereco nao pode conter tags HTML ou scripts")
    private String endereco;

    @Email(message = "E-mail invalido")
    @Size(max = 150, message = "E-mail deve ter no maximo 150 caracteres")
    private String email;

    @Size(max = 20, message = "Telefone deve ter no maximo 20 caracteres")
    @Pattern(regexp = "^[0-9()+\\-\\s.]*$", message = "Telefone deve conter apenas numeros e caracteres telefonicos")
    private String telefone;

    private boolean ativa = true;
    private LocalDateTime criadoEm = LocalDateTime.now();
}
