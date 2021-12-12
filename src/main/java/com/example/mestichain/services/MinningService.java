package com.example.mestichain.services;

import com.example.mestichain.domain.Block;
import com.example.mestichain.domain.RecordBalances;
import com.example.mestichain.domain.Transaction;
import com.example.mestichain.utils.constants.Path;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
public class MinningService implements Runnable {

    @Value("${coinbaseAddress}")
    private String coinbaseAddress;


    private final TransactionService transactionService;
    private final NodeService nodeService;
    private final BlockService blockService;

    @Autowired
    public MinningService(TransactionService transactionService, NodeService nodeService, BlockService blockService) {
        this.transactionService = transactionService;
        this.nodeService = nodeService;
        this.blockService = blockService;
    }

    private AtomicBoolean isMining = new AtomicBoolean(false);

    /**
     * Comenzar el servicio de minado
     */
    public void start() {
        if (isMining.compareAndSet(false, true)) {
            log.info("Starting minning service");
            Thread thread = new Thread(this);
            thread.start();
        }
    }

    /**
     * Parar el servicio de minado
     */
    public void stop() {
        if (isMining.compareAndSet(true, false)) {
            log.info("Stopping minning service");
        }
    }

    /**
     * Resetear el servicio de minado
     */
    public void reset() {
        log.info("Resetting minning service");
        this.stop();
        this.start();
    }

    /**
     * Busqueda de bloque valido y propagacion
     */
    @Override
    public void run() {
        while (isMining.get()) {
            Block block = this.mineBlock();
            if (block != null) {
                log.info("Block mined: {}", block);


                // Propagacion del bloque
                try {
                    this.blockService.add(block);
                    this.nodeService.broadcast(Path.BLOCK, block);
                } catch (Exception e) {
                    log.error("Error propagating block: {}", block, e);
                }
            }
        }
    }

    /**
     * Iterar nonce hasta que cumpla con la dificultad configurada
     */
    private Block mineBlock() {
        long nonce = 0;

        Block lastBlock = this.blockService.getBlockchain().getLastBlock();
        byte[] lastHash = lastBlock != null ? lastBlock.getHash() : null;

//      Saldos temporales para ver si una transaccion hace doble gasto
        RecordBalances tempBalances = new RecordBalances();
        RecordBalances actualBalances = this.blockService.getBlockchain().getBalances();
        Iterator<Transaction> iterator = this.transactionService.getTransactionPool().getPool().iterator();
        while (iterator.hasNext()) {
            Transaction transaction = iterator.next();
            if (actualBalances.isValidAccount(transaction.getSender())) {
                tempBalances.setAccountBalance(transaction.getSender(), actualBalances.getAccountBalance(transaction.getSender()));
            }
        }

        List<Transaction> transactions = new ArrayList<>();
        // iteramos las transacciones y las añadimos al bloque si el emisor tiene saldo
        iterator = this.transactionService.getTransactionPool().getPool().iterator();
        while (transactions.size() < this.blockService.getMaxTransactionsPerBlock() && iterator.hasNext()) {
            Transaction transaction = iterator.next();
            try {
                if (tempBalances.isValidAccount(transaction.getSender())) {
                    tempBalances.settleTransaction(transaction);
                } else {
                    throw new Exception("Invalid sender: " + Base64.encodeBase64String(transaction.getSender()));
                }
            } catch (Exception e) {
                log.error("Error validating transaction: {} - insufficient funds", Base64.encodeBase64String(transaction.getHash()), e);
            }
        }

        // añadir transaccion coinbase como recompensa por resolver la prueba de trabajo
        Transaction txCoinbase = new Transaction(Base64.decodeBase64(this.coinbaseAddress));
        txCoinbase.setTimestamp(System.currentTimeMillis());
        txCoinbase.setHash(txCoinbase.calculateHash());

        transactions.add(0, txCoinbase);

        // iterar nonce hasta encontrar solucion
        while (isMining.get()) {
            if (lastBlock != blockService.getBlockchain().getLastBlock()) {
                return null;
            }
            Block block = new Block(lastHash, transactions, nonce);
            if (block.getLeadingZeros() >= this.blockService.getDifficulty()) {
                return block;
            }
            nonce++;
        }
        return null;
    }
}
