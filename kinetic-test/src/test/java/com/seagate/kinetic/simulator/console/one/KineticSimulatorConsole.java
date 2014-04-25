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
