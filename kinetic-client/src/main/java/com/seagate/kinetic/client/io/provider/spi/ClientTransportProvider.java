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
