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
package com.seagate.kinetic.usage.p2p;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Random;

import kinetic.client.ClientConfiguration;
import kinetic.client.Entry;
import kinetic.client.EntryMetadata;
import kinetic.client.KineticClient;
import kinetic.client.KineticClientFactory;
import kinetic.client.KineticException;
import kinetic.simulator.KineticSimulator;
import kinetic.simulator.SimulatorConfiguration;

public class KineticUsageExample {

	public void helloworld(ClientConfiguration config)
			throws UnsupportedEncodingException, KineticException {

		/**
		 * get a new instance of kinetic client.
		 */
		KineticClient kineticClient = KineticClientFactory.createInstance(config);

		/**
		 * start playing with the APIs.
		 */
		Random random = new Random();
		byte[] key = new byte[20];
		random.nextBytes(key);

		byte[] value = toByteArray("world");

		byte[] initVersion = new byte[20];
		random.nextBytes(initVersion);

		Entry entry = new Entry(key, value, null);

		/**
		 * put key/value and validate.
		 */
		Entry dbVersioned = kineticClient.put(entry, initVersion);

		Entry vFromDb = kineticClient.get(key);

		if (Arrays.equals(vFromDb.getValue(), value) == false) {
			throw new RuntimeException("put/get validation failed");
		}

		byte[] newWorld = toByteArray("new world");

		// set value to "new world" for the entry.
		dbVersioned.setValue(newWorld);
		EntryMetadata entryMetadata1 = new EntryMetadata();
		entryMetadata1.setVersion(initVersion);

		/**
		 * update entry with new version in the persistent store
		 */
		Entry dbVersioned2 = kineticClient.put(dbVersioned, toByteArray("2"));

		if (Arrays.equals(dbVersioned2.getEntryMetadata().getVersion(),
				toByteArray("2")) == false) {
			throw new RuntimeException("error validating dbVersion");
		}

		Entry v2FromDb = kineticClient.get(key);
		if (Arrays.equals(v2FromDb.getValue(), newWorld) == false) {
			throw new RuntimeException("put/get new world validation failed");
		}

		System.out.println("put/get twice successfully, deleting entry ...");

		kineticClient.delete(dbVersioned2);

		Entry versionedGetFromDb = kineticClient.get(key);

		if (versionedGetFromDb != null) {
			throw new RuntimeException("key not deleted");
		}

		/**
		 * close and release
		 */
		kineticClient.close();

		System.out.println("entry deleted., test ran successfully.");
	}

	public static byte[] toByteArray(String s)
			throws UnsupportedEncodingException {
		return s.getBytes("utf8");
	}

	public static void main(String[] args) throws Exception {

		int port = 8123;

		SimulatorConfiguration serverConfig = new SimulatorConfiguration();
		serverConfig.setPort(port);
		KineticSimulator s = new KineticSimulator(serverConfig);

		for (int i = 0; i < 1; i++) {

			/**
			 * set up client configuration.
			 */
			ClientConfiguration config = new ClientConfiguration();
			config.setHost("localhost");
			config.setPort(port);

			KineticUsageExample example = new KineticUsageExample();

			example.helloworld(config);

			System.out.println("test iteration: " + i);
		}
		s.close();
	}
}
