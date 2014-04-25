/**
 * Copyright (C) 2014 Seagate Technology.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
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
