/** Do NOT modify or remove this copyright and confidentiality notice!
 *
 * Copyright (c) 2001 - $Date: 2012/06/27 $ Seagate Technology, LLC.
 *
 * The code contained herein is CONFIDENTIAL to Seagate Technology, LLC.
 * Portions are also trade secret. Any use, duplication, derivation, distribution
 * or disclosure of this code, for any reason, not expressly authorized is
 * prohibited. All other rights are expressly reserved by Seagate Technology, LLC.
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
