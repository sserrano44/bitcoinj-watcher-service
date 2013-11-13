package org.gitian.bitcoin.core;

/**
 * @author devrandom
 */
public class TxOut {
    private final String txid;
    private final int output;

    public TxOut(String txid, int output) {
        this.txid = txid;
        this.output = output;
    }

    public int getOutput() {
        return output;
    }

    public String getTxid() {
        return txid;
    }
}
