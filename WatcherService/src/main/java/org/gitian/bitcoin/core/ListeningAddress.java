package org.gitian.bitcoin.core;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigInteger;
import java.util.List;

/**
 * @author devrandom
 */
public class ListeningAddress {
    @JsonProperty
    private final String address;

    @JsonProperty
    private final List<String> addresses;

    public ListeningAddress(@JsonProperty("address") String address,
                            @JsonProperty("addresses") List<String> addresses) {
        this.address = address;
        this.addresses = addresses;
    }

    public String getAddress() {
        return address;
    }

    public List<String> getAddresses() {
        return addresses;
    }
}
