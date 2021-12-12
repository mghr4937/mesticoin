package com.example.mestichain.rest.controllers;

import com.example.mestichain.domain.Block;
import com.example.mestichain.domain.Blockchain;
import com.example.mestichain.services.BlockService;
import com.example.mestichain.services.MinningService;
import com.example.mestichain.services.NodeService;
import com.example.mestichain.utils.constants.Path;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.Base64;

@Slf4j
@RestController
@RequestMapping(Path.BLOCK)
public class BlockRestController {

    private final BlockService blockService;
    private final NodeService nodeService;
    private final MinningService minningService;

    @Autowired
    public BlockRestController(BlockService blockService, NodeService nodeService, MinningService minningService) {
        this.blockService = blockService;
        this.nodeService = nodeService;
        this.minningService = minningService;
        minningService.start();
    }

    /**
     * Obtener la cadena de bloques
     *
     * @return JSON Lista de bloques
     */
    @GetMapping()
    public Blockchain getBlockChain() {
        log.info("Obteniendo cadena de bloques");
        return blockService.getBlockchain();
    }

    /**
     * Añadir un bloque a la cadena
     *
     * @param block     El bloque a ser añadido
     * @param propagate Si el bloque debe ser propagado al resto de nodos en la red
     * @param response  codigo 202 si el bloque es aceptado y añadido, código 406 en caso contrario
     */
    @PostMapping
    public void addBlock(@RequestBody Block block, @RequestParam(required = false) Boolean propagate,
                    HttpServletResponse response) throws Exception {
        log.info("Añadiendo bloque: {}", Base64.getEncoder().encodeToString(block.getHash()));
        if (blockService.add(block)) {
            response.setStatus(HttpServletResponse.SC_ACCEPTED);

            if (propagate != null && propagate) {
                nodeService.broadcast(Path.BLOCK, block);
            }
        } else {
            log.info("Bloque rechazado: {}", Base64.getEncoder().encodeToString(block.getHash()));
            response.setStatus(HttpServletResponse.SC_NOT_ACCEPTABLE);
        }

    }
}
