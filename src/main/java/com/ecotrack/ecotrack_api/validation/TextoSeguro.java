package com.ecotrack.ecotrack_api.validation;

import com.ecotrack.ecotrack_api.exception.RegraNegocioException;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;

public final class TextoSeguro {

    private static final List<String> TERMOS_OFENSIVOS = List.of(
            "caralho",
            "porra",
            "merda",
            "puta",
            "foda",
            "bosta",
            "desgracado",
            "idiota",
            "imbecil"
    );

    private TextoSeguro() {
    }

    public static void validar(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            return;
        }

        if (valor.contains("<") || valor.contains(">")) {
            throw new RegraNegocioException(campo + " nao pode conter tags HTML ou scripts");
        }

        if (valor.chars().anyMatch(TextoSeguro::caractereControleInvalido)) {
            throw new RegraNegocioException(campo + " contem caracteres de controle invalidos");
        }

        String normalizado = normalizar(valor);
        boolean ofensivo = TERMOS_OFENSIVOS.stream().anyMatch(normalizado::contains);
        if (ofensivo) {
            throw new RegraNegocioException(campo + " contem conteudo ofensivo ou inadequado");
        }
    }

    private static boolean caractereControleInvalido(int caractere) {
        return Character.isISOControl(caractere)
                && caractere != '\n'
                && caractere != '\r'
                && caractere != '\t';
    }

    private static String normalizar(String valor) {
        String semAcentos = Normalizer.normalize(valor, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return semAcentos.toLowerCase(Locale.ROOT);
    }
}
