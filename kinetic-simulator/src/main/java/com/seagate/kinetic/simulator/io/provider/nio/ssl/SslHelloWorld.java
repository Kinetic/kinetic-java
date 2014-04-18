/** Do NOT modify or remove this copyright and confidentiality notice!
 *
 * Copyright (c) 2001 - $Date: 2012/06/27 $ Seagate Technology, LLC.
 *
 * The code contained herein is CONFIDENTIAL to Seagate Technology, LLC.
 * Portions are also trade secret. Any use, duplication, derivation, distribution
 * or disclosure of this code, for any reason, not expressly authorized is
 * prohibited. All other rights are expressly reserved by Seagate Technology, LLC.
 */
package com.seagate.kinetic.simulator.io.provider.nio.ssl;

import kinetic.client.ClientConfiguration;
import kinetic.client.Entry;
import kinetic.client.KineticClient;
import kinetic.client.KineticClientFactory;
import kinetic.client.KineticException;

import com.google.protobuf.ByteString;

public class SslHelloWorld {

	public static void main(String[] args) throws KineticException,
	InterruptedException {

		ClientConfiguration clientConfig = new ClientConfiguration();
		clientConfig.setUseSsl(true);
		clientConfig.setPort(8443);

		KineticClient lc = KineticClientFactory.createInstance(clientConfig);

		Entry v1 = new Entry();
		v1.setKey(ByteString.copyFromUtf8("some_key").toByteArray());
		v1.setValue(ByteString.copyFromUtf8("some_value").toByteArray());

		lc.put(v1, null);

		System.out.println("put some_key ");


		Entry v2 = lc
				.get(ByteString.copyFromUtf8("some_key")
						.toByteArray());

		System.out.println("get reply, entry="
				+ ByteString.copyFrom(v2.getValue()).toStringUtf8());


		boolean deleted = lc.delete(v2);

		System.out.println("deleted=" + deleted);

		lc.close();
	}

}
