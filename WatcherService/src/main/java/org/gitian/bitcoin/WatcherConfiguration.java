package org.gitian.bitcoin;

/**
 * @author devrandom
 */

import com.fasterxml.jackson.annotation.JsonProperty;
import com.yammer.dropwizard.config.Configuration;

import org.hibernate.validator.constraints.NotEmpty;

public class WatcherConfiguration extends Configuration {
    @JsonProperty
    private String filePrefix;

    public String getFilePrefix() {
        return filePrefix;
    }
}