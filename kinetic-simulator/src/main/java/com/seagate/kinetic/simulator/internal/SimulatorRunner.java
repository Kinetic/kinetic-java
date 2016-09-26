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
package com.seagate.kinetic.simulator.internal;

import java.util.logging.Logger;

import kinetic.client.KineticException;
import kinetic.simulator.KineticSimulator;
import kinetic.simulator.SimulatorConfiguration;

public class SimulatorRunner {
	private static final int OK = 0;
	private static final int ERROR = 1;
	private static final int MAX_PORT = 65535;
	private static final int MIN_PORT = 0;
	private static Logger logger = Logger.getLogger(SimulatorRunner.class
			.getName());

	public static void printHelp() {
		StringBuffer sb = new StringBuffer();
		sb.append("usage: startSimulator\n");
		sb.append("startSimulator -h|-help\n");
		sb.append("startSimulator [-port <port>] [-tlsport <port>] [-home <kinetichome>]");
		System.out.println(sb.toString());
	}

	public String getArgValue(String argName, String args[]) {
		if (null == argName || argName.isEmpty() || args.length <= 1) {
			return null;
		}

		int index = -1;
		for (int i = 0; i < args.length; i++) {
			if (argName.equalsIgnoreCase(args[i])) {
				index = i;
				break;
			}
		}

		if (index != -1 && args.length > (index + 1)
				&& !args[index + 1].isEmpty()) {
			if (args[index + 1].startsWith("-")) {
				throw new IllegalArgumentException("value can't start with -");
			}
			if (null == args[index + 1]) {
				throw new IllegalArgumentException("value can't be null");
			}
			return args[index + 1].trim();
		}

		return null;
	}

	public SimulatorConfiguration initConfig(String port, String tslPort,
			String dbHome) throws KineticException {
		SimulatorConfiguration simulatorConfig = new SimulatorConfiguration();

		if (port != null && !port.isEmpty()) {
			validatePort(port);
			simulatorConfig.setPort(Integer.parseInt(port));
		}

		if (tslPort != null && !tslPort.isEmpty()) {
			validatePort(tslPort);
			simulatorConfig.setSslPort(Integer.parseInt(tslPort));
		}

		if (dbHome != null && !dbHome.isEmpty()) {
			validateDbHome(dbHome);
			simulatorConfig.setProperty(SimulatorConfiguration.KINETIC_HOME,
					dbHome);
		}

		return simulatorConfig;
	}

	private void validatePort(String port){
		if (port == null || port.isEmpty()) {
			throw new IllegalArgumentException("Port can not be empty");
		}

		int portIn;
		try {
            portIn = Integer.parseInt(port);
		} catch (NumberFormatException e) {
		    throw new IllegalArgumentException("Illegal port: " + port);
		}
		    
		if (portIn < MIN_PORT || portIn > MAX_PORT) {
		    throw new IllegalArgumentException("Port out of range: " + port);
		}

	}

	private void validateDbHome(String dbHome){
		if (dbHome == null || dbHome.isEmpty()) {
			throw new IllegalArgumentException("Kinetic home can not be empty");
		}
	}

	private void validateArgs(String[] args)
	{
		for (String arg: args)
		{
			if (arg.startsWith("-")) {
				if (!arg.equalsIgnoreCase("-h")
						&& !arg.equalsIgnoreCase("-help")
						&& !arg.equalsIgnoreCase("-port")
						&& !arg.equalsIgnoreCase("-tlsport")
						&& !arg.equalsIgnoreCase("-home")) {
					throw new IllegalArgumentException("Illegal arguments");
				}
			}
		}
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SimulatorRunner simulatorRunner;
		KineticSimulator simulator;
		SimulatorConfiguration simulatorConfig;
		
		if (args.length < 1) {
			simulatorConfig = new SimulatorConfiguration();
			simulator = new KineticSimulator(simulatorConfig);

			logger.info("Kinetic simulator started, port: "
					+ simulator.getServerConfiguration().getPort());
		} else {
			try {
				
				simulatorRunner = new SimulatorRunner();
				simulatorRunner.validateArgs(args);
				if (args[0].equalsIgnoreCase("-help")
						|| args[0].equalsIgnoreCase("-h")) {
					SimulatorRunner.printHelp();
					System.exit(OK);
				} else {
					String port = simulatorRunner.getArgValue("-port", args);
					String tslport = simulatorRunner.getArgValue("-tlsport",
							args);
					String dbhome = simulatorRunner.getArgValue("-home",
							args);

					simulatorConfig = simulatorRunner.initConfig(port, tslport,
							dbhome);
					simulator = new KineticSimulator(simulatorConfig);

					logger.info("Kinetic simulator started, port: "
							+ simulator.getServerConfiguration().getPort());
				}
			} catch (KineticException e) {
				System.exit(ERROR);
			}
		}
	}

}
