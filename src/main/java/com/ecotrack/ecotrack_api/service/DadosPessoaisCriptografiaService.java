package com.ecotrack.ecotrack_api.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Locale;

@Service
public class DadosPessoaisCriptografiaService {

    private static final String ALGORITMO = "AES/GCM/NoPadding";
    private static final String PREFIXO = "enc:";
    private static final int TAMANHO_IV = 12;
    private static final int TAMANHO_TAG_BITS = 128;

    private final SecureRandom secureRandom = new SecureRandom();
    private final SecretKeySpec chave;

    public DadosPessoaisCriptografiaService(@Value("${lgpd.crypto.secret:${jwt.secret}}") String secret) {
        this.chave = new SecretKeySpec(gerarChave(secret), "AES");
    }

    public String criptografar(String valor) {
        if (valor == null || valor.startsWith(PREFIXO)) {
            return valor;
        }

        try {
            byte[] iv = new byte[TAMANHO_IV];
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGORITMO);
            cipher.init(Cipher.ENCRYPT_MODE, chave, new GCMParameterSpec(TAMANHO_TAG_BITS, iv));

            byte[] valorCriptografado = cipher.doFinal(valor.getBytes(StandardCharsets.UTF_8));
            ByteBuffer buffer = ByteBuffer.allocate(iv.length + valorCriptografado.length);
            buffer.put(iv);
            buffer.put(valorCriptografado);

            return PREFIXO + Base64.getUrlEncoder().withoutPadding().encodeToString(buffer.array());
        } catch (Exception e) {
            throw new IllegalStateException("Erro ao criptografar dados pessoais", e);
        }
    }

    public String descriptografar(String valor) {
        if (valor == null || !valor.startsWith(PREFIXO)) {
            return valor;
        }

        try {
            byte[] conteudo = Base64.getUrlDecoder().decode(valor.substring(PREFIXO.length()));
            ByteBuffer buffer = ByteBuffer.wrap(conteudo);
            byte[] iv = new byte[TAMANHO_IV];
            buffer.get(iv);
            byte[] valorCriptografado = new byte[buffer.remaining()];
            buffer.get(valorCriptografado);

            Cipher cipher = Cipher.getInstance(ALGORITMO);
            cipher.init(Cipher.DECRYPT_MODE, chave, new GCMParameterSpec(TAMANHO_TAG_BITS, iv));
            return new String(cipher.doFinal(valorCriptografado), StandardCharsets.UTF_8);
        } catch (Exception e) {
            return valor;
        }
    }

    public String hashBusca(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }

        try {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                    .digest(valor.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexadecimal = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                hexadecimal.append(String.format("%02x", b));
            }
            return hexadecimal.toString();
        } catch (Exception e) {
            throw new IllegalStateException("Erro ao gerar hash de busca", e);
        }
    }

    public String normalizarEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    public String normalizarCnpj(String cnpj) {
        return cnpj == null ? null : cnpj.replaceAll("\\D", "");
    }

    private byte[] gerarChave(String secret) {
        try {
            return MessageDigest.getInstance("SHA-256")
                    .digest(secret.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new IllegalStateException("Erro ao preparar chave de criptografia", e);
        }
    }
}
