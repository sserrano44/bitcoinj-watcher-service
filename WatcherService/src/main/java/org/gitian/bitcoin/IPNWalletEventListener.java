package org.gitian.bitcoin;

/**
 * @author sserrano44
 */

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import com.google.bitcoin.core.*;

public class IPNWalletEventListener extends AbstractWalletEventListener {

	private String IPN = null;
	
	
	
	public IPNWalletEventListener(String IPN) {
		super();
		this.IPN = IPN;
	}

	@Override
    public void onCoinsReceived(Wallet w, Transaction tx, BigInteger prevBalance, BigInteger newBalance) {

		HttpURLConnection conn = null;
		String rawData = "{";
	    rawData += "\"type\": \"tx\", ";
		rawData += "\"hash\": \"" + tx.getHash().toString() + "\", ";
	    if (tx.isPending()) {
	    	rawData += "\"pending\": true";
	    } else {
	    	rawData += "\"pending\": false";
	    }
	    rawData += "}";
		
		try {
			URL u = new URL(this.IPN);

	    	if (this.IPN.startsWith("https")) {
				conn = (HttpsURLConnection) u.openConnection();
	    	} else {
	    		conn = (HttpURLConnection) u.openConnection();    		
	    	}
	    	
	    	conn.setDoOutput(true);
	    	conn.setRequestMethod("POST");
	    	conn.setRequestProperty("Content-Type", "application/json");
	    	conn.setRequestProperty("Content-Length", String.valueOf(rawData.length()));
	    	OutputStream os = conn.getOutputStream();
	    	os.write( rawData.getBytes() );

	    	
	    	//Send request
	        DataOutputStream wr = new DataOutputStream (
	        		conn.getOutputStream ());
	        wr.writeBytes ("");
	        wr.flush ();
	        wr.close ();

	        //Get Response	
	        InputStream is = conn.getInputStream();
	        BufferedReader rd = new BufferedReader(new InputStreamReader(is));
	        String line;
	        StringBuffer response = new StringBuffer(); 
	        while((line = rd.readLine()) != null) {
	          response.append(line);
	          response.append('\r');
	        }
	        rd.close();
	    	
	    	System.out.println("\n#### TX: ####");
	    	System.out.println(tx.getHash().toString());
	    	System.out.println("\nResponse: " + response);
	        System.out.println("#####\n");
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if(conn != null) {
				conn.disconnect(); 
			}
	    }

	}
}
