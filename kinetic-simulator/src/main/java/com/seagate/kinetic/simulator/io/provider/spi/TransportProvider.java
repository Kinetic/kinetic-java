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

import java.io.IOException;

/**
 * 
 * A Kinetic transport provider must provide a no-arg constructor and implement
 * all the life-cycle methods defined in this interface.
 * <p>
 * 
 * The simulator engine will invoke each transport provider instance with the
 * following sequences.
 * <p>
 * 1. Loads the provider class with its provided no-arg constructor.
 * <p>
 * 2. calls {@link #init(MessageService)} with the specified
 * <code>MessageService</code>
 * <p>
 * 3. calls {@link #start()} to start the transport.
 * <p>
 * 4. calls {@link #stop()} to stop the transport. after this, the transport
 * instance will not be used by the simulator.
 * <p>
 * 5. calls {@link #close()} method. a provider may choose to implement and
 * release extra resources in this method.
 * 
 * @see MessageService
 * 
 * @author chiaming
 */
public interface TransportProvider {

	/**
	 * initialize the transport service with the specified message service
	 * resource.
	 * 
	 * @param messageService
	 *            the services provided by the simulator.
	 */
	public void init(MessageService messageService);

	/**
	 * Start the transport service.
	 * 
	 * @throws IOException
	 *             if the transport cannot be started, such as service port is
	 *             in use.
	 */
	public void start() throws IOException;

	/**
	 * Stop the transport service. the simulator engine will not use this
	 * transport instance after this call returns.
	 */
	public void stop();

	/**
	 * Close the transport service. the provider may release extra resources
	 * when this is invoked.
	 */
	public void close();
}
