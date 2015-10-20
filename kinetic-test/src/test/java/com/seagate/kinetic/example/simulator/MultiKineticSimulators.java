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
package com.seagate.kinetic.example.simulator;

import kinetic.simulator.KineticSimulator;
import kinetic.simulator.SimulatorConfiguration;

/**
 * 
 * Start and shutdown 10 simulator instances example.
 * <p>
 * This example starts 10 instances of Kinetic Simulator. Each listens on its
 * own service port starting from 8123 for (TCP) and 8443 for (SSL/TLS).
 * <p>
 * After all the services are started, the example pauses for 30 seconds and
 * shutdown all the services.
 */
public class MultiKineticSimulators {


	public static void main(String[] args) throws InterruptedException {

		// max number of simulators to instantiate.
		int max = 10;

		// simulator instances holder
		KineticSimulator simulators[] = new KineticSimulator[max];

		// base port number
		int port = 8123;

		// base ssl port
		int sslPort = 18123;

		for (int i = 0; i < max; i++) {

			// instantiate a new instance of configuration object
			SimulatorConfiguration config = new SimulatorConfiguration();

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

			System.out.println("\nstarted simulator on port="
					+ config.getPort() + ", ssl port=" + config.getSslPort()
					+ "\n");
		}

		Thread.sleep(30000);

		for (int i = 0; i < max; i++) {

			// close the simulator instance
			simulators[i].close();

			System.out.println("closed simulator on port="
					+ simulators[i].getServerConfiguration().getPort()
					+ ", ssl port="
					+ simulators[i].getServerConfiguration().getSslPort());
		}

	}


}
