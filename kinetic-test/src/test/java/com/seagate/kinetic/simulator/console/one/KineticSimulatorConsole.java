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
package com.seagate.kinetic.simulator.console.one;

import kinetic.simulator.KineticSimulator;
import kinetic.simulator.SimulatorConfiguration;

public class KineticSimulatorConsole {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		int startPort = 8123;
		int instances = 1;
		if (args.length > 0)
			startPort = Integer.parseInt(args[0]);
		if (args.length > 1)
			instances = Integer.parseInt(args[1]);

		if (instances == 1)
			System.out.printf("KineticClient server configured at port=%d",
					startPort);
		else
			System.out
					.printf("KineticClient configured for multiple servers on range %d to %d.",
							startPort, startPort + instances - 1);

		for (int port = startPort; port < startPort + (instances * 2); port += 2) {
			SimulatorConfiguration serverConfig = new SimulatorConfiguration();
			serverConfig.setPort(port);
			serverConfig.setSslPort(port + 1);
			serverConfig.setStartSsl(true);
			serverConfig.setTickTime(0);
			serverConfig.setUseMemoryStore(true);
			serverConfig.put(SimulatorConfiguration.PERSIST_HOME, "instance_"
					+ port);
			@SuppressWarnings("unused")
			KineticSimulator s = new KineticSimulator(serverConfig);

			System.out.printf("Running server at port=%d", port);
		}

		System.out.printf("Done.");
	}

}
