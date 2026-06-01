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

@Service
public class DadosPessoaisCriptografiaService {

    private static final String ALGORITMO = "AES/GCM/NoPadding";
    private static final int TAMANHO_IV = 12;
    private static final int TAMANHO_TAG_BITS = 128;

    private final SecureRandom secureRandom = new SecureRandom();
    private final SecretKeySpec chave;

    public DadosPessoaisCriptografiaService(@Value("${lgpd.crypto.secret:${jwt.secret}}") String secret) {
        this.chave = new SecretKeySpec(gerarChave(secret), "AES");
    }

    public String criptografar(String valor) {
        try {
            byte[] iv = new byte[TAMANHO_IV];
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGORITMO);
            cipher.init(Cipher.ENCRYPT_MODE, chave, new GCMParameterSpec(TAMANHO_TAG_BITS, iv));

            byte[] valorCriptografado = cipher.doFinal(valor.getBytes(StandardCharsets.UTF_8));
            ByteBuffer buffer = ByteBuffer.allocate(iv.length + valorCriptografado.length);
            buffer.put(iv);
            buffer.put(valorCriptografado);

            return Base64.getUrlEncoder().withoutPadding().encodeToString(buffer.array());
        } catch (Exception e) {
            throw new IllegalStateException("Erro ao criptografar dados pessoais", e);
        }
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
