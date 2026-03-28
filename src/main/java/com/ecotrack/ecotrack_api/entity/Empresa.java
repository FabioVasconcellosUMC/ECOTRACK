package com.ecotrack.ecotrack_api.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "empresa")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Empresa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String razaoSocial;

    @Column(unique = true)
    private String cnpj;

    @Enumerated(EnumType.STRING)
    private TipoEmpresa tipo;

    private String endereco;
    private String email;
    private String telefone;
    private boolean ativa = true;
    private LocalDateTime criadoEm = LocalDateTime.now();
}