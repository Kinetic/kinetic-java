/** Do NOT modify or remove this copyright and confidentiality notice!
 *
 * Copyright (c) 2001 - $Date: 2012/06/27 $ Seagate Technology, LLC.
 *
 * The code contained herein is CONFIDENTIAL to Seagate Technology, LLC.
 * Portions are also trade secret. Any use, duplication, derivation, distribution
 * or disclosure of this code, for any reason, not expressly authorized is
 * prohibited. All other rights are expressly reserved by Seagate Technology, LLC.
 */
package com.seagate.kinetic.client.io.provider.spi;

import kinetic.client.ClientConfiguration;

import com.seagate.kinetic.common.lib.KineticMessage;
import com.seagate.kinetic.proto.Kinetic.Message;

/**
 *
 * The client message service provides a message routing service for the kinetic
 * client transport provider.
 * <p>
 * When a transport provider received a (response) message from the drive or
 * simulator, the provider calls {@link #routeMessage(Message)} to dispatch the
 * message to the associated application caller.
 * <p>
 *
 * @see ClientTransportProvider
 *
 * @author chiaming
 *
 */
public interface ClientMessageService {

	/**
	 * Route the message to the associated application.
	 *
	 * @param message
	 *            the message to be routed to the associated application.
	 *
	 * @throws InterruptedException
	 *             if the call is blocked and interrupted.
	 */
	public void routeMessage(KineticMessage message)
			throws InterruptedException;

	/**
	 * Get the client configuration associated with the kinetic client instance.
	 *
	 * @return the client configuration associated with the kinetic client
	 *         instance
	 */
	public ClientConfiguration getConfiguration();

	/**
	 * Close the client message service and release associated resources.
	 */
	public void close();

}
