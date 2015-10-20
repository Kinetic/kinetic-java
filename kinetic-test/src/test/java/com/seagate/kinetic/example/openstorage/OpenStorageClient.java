/**
 * Copyright 2013-2015 Seagate Technology LLC.
 *
 * This Source Code Form is subject to the terms of the Mozilla
 * Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at
 * https://mozilla.org/MP:/2.0/.
 * 
 * This program is distributed in the hope that it will be useful,
 * but is provided AS-IS, WITHOUT ANY WARRANTY; including without 
 * the implied warranty of MERCHANTABILITY, NON-INFRINGEMENT or 
 * FITNESS FOR A PARTICULAR PURPOSE. See the Mozilla Public 
 * License for more details.
 *
 * See www.openkinetic.org for more project information
 */
package com.seagate.kinetic.example.openstorage;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import kinetic.client.ClientConfiguration;
import kinetic.client.Entry;

import kinetic.client.KineticClient;
import kinetic.client.KineticClientFactory;
import kinetic.client.KineticException;

/**
 * 
 * Open storage client to show an example of connecting to many drives.
 * <p>
 * This class is designed such that it assumed that VirtualDrives is up and running. 
 * <p>
 * By default, 1000 simulators (VirtualDrives) are running in a separate JVM.
 * <p>
 * This class instantiates 1000 instance of KineticClient and each connects to one simulator.
 * <P>
 * Each instance of the KineticClient performs PUT/GET/DELETE/... operations on the connected simulator. 
 * <p>
 * The number of concurrent client instances performing operations are defined in 
 * {@link #main(String[])} <code>MAX_THREAD</code> variable.
 * <p>
 * Please adjust the parameters in the main method as necessary to fit your test environment. 
 * 
 * @see VirtualDrives
 * @see #main(String[]) for usage information.
 * 
 * @author chiaming
 * 
 */
public class OpenStorageClient implements Runnable {

	// String to byte[] encoding
	public static final String UTF8 = "utf8";

	// kinetic client
	private KineticClient client = null;
	
	//name of client instance
	private int name = 0;
	
	//default port.
	private int port = 8123;
	
	//done signal for each iteration
	private CountDownLatch doneSignal = null;
	
	//iteration count
	private long iterateCount = 0;

	/**
	 * Construct a client instance with the specified parameters.
	 * 
	 * @param name name of the client
	 * @param host host name for the drive
	 * @param port port number of the drive
	 */
	public OpenStorageClient (int name, String host, int port) {
	    try {
	       
	    this.name = name;
	    this.port = port;
	   
	    ClientConfiguration clientConfig = new ClientConfiguration();
        clientConfig.setHost(host);
        clientConfig.setPort(port);

        client = KineticClientFactory.createInstance(clientConfig);
        
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
	
	public void setDoneSignal (CountDownLatch doneSignal) {
	    this.doneSignal = doneSignal;
	}
	
	
	//@SuppressWarnings("unused")
	public void run() {
	    
	    this.iterateCount ++;

	    try {
		
	    System.out.println ("running client instance: " + name + ", simulator port: " + port +", iteration=" + this.iterateCount);
	    
		// initial key, value and new version
		byte[] key1 = stringToBytes("key1-" + port + "-" + this.iterateCount);
		byte[] value1 = new byte[1024];
		
		byte[] key2 = stringToBytes("key2-" + port + "-" + this.iterateCount);
		byte[] value2 = new byte[1024];

		// create two entries
		Entry entry1 = new Entry(key1, value1);
		Entry entry2 = new Entry(key2, value2);

		// forced put two entries
		client.putForced(entry1);
		client.putForced(entry2);
		
		// get entry, expect to receive entry1
		Entry se1 = client.get(key1);
		if (se1 == null) {
            throw new Error ("getKey1 failed.");
        }
	
		// get next entry, expect to receive entry2
		Entry se2 = client.getNext(key1);
		if (se2 == null) {
            throw new Error ("getKey2 failed.");
        }

		// get previous entry, expect to receive simpleEntry1 entry
		Entry se11 = client.getPrevious(key2);
		if (se11 == null) {
            throw new Error ("get getPrevious(key2) failed.");
        }
		
		// get key range from entry1 to entry2, expect to receive entry1 and
		// entry2
		List<byte[]> keys = client.getKeyRange(key1, true, key2, true, 2);
		if (keys.size() !=2) {
		    throw new Error ("getKeyRange failed.");
		}
		
		// delete entry1
		boolean deleted = client.delete(entry1);
		if (deleted == false) {
		    throw new Error ("delete entry failed");
		}

		// forced delete entry2
		deleted = client.deleteForced(key2);
		if (deleted == false) {
            throw new Error ("delete entry failed");
        }
				
	    } catch (Exception e) {
	        e.printStackTrace();
	        //throw new Error (e);
	    } finally {
	        System.out.println ("Done running client instance: " + name + ", simulator port: " + port +", iteration=" + this.iterateCount);
	        doneSignal.countDown();
	    }
	}
	
	/**
	 * close client instance.
	 */
	public void close() {
	    try {
            this.client.close();
            System.out.println ("closed client instance: " + name);
        } catch (KineticException e) {
            e.printStackTrace();
        }
	}

	/**
	 * convert string to byte[] using UTF8 encoding.
	 * 
	 * @param string
	 *            string to be converted to byte[].
	 * 
	 * @return the byte[] representation of the specified string
	 */
	private static byte[] stringToBytes(String string) {

		try {
			return string.getBytes(UTF8);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Start many instances of kinetic client and connect to each of the specified drives.
	 * <p>
	 * This class is designed such that it assumed that VirtualDrives is up and running. 
	 * <p>
	 * By default, 1000 simulators (VirtualDrives) can be deployed and started in a separate JVM.
	 * <p>
	 * This class instantiates the same number of instance of KineticClient and each connects to one simulator.
	 * <P>
	 * Each instance of the KineticClient performs PUT/GET/DELETE/... operations on the connected simulator. 
	 * <p>
	 * The number of concurrent client instances performing operations are defined in 
	 * {@link #main(String[])} <code>MAX_THREAD</code> variable.
	 * <p>
	 * Please adjust the parameters in the main method as necessary to fit your test environment. 
	 * 
	 * @param args no args is used.
	 * @throws KineticException if any internal error occurred.
	 * 
	 * @throws InterruptedException if interrupted.
	 */
	public static void main(String[] args) throws KineticException,
	InterruptedException {
	    
	    // start port
	    int startPort = 8123;
	    
	    //assume all simulators are within the same host
	    String host = "localhost";
	  
	    //port for a specific simulator
	    int port = 0;
	    
	    //max concurrent client ops
	    int MAX_THREAD = 5;
	    
	    //max iterations
	    int MAX_ITERATION = 1000;
	    
	    //max client instances
	    int MAX_CLIENT = VirtualDrives.MAX_SIMULATOR;
	    
	    //thread pool
	    ExecutorService service = Executors.newFixedThreadPool(MAX_THREAD);
	    
	    //allocate client instances 
	    OpenStorageClient[] clients = new OpenStorageClient[MAX_CLIENT];
	    
	    //create client instances with sequential ports
	    for (int i = 0; i < MAX_CLIENT; i ++) {
	        
	        //increase port number
	        port = startPort + i;
	        
	        //create a client instance
	        clients[i] = new OpenStorageClient(i, host, port);
	        
	        System.out.println ("instantiated client: " + i);
	    }
	    
	    /**
	     * performing operations on each of the conencted simulator.
	     */
	    for (int j = 0; j < MAX_ITERATION; j++) {
	        
	        //instantiate done signal
	        CountDownLatch doneSignal = new CountDownLatch(MAX_CLIENT);
	        
	        for (int i = 0; i < MAX_CLIENT; i ++) {
	            //set done signal
	            clients[i].setDoneSignal(doneSignal);
	            
	            //run client
	            service.execute(clients[i]); 
	        }
	       
	        //wait for operations to finish
	        doneSignal.await();
	        
	        System.out.println("*** iteration finished: " + j + ", total simulators accessed: " + MAX_CLIENT);
	        
	        //pause for demo (visual) purposes
	        Thread.sleep(3000);
	    }
	    
	    //all iterations finished
	    service.shutdownNow();
	    
	    /**
	     * close all client instances
	     */
	    for (int i = 0; i < MAX_CLIENT; i ++) {
            clients[i].close();
        }
	    
	    System.out.println("***** test finished, total clients = " + MAX_CLIENT);
	    
	}
}
