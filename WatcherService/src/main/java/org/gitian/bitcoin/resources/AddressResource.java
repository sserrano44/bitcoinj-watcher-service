package org.gitian.bitcoin.resources;

/**
 * @author devrandom
 */

import com.google.bitcoin.core.Address;
import com.google.bitcoin.core.AddressFormatException;
import com.google.bitcoin.core.ECKey;
import com.google.bitcoin.core.NetworkParameters;
import com.google.bitcoin.core.ScriptException;
import com.google.bitcoin.core.Transaction;
import com.google.bitcoin.core.TransactionOutput;
import com.google.bitcoin.core.Utils;
import com.google.bitcoin.core.Wallet;
import com.google.bitcoin.wallet.CoinSelection;
import com.google.bitcoin.wallet.CoinSelector;
import com.google.common.collect.Lists;
import com.yammer.metrics.annotation.Timed;

import org.gitian.bitcoin.core.Balance;
import org.gitian.bitcoin.core.ListeningAddress;
import org.gitian.bitcoin.core.TxOut;

import java.math.BigInteger;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/address")
@Produces(MediaType.APPLICATION_JSON)
public class AddressResource {
    public static final int DEFAULT_LOOKBACK_SECONDS = 3600 * 24 * 7;
    private final Wallet wallet;

    public AddressResource(Wallet wallet) {
        this.wallet = wallet;
    }

    @GET @Path("/{address}/balance")
    @Timed
    public Balance balance(@PathParam("address")final String address) {
        return new Balance(wallet.getBalance(new MyCoinSelector(address)));
    }

    @GET @Path("/{address}/unspent")
    @Timed
    public List<TxOut> unspent(@PathParam("address")final String address) {
        LinkedList<TransactionOutput> candidates = wallet.calculateAllSpendCandidates(true);
        CoinSelection selection =
                new MyCoinSelector(address).select(NetworkParameters.MAX_MONEY, candidates);
        List<TxOut> outputs = Lists.newArrayList();
        for (TransactionOutput output : selection.gathered) {
            Transaction tx = output.getParentTransaction();
            int index = tx.getOutputs().indexOf(output);
            outputs.add(new TxOut(tx.getHashAsString(), index));
        }
        return outputs;
    }

    @GET @Path("/{pubkey}/addpubkey")
    @Timed
    public boolean addPubkey(@PathParam("pubkey")String pubkeyString) {
        byte[] pubkey = Utils.parseAsHexOrBase58(pubkeyString);
        ECKey key = new ECKey(null, pubkey);
        key.setCreationTimeSeconds(System.currentTimeMillis()/1000 - DEFAULT_LOOKBACK_SECONDS);
        return wallet.addKey(key);
    }

    @PUT @Path("/")
    @Timed
    public boolean add(ListeningAddress listen) throws AddressFormatException {
        byte[] pubKeyHash = new Address(wallet.getNetworkParameters(), listen.getAddress()).getHash160();
        ECKey key = new ECKey(pubKeyHash);
        key.setCreationTimeSeconds(System.currentTimeMillis()/1000 - DEFAULT_LOOKBACK_SECONDS);
        return wallet.addKey(key);
    }

    private class MyCoinSelector implements CoinSelector {
        private final String address;

        public MyCoinSelector(String address) {
            this.address = address;
        }

        @Override
        public CoinSelection select(BigInteger target, LinkedList<TransactionOutput> candidates) {
            Collection<TransactionOutput> gathered = Lists.newArrayList();
            BigInteger sum = BigInteger.ZERO;
            for (TransactionOutput item : candidates) {
                try {
                    Address itemAddress =
                            item.getScriptPubKey().getToAddress(wallet.getNetworkParameters());
                    if (itemAddress.toString().equals(address)){
                        sum = sum.add(item.getValue());
                        gathered.add(item);
                    }
                } catch (ScriptException e) {
                    throw new RuntimeException(e);
                }
            }
            return new CoinSelection(sum, gathered);
        }
    }
}