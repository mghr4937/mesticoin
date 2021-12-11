package com.example.mestichain.domain;

import lombok.Data;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Data
public class TransactionPool {

    private Set<Transaction> pool = new HashSet<>();

    /**
     * Añadir una transaccion al pool
     * @param transaction Transaccion a ser añadida
     * @return true si la transaccion es válida y es añadida al pool
     */
    public synchronized boolean add(Transaction transaction) {
        if (transaction.isValidTransaction()) {
            pool.add(transaction);
            return true;
        }
        return false;
    }

    /**
     * Eliminar una transaccion del pool
     * @param transaction Transaccion a eliminar
     */
    public void remove(Transaction transaction) {
        pool.remove(transaction);
    }

    /**
     * Comprobar si el pool contiene todas las transacciones de una lista de transacciones
     * @param transaction Lista de transacciones a comprobar
     * @return true si todas las transacciones de la coleccion están en el pool
     */
    public boolean contains(Collection<Transaction> transaction) {
        return pool.containsAll(transaction);
    }

    @Override
    public String toString() {
        return "TransactionPool{" +
                "pool=" + pool.toString() +
                '}';
    }
}
