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
import com.google.bitcoin.script.Script;
import com.google.bitcoin.wallet.CoinSelection;
import com.google.bitcoin.wallet.CoinSelector;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.yammer.metrics.annotation.Timed;

import org.gitian.bitcoin.core.Balance;
import org.gitian.bitcoin.core.ListeningAddress;
import org.gitian.bitcoin.core.TxOut;

import java.math.BigInteger;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/address")
@Produces(MediaType.APPLICATION_JSON)
public class AddressResource {
    public static final int DEFAULT_LOOKBACK_SECONDS = 3600 * 24 * 7;
    private final Wallet wallet;

    public AddressResource(Wallet wallet) {
        this.wallet = wallet;
    }

    private void ensureAddressIsMine(String address) {
        try {
            if (!wallet.isAddressWatched(new Address(wallet.getNetworkParameters(), address))) {
                badRequest("unregistered address");
            }
        } catch (AddressFormatException e) {
            badRequest("bad address");
        }
    }

    static private void badRequest(String error) {
        Map<String, String> result = Maps.newHashMap();
        result.put("error", error);
        throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity(result).build());
    }

    @GET @Path("/{address}/balance")
    @Timed
    public Balance balance(@PathParam("address")final String address) {
        ensureAddressIsMine(address);
        return new Balance(wallet.getWatchedBalance(new MyCoinSelector(address)));
    }

    @GET @Path("/{address}/unspent")
    @Timed
    public List<TxOut> unspent(@PathParam("address")final String address) {
        ensureAddressIsMine(address);
        LinkedList<TransactionOutput> candidates = wallet.getWatchedOutputs(true);
        CoinSelection selection =
                new MyCoinSelector(address).select(NetworkParameters.MAX_MONEY, candidates);
        List<TxOut> outputs = Lists.newArrayList();
        for (TransactionOutput output : selection.gathered) {
            Transaction tx = output.getParentTransaction();
            int index = tx.getOutputs().indexOf(output);
            outputs.add(new TxOut(tx.getHashAsString(), index, output, tx.getConfidence().getDepthInBlocks()));
        }
        return outputs;
    }

    @PUT
    @Timed
    public boolean add(ListeningAddress listenAddress) throws AddressFormatException {
        if (listenAddress.getAddress() != null) {
            Address address = new Address(wallet.getNetworkParameters(), listenAddress.getAddress());
            return wallet.addWatchedAddress(address);
        }

        if (listenAddress.getAddresses() != null) {
            List<Address> addresses = Lists.newArrayList();
            for (String addressString : listenAddress.getAddresses()) {
                Address address = new Address(wallet.getNetworkParameters(), addressString);
                addresses.add(address);
            }
            return wallet.addWatchedAddresses(addresses) == addresses.size();
        }

        return false;
    }

    @GET
    @Timed
    public List<String> index() throws Exception {
        List<String> addresses = Lists.newArrayList();

        for (Script script : wallet.getWatchedScripts()) {
            addresses.add(script.getToAddress(wallet.getNetworkParameters()).toString());
        }

        return addresses;
    }

    @GET @Path("/debug")
    @Timed
    public String debug() {
        return wallet.toString();
    }

    @POST @Path("/debug/add")
    public int debugAdd(int count) {
        List<Address> addresses = Lists.newArrayList();

        for (int i = 0 ; i < count ; i++) {
            ECKey key = new ECKey();
            addresses.add(key.toAddress(wallet.getNetworkParameters()));
        }

        return wallet.addWatchedAddresses(addresses);
    }

    @GET @Path("/check")
    @Timed
    public boolean check() {
        return wallet.isConsistent();
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