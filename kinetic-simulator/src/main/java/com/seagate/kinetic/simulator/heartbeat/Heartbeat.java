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
package com.seagate.kinetic.simulator.heartbeat;

import java.util.TimerTask;
import java.util.logging.Logger;

import kinetic.simulator.SimulatorConfiguration;

import com.seagate.kinetic.simulator.internal.SimulatorEngine;

/**
 * 
 * Send hear beat message to a group specified in the configuration file.
 * 
 */
public class Heartbeat extends TimerTask {

	private final static Logger logger = Logger.getLogger(Heartbeat.class
			.getName());

	// simulator configuration
	private SimulatorConfiguration sconfig = null;



	private HeartbeatProvider provider = null;

	/**
	 * Heart beat constructor.
	 * 
	 * @param engine
	 *            current simulator engine instance
	 */
	public Heartbeat(SimulatorEngine engine) {

		// simulator configuration
		this.sconfig = engine.getServiceConfiguration();

		// get heartbeat provider
		provider = this.sconfig.getHeartbeatProvider();

		// init heart beat
		provider.init(sconfig);
	}

	@Override
	public void run() {
		this.provider.sendHeartbeat();
	}

	/**
	 * stop heart beat
	 */
	public void close() {

		this.cancel();

		this.provider.close();

		logger.info("heartbeat stopped");
	}

}
