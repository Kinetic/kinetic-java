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
package com.seagate.kinetic.simulator.performance;

import kinetic.simulator.KineticSimulator;
import kinetic.simulator.SimulatorConfiguration;

/**
 * 
 * Async and sync performance test server side
 * <p>
 * 
 * @author Chenchong(Emma) Li
 * 
 */
public class PerfServer {
	private static int PORT = 8123;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length != 0 && args.length != 1) {
			System.out.println("Parameter error!!!");
			System.out.println("Usage:");
			System.out.println("PerfServer [Server_Port]");
			System.out.println("Welcome to try again.");
			return;
		}
		if (args.length > 0 && args.length == 1) {
			PORT = Integer.parseInt(args[0]);
			System.out.println("Server_Port=" + PORT);
		}
		SimulatorConfiguration serverConfig = new SimulatorConfiguration();
		serverConfig.put(SimulatorConfiguration.PERSIST_HOME, "performance");
		serverConfig.setPort(PORT);
		@SuppressWarnings("unused")
		KineticSimulator server = new KineticSimulator(serverConfig);
		System.out.println("KineticClient perf Server started, port=" + PORT);

	}

}
