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
