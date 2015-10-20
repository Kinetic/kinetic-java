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
