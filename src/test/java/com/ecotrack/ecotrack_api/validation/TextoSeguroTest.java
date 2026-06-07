package com.ecotrack.ecotrack_api.validation;

import com.ecotrack.ecotrack_api.exception.RegraNegocioException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TextoSeguroTest {

    @Test
    void permiteTextoOperacionalValido() {
        assertThatCode(() -> TextoSeguro.validar("Residuo quimico Classe I - coleta urgente", "Descricao"))
                .doesNotThrowAnyException();
    }

    @Test
    void rejeitaHtmlOuScript() {
        assertThatThrownBy(() -> TextoSeguro.validar("<script>alert(1)</script>", "Descricao"))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("HTML");
    }

    @Test
    void rejeitaCaracteresDeControle() {
        assertThatThrownBy(() -> TextoSeguro.validar("texto\u0000invalido", "Descricao"))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("controle");
    }

    @Test
    void rejeitaConteudoOfensivo() {
        assertThatThrownBy(() -> TextoSeguro.validar("empresa merda", "Razao social"))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("ofensivo");
    }
}
