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
