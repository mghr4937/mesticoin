package com.example.mestichain.domain;

import com.example.mestichain.utils.SignatureUtils;
import com.google.common.primitives.Longs;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Value;


import java.util.Arrays;
import java.util.Date;

/**
 * La información principal en una transacción incluye:
 * - Hash de la transacción
 * - El emisor
 * - El destinatario
 * - La cantidad a ser transferida
 * - El timestamp de cuándo fue creada
 * - La firma con la clave privada del emisor
 */
@Slf4j
@Data
public class Transaction {

    @Value("${coinbaseAmount}")
    private long coinbaseAmount;

    private byte[] hash;
    private byte[] sender;
    private byte[] recipient;
    private long amount;
    private byte[] signature;
    private long timestamp;

    private boolean isCoinbase;

    public Transaction() {
    }

    public Transaction(byte[] sender, byte[] recipient, long amount) {
        this.isCoinbase = false;
        this.sender = sender;
        this.recipient = recipient;
        this.amount = amount;
        this.timestamp = System.currentTimeMillis();
        this.hash = calculateHash();
    }

    public Transaction(byte[] recipient) {
        this.recipient = recipient;
        this.isCoinbase = true;
        this.timestamp = System.currentTimeMillis();
        this.hash = this.calculateHash();
        this.amount = coinbaseAmount;
    }

    /**
     * El contenido de la transaccion que es firmado por el emisor con su clave privada
     *
     * @return byte[] Array de bytes representando el contenido de la transaccion
     */
    public byte[] getContent() {
        byte[] content = ArrayUtils.addAll(String.valueOf(amount).getBytes());
        content = ArrayUtils.addAll(content, this.sender);
        content = ArrayUtils.addAll(content, this.recipient);
        content = ArrayUtils.addAll(content, Longs.toByteArray(this.timestamp));
        return content;
    }

    /**
     * Calcular el hash del contenido de la transacción y que pasa a ser el identificador de la transacción
     *
     * @return Hash SHA256
     */
    public byte[] calculateHash() {
        return DigestUtils.sha256(getContent());
    }

    /**
     * Comprobar si una transacción es válida
     *
     * @return true si tiene un hash válido y la firma es válida
     */
    public boolean isValidTransaction() {
        if (this.recipient == null) {
            log.error("Destinatario inválido");
            return false;
        }

        if (this.amount < 0) {
            log.error("Cantidad inválida");
            return false;
        }

        if (this.signature == null) {
            log.error("Firma inválida");
            return false;
        }

        // verificar hash
        if (!Arrays.equals(getHash(), calculateHash())) {
            log.error("Hash inválido");
            return false;
        }

        // no coinbase tx
        if (!this.isCoinbase) {
            if (this.sender == null) {
                log.error("Emisor inválido");
                return false;
            }

            // verificar firma
            try {
                if (!SignatureUtils.validateSignature(this.getContent(), this.signature, this.sender))
                    return false;

            } catch (Exception e) {
                return false;
            }
        }

        return true;
    }

    @Override
    public String toString() {
        return "{\nHash: " + Base64.encodeBase64String(this.hash) + ",\nEmisor: " + Base64.encodeBase64String(this.sender) + ",\nDestinatario: "
                + Base64.encodeBase64String(this.recipient) + ",\nCantidad: " + this.amount + ",\nFirma: " + Base64.encodeBase64String(this.signature)
                + ",\nTimestamp: " + new Date(this.timestamp) + "\n}";
    }
}



