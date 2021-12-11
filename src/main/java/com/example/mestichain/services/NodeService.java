package com.example.mestichain.services;

import com.example.mestichain.utils.constants.Path;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.context.ServletWebServerInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PreDestroy;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;


@Slf4j
@Service
@Data
public class NodeService implements ApplicationListener<ServletWebServerInitializedEvent> {

    @Value("${masterNodeUrl}")
    private String masterNodeUrl;

    //URL de mi nodo (host + port)
    private URL myUrlNode;

    //Nodos en la red
    private Set<URL> parentNodes = new HashSet<>();

    private final BlockService blockService;
    private final TransactionService transactionService;

    private RestTemplate restTemplate = new RestTemplate();

    @Autowired
    public NodeService(BlockService blockService, TransactionService transactionService) {
        this.blockService = blockService;
        this.transactionService = transactionService;
    }

    /**
     * Al iniciar el nodo tenemos que: - Obtener la lista de nodos en la red -
     * Obtener la cadena de bloques - Obtener transactiones en el pool - Dar de alta
     * mi nodo en el resto de nodos
     *
     * @param servletWebServerInitializedEvent WebServer para obtener el puerto
     */
    @Override
    public void onApplicationEvent(ServletWebServerInitializedEvent servletWebServerInitializedEvent) {

        //Obtener la url del Nodo master
        URL masterNodeUrl = this.getMasterNode();

        //calcular mi url (host + port)
        String host = getPublicIp(masterNodeUrl, restTemplate);
        int port = servletWebServerInitializedEvent.getWebServer().getPort();

        this.myUrlNode = getMyUrlNode(host, port);

        //obtener cadena de bloques y transacciones en pool si no soy nodo master
        if (this.myUrlNode.equals(masterNodeUrl)) {
            log.info("Ejecutando nodo master");


        } else {
            log.info("Ejecutando nodo normal");
            parentNodes.add(masterNodeUrl);

            // obtener lista de nodos, bloques y transacciones
            getParentNodes(Objects.requireNonNull(masterNodeUrl), restTemplate);
            blockService.getBlockchain(masterNodeUrl, restTemplate);
            transactionService.getTransactionPool(masterNodeUrl, restTemplate);

            // dar de alta mi nodo en el resto de nodos en la red
            this.broadcast(Path.NODE, myUrlNode);
        }
    }

    /**
     * Construir mi url a partir de mi host y puerto
     *
     * @param host Mi host publico
     * @param port Puerto en el que se lanza el servicio
     */
    public URL getMyUrlNode(String host, int port) {
        try {
            return new URL("http", host, port, "");
        } catch (MalformedURLException e) {
            System.out.println("URL del nodo invalida:" + e);
            return null;
        }
    }

    /**
     * Obtener la lista de nodos en la red
     *
     * @param masterNodeUrl Nodo vecino al que hacer la peticion
     * @param restTemplate  RestTemplate a usar
     */
    public void getParentNodes(URL masterNodeUrl, RestTemplate restTemplate) {
        var nodes = restTemplate.getForObject(masterNodeUrl.toString() + Path.NODE, Set.class);
        if (nodes != null) this.parentNodes.addAll(nodes);
    }

    /**
     * Enviar petición de tipo POST al resto de nodos en la red (nodos vecinos)
     *
     * @param endpoint el endpoint para esta petición
     * @param data     los datos que se quieren enviar con la peticion
     */
    public void broadcast(String endpoint, Object data) {
        parentNodes.parallelStream().forEach(urlNode -> restTemplate.postForLocation(urlNode.toString() + endpoint, data));
    }

    /**
     * Dar de baja el nodo del resto de nodos antes de pararlo completamente
     */
    @PreDestroy
    public void shutdown() {
        log.info("Dando de baja nodo {}", this.myUrlNode);
        sendDeleteRequestParentNodes("/node", this.myUrlNode);
    }

    /**
     * Enviar petición de tipo DELETE al resto de nodos en la red (nodos vecinos)
     *
     * @param endpoint el endpoint para esta petición
     * @param data     los datos que se quieren enviar con la peticion
     */
    public void sendDeleteRequestParentNodes(String endpoint, Object data) {
        parentNodes.parallelStream().forEach(urlNode -> restTemplate.delete(urlNode + endpoint, data));
    }

    /**
     * Obtener la IP publica con la que me conecto a la red
     *
     * @param parentNodeUrl Nodo vecino al que hacer la peticion
     * @param restTemplate  RestTemplate a usar
     */
    private String getPublicIp(URL parentNodeUrl, RestTemplate restTemplate) {
        return restTemplate.getForObject(parentNodeUrl + Path.NODE + Path.NODE_IP, String.class);
    }

    /**
     * Obtener URL del nodo master del archivo de configuracion
     */
    private URL getMasterNode() {
        try {

            return new URL(masterNodeUrl);
        } catch (MalformedURLException e) {
            log.error("Error al obtener la URL del nodo master", e);
            return null;
        }
    }

    /**
     * Dar de alta un nodo
     */
    public synchronized void registerNode(URL nodeUrl) {
        parentNodes.add(nodeUrl);
    }

    /**
     * Dar de baja un nodo
     */
    public synchronized void removeNode(URL nodeUrl) {
        parentNodes.remove(nodeUrl);
    }


}
