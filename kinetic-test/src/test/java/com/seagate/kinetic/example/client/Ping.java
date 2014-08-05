/**
 * 
 * Copyright (C) 2014 Seagate Technology.
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
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
