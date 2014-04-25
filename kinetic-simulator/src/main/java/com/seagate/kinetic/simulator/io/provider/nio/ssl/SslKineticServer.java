/**
 * 
 * Copyright (C) 2014 Seagate Technology.
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
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
