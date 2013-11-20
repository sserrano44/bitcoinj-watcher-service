== Running

Compile and install bitcoinj, at top level:

    mvn package  # optionally skip tests with -DskipTests

Run:

    cd WatcherService
    java -jar target/watcher-service-0.11-SNAPSHOT.jar prodnet server config.yml

Files created:

    watcher-service.spvchain
    watcher-service.wallet
