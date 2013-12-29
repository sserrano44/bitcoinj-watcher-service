package org.gitian.bitcoin;

/**
 * Copyright 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.google.bitcoin.core.*;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.bitcoin.core.NetworkParameters;
import com.google.bitcoin.kits.WalletAppKit;
import com.google.bitcoin.params.MainNetParams;
import com.google.bitcoin.params.RegTestParams;
import com.google.bitcoin.params.TestNet3Params;
import com.google.bitcoin.utils.BriefLogFormatter;
import com.google.common.collect.Lists;
import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Environment;

import org.gitian.bitcoin.resources.AddressResource;
import org.gitian.bitcoin.resources.TransactionResource;

import java.math.BigInteger;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;


/**
 * ForwardingService demonstrates basic usage of the library. It sits on the network and when it receives coins, simply
 * sends them onwards to an address given on the command line.
 */
public class WatcherService extends Service<WatcherConfiguration> {
    private WalletAppKit kit;
    private final NetworkParameters params;
    private String filePrefix;

    public WatcherService(NetworkParameters params, String filePrefix) {
        this.params = params;
        this.filePrefix = filePrefix;
    }

    public static void main(String[] args) throws Exception {
        // This line makes the log output more compact and easily read, especially when using the JDK log adapter.
        BriefLogFormatter.init();
        if (args.length < 1) {
            System.err.println("Usage: [regtest|testnet]");
            return;
        }

        // Figure out which network we should connect to. Each one gets its own set of files.
        NetworkParameters params;
        String filePrefix;
        ArrayList<String> argsList = Lists.newArrayList(args);
        String net = argsList.remove(0);
        args = argsList.toArray(new String[argsList.size()]);

        if (net.equals("testnet")) {
            params = TestNet3Params.get();
            filePrefix = "watcher-service-testnet";
        } else if (net.equals("regtest")) {
            params = RegTestParams.get();
            filePrefix = "watcher-service-regtest";
        } else {
            params = MainNetParams.get();
            filePrefix = "watcher-service";
        }

        new WatcherService(params, filePrefix).run(args);

        try {
            Thread.sleep(Long.MAX_VALUE);
        } catch (InterruptedException ignored) {}
    }

    @Override
    public void initialize(Bootstrap<WatcherConfiguration> bootstrap) {
        bootstrap.setName("watcher");
    }

    @Override
    public void run(WatcherConfiguration configuration, Environment environment) throws Exception {
        if (configuration.getFilePrefix() != null) {
            filePrefix = configuration.getFilePrefix();
        }
        kit = new WalletAppKit(params, new File("."), filePrefix);

        if (params == RegTestParams.get()) {
            // Regression test mode is designed for testing and development only, so there's no public network for it.
            // If you pick this mode, you're expected to be running a local "bitcoind -regtest" instance.
            kit.connectToLocalHost();
        }

        InputStream checkpointsStream = WatcherService.class.getResourceAsStream("/watcherservice/checkpoints");
        kit.setCheckpoints(checkpointsStream);


        // Download the block chain and wait until it's done.
        kit.startAndWait();

        if (configuration.getMaxConnections() > 0)
            kit.peerGroup().setMaxConnections(configuration.getMaxConnections());

        environment.addResource(new AddressResource(kit.wallet()));
        environment.addResource(new TransactionResource(kit.peerGroup(), kit.wallet()));
        environment.getObjectMapperFactory().enable(SerializationFeature.INDENT_OUTPUT);
                
        if (configuration.getIPN() != null) {
        	//TODO: check is a valid URL
            System.out.println("\n #### IPN ACTIVE: " + configuration.getIPN() + " \n");
        	kit.wallet().addEventListener(new IPNWalletEventListener(configuration.getIPN()));
        }
        
    }
}
