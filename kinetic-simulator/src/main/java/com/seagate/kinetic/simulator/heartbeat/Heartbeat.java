/** Do NOT modify or remove this copyright and confidentiality notice!
 *
 * Copyright (c) 2001 - $Date: 2012/06/27 $ Seagate Technology, LLC.
 *
 * The code contained herein is CONFIDENTIAL to Seagate Technology, LLC.
 * Portions are also trade secret. Any use, duplication, derivation, distribution
 * or disclosure of this code, for any reason, not expressly authorized is
 * prohibited. All other rights are expressly reserved by Seagate Technology, LLC.
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
