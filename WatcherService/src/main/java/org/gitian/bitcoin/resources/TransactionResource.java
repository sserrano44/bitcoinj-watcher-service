package org.gitian.bitcoin.resources;

/**
 * @author devrandom
 */

import com.google.bitcoin.core.ProtocolException;
import com.google.bitcoin.core.Transaction;
import com.google.bitcoin.core.TransactionBroadcaster;
import com.google.bitcoin.core.Wallet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.yammer.metrics.annotation.Timed;

import org.gitian.bitcoin.core.SendTransaction;
import org.spongycastle.util.encoders.Hex;

import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/transaction")
@Produces(MediaType.APPLICATION_JSON)
public class TransactionResource {
    private final TransactionBroadcaster broadcaster;
    private final Wallet wallet;

    public TransactionResource(TransactionBroadcaster broadcaster, Wallet wallet) {
        this.broadcaster = broadcaster;
        this.wallet = wallet;
    }

    static private void badRequest(String error) {
        Map<String, String> result = Maps.newHashMap();
        result.put("error", error);
        throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity(result).build());
    }

    @PUT
    @Timed
    public boolean send(SendTransaction sendTx) {
        byte[] bytes = sendTx.getBytes();
        try {
            Transaction tx = new Transaction(wallet.getNetworkParameters(), bytes);
            broadcaster.broadcastTransaction(tx);
        } catch (ProtocolException e) {
            badRequest("invalid transaction");
        }
        return true;
    }

    @GET
    @Timed
    public List<String> index() throws Exception {
        return Lists.newArrayList();
    }
}