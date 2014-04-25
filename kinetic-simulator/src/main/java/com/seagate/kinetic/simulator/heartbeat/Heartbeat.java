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
