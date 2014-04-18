/** Do NOT modify or remove this copyright and confidentiality notice!
 *
 * Copyright (c) 2001 - $Date: 2012/06/27 $ Seagate Technology, LLC.
 *
 * The code contained herein is CONFIDENTIAL to Seagate Technology, LLC.
 * Portions are also trade secret. Any use, duplication, derivation, distribution
 * or disclosure of this code, for any reason, not expressly authorized is
 * prohibited. All other rights are expressly reserved by Seagate Technology, LLC.
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
