package org.gitian.bitcoin;

/**
 * @author sserrano44
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.HttpsURLConnection;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.AbstractExecutionThreadService;
import com.google.common.util.concurrent.Service;

import com.google.bitcoin.core.*;

public class IPNWalletEventListener extends AbstractWalletEventListener {

	private ObjectMapper mapper = null;
	private String IPN = null;
    private Service service = null;
    private BlockingQueue<IPNNotification> post_queue = null;  
    private static final Logger log = LoggerFactory.getLogger(IPNWalletEventListener.class);

	public class IPNPostService extends AbstractExecutionThreadService {
		  protected void run() throws InterruptedException {
			ObjectMapper mapper = new ObjectMapper();

			while (isRunning()) {
				HttpURLConnection conn = null;
				String rawData = null;
				IPNNotification notification = (IPNNotification) post_queue.take();
				
		    	try {
			    	rawData = mapper.writeValueAsString(notification);		    		
		    	} catch (JsonProcessingException e) {
		    		continue;
		    	}
		    	
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
					log.error("Error posting to IPN URL",e);
					post_queue.put(notification);
				} finally {
					if(conn != null) {
						conn.disconnect(); 
					}
			    }
		    }
		  }

	}
	
	public IPNWalletEventListener(String IPN) {
		super();
		this.IPN = IPN;
		this.post_queue = new ArrayBlockingQueue<IPNNotification>(1024);
		this.service = new IPNPostService();
		this.service.start();
		this.mapper =  new ObjectMapper();
	}

	@Override
    public void onCoinsReceived(Wallet w, Transaction tx, BigInteger prevBalance, BigInteger newBalance) {

		try {
			post_queue.put(new IPNNotification(tx));
		} catch (IOException e) {
			log.error("Error queueing notification",e);
		} catch (InterruptedException e) {
			log.error("Error queueing notification",e);
		}

		System.out.println("\n#### TX: ####");
		System.out.println(tx.getHash().toString());
		System.out.println("#####\n");
	}
}
