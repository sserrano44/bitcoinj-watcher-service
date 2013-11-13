package org.gitian.bitcoin.core;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigInteger;

/**
 * @author devrandom
 */
public class ListeningAddress {
    @JsonProperty
    private final String address;

    public ListeningAddress(@JsonProperty("address") String address) {
        this.address = address;
    }

    public String getAddress() {
        return address;
    }
}
