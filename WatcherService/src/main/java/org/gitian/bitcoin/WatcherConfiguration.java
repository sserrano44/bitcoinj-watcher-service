package org.gitian.bitcoin;

/**
 * @author devrandom
 */

import com.fasterxml.jackson.annotation.JsonProperty;
import com.yammer.dropwizard.config.Configuration;


public class WatcherConfiguration extends Configuration {
    @JsonProperty
    private String filePrefix;

    public String getFilePrefix() {
        return filePrefix;
    }

    @JsonProperty
    private int maxConnections = 0;

    @JsonProperty
    private String IPN = null;
    
    public int getMaxConnections() {
        return maxConnections;
    }
    
    public String getIPN() {
        return IPN;
    }
}