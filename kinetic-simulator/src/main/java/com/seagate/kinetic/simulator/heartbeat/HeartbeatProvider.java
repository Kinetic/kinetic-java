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
