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
package com.seagate.kinetic.example.simulator;

import kinetic.simulator.KineticSimulator;
import kinetic.simulator.SimulatorConfiguration;

/**
 * 
 * Start a simulator instance with MemoryStore as the persistent store example.
 * <p>
 * This example starts one instance of Kinetic Simulator that uses MemoryStore
 * as persistent store. The following is a configuration example.
 * <p>
 * <code>SimulatorConfiguration.setUseMemoryStore(true);</code>
 * <p>
 */
public class KineticSimulatorOnMemoryStore {

	public static void main(String[] args) {

		// simulator instance configuration
		SimulatorConfiguration config = new SimulatorConfiguration();

		// set property to use memory store
		config.setUseMemoryStore(true);

		// disable ssl/tls service
		config.setStartSsl(false);

		// start new instance of simulator with specified configurations.
		KineticSimulator simulator = new KineticSimulator(config);

		System.out.println("Example simulator started on port="
				+ simulator.getServerConfiguration().getPort());
	}


}
