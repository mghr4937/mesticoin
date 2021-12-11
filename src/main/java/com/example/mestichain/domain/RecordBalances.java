package com.example.mestichain.domain;

import lombok.Data;
import org.apache.commons.codec.binary.Base64;

import java.util.Enumeration;
import java.util.Hashtable;

/*
 * La cadena de bloques es esencialmente una lista de bloques enlazados ya que cada bloque tiene el identificador del bloque anterior.
 * */
@Data
public class RecordBalances {

    private Hashtable<String, Long> balances = new Hashtable<>();


    public Long getAccountBalance(byte[] address) {
        return (this.balances.getOrDefault(this.getAddressAsString(address), 0L));
    }

    public void setAccountBalance(byte[] address, Long balance) {
        this.balances.put(this.getAddressAsString(address), balance);
    }

    public void addAccountBalance(byte[] address, Long balance) {
        this.balances.put(this.getAddressAsString(address), this.getAccountBalance(address) + balance);
    }

    public void settleTransaction(Transaction transaction) throws Exception {
        if(transaction.isCoinbase()){
            this.addAccountBalance(transaction.getSender(), transaction.getAmount());
        } else {
            if(this.getAccountBalance(transaction.getSender()) >= transaction.getAmount()) {
                this.addAccountBalance(transaction.getSender(), -transaction.getAmount());
                this.addAccountBalance(transaction.getRecipient(), transaction.getAmount());
            } else {
                throw new Exception("Saldo Insuficiente");
            }
        }
    }

    private String getAddressAsString(byte[] address) {
        return Base64.encodeBase64String(address);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        Enumeration<String> accounts = this.balances.keys();
        sb.append("CLAVE PUBLICA | SALDO\n");
        sb.append("--------------------------------\n");
        while (accounts.hasMoreElements()) {
            String account = accounts.nextElement();
            sb.append(account).append(" | ").append(this.balances.get(account)).append("\n");
            if (accounts.hasMoreElements()) sb.append("\n");
        }
        return sb.toString();
    }
}
