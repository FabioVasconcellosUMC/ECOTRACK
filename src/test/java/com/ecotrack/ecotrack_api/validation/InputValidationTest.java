package com.ecotrack.ecotrack_api.validation;

import com.ecotrack.ecotrack_api.dto.CadastroRequest;
import com.ecotrack.ecotrack_api.dto.LoginRequest;
import com.ecotrack.ecotrack_api.entity.Empresa;
import com.ecotrack.ecotrack_api.entity.Lote;
import com.ecotrack.ecotrack_api.entity.TipoEmpresa;
import com.ecotrack.ecotrack_api.entity.Transporte;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class InputValidationTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setupValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void closeValidator() {
        validatorFactory.close();
    }

    @Test
    void cadastroRejeitaScriptNoNomeESenhaMuitoGrande() {
        CadastroRequest request = new CadastroRequest(
                "<script>alert(1)</script>",
                "fabio@email.com",
                "a".repeat(73),
                "GERADORA",
                true
        );

        assertThat(validator.validate(request))
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("nome", "senha");
    }

    @Test
    void cadastroRejeitaTermosDeUsoNaoAceitos() {
        CadastroRequest request = new CadastroRequest(
                "Fabio",
                "fabio@email.com",
                "123456",
                "GERADORA",
                false
        );

        assertThat(validator.validate(request))
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("aceitouTermosUso");
    }

    @Test
    void loginRejeitaEmailInvalidoESenhaMuitoGrande() {
        LoginRequest request = new LoginRequest("nao-e-email", "a".repeat(100));

        assertThat(validator.validate(request))
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("email", "senha");
    }

    @Test
    void empresaRejeitaTagsNaRazaoSocial() {
        Empresa empresa = new Empresa();
        empresa.setRazaoSocial("<img src=x onerror=alert(1)>");
        empresa.setCnpj("12.345<script>");
        empresa.setTipo(TipoEmpresa.GERADORA);
        empresa.setEndereco("a".repeat(301));
        empresa.setEmail("email-invalido");
        empresa.setTelefone("telefone<script>");

        assertThat(validator.validate(empresa))
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("razaoSocial");
    }

    @Test
    void loteRejeitaScriptQuantidadeNegativaEQuantidadeGigante() {
        Lote lote = new Lote();
        lote.setDescricao("<script>alert(1)</script>");
        lote.setTipoResiduo("Quimico");
        lote.setQuantidade(new BigDecimal("123456789.123"));
        lote.setUnidade("KG");
        lote.setEmpresaGeradora(new Empresa());

        assertThat(validator.validate(lote))
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("descricao", "quantidade");

        lote.setDescricao("Residuo comum");
        lote.setQuantidade(new BigDecimal("-1"));

        assertThat(validator.validate(lote))
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("quantidade");
    }

    @Test
    void transporteRejeitaScriptEmResponsavelEObservacao() {
        Transporte transporte = new Transporte();
        transporte.setLote(new Lote());
        transporte.setTransportadora(new Empresa());
        transporte.setReceptora(new Empresa());
        transporte.setResponsavel("<script>alert(1)</script>");
        transporte.setObservacao("<b>teste</b>");

        assertThat(validator.validate(transporte))
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("responsavel", "observacao");
    }
}
