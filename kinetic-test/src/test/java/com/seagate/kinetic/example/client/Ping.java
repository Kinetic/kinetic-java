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
package com.seagate.kinetic.example.client;

import kinetic.client.ClientConfiguration;

import kinetic.client.KineticClient;
import kinetic.client.KineticClientFactory;
import kinetic.client.KineticException;

/**
 * Kinetic Ping usage example.
 */
public class Ping {

    public static void ping(String host, int port) throws KineticException, InterruptedException {

        // kinetic client
        KineticClient client = null;

        // Client configuration and initialization
        ClientConfiguration clientConfig = new ClientConfiguration();
        
        clientConfig.setHost(host);
        clientConfig.setPort(port);
        
        // create client instance
        client = KineticClientFactory.createInstance(clientConfig);

        while (true) {
            // ping server
            long time = client.noop();
            //write output to console
            System.out.println("kinetic ping runtrip time: " + time
                    + " milli seconds., ping host=" + clientConfig.getHost()
                    + ", port=" + clientConfig.getPort());
            
            //sleep for two seconds.
            Thread.sleep(1000);
        }

        // close kinetic client
        // client.close();
    }

    /**
     * Ping a kinetic service and prints the rundtrip time.
     * 
     * @param args no used.
     * @throws KineticException if any errors occurred.
     * @throws InterruptedException if interrupted.
     */
	public static void main(String[] args) throws KineticException,
	InterruptedException {
	    
	    // default host/port
	    String host = System.getProperty("kinetic.host", "localhost");
	    String sport = System.getProperty("kinetic.port", "8123");
	    int port = Integer.parseInt(sport);
	    
	    // over-ride the default host
	    if (args.length > 0) {
	        host = args[0];
	    }
	    
	    //over-ride the default port
	    if (args.length > 1) {
	        port = Integer.parseInt(args[1]);
	    }
	    
	    // launch ping
	    Ping.ping(host, port);
	}
}
