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
