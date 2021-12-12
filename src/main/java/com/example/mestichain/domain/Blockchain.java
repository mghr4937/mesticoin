package com.example.mestichain.domain;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/*
 * La cadena de bloques es esencialmente una lista de bloques enlazados ya que cada bloque tiene el identificador del
 * bloque anterior.
 * */
@Slf4j
@Data
public class Blockchain {

    //Lista de bloques en la cadena ordenados por altura (posición en la cadena)
    private List<Block> blocks = new ArrayList<>();
    //Saldos actuales de las cuentas
    private RecordBalances balances = new RecordBalances();

    public boolean isEmpty() {
        return this.blocks == null || this.blocks.isEmpty();
    }

    public int getSize() {
        return (isEmpty() ? 0 : this.blocks.size());
    }

    /**
     * Obtener el ultimo bloque en la cadena
     *
     * @return Ultimo bloque de la cadena
     */
    public Block getLastBlock() {
        if (isEmpty()) {
            return null;
        }
        return this.blocks.get(this.blocks.size() - 1);
    }

    /**
     * Añadir un bloque a la cadena
     *
     * @param block a ser añadido
     */
    public void add(Block block) throws Exception {
        //iteramos y procesamos las transacciones. Si esto es correcto lo añadimos a la cadena
        for (Transaction transaction : block.getTransactions()) {
            //actualizar saldos
            balances.settleTransaction(transaction);
        }
        this.blocks.add(block);
        log.info(balances.toString() + "\n");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        Blockchain cadena = (Blockchain) o;

        if (blocks.size() != cadena.getBlocks().size())
            return false;

        for (int i = 0; i < blocks.size(); i++) {
            if (blocks.get(i) != cadena.getBlocks().get(i))
                return false;
        }

        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Block block : this.blocks) {
            sb.append(block.toString());
        }
        return sb.toString();
    }

}
