/**
 * 
 * Copyright (C) 2014 Seagate Technology.
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
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
