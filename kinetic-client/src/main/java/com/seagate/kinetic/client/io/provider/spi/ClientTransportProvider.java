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

import java.io.IOException;

import kinetic.client.KineticException;

import com.seagate.kinetic.common.lib.KineticMessage;

/**
 * A Kinetic transport provider implements this interface to provide network
 * transport and Kinetic protocol services to communicate with the Kinetic drive
 * or simulator.
 * <p>
 * The contract of a transport provider is specified as follows.
 * <ul>
 * <li>The implementation must provide a no-arg constructor.
 * <li>The provider is instantiated with its no-arg constructor,
 * <li>The client runtime invokes the {@link #init(ClientMessageService)} method
 * with an instance of {@link ClientMessageService}. The transport provider is
 * ready to serve if the call returns successfully.
 * <li>The client runtime calls the {@link #close()} method when the associated
 * {@link kinetic.client.KineticClient} is closed by an application.
 * </ul>
 *
 * @see ClientMessageService
 * @see kinetic.client.KineticClient
 *
 * @author chiaming
 *
 */
public interface ClientTransportProvider {

	/**
	 * Initialize the transport provider with the specified
	 * {@link ClientMessageService} instance.
	 * <p>
	 * This method is invoked immediately after the provider is instantiated
	 * with its no-arg constructor.
	 *
	 * @param messageService
	 *            the message routing service provided by the client runtime.
	 *
	 * @throws KineticException
	 *             if any internal errors occur.
	 */
	public void init(ClientMessageService messageService)
			throws KineticException;

	/**
	 * Write the specified protocol buffer message to the kinetic drive or
	 * simulator based on the implemented transport and kinetic protocol.
	 *
	 * @param message
	 *            the Kinetic message to be sent to the service.
	 *
	 * @throws IOException
	 *             if any I/O error occurred.
	 */
	public void write(KineticMessage message) throws IOException;

	/**
	 * Close the transport provider and release any associated resources.
	 *
	 * @throws IOException
	 *             if any errors occur when closing the provider.
	 */
	public void close() throws IOException;

}
