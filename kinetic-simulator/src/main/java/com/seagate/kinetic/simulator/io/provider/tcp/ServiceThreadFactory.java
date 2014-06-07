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
