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
package com.seagate.kinetic.simulator.io.provider.nio.ssl;

import kinetic.simulator.KineticSimulator;
import kinetic.simulator.SimulatorConfiguration;

/**
 * 
 * Define Java system property to show SSL debugging output:
 * 
 * -Djavax.net.debug=all
 * 
 * @author chiaming
 * 
 */
public class SslKineticServer {

	public static void main(String[] args) {
		int port = 8123;

		if (args.length > 0) {
			port = Integer.parseInt(args[0]);
		}

		SimulatorConfiguration serverConfig = new SimulatorConfiguration();
		serverConfig.setPort(port);

		if (args.length > 1) {
			serverConfig.setProperty(SimulatorConfiguration.KINETIC_HOME,
					args[1]);
		}

		serverConfig.setStartSsl(true);
		serverConfig.setSslPort(8443);

		@SuppressWarnings("unused")
        KineticSimulator lcs = new KineticSimulator(serverConfig);

	}

}
