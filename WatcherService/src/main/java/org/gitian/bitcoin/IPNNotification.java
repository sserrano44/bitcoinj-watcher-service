package org.gitian.bitcoin;

import java.io.IOException;

import com.google.bitcoin.core.Transaction;
import com.google.bitcoin.core.Utils;
import com.yammer.dropwizard.json.JsonSnakeCase;

@JsonSnakeCase
public class IPNNotification {
	private final String type;
	private final String hash;
	private final boolean pending;
	private final String hex;
	
	protected IPNNotification(Transaction tx) throws IOException{

		this.type = "tx";
		this.hash = tx.getHash().toString();
		this.pending = tx.isPending();
		this.hex = Utils.bytesToHexString(tx.bitcoinSerialize());
	}

	public String getType() {
		return type;
	}

	public String getHash() {
		return hash;
	}

	public boolean isPending() {
		return pending;
	}

	public String getHex() {
		return hex;
	}
}
