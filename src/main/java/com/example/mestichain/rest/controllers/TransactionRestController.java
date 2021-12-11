package com.example.mestichain.rest.controllers;

import com.example.mestichain.domain.Transaction;
import com.example.mestichain.domain.TransactionPool;
import com.example.mestichain.services.NodeService;
import com.example.mestichain.services.TransactionService;
import com.example.mestichain.utils.constants.Path;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;


@Slf4j
@RestController
@RequestMapping(Path.TRANSACTION)
public class TransactionRestController {

    private final TransactionService transactionService;
    private final NodeService nodeService;

    @Autowired
    public TransactionRestController(TransactionService transactionService, NodeService nodeService) {
        this.transactionService = transactionService;
        this.nodeService = nodeService;
    }

    /**
     * Obtener el pool de transacciones pendientes de ser incluidas en un bloque
     *
     * @return JSON pool de transacciones
     */
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody TransactionPool getTransaction() {
        var transactionPool = transactionService.getTransactionPool();
        log.info("Obteniendo pool de transacciones: {}", transactionPool);
        return transactionPool;
    }

    /**
     * Añadir una transaccion al pool
     *
     * @param transaction Transaccion a ser añadida
     * @param propagate   si la transacción debe ser propaga a otros nodos en la red
     * @param response    código 202 si la transacción es añadida al pool, 406 en otro caso
     */
    @PostMapping
    public void addTransaction(@RequestBody Transaction transaction, @RequestParam(required = false) Boolean propagate,
                               HttpServletResponse response) {
        log.info("Añadiendo transaccion al pool: {}", Base64.encodeBase64String(transaction.getHash()));
        boolean result = transactionService.add(transaction);

        if (result) {
            log.info("Transaccion añadida al pool: {}", Base64.encodeBase64String(transaction.getHash()));
            response.setStatus(HttpServletResponse.SC_ACCEPTED);

            if (propagate != null && propagate) {
                nodeService.broadcast(Path.TRANSACTION, transaction);
            }
        } else {
            log.info("Transaccion invalida, no añadida al pool: {}", Base64.encodeBase64String(transaction.getHash()));
            response.setStatus(HttpServletResponse.SC_NOT_ACCEPTABLE);
        }
    }

}
