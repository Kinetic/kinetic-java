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
