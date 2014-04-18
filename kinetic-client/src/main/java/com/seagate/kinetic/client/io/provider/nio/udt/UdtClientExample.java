/** Do NOT modify or remove this copyright and confidentiality notice!
 *
 * Copyright (c) 2001 - $Date: 2012/06/27 $ Seagate Technology, LLC.
 *
 * The code contained herein is CONFIDENTIAL to Seagate Technology, LLC.
 * Portions are also trade secret. Any use, duplication, derivation, distribution
 * or disclosure of this code, for any reason, not expressly authorized is
 * prohibited. All other rights are expressly reserved by Seagate Technology, LLC.
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
