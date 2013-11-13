package org.gitian.bitcoin.core;

import java.math.BigInteger;

/**
 * @author devrandom
 */
public class Balance {
    private final BigInteger amount;
    public Balance(BigInteger amount) {
        this.amount = amount;
    }

    public BigInteger getAmount() {
        return amount;
    }
}
