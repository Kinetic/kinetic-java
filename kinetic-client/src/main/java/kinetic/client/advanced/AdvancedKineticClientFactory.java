/** Do NOT modify or remove this copyright and confidentiality notice!
 *
 * Copyright (c) 2001 - $Date: 2012/06/27 $ Seagate Technology, LLC.
 *
 * The code contained herein is CONFIDENTIAL to Seagate Technology, LLC.
 * Portions are also trade secret. Any use, duplication, derivation, distribution
 * or disclosure of this code, for any reason, not expressly authorized is
 * prohibited. All other rights are expressly reserved by Seagate Technology, LLC.
 */
package kinetic.client.advanced;

import kinetic.client.ClientConfiguration;
import kinetic.client.KineticException;

import com.seagate.kinetic.client.internal.DefaultKineticClient;

/**
 * Kinetic advanced client boot-strap interface. This is the starting point for
 * the advanced Kinetic applications.
 * <p>
 * This factory class provides a static factory method to construct new
 * instances of <code>AdvancedKineticClient</code>.
 * <p>
 * Kinetic applications construct a new instance of
 * <code>ClientConfiguration</code> and set appropriate configurations (such as
 * server host/port) to the configuration instance. Applications then invoke the
 * static createInstance method to obtain a new instance of
 * AdvancedKineticClient.
 * 
 * @see AdvancedKineticClient
 * @see ClientConfiguration
 * 
 * @author Chiaming Yang
 * 
 */
public class AdvancedKineticClientFactory {

	/**
	 * Construct a new instance of the <code>AdvancedKineticClient</code>.
	 * 
	 * @param config
	 *            client configuration to create the client instance.
	 * @return a new AdvancedKineticClient instance that is connected to the
	 *         server.
	 * 
	 * @throws KineticException
	 *             if any internal errors occur.
	 */
	public static AdvancedKineticClient createAdvancedClientInstance(
			ClientConfiguration config)
					throws KineticException {
		return new DefaultKineticClient(config);
	}
}
