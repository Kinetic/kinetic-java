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
package com.seagate.kinetic;

import org.testng.Assert;
import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import kinetic.client.ClientConfiguration;
import kinetic.client.KineticClient;
import kinetic.client.KineticClientFactory;
import kinetic.client.KineticException;
import kinetic.simulator.KineticSimulator;
import kinetic.simulator.SimulatorConfiguration;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import com.leacox.process.FinalizedProcess;
import com.leacox.process.FinalizedProcessBuilder;
import com.seagate.kinetic.client.internal.MessageFactory;
import com.seagate.kinetic.common.lib.KineticMessage;
import com.seagate.kinetic.proto.Kinetic.Command;
import com.seagate.kinetic.simulator.lib.MyLogger;

/**
 * Wrapper around java simulator for use in tests. Handles checking System
 * properties to see if the user wanted to run the tests against the Java
 * simulator or some other external device and starts a server as appropriate
 */
public class KineticTestSimulator {
	private final static Logger LOG = MyLogger.get();
	private final String host;
	private final int port;
	private final KineticSimulator kineticServer;
	private final SimulatorConfiguration javaServerConfiguration;
	private final FinalizedProcess externalKineticServer;

	/**
	 *
	 * Create test simulator.
	 *
	 * @param clearExistingDatabase
	 *            If clear existing data, set true, if not, set false.
	 *
	 */
	public KineticTestSimulator(boolean clearExistingDatabase)
			throws IOException, InterruptedException, KineticException {
		this(clearExistingDatabase, new SimulatorConfiguration());
	}

	/**
	 *
	 * Create test simulator.
	 *
	 * @param clearExistingDatabase
	 *            If clear existing data, set true, if not, set false.
	 * @param serverConfiguration
	 *            Using different configuration to generate different simulator.
	 *
	 * @throws KineticException
	 *             if any internal error occurred.
	 * @throws IOException
	 *             if any IO error occurred
	 * @throws InterruptedException
	 *             if any Interrupt error occurred
	 *
	 * @see SimulatorConfiguration
	 */
	public KineticTestSimulator(boolean clearExistingDatabase,
			SimulatorConfiguration serverConfiguration) throws IOException,
			InterruptedException, KineticException {
		javaServerConfiguration = serverConfiguration;

		String kineticPath = System.getProperty("KINETIC_PATH");
		String kineticHost = System.getProperty("KINETIC_HOST");

		int requestedPort = Integer.parseInt(System.getProperty("KINETIC_PORT",
				"8123"));

		if (!Boolean.parseBoolean(System.getProperty("RUN_AGAINST_EXTERNAL"))) {
			port = 8123;
			LOG.fine("Starting java simulator on port " + port);
			host = "localhost";
			serverConfiguration.setPort(port);

			if (clearExistingDatabase) {
				deleteJavaServerAuxilaryData();

				String defaultHome = System.getProperty("user.home")
						+ File.separator + "leafcutter";

				String leafcutterHome = serverConfiguration.getProperty(
						SimulatorConfiguration.KINETIC_HOME, defaultHome);

				FileUtils.deleteQuietly(new File(leafcutterHome));
			}

			// set nio services thread pool size
			serverConfiguration.setNioServiceBossThreads(1);
			serverConfiguration.setNioServiceWorkerThreads(1);

			kineticServer = new KineticSimulator(serverConfiguration);
			externalKineticServer = null;
		} else if (kineticPath != null) {
			port = requestedPort;
			LOG.fine("Running tests against external simulator at "
					+ kineticPath + " using port " + port);
			host = "localhost";
			kineticServer = null;

			FinalizedProcessBuilder finalizedProcessBuilder = new FinalizedProcessBuilder(
					"killall", "-9", "kineticd");
			finalizedProcessBuilder.start().waitFor(10 * 1000);
			Thread.sleep(500);

			// Since the cluster version is checked before performing an ISE we
			// need to manually remove
			// the file used to store the cluster version
			if (clearExistingDatabase) {
				final String workingDirectory = FilenameUtils
						.getFullPath(kineticPath);
				final String clusterStorePath = FilenameUtils.concat(
						workingDirectory, "cluster_version");
				FileUtils.deleteQuietly(new File(clusterStorePath));
			}

			finalizedProcessBuilder = new FinalizedProcessBuilder(kineticPath);
			finalizedProcessBuilder.directory(new File("."));
			finalizedProcessBuilder.gobbleStreamsWithLogging(true);

			externalKineticServer = finalizedProcessBuilder.start();
			waitForServerReady();
		} else {
			host = kineticHost;
			port = requestedPort;
			LOG.fine("Running tests against " + host + ":" + port);

			kineticServer = null;
			externalKineticServer = null;
		}

		if (clearExistingDatabase) {
			
		    KineticClient kineticClient = buildClient();
			
		    KineticMessage km = MessageFactory.createKineticMessageWithBuilder();
		    Command.Builder commandBuillder = (Command.Builder) km.getCommand();
		    
		    commandBuillder.getBodyBuilder().getSetupBuilder();
		    
		    /**
		     * XXX protocol-3.0.0
		     */
		    
			//com.seagate.kinetic.proto.Kinetic.Message.Builder builder = com.seagate.kinetic.proto.Kinetic.Message
			//		.newBuilder();
			//builder.getCommandBuilder().getBodyBuilder().getSetupBuilder()
			//.setInstantSecureErase(true);
			
		    commandBuillder
			.getHeaderBuilder()
			.setMessageType(
					com.seagate.kinetic.proto.Kinetic.Command.MessageType.SETUP);

			kineticClient.request(km);
			kineticClient.close();
		}
	}

	/**
	 *
	 * Sleep enough time for simulator start and close.
	 *
	 * @throws InterruptedException
	 *             If any interrupt error occurred.
	 *
	 */
	private void waitForServerReady() throws InterruptedException {
		final int pollingIntervalMS = 50;
		final int maxWaitTimeSec = 10;

		long waitStartTime = System.currentTimeMillis();
		float elapsedTimeMS;
		while (true) {
			Thread.sleep(pollingIntervalMS);

			elapsedTimeMS = System.currentTimeMillis() - waitStartTime;
			if (elapsedTimeMS > maxWaitTimeSec * 1000) {
				throw new RuntimeException("Server never became available");
			}
			try {
				KineticClient kineticClient = buildClient();
				kineticClient.noop();
				kineticClient.close();
				break;
			} catch (KineticException e) {
				// Since all exceptions get turned into KineticException we have
				// to manually check the message
				if (e.getMessage().contains("Kinetic Command Exception: ")) {
					break;
				}
				// Ignore this exception because it probably means that the
				// server isn't ready yet so
				// we'll just take a brief nap and try again in a bit
			}
		}

		LOG.info("Server ready after waiting ms: " + elapsedTimeMS);
	}

	/**
	 *
	 * Get simulator port info.
	 *
	 * @return port return port info.
	 *
	 */
	public int getPort() {
		return port;
	}

	/**
	 *
	 * close simulator.
	 *
	 * @throws IOException
	 *             if any IO error occurred.
	 *
	 */
	public void shutdown() throws IOException {
		if (kineticServer != null) {
			LOG.fine("Stopping Java simulator");
			kineticServer.close();
		}

		if (externalKineticServer != null) {
			externalKineticServer.close();
		}
	}

	/**
	 *
	 * Generate a Kinetic client.
	 *
	 * @throws KineticException
	 *             if any internal error occurred.
	 *
	 */
	public KineticClient buildClient() throws KineticException {
		return KineticClientFactory.createInstance(buildClientConfig());
	}

	/**
	 *
	 * Generate a Kinetic client configuration.
	 *
	 */
	public ClientConfiguration buildClientConfig() {
		ClientConfiguration clientConfig = new ClientConfiguration();
		clientConfig.setHost(host);
		clientConfig.setPort(getPort());

		// nio service threads in pool
		clientConfig.setNioServiceThreads(1);

		return clientConfig;
	}

	/**
	 *
	 * Clean data after every test.
	 *
	 */
	public void deleteJavaServerAuxilaryData() {
		String kineticHome = System.getProperty("user.home") + File.separator
				+ "kinetic";
		if (javaServerConfiguration
				.getProperty(SimulatorConfiguration.KINETIC_HOME) != null) {
			kineticHome = javaServerConfiguration
					.getProperty(SimulatorConfiguration.KINETIC_HOME);
		}

		// Delete ACLs
		FileUtils
		.deleteQuietly(new File(kineticHome + File.separator + ".acl"));
		FileUtils.deleteQuietly(new File(kineticHome + File.separator
				+ ".acl.bak"));

		// Delete setup files
		FileUtils.deleteQuietly(new File(kineticHome + File.separator
				+ ".setup"));
		FileUtils.deleteQuietly(new File(kineticHome + File.separator
				+ ".setup.bak"));

		// Delete history files
		try {
			FileUtils.deleteDirectory(new File(kineticHome
					+ File.separator
					+ javaServerConfiguration
					.getProperty(SimulatorConfiguration.PERSIST_HOME)));
			FileUtils.deleteQuietly(new File(kineticHome + File.separator
					+ ".acl"));
		} catch (IOException e) {
			Assert.fail("delete directory failed" + e.getMessage());
		}
	}
}
