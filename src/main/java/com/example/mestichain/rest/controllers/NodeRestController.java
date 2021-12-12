package com.example.mestichain.rest.controllers;

import com.example.mestichain.services.NodeService;
import com.example.mestichain.utils.constants.Path;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URL;
import java.util.Set;


@Slf4j
@RestController
@RequestMapping(Path.NODE)
public class NodeRestController {

    private final NodeService nodeService;

    @Autowired
    public NodeRestController(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    /**
     * Obtener la lista de nodos vecinos en la red
     *
     * @return JSON lista de URLs
     */
    @GetMapping()
    public Set<URL> getParentNodes() {
        log.info("Obteniendo nodos vecinos");
        var parentNodes = nodeService.getParentNodes();
        log.info("Nodos vecinos: {}", parentNodes);
        return parentNodes;
    }

    /**
     * Dar de alta un nodo en la red
     *
     * @param urlNode a ser dado de alta
     */
    @PostMapping()
    public void addNode(@RequestBody URL urlNode, HttpServletResponse response) {
        log.info("Alta de nodo {}", urlNode);
        nodeService.registerNode(urlNode);
        response.setStatus(HttpServletResponse.SC_OK);
    }

    /**
     * Dar de baja a un nodo en la red
     *
     * @param urlNodo a ser dado de baja
     */
    @DeleteMapping()
    public void removeNode(@RequestBody URL urlNodo, HttpServletResponse response) {
        log.info("Baja de nodo {}", urlNodo);
        nodeService.removeNode(urlNodo);
        response.setStatus(HttpServletResponse.SC_OK);
    }

    /**
     * Endpoint auxiliar para que un nodo pueda conocer su IP publica y con la que otros nodos se comunicarán con él
     *
     * @param request HttpServletRequest
     * @return la IP publica
     */
    @GetMapping(Path.NODE_IP)
    public String getIp(HttpServletRequest request) {
        var remoteAddr = request.getRemoteAddr();
        log.info("Obteniendo IP publica: {}", remoteAddr);
        return remoteAddr;
    }

}
