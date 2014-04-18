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
package com.seagate.kinetic.example.client;

import java.io.UnsupportedEncodingException;
import java.util.List;

import kinetic.client.ClientConfiguration;
import kinetic.client.Entry;
import kinetic.client.EntryMetadata;
import kinetic.client.KineticClient;
import kinetic.client.KineticClientFactory;
import kinetic.client.KineticException;

/**
 * Kinetic synchronous API with SSL/TLS usage sample code.
 * <p>
 * This example assumes that a simulator is running on the localhost:8443
 * (SSL/TLS port)
 * <p>
 * This example performs the synchronously operations
 * <ul>
 * <li>1. start Kinetic client with SSL/TLS enabled.
 * <li>2. put two entries ("hello1", "world1") and ("hello2", "world2");
 * <li>3. (forced) put two entries ("hello1", "world1") and ("hello2",
 * "world2");
 * <li>4. get entry ("hello1");
 * <li>5. get next entry ("hello1");
 * <li>6. get previous entry ("hello2");
 * <li>7. get keyRange between ("hello1") and ("hello2");
 * <li>8. get metadata ("hello1");
 * <li>9. delete entry ("hello1");
 * <li>10. (forced) delete entry ("hello2");
 * <li>11. close Kinetic client.
 * </ul>
 */
public class BasicApiUsage {

	// String to byte[] encoding
	public static final String UTF8 = "utf8";

	// kinetic client
	private KineticClient client = null;

	/**
	 * Start the async API usage example.
	 * 
	 * @throws KineticException
	 *             if any Kinetic internal error occurred.
	 * @throws InterruptedException
	 *             if the example is interrupted before it is completed.
	 */
	@SuppressWarnings("unused")
	public void runExample() throws KineticException, InterruptedException {

		// Client configuration and initialization
		ClientConfiguration clientConfig = new ClientConfiguration();

		// set use SSL/TLS
		clientConfig.setUseSsl(true);

		// set SSL/TLS service port
		clientConfig.setPort(8443);

		client = KineticClientFactory.createInstance(clientConfig);

		// initial key, value and new version
		byte[] key1 = stringToBytes("hello1");
		byte[] value1 = stringToBytes("world1");
		byte[] key2 = stringToBytes("hello2");
		byte[] value2 = stringToBytes("world2");
		byte[] newVersion = stringToBytes("0");

		// create two entries
		Entry simpleEntry1 = new Entry(key1, value1);
		Entry simpleEntry2 = new Entry(key2, value2);

		// put two entries
		client.put(simpleEntry1, newVersion);
		client.put(simpleEntry2, newVersion);
		System.out.println("put two entries: key1=" + new String(key1)
		+ ", value1=" + new String(value1) + "; key2="
		+ new String(key2) + ", value2=" + new String(value2)
		+ "; newVersion=" + new String(newVersion));

		// forced put two entries
		client.putForced(simpleEntry1);
		client.putForced(simpleEntry2);
		System.out.println("forced put two entries: key1=" + new String(key1)
		+ ", value1=" + new String(value1) + "; key2="
		+ new String(key2) + ", value2=" + new String(value2));

		// get entry, expect to receive hello1 entry
		Entry entryHello1 = client.get(key1);
		System.out.println("get the entry of key1=" + new String(key1)
		+ ", value1=" + new String(entryHello1.getValue()));

		// get hello1 next entry, expect to receive hello2 entry
		Entry entryHello2 = client.getNext(key1);
		System.out.println("get the next entry of key=" + new String(key1)
		+ ", result: key2=" + new String(entryHello2.getKey()) + ", value1="
		+ new String(entryHello2.getValue()));

		// get hello2 previous entry, expect to receive hello1 entry
		Entry Hello1Entry = client.getPrevious(key2);
		System.out.println("get the previous entry of key=" + new String(key2)
		+ ", result: key1=" + new String(Hello1Entry.getKey()) + ", value1="
		+ new String(Hello1Entry.getValue()));

		// get key range from hello1 to hello2, expect to receive hello1 and
		// hello2
		List<byte[]> keys = client.getKeyRange(key1, true, key2, true, 2);
		System.out.println("get the key range from key1=" + new String(key1)
		+ " to key2=" + new String(key2) + ", result: "
		+ new String(keys.get(0)) + " " + new String(keys.get(1)));

		// get hello1 metadata, expect to receive hello1 metadata
		EntryMetadata emdHello1 = client.getMetadata(key1);
		System.out.println("get the metadata of key1=" + new String(key1));

		// delete hello1 entry
		client.delete(simpleEntry1);
		System.out.println("delete the entry of key1=" + new String(key1));

		// forced delete hello2
		client.deleteForced(key2);
		System.out.println("forced delete the entry of key2="
				+ new String(key2));

		// close kinetic client
		this.client.close();
	}

	/**
	 * convert string to byte[] using UTF8 encoding.
	 * 
	 * @param string
	 *            string to be converted to byte[].
	 * 
	 * @return the byte[] representation of the specified string
	 */
	private static byte[] stringToBytes(String string) {

		try {
			return string.getBytes(UTF8);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		return null;
	}

	public static void main(String[] args) throws KineticException,
	InterruptedException {
		BasicApiUsage syncUsage = new BasicApiUsage();

		syncUsage.runExample();
	}
}
