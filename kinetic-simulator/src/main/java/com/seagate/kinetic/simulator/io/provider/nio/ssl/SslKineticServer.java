/** Do NOT modify or remove this copyright and confidentiality notice!
 *
 * Copyright (c) 2001 - $Date: 2012/06/27 $ Seagate Technology, LLC.
 *
 * The code contained herein is CONFIDENTIAL to Seagate Technology, LLC.
 * Portions are also trade secret. Any use, duplication, derivation, distribution
 * or disclosure of this code, for any reason, not expressly authorized is
 * prohibited. All other rights are expressly reserved by Seagate Technology, LLC.
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
