/** Do NOT modify or remove this copyright and confidentiality notice!
 *
 * Copyright (c) 2001 - $Date: 2012/06/27 $ Seagate Technology, LLC.
 *
 * The code contained herein is CONFIDENTIAL to Seagate Technology, LLC.
 * Portions are also trade secret. Any use, duplication, derivation, distribution
 * or disclosure of this code, for any reason, not expressly authorized is
 * prohibited. All other rights are expressly reserved by Seagate Technology, LLC.
 */
package com.seagate.kinetic.simulator.io.provider.tcp;

import java.util.concurrent.ThreadFactory;

import kinetic.simulator.SimulatorConfiguration;

/**
 * 
 * Server network I/O thread factory.
 * <p>
 * All threads created from I/O service has the following naming syntax.
 * <p>
 * <IoService-serverPort-sequence>
 * <p>
 * 
 * @author James Hughes
 * @author chiaming Yang
 * 
 */
public class ServiceThreadFactory implements ThreadFactory {

	// sequence for thread naming
	private int sequence = 0;

	// thread naming prefix
	public static final String MY_NAME_PREFIX = "IoService";

	// variable for holding thread name
	private String myName = null;

	/**
	 * Factory constructor.
	 * 
	 * @param config
	 *            server configuration
	 */
	public ServiceThreadFactory(SimulatorConfiguration config) {
		myName = MY_NAME_PREFIX + "-" + config.getPort() + "-";
	}

	@Override
	public Thread newThread(Runnable r) {
		Thread t = new Thread(r);
		t.setName(myName + this.nextSequence());
		return t;
	}

	/**
	 * Get next sequence number.
	 * 
	 * @return next sequence number.
	 */
	private synchronized int nextSequence() {
		return sequence++;
	}

}
