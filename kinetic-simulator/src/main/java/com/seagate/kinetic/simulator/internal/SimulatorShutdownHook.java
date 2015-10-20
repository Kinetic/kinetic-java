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
package com.seagate.kinetic.simulator.internal;

import kinetic.simulator.SimulatorConfiguration;

import com.seagate.kinetic.simulator.io.provider.nio.NioSharedResourceManager;

/**
 * 
 * Simulator shutdown hook.
 * 
 * @author chiaming
 * 
 */
public class SimulatorShutdownHook extends Thread {

	ThreadPoolService tpService = null;

	public SimulatorShutdownHook(ThreadPoolService tpService) {
		this.tpService = tpService;
	}

	@Override
	public void run() {

		if (SimulatorConfiguration.getNioResourceSharing()) {
			NioSharedResourceManager.close();
		}
		// System.out.println("shutdown hook executed ...");
	}

}
