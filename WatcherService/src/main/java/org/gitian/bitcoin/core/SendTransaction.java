package org.gitian.bitcoin.core;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.spongycastle.util.encoders.Hex;

/**
 * @author devrandom
 */
public class SendTransaction {
    @JsonProperty("bytes")
    private final String hexBytes;

    public SendTransaction(@JsonProperty("bytes") String hexBytes) {
        this.hexBytes = hexBytes;
    }

    public String getHexBytes() {
        return hexBytes;
    }

    public byte[] getBytes() {
        return Hex.decode(hexBytes);
    }
}
