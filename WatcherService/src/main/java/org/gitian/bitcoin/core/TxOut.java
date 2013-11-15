package org.gitian.bitcoin.core;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.google.bitcoin.core.ScriptException;
import com.google.bitcoin.core.TransactionOutput;
import com.google.bitcoin.core.Utils;
import com.yammer.dropwizard.json.JsonSnakeCase;

import java.math.BigInteger;

/**
 * @author devrandom
 */
@JsonSnakeCase
public class TxOut {
    private final String txHash;
    private final int outputIndex;
    private final TransactionOutput output;
    private final int confirmations;

    public TxOut(String txHash, int outputIndex, TransactionOutput output, int depthInBlocks) {
        this.txHash = txHash;
        this.outputIndex = outputIndex;
        this.output = output;
        this.confirmations = depthInBlocks;
    }

    @JsonGetter("tx_output_n")
    public int getOutputIndex() {
        return outputIndex;
    }

    public String getTxHash() {
        return txHash;
    }

    public String getScript() throws ScriptException {
        return Utils.bytesToHexString(output.getScriptPubKey().getProgram());
    }

    public BigInteger getValue() {
        return output.getValue();
    }

    public String getValueHex() {
        return output.getValue().toString(16);
    }

    public int getConfirmations() {
        return confirmations;
    }
}
