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
package com.seagate.kinetic.usage.async;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import kinetic.client.ClientConfiguration;
import kinetic.client.Entry;
import kinetic.client.EntryMetadata;
import kinetic.client.KineticClient;
import kinetic.client.KineticClientFactory;
import kinetic.client.KineticException;

/**
 * 
 * put async example.
 * <p>
 * 
 * @author Chenchong(Emma) Li
 * 
 */
public class ApplicationExample {

	private static int limit = 50;
	private static int putCount = 1000;

	public static void main(String[] args) throws KineticException,
			InterruptedException {
		Logger logger = Logger.getLogger(ApplicationExample.class.getName());

		if (2 == args.length) {
			limit = Integer.parseInt(args[0]);
			putCount = Integer.parseInt(args[1]);
		}

		ClientConfiguration clientConfig = new ClientConfiguration();
		KineticClient client = null;

		clientConfig.setHost("localhost");
		clientConfig.setPort(8123);
		client = KineticClientFactory.createInstance(clientConfig);

		byte[] value = "value".getBytes();
		byte[] newVersion = "0".getBytes();

		List<byte[]> keys = new ArrayList<byte[]>();

		// generate key
		for (int i = 0; i < putCount; i++) {
			byte[] key = ("key" + i).getBytes();
			keys.add(key);
		}

		// put entry
		EntryMetadata emd = new EntryMetadata();
		PutAsyncUsage putAsync = new PutAsyncUsage(limit);
		for (int j = 0; j < putCount; j++) {
			byte[] key = keys.get(j);
			Entry entry = new Entry(key, value, emd);
			putAsync.Put(entry, newVersion, client);
		}

		// clean up
		EntryMetadata emdD = new EntryMetadata();
		emdD.setVersion("0".getBytes());
		for (int k = 0; k < keys.size(); k++) {
			Entry entry = new Entry(keys.get(k), value, emdD);
			logger.info("delete key=" + new String(entry.getKey()));
			client.delete(entry);
		}

		client.close();
	}
}
