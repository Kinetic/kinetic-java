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
package com.seagate.kinetic.example.advanced;

import kinetic.client.ClientConfiguration;
import kinetic.client.Entry;
import kinetic.client.KineticException;
import kinetic.client.advanced.AdvancedKineticClient;
import kinetic.client.advanced.AdvancedKineticClientFactory;
import kinetic.client.advanced.PersistOption;

/**
 * An example that uses PersistOption to perform PUT/DELETE operations.
 */
public class AdvancedKineticClientExample {

	public static void main(String[] args) throws KineticException,
	InterruptedException {

		/**
		 * new instance of client configuration
		 */
		ClientConfiguration clientConfig = new ClientConfiguration();

		/**
		 * new instance of advanced Kinetic client
		 */
		AdvancedKineticClient lc = AdvancedKineticClientFactory
				.createAdvancedClientInstance(clientConfig);

		/**
		 * construct a new entry
		 */
		Entry entry1 = new Entry();

		/**
		 * entry key
		 */
		byte[] key = "key1".getBytes();

		/**
		 * entry value
		 */
		byte[] value = "value1".getBytes();

		/**
		 * set entry key
		 */
		entry1.setKey(key);

		/**
		 * set entry value
		 */
		entry1.setValue(value);

		/**
		 * forced put with ASYNC option
		 */
		entry1 = lc.putForced(entry1, PersistOption.ASYNC);

		/**
		 * forced delete with FLUSH option
		 */
		lc.deleteForced(key, PersistOption.FLUSH);

		/**
		 * get entry
		 */
		Entry entry2 = lc.get(key);

		/**
		 * verify if deleted
		 */
		if (entry2 != null) {
			throw new RuntimeException(
					"operation verification failed - entry not deleted");
		}

		/**
		 * close advance client
		 */
		lc.close();

		System.out
				.println("Advanced Kietic API Example finished successfully.");
	}

}
