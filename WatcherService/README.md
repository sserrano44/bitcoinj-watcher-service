Running
=======

Compile and install bitcoinj, at top level:

        mvn package  # optionally skip tests with -DskipTests

Run:

        cd WatcherService
        java -jar target/watcher-service-0.11-SNAPSHOT.jar prodnet server config.yml

Files created:

        watcher-service.spvchain
        watcher-service.wallet

JSON RPC Quickstart
===================

Dump the wallet:

        curl http://localhost:8080/address/debug

The first JSON RPC call adds the address to the wallet:

        echo '{"address": "1A5JXpQ4gUvV9rD8V4EWeV4rmnBiPvZbj7"}' | curl -T - -H 'Content-Type: application/json' http://localhost:8080/address/

This one checks the balance:  

        curl http://localhost:8080/address/1A5JXpQ4gUvV9rD8V4EWeV4rmnBiPvZbj7/balance

Note that it doesn't scan the blockchain for times before the address was added to the wallet - yet.

This one gets unspent outputs.

        curl http://localhost:8080/address/1A5JXpQ4gUvV9rD8V4EWeV4rmnBiPvZbj7/unspent

Post a transaction in hex: 
        echo '{"bytes": "023fed..."}' | curl -T - -H 'Content-Type: application/json' http://localhost:8080/transaction

To change running port edit the config.yml and add: 

        http:
          port: 6666

To activate the IPN (Instant Payment Notification) add:

        IPN: http://UrlToYourService.com/foo/bar

