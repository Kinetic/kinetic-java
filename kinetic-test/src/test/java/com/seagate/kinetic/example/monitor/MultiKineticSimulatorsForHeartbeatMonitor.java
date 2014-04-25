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
package com.seagate.kinetic.example.monitor;

import kinetic.simulator.KineticSimulator;
import kinetic.simulator.SimulatorConfiguration;

/**
 * 
 * Start and 10 simulator instances example.
 * <p>
 * This example starts 10 instances of Kinetic Simulator. Each listens on its
 * own service port starting from 8123 for (TCP) and 8443 for (SSL/TLS).
 * <p>
 */
public class MultiKineticSimulatorsForHeartbeatMonitor {


	public static void main(String[] args) throws InterruptedException {

		// max number of simulators to instantiate.
		int max = 10;

		// simulator instances holder
		KineticSimulator simulators[] = new KineticSimulator[max];

		// base port number
		int port = 8123;

		// base ssl port
		int sslPort = 8443;

		for (int i = 0; i < max; i++) {

			// instantiate a new instance of configuration object
			SimulatorConfiguration config = new SimulatorConfiguration();

			// set service ports to the configuration
			int myport = port + i;
			int mySslPort = sslPort + i;
			config.setPort(myport);
			config.setSslPort(mySslPort);
			config.setUseMemoryStore(true);

			// set persist store home folder for each instance
			config.put(SimulatorConfiguration.PERSIST_HOME, "instance_"
					+ myport);

			// start the simulator instance
			simulators[i] = new KineticSimulator(config);

			System.out.println("\nstarted simulator on port="
					+ config.getPort() + ", ssl port=" + config.getSslPort()
					+ "\n");
		}
	}


}
