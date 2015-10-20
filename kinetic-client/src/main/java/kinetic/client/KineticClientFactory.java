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
package kinetic.client;

import com.seagate.kinetic.client.internal.DefaultKineticClient;

/**
 * Kinetic client boot-strap interface. This is the starting point for the
 * Kinetic applications.
 * <p>
 * This factory class provides a static factory method to construct new
 * instances of <code>KineticClient</code>.
 * <p>
 * Kinetic applications construct a new instance of <code>ClientConfiguration</code>
 * and set appropriate configurations (such as server host/port) to the
 * configuration instance. Applications then invoke the static createInstance
 * method to obtain a new instance of KineticClient.
 * 
 * @see KineticClient
 * @see ClientConfiguration
 * 
 * @author Chiaming Yang
 * 
 */
public class KineticClientFactory {

	/**
	 * Construct a new instance of the <code>KineticClient</code>.
	 * 
	 * @param config
	 *            client configuration used to create new instance.
	 * @return a new KineticClient instance that is connected to the server.
	 * 
	 * @throws KineticException
	 *             if any internal errors occur.
	 */
	public static KineticClient createInstance(ClientConfiguration config)
			throws KineticException {
		return new DefaultKineticClient(config);
	}
}
