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
package com.seagate.kinetic.simulator.io.provider.spi;

import io.netty.channel.ChannelHandlerContext;
import kinetic.simulator.SimulatorConfiguration;

import com.seagate.kinetic.common.lib.KineticMessage;
import com.seagate.kinetic.simulator.internal.ConnectionInfo;
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
	
	/**
	 * Register a new connection for the message service.
	 * 
	 * @param ctx
	 * @return
	 */
	public ConnectionInfo registerNewConnection (ChannelHandlerContext ctx);
}
