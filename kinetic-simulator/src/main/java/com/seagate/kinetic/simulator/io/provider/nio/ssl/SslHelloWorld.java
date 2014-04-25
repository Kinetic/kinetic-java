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
