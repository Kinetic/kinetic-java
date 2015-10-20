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
		t.setDaemon(true);
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
