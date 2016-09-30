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
package com.seagate.kinetic.example.client;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import kinetic.client.ClientConfiguration;
import kinetic.client.Entry;
import kinetic.client.KineticClient;
import kinetic.client.KineticClientFactory;
import kinetic.client.KineticException;

import com.google.protobuf.ByteString;
import com.seagate.kinetic.common.lib.MessageDigestUtil;
import com.seagate.kinetic.proto.Kinetic.Command.Algorithm;

/**
 * Kinetic tag field usage example.
 * 
 */
public class TagUsageExample {

	// String to byte[] encoding
	public static final String UTF8 = "utf8";

	// kinetic client
	private KineticClient client = null;

	public void runExample(Algorithm algorithm) throws KineticException, InterruptedException {

		// Client configuration and initialization
		ClientConfiguration clientConfig = new ClientConfiguration();

        // create client instance
		client = KineticClientFactory.createInstance(clientConfig);

        // initialize key and value
        byte[] key = stringToBytes("hello");

        byte[] value = new byte[32];
        for (int i = 0; i < value.length; i++) {
            value[i] = (byte) 0XFF;
        }

		// create two entries
        Entry simpleEntry1 = new Entry(key, value);

        // set tag algorithm
        simpleEntry1.getEntryMetadata()
                .setAlgorithm(algorithm.toString());

        // calculate tag with CRC32C algorithm
        ByteString tag = MessageDigestUtil.calculateTag(algorithm, value);

        // to byte[]
        byte[] crc32c = tag.toByteArray();

        // set tag
        simpleEntry1.getEntryMetadata().setTag(crc32c);

        // put entry to store
        client.putForced(simpleEntry1);

        // get entry from store
        Entry entry2 = client.get(key);

        // calculate tag from value
        byte[] calculatedTag = MessageDigestUtil.calculateTag(algorithm,
                entry2.getValue()).toByteArray();

        // get the tag field
        byte[] tag2 = entry2.getEntryMetadata().getTag();

        // compare tags
        if (Arrays.equals(calculatedTag, tag2) == false) {
            throw new RuntimeException("tag does not compare");
        }

        System.out.println("tag is verified.");

        // delete entry
		client.delete(simpleEntry1);

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

    /**
     * Simple example to demonstrate Kinetic tag field usage.
     */
	public static void main(String[] args) throws KineticException,
	InterruptedException {
	    
	    Algorithm algorithm = Algorithm.CRC32C;

        TagUsageExample tagUsage = new TagUsageExample();

        tagUsage.runExample(algorithm);
	}
}
