package com.example.mestichain.services;

import com.example.mestichain.domain.Transaction;
import com.example.mestichain.domain.TransactionPool;
import com.example.mestichain.utils.constants.Path;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URL;
import java.util.Collection;

@Slf4j
@Service
@Data
public class TransactionService {

    // Pool de transacciones con transacciones pendientes de ser incluidas en un bloque
    private TransactionPool transactionPool = new TransactionPool();

    @Autowired
    public TransactionService() {
    }

    /**
     * Añadir transaccion al pool
     *
     * @param transaction Transaccion a ser añadida
     * @return true si la transaccion es valida y es añadida al pool
     */
    public boolean add(Transaction transaction) {
        return transactionPool.add(transaction);
    }

    /**
     * Eliminar una transacción del pool
     *
     * @param transaction Transaccion a ser eliminada
     */
    public void remove(Transaction transaction) {
        transactionPool.remove(transaction);
    }

    /**
     * Comprobar si el pool contiene una lista de transacciones
     *
     * @param transactions Transacciones a ser verificadas
     * @return true si todas las transacciones están en el pool
     */
    public boolean contains(Collection<Transaction> transactions) {
        return transactionPool.contains(transactions);
    }

    /**
     * Descargar pool de transacciones desde otro nodo
     *
     * @param nodeUrl Nodo al que pedir las transacciones
     * @param restTemplate RestTemplate a usar
     */
    public void getTransactionPool(URL nodeUrl, RestTemplate restTemplate) {
        this.transactionPool = restTemplate.getForObject(nodeUrl.toString() + Path.TRANSACTION,
                TransactionPool.class);
    }


}
