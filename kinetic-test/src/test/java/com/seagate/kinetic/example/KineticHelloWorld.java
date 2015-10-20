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

/**
 * Copyright (c) 2013 Seagate Technology LLC
 * All rights reserved.

 * Redistribution and use in source and binary forms, with or without modification, are permitted
 * provided that the following conditions are met:

 * 1) Redistributions of source code must retain the above copyright notice, this list of conditions
 * and the following disclaimer.

 * 2) Redistributions in binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other materials provided
 * with the distribution.

 * 3) Neither the name of Seagate Technology nor the names of its contributors may be used to
 * endorse or promote products derived from this software without specific prior written permission
 * from Seagate Technology.

 * 4) No patent or trade secret license whatsoever, either express or implied, is granted by Seagate
 * Technology or its contributors by this copyright license.

 * 5) All modifications must be reposted in source code form in a manner that allows user to
 * readily access the source code.

 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
 * CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY, NONINFRINGEMENT AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.  THE COPYRIGHT
 * HOLDERS AND CONTRIBUTORS DISCLAIM ALL LIABILITY FOR
 * INTELLECTUAL PROPERTY INFRINGEMENT RELATED TO THIS SOFTWARE.
 */

package com.seagate.kinetic.example;

import kinetic.client.ClientConfiguration;
import kinetic.client.Entry;
import kinetic.client.KineticClient;
import kinetic.client.KineticClientFactory;
import kinetic.client.KineticException;
import kinetic.simulator.KineticSimulator;
import kinetic.simulator.SimulatorConfiguration;

/**
 * Kinetic Hello World sample code.
 * <p>
 * This example performs the following operations.
 * <ul>
 * <li>1. start Kinetic simulator.
 * <li>2. start Kinetic client.
 * <li>3. put entry ("hello", "world");
 * <li>4. get entry ("hello");
 * <li>5. delete the entry ("hello_world");
 * <li>6. close Kinetic client.
 * <li>7. close Kinetic simulator.
 * </ul>
 */
public class KineticHelloWorld {

	public static void main(String[] args) throws KineticException {

		// Simulator configuration and initialization
		SimulatorConfiguration simulatorConf = new SimulatorConfiguration();

		// Use memory store.
		simulatorConf.setUseMemoryStore(true);

		KineticSimulator simulator = new KineticSimulator(simulatorConf);

		// Client configuration and initialization
		ClientConfiguration clientConfig = new ClientConfiguration();
		KineticClient client = KineticClientFactory
				.createInstance(clientConfig);

		// Create the entry key/value
		byte[] key = "Hello".getBytes();
		byte[] value = "World".getBytes();
		Entry entry = new Entry(key, value);

		// Put entry ignore version
		client.putForced(entry);
		System.out.printf("put key=\"%s\" value=\"%s\"\n",
				new String(key), new String(value));

		// Get key
		Entry entryGet = client.get(key);
		System.out.printf("get key=\"%s\" retrieved value=\"%s\"\n",
				new String(key), new String(entryGet.getValue()));

		// Delete entry
		client.delete(entry);
		System.out.printf("delete key=\"%s\"\n", new String(key));

		// Close client and simulator
		client.close();
		simulator.close();
	}
}
