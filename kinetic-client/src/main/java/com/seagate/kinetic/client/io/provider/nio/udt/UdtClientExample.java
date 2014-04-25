/**
 * Copyright (C) 2014 Seagate Technology.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package com.seagate.kinetic.client.io.provider.nio.udt;

import java.util.Arrays;

import kinetic.client.ClientConfiguration;
import kinetic.client.Entry;
import kinetic.client.KineticClient;
import kinetic.client.KineticClientFactory;
import kinetic.client.KineticException;

import com.google.protobuf.ByteString;

/**
 *
 * Please note: This class is for evaluation only and in prototype state.
 * <p>
 *
 * An example to run Kinetic client with UDT transport.
 *
 * @author chiaming
 *
 */
public class UdtClientExample {

	public void run() throws KineticException, InterruptedException {

		// set property to use UDT transport
		System.setProperty("kinetic.io.udt", "true");

		ClientConfiguration clientConfig = new ClientConfiguration();

		KineticClient lc = KineticClientFactory
				.createInstance(clientConfig);

		Entry entry1 = new Entry();
		byte[] key = ByteString.copyFromUtf8("demo_key1").toByteArray();
		byte[] value = ByteString.copyFromUtf8("demo_value1").toByteArray();

		entry1 = new Entry();
		entry1.setKey(key);
		entry1.setValue(value);

		lc.putForced(entry1);

		Entry e1 = lc.get(key);
		if (Arrays.equals(e1.getValue(), value) == false) {
			throw new RuntimeException("get comparison failed.");
		}

		// entry 2
		Entry entry2 = new Entry();

		String sk2 = "demo_key2";

		byte[] key2 = ByteString.copyFromUtf8(sk2).toByteArray();
		byte[] value2 = ByteString.copyFromUtf8("demo_value2")
				.toByteArray();

		entry2 = new Entry();
		entry2.setKey(key2);
		entry2.setValue(value2);

		// force put
		lc.putForced(entry2);

		Entry e2 = lc.get(key2);
		if (Arrays.equals(e2.getValue(), value2) == false) {
			throw new RuntimeException("get comparison failed.");
		}

		lc.close();

		System.out.println("UDT client example finished.");

	}

	/**
	 * Main class to start UDT example.
	 *
	 * @param args
	 *            no arg is supported in this class.
	 * @throws Exception
	 *             if any errors occur
	 */
	public static void main(String[] args) throws Exception {
		new UdtClientExample().run();
	}

}
