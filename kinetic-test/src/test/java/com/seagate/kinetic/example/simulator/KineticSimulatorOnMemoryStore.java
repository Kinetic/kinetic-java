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
