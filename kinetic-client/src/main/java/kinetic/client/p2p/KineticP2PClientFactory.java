/** Do NOT modify or remove this copyright and confidentiality notice!
 *
 * Copyright (c) 2001 - $Date: 2012/06/27 $ Seagate Technology, LLC.
 *
 * The code contained herein is CONFIDENTIAL to Seagate Technology, LLC.
 * Portions are also trade secret. Any use, duplication, derivation, distribution
 * or disclosure of this code, for any reason, not expressly authorized is
 * prohibited. All other rights are expressly reserved by Seagate Technology, LLC.
 */
package kinetic.client.p2p;

import kinetic.client.ClientConfiguration;
import kinetic.client.KineticClientFactory;
import kinetic.client.KineticException;

import com.seagate.kinetic.client.internal.p2p.DefaultKineticP2pClient;

/**
 * 
 * Factory class to create peer to peer client instances.
 * 
 * @see KineticP2pClient
 * 
 * @author chiaming
 * 
 */
public class KineticP2PClientFactory extends KineticClientFactory {

	/**
	 * Create a new instance of Kinetic peer to peer client based on the
	 * specified client configuration.
	 * 
	 * @param config
	 *            client configuration used to create peer to peer client.
	 * 
	 * @return a new instance of Kinetic peer to peer client
	 * 
	 * @throws KineticException
	 *             if any internal exception occurred.
	 */
	public static KineticP2pClient createP2pClient(ClientConfiguration config)
			throws KineticException {
		return new DefaultKineticP2pClient(config);
	}

}
