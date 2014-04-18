package com.seagate.kinetic.simulator.performance;

import kinetic.simulator.KineticSimulator;
import kinetic.simulator.SimulatorConfiguration;

/**
 * 
 * Async and sync performance test server side
 * <p>
 * 
 * @author Chenchong(Emma) Li
 * 
 */
public class PerfServer {
	private static int PORT = 8123;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length != 0 && args.length != 1) {
			System.out.println("Parameter error!!!");
			System.out.println("Usage:");
			System.out.println("PerfServer [Server_Port]");
			System.out.println("Welcome to try again.");
			return;
		}
		if (args.length > 0 && args.length == 1) {
			PORT = Integer.parseInt(args[0]);
			System.out.println("Server_Port=" + PORT);
		}
		SimulatorConfiguration serverConfig = new SimulatorConfiguration();
		serverConfig.put(SimulatorConfiguration.PERSIST_HOME, "performance");
		serverConfig.setPort(PORT);
		@SuppressWarnings("unused")
		KineticSimulator server = new KineticSimulator(serverConfig);
		System.out.println("KineticClient perf Server started, port=" + PORT);

	}

}
