package com.example.mestichain.services;

import com.example.mestichain.domain.Block;
import com.example.mestichain.domain.Blockchain;
import com.example.mestichain.utils.constants.Path;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URL;
import java.util.Arrays;

@Slf4j
@Service
@Data
public class BlockService {

    private final TransactionService transactionService;
    private Blockchain blockchain = new Blockchain();

    @Value("${maxTransactionsPerBlock}")
    private int maxTransactionsPerBlock;
    @Value("${difficulty}")
    private int difficulty;

    @Autowired
    public BlockService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    /**
     * Añadir un bloque a la cadena
     *
     * @param block Bloque a ser añadido
     * @return true si el bloque pasa la validacion y es añadida a la cadena
     */
    public synchronized boolean add(Block block) {
        if (validate(block)) {
            this.blockchain.getBlocks().add(block);
            //eliminar las transacciones incluidas en el bloque del pool de transacciones
            block.getTransactions().forEach(transactionService::remove);
            return true;
        }
        return false;
    }

    /**
     * Validar un bloque a ser añadido a la cadena
     *
     * @param block Bloque a ser validado
     */
    private boolean validate(Block block) {
        // comprobar que el bloque tiene un formato valido
        if(!block.isValid()) {
            log.error("El bloque no es valido: {}", block);
            return false;
        }

        //el hash de bloque anterior hace referencia al ultimo bloque en mi cadena
        if(!this.blockchain.isEmpty()) {
            byte[] lastBlockHash = this.blockchain.getLastBlock().getHash();
            if(!Arrays.equals(block.getPreviousHash(), lastBlockHash)) {
                log.error("El bloque anterior no coincide con el ultimo bloque de la cadena: {}", block);
                return false;
            }
        } else {
            if(block.getPreviousHash() != null) {
                log.error("El bloque anterior no coincide con el ultimo bloque de la cadena: {}", block);
                return false;
            }
        }

        //max numero de bloques en la cadena
        if(block.getTransactions().size() > maxTransactionsPerBlock) {
            log.error("El bloque tiene mas transacciones de las permitidas: {}", block);
            return false;
        }

        //verificar que todas las transacciones estaban en mi pool
        if(!transactionService.contains(block.getTransactions())) {
            log.error("Alguna de las transacciones del bloque no esta en el pool: {}", block);
            return false;
        }

        //la dificultad coincide
        if(block.getLeadingZeros() < difficulty) {
            log.error("La dificultad del bloque no coincide: {}", block);
            return false;
        }

        return true;
    }

    /**
     * Descargar la cadena de bloques de otro nodo
     *
     * @param nodeUrl      Url del nodo al que enviar la peticion
     * @param restTemplate RestTemplate a usar
     */
    public void getBlockchain(URL nodeUrl, RestTemplate restTemplate) {
        this.blockchain = restTemplate.getForObject(nodeUrl.toString() + Path.BLOCKCHAIN, Blockchain.class);
        log.info("Cadena de bloques descargada de nodo: {}", nodeUrl);
    }
}
