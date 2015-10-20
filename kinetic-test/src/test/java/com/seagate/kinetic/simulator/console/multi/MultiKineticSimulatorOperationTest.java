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
package com.seagate.kinetic.simulator.console.multi;

import static org.testng.AssertJUnit.assertTrue;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;
import org.testng.Assert;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import kinetic.client.ClientConfiguration;
import kinetic.client.Entry;
import kinetic.client.EntryMetadata;
import kinetic.client.KineticClient;
import kinetic.client.KineticClientFactory;
import kinetic.client.KineticException;
import kinetic.simulator.KineticSimulator;
import kinetic.simulator.SimulatorConfiguration;

/**
 * 
 * multi server, start server, close server then restart server, do the
 * put/get/delete operation
 * <p>
 * 
 * @author Chenchong(Emma) Li
 * 
 */

@Test(groups = {"simulator"})
public class MultiKineticSimulatorOperationTest {
	private final Logger logger = Logger
			.getLogger(MultiKineticSimulatorOperationTest.class.getName());

	private final int INIT_PORT = 18123;
	private final int INIT_SSL_PORT = 18443;
	private final int SERVER_COUNTS = 10;
	private final int OPERATE_COUNTS = 100;
	private final long SLEEP_TIME = 200;
	private final byte[] INIT_VERSION = "0".getBytes();

	private final KVGenerator kvGenerator = new KVGenerator();

	private final List<SimulatorConfiguration> serverConfigs = new ArrayList<SimulatorConfiguration>();
	private final List<ClientConfiguration> clientConfigs = new ArrayList<ClientConfiguration>();
	private final List<KineticSimulator> servers = new ArrayList<KineticSimulator>();

	@BeforeMethod
    public void setUp() throws Exception {
		init();

		startServers();
		kvGenerator.reset();
		for (int i = 0; i < SERVER_COUNTS; i++) {
			KineticClient kineticClient = KineticClientFactory
					.createInstance(clientConfigs.get(i));
			multiDelete(kineticClient, OPERATE_COUNTS);

			kineticClient.close();
		}
		closeServers();

	}

	@AfterMethod
    public void tearDown() throws Exception {
		startServers();
		kvGenerator.reset();
		for (int i = 0; i < SERVER_COUNTS; i++) {
			KineticClient kineticClient = KineticClientFactory
					.createInstance(clientConfigs.get(i));
			multiDelete(kineticClient, OPERATE_COUNTS);

			kineticClient.close();
		}
		closeServers();
	}

	@Test
	public void multiServerOperationTest() throws KineticException,
	UnsupportedEncodingException, InterruptedException {

		// step 1
		// start server, multi put, close server
		startServers();
		kvGenerator.reset();
		for (int i = 0; i < SERVER_COUNTS; i++) {
			KineticClient kineticClient = KineticClientFactory
					.createInstance(clientConfigs.get(i));
			multiPut(kineticClient, OPERATE_COUNTS);

			kineticClient.close();
		}
		closeServers();
		logger.info("multi put finished");

		// step 2
		// start server, multi get, close server
		startServers();
		kvGenerator.reset();
		for (int i = 0; i < SERVER_COUNTS; i++) {
			KineticClient kineticClient = KineticClientFactory
					.createInstance(clientConfigs.get(i));
			multiGetAndVerify(kineticClient, OPERATE_COUNTS, false);

			kineticClient.close();
		}
		closeServers();
		logger.info("multi get finished");

		// step 3
		// start server, multi delete, close server
		startServers();
		kvGenerator.reset();
		for (int i = 0; i < SERVER_COUNTS; i++) {
			KineticClient kineticClient = KineticClientFactory
					.createInstance(clientConfigs.get(i));
			multiDelete(kineticClient, OPERATE_COUNTS);

			kineticClient.close();
		}
		closeServers();

		// step 4
		// start server, multi get, close server
		startServers();
		kvGenerator.reset();
		for (int i = 0; i < SERVER_COUNTS; i++) {
			KineticClient kineticClient = KineticClientFactory
					.createInstance(clientConfigs.get(i));
			multiGetAndVerify(kineticClient, OPERATE_COUNTS, true);

			kineticClient.close();
		}
		closeServers();
	}

	private void init() {
		for (int i = 0; i < SERVER_COUNTS; i++) {
			SimulatorConfiguration serverConfig = new SimulatorConfiguration();
			ClientConfiguration clientConfig = new ClientConfiguration();

			int port = INIT_PORT + i;
			int sslPort = INIT_SSL_PORT + i;
			serverConfig.setPort(port);
			serverConfig.setSslPort(sslPort);
			serverConfig.put(SimulatorConfiguration.PERSIST_HOME, "multi_server_"
					+ port);

			// set nio services thread pool size
			serverConfig.setNioServiceBossThreads(1);
			serverConfig.setNioServiceWorkerThreads(1);

			clientConfig.setPort(port);

			// nio threads in pool
			clientConfig.setNioServiceThreads(1);

			serverConfigs.add(serverConfig);
			clientConfigs.add(clientConfig);

		}
	}

	private void startServers() throws InterruptedException {
		servers.clear();
		for (int i = 0; i < SERVER_COUNTS; i++) {
			KineticSimulator server = new KineticSimulator(serverConfigs.get(i));
			servers.add(server);

			//			logger.info("server start, port="
			//					+ servers.get(i).getServerConfiguration().getPort());

			Thread.sleep(SLEEP_TIME);
		}
	}

	private void closeServers() {
		for (int i = 0; i < SERVER_COUNTS; i++) {
			servers.get(i).close();

			//			logger.info("server close, port="
			//					+ servers.get(i).getServerConfiguration().getPort());
		}
	}

	private void multiPut(KineticClient client, int putCounts)
			throws UnsupportedEncodingException, KineticException {
		Entry versioned = null;

		for (int i = 0; i < putCounts; i++) {
			String key = kvGenerator.getNextKey();
			String value = kvGenerator.getValue(key);
			EntryMetadata entryMetadata = new EntryMetadata();
			versioned = new Entry(toByteArray(key), toByteArray(value),
					entryMetadata);
			client.put(versioned, INIT_VERSION);

		}
	}

	private void multiGetAndVerify(KineticClient client, int getCounts,
			boolean afterDelete) throws UnsupportedEncodingException,
			KineticException {
		for (int i = 0; i < getCounts; i++) {
			String key = kvGenerator.getNextKey();
			Entry vGetReturn = client.get(toByteArray(key));

			if (afterDelete) {
				if (null != vGetReturn) {
					Assert.fail("the value is not null after deleting");
				}
			} else {
				if (null == vGetReturn) {
					Assert.fail("get a null vesioned");
				}

				assertTrue(equals(toByteArray(kvGenerator.getValue(key)),
						vGetReturn.getValue()));
			}
		}

	}

	private void multiDelete(KineticClient client, int deleteCounts)
			throws KineticException, UnsupportedEncodingException {
		Entry versioned = null;

		for (int i = 0; i < deleteCounts; i++) {
			String key = kvGenerator.getNextKey();
			versioned = client.get(toByteArray(key));
			if (null != versioned && null != versioned.getKey()) {
				client.delete(versioned);
			}
		}
	}

	// convert String to byte[]
	private byte[] toByteArray(String s) throws UnsupportedEncodingException {
		return s.getBytes("utf8");
	}

	// compare two byte[] equal
	private boolean equals(byte[] byteArray1, byte[] byteArray2) {
		if (null == byteArray1 && null == byteArray2) {
			return true;
		} else if (null == byteArray1 || null == byteArray2) {
			return false;
		} else {
			if (byteArray1.length != byteArray2.length) {
				return false;
			} else {
				for (int i = 0; i < byteArray1.length; i++) {
					if (byteArray1[i] != byteArray2[i]) {
						return false;
					}
				}
			}
		}

		return true;
	}

	// generator the key value, support reset and getNextKey and getValue
	class KVGenerator {
		private String keyPrefix = "key";
		private String valuePrefix = "value";
		private int start = 0;
		private int current = 0;
		private final int alignLength = ("" + Integer.MAX_VALUE).length() + 1;

		public KVGenerator() {

		}

		private String align(int id) {
			String idAsString = "" + id;
			int length = idAsString.length();
			for (int i = 0; i < alignLength - length; i++) {
				idAsString = "0" + idAsString;
			}
			return idAsString;
		}

		public KVGenerator(int start) {
			this.start = start;
			this.current = start;
		}

		public KVGenerator(String keyPrefix, String valuePrefix, int start) {
			this.keyPrefix = keyPrefix;
			this.valuePrefix = valuePrefix;
			this.start = start;
			this.current = start;
		}

		public void reset() {
			this.current = start;
		}

		public synchronized String getNextKey() {
			if (current >= Integer.MAX_VALUE) {
				throw new RuntimeException("out of keys");
			}
			return keyPrefix + align(current++);
		}

		public String getValue(String key) {
			int keyId = Integer.parseInt(key.replaceAll(keyPrefix, ""));
			return valuePrefix + keyId;
		}
	}
}
