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

import kinetic.simulator.SimulatorConfiguration;

/**
 * 
 * Heartbeat provider interface. All heart beat implementations for the
 * simulator must implement this interface.
 * <p>
 * A heartbeat provider must provide a no-arg constructor.
 * <p>
 * The life cycle of a heartbeat provider is as follows. All methods are invoked
 * by the simulator.
 * <ul>
 * <li>invokes the no-arg constructor.
 * <li>invokes the {@link #init(SimulatorConfiguration)} method.
 * <li>invokes the {@link #sendHeartbeat()} based on the configured tick time
 * set in the simulator configuration.
 * <li>invokes the {@link #close()} when the simulator is closed.
 * </ul>
 * 
 * @author chiaming
 * 
 */
public interface HeartbeatProvider {

	/**
	 * init the heart beat provider.
	 * 
	 * @param config
	 *            simulator configuration
	 */
	public void init(SimulatorConfiguration config);

	/**
	 * send heart beat message. this is invoked per tick time.
	 */
	public void sendHeartbeat();

	/**
	 * close heart beat provider. all resources should be released.
	 */
	public void close();
}
