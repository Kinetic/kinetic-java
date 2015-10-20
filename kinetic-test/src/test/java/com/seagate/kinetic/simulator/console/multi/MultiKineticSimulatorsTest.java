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
import java.util.Arrays;
import java.util.Random;
import java.util.logging.Logger;

import kinetic.client.ClientConfiguration;
import kinetic.client.Entry;
import kinetic.client.EntryMetadata;
import kinetic.client.KineticClient;
import kinetic.client.KineticClientFactory;
import kinetic.simulator.KineticSimulator;
import kinetic.simulator.SimulatorConfiguration;

/**
 * Test running multiple servers within a JVM.
 * 
 * @author Chiaming Yang
 * 
 */
@Test(groups = {"simulator"})
public class MultiKineticSimulatorsTest {

	Logger logger = Logger
			.getLogger(MultiKineticSimulatorsTest.class.getName());

	private final int max = 10;
	private final int portbase = 10100;

	private final SimulatorConfiguration[] sconfigs = new SimulatorConfiguration[max];
	private final ClientConfiguration[] cconfigs = new ClientConfiguration[max];

	private final KineticSimulator[] servers = new KineticSimulator[max];

	@BeforeMethod
    public void setUp() throws Exception {
		// server configs

		for (int i = 0; i < max; i++) {

			int myport = portbase + i;

			// client configuratiomn
			cconfigs[i] = new ClientConfiguration();
			cconfigs[i].setPort(myport);

			// nio service thread number in pool
			cconfigs[i].setNioServiceThreads(1);


			// server configuration
			sconfigs[i] = new SimulatorConfiguration();
			sconfigs[i].setPort(myport);
			sconfigs[i].setSslPort(myport + 100);

			sconfigs[i].put(SimulatorConfiguration.PERSIST_HOME, "instance_"
					+ myport);

			// set nio services thread pool size
			sconfigs[i].setNioServiceBossThreads(1);
			sconfigs[i].setNioServiceWorkerThreads(1);

			servers[i] = new KineticSimulator(sconfigs[i]);

			logger.info("server started, port=" + myport);

			Thread.sleep(200);
		}

	}

	@Test
	public void simpleTest() throws Exception {

		for (int i = 0; i < max; i++) {
			KineticClient kineticClient = KineticClientFactory
					.createInstance(cconfigs[i]);

			Random random = new Random();
			byte[] key = new byte[20];
			random.nextBytes(key);

			byte[] value = new byte[20];
			random.nextBytes(value);

			byte[] initVersion = new byte[20];
			random.nextBytes(initVersion);

			EntryMetadata entryMetadata = new EntryMetadata();
			Entry versioned = new Entry(key, value, entryMetadata);

			/**
			 * put key/value and validate.
			 */
			Entry dbVersioned = kineticClient.put(versioned, initVersion);

			// get from db
			Entry vFromDb = kineticClient.get(key);

			// check value
			assertTrue(Arrays.equals(vFromDb.getValue(), value));

			// check version
			assertTrue(Arrays.equals(dbVersioned.getEntryMetadata().getVersion(),
					vFromDb.getEntryMetadata().getVersion()));

			logger.info("validated value/version for instance: "
					+ cconfigs[i].getPort());

			kineticClient.close();
		}
	}

	@AfterMethod
    public void tearDown() throws Exception {

		for (int i = 0; i < max; i++) {

			servers[i].close();

			logger.info("server close, myport="
					+ servers[i].getServerConfiguration().getPort());
		}
	}

}
