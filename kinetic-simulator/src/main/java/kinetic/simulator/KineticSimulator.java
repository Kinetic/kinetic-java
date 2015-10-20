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
package kinetic.simulator;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.seagate.kinetic.simulator.internal.SimulatorEngine;

/**
 * 
 * Simulator boot-strap class.
 * <p>
 * Applications may use this class to start new instance(s) of the simulator.
 * <p>
 * There is a main method provided in this class as a reference to start a new
 * instance of simulator.
 * <p>
 * Applications may also define their own SimulatorConfigration instances and
 * start the simulator with customized configurations.
 * 
 * @see SimulatorConfiguration
 * 
 * @author James Hughes
 * @author Chiaming Yang
 * 
 */
public class KineticSimulator {

	private final static Logger logger = Logger.getLogger(KineticSimulator.class
			.getName());

	private SimulatorConfiguration config = null;

	private SimulatorEngine engine = null;

	/**
	 * Constructor for the Kinetic Simulator.
	 * 
	 * 
	 * @param config
	 *            configurations for the simulator.
	 */
	public KineticSimulator(SimulatorConfiguration config) {

		this.config = config;

		try {
			this.engine = new SimulatorEngine(this.getServerConfiguration());
		} catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
			close();
		}
	}


	/**
	 * Get server configuration for this simulator.
	 * 
	 * @return server configuration for this simulator.
	 */
	public SimulatorConfiguration getServerConfiguration() {
		return this.config;
	}

	/**
	 * Close the simulator instance and release all associated resources.
	 */
	public void close() {
		if (this.engine != null) {
			this.engine.close();
		}
	}


	/**
	 * A default simulator instance boot-strap method. If no arguments are
	 * specified, port 8123 is used as the service port and user's home folder
	 * is used as the home folder for the simulator.
	 * 
	 * @param args
	 *            Two optional arguments may be provided. If one argument is
	 *            present, it is used as the service port for the simulator. If
	 *            two arguments are present, the second is used as the home
	 *            folder for the simulator.
	 */
	public static void main(String[] args) {

		int port = 8123;

		if (args.length > 0) {
			port = Integer.parseInt(args[0]);
		}

		SimulatorConfiguration serverConfig = new SimulatorConfiguration();
		serverConfig.setPort(port);

		if (args.length > 1) {
			serverConfig.setProperty(SimulatorConfiguration.KINETIC_HOME, args[1]);
		}

		if (args.length > 2) {
			serverConfig.setSslPort(Integer.parseInt(args[2]));
		}

		KineticSimulator simulator = new KineticSimulator(serverConfig);

		logger.info("Kinetic simulator started, port: "
                + simulator.getServerConfiguration().getPort() + ", WWN="
                + serverConfig.getWorldWideName());
	}
}
