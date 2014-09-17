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
package com.seagate.kinetic.example.openstorage;

import kinetic.simulator.KineticSimulator;
import kinetic.simulator.SimulatorConfiguration;

/**
 * Starts (by default) 1000 simulators each listens on its own ports and with its own persistent storage.
 * <p>
 * You may require to configure the OS open process/file parameters to run this program.
 * 
 * @see OpenStorageClient
 * @author chiaming
 */
public class VirtualDrives {
    
    public static final int MAX_SIMULATOR = 100;

    private int maxSimulator = MAX_SIMULATOR;
    
 // base port number
    private int port = 8123;

    // base ssl port
    private int sslPort = 18123;
    
    public VirtualDrives (int maxSimulator) {
        this.maxSimulator = maxSimulator;
    }
    
    public void run() {
        
     // simulator instances holder
        KineticSimulator simulators[] = new KineticSimulator[maxSimulator];

        for (int i = 0; i < maxSimulator; i++) {

            // instantiate a new instance of configuration object
            SimulatorConfiguration config = new SimulatorConfiguration();
            //config.setStartSsl(false);
            //config.setUseMemoryStore(true);

            // set service ports to the configuration
            int myport = port + i;
            int mySslPort = sslPort + i;
            config.setPort(myport);
            config.setSslPort(mySslPort);

            // set persist store home folder for each instance
            config.put(SimulatorConfiguration.PERSIST_HOME, "instance_"
                    + myport);

            // start the simulator instance
            simulators[i] = new KineticSimulator(config);

            System.out.println("\nstarted simulator. port="
                    + config.getPort() + ", ssl port=" + config.getSslPort()
                    + "\n");
        }
        
    }
    
	public static void main(String[] args) throws InterruptedException {
	    
	    //use bdb store 
	    //System.setProperty("kinetic.db.class", "com.seagate.kinetic.simulator.persist.bdb.BdbStore");
	    
		// max number of simulators to instantiate.
		VirtualDrives vdrives = new VirtualDrives(MAX_SIMULATOR);
		//start the simulator
		vdrives.run();
	}


}
