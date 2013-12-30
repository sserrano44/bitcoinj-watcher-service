package org.gitian.bitcoin;

/**
 * @author sserrano44
 */

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import javax.net.ssl.HttpsURLConnection;

import org.spongycastle.util.encoders.Hex;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.AbstractExecutionThreadService;
import com.google.common.util.concurrent.Service;

import com.google.bitcoin.core.*;
import com.google.bitcoin.params.MainNetParams;

public class IPNWalletEventListener extends AbstractWalletEventListener {

	private ObjectMapper mapper = null;
	private String IPN = null;
    private Service service = null;
    private BlockingQueue<String> post_queue = null;  

	public class IPNPostService extends AbstractExecutionThreadService {
		  protected void run() throws InterruptedException {
			HttpURLConnection conn = null;
			String rawData = null;
			while (isRunning()) {
		    	rawData = (String) post_queue.take();
		    	
				try {
					URL u = new URL(IPN);

			    	if (IPN.startsWith("https")) {
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
			    	
				} catch (IOException e) {
					if (rawData != null) {
						post_queue.put(rawData);
					}
					e.printStackTrace();
				} finally {
					if(conn != null) {
						conn.disconnect(); 
					}
					rawData = null;
			    }
		    
		    }
		  }

//		  // Can also start shutdown in another method
//		  private void method() {
//		    if (false == true) {
//		      triggerShutdown();
//		      return;
//		    }
//		  }
	}
	
	public IPNWalletEventListener(String IPN) {
		super();
		this.IPN = IPN;
		this.post_queue = new ArrayBlockingQueue<String>(1024);
		this.service = new IPNPostService();
		this.service.start();
		this.mapper =  new ObjectMapper();
	}

	@Override
    public void onCoinsReceived(Wallet w, Transaction tx, BigInteger prevBalance, BigInteger newBalance) {

		String rawData;
		Map<String,Object> postData = new HashMap<String,Object>();

		postData.put("type", "tx");
		postData.put("hash", tx.getHash().toString());
		if (tx.isPending()) {
			postData.put("pending", Boolean.TRUE);
		} else {
			postData.put("pending", Boolean.FALSE);
		}

		BitcoinSerializer bs = new BitcoinSerializer(MainNetParams.get(), true, false);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try {
			bs.serialize(tx, bos);
			byte[] hex = bos.toByteArray();
			postData.put("hex", Hex.encode(hex));
		} catch (IOException e2) {
			e2.printStackTrace();
		}

		try {
			rawData = mapper.writeValueAsString(postData);
		} catch (JsonProcessingException e1) {
			e1.printStackTrace();
			return;
		}

		try {
			post_queue.put(rawData);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		System.out.println("\n#### TX: ####");
		System.out.println(tx.getHash().toString());
		System.out.println("#####\n");
	}
}
