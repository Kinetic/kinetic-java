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
package com.seagate.kinetic.simulator.io.provider.spi;

import kinetic.simulator.SimulatorConfiguration;

import com.seagate.kinetic.common.lib.KineticMessage;
import com.seagate.kinetic.simulator.io.provider.nio.NioEventLoopGroupManager;

/**
 * The <code>MessageService</code> are resources/services provided by the
 * associated instance of simulator. These resources/services may be used by a
 * plug-in service, such as a transport provider, to process messages
 * synchronously or asynchronously.
 *
 * The resources/services are as follows.
 * <ul>
 * <li>thread pool service.</li>
 * <li>message request process service.</li>
 * <li>service configurations.</li>
 * <li>nio resource manager</li>
 * <li>simulator configurations.</li>
 * </ul>
 *
 * @author chiaming
 *
 */
public interface MessageService {

	/**
	 * The request runnable object is executed by the thread pool service.
	 *
	 * @param request
	 *            runnable object to be executed.
	 */
	public void execute(Runnable request);

	/**
	 * request the simulator to process the request message.
	 *
	 * @param request
	 *            the kinetic request message.
	 *
	 * @return the response message corresponding to the request message.
	 */
	public KineticMessage processRequest(KineticMessage request);

	/**
	 * Get the simulator configuration of the current instance.
	 *
	 * @return the simulator configuration for the current simulator instance.
	 */
	public SimulatorConfiguration getServiceConfiguration();

	/**
	 * get an instance of NioEventLoopGroupManager associated with nio events.
	 *
	 * @return a new instance of NioEventLoopGroupManager.
	 */
	public NioEventLoopGroupManager getNioEventLoopGroupManager();
}
