package com.example.mestichain.domain;

import com.google.common.primitives.Longs;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;

/**
 * Un bloque está formado por una cabecera de bloque y un contenido.
 * La cabecera incluye:
 * 	- Hash del bloque calculado a partir del contenido de la cabecera
 * 	- Hash del bloque anterior (permite mantener la cadena enlazada)
 * 	- Timestamp
 * 	- Nonce Dificultad prueba de trabajo (en nuestro caso es constante asi que no lo incluimos por ahora).
 * 	- Raiz arbol de merkle
 * El contenido del bloque está formado por la lista de transacciones incluidas en dicho bloque.
 * */
@Slf4j
@Data
public class Block {

    private byte[] hash;
    private byte[] previousHash;
    private long timestamp;
    private long nonce;
    private byte[] merkleRoot;
    private List<Transaction> transactions;

    /**
     * Constructor de bloque
     * @param previousHash Hash del bloque anterior
     * @param transactions Lista de transacciones
     * @param nonce calculado como solución a la prueba de trabajo
     * */
    public Block(byte[] previousHash, List<Transaction> transactions, long nonce) {
        this.previousHash = previousHash;
        this.transactions = transactions;
        this.nonce = nonce;
        this.merkleRoot = this.calculateMerkleRoot();
        this.timestamp = System.currentTimeMillis();
        this.hash = this.calculateHash();
    }

    /**
     * Calcular el hash del bloque a partir de la información de la cabecera del bloque (sin transacciones)
     *
     * @return Hash SHA256
     */
    private byte[] calculateHash() {
        byte[] hashableData = ArrayUtils.addAll(this.previousHash, this.merkleRoot);
        hashableData = ArrayUtils.addAll(hashableData, Longs.toByteArray(nonce));
        hashableData = ArrayUtils.addAll(hashableData, Longs.toByteArray(timestamp));
        return DigestUtils.sha256(hashableData);
    }

    /**
     * Calcular la raiz del arbol de merkle formado con las transacciones
     * @return Hash SHA256
     */
     public byte[] calculateMerkleRoot() {
         Queue<byte[]> hashesQueue = this.transactions.stream().map(Transaction::getHash).collect(Collectors.toCollection(LinkedList::new));
         while (hashesQueue.size() > 1) {
//          calcular hash a partir de dos hashes previos
            byte[] info = ArrayUtils.addAll(hashesQueue.poll(), hashesQueue.poll());
//           añadir hash calculado a la cola
             hashesQueue.add(DigestUtils.sha256(info));
         }
         return hashesQueue.poll();
    }

    /**
     * Numero de ceros al principio del hash del bloque (para la prueba de trabajo)
     *
     * @return int number of leading zeros
     */
    public int getLeadingZeros() {
        for (int i = 0; i < this.hash.length; i++) {
            if (this.hash[i] != 0) {
                return i;
            }
        }
        return this.hash.length;
    }

    @Override
    public String toString() {
        return "Block{" +
                "\nhash=" + Base64.encodeBase64String(hash) +
                ",\npreviousHash=" + Base64.encodeBase64String(previousHash) +
                ",\ntimestamp=" + timestamp +
                ",\nnonce=" + nonce +
                ",\nmerkleRoot=" + Base64.encodeBase64String(merkleRoot) +
                ",\ntransactions=" + transactions +
                '}';
    }

    public boolean isValid() {
        if(this.hash == null) {
            log.info("Hash inválido");
            return false;
        }

        if(this.previousHash != null && this.nonce <= 0) {
            log.info("Nonce inválido");
            return false;
        }

        if(this.merkleRoot == null) {
            log.info("Merkle inválido");
            return false;
        }

        if(this.transactions == null || this.transactions.size() == 0) {
            log.info("Bloque sin transacciones");
            return false;
        }

        if(!this.transactions.get(0).isCoinbase()) {
            log.info("Primera transaccion no es coinbase");
            return false;
        }

        // la raiz del arbol de Merkle coincide
        if (!Arrays.equals(getMerkleRoot(), calculateMerkleRoot())) {
            log.info("Raiz Merkle inválida");
            return false;
        }

        // el hash de bloque coincide
        if (!Arrays.equals(getHash(), calculateHash())) {
            log.info("Hash bloque inválido");
            return false;
        }

        return true;
    }

}
