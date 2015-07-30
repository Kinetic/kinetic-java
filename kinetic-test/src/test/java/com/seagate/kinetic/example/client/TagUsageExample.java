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

	public void runExample() throws KineticException, InterruptedException {

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
                .setAlgorithm(Algorithm.CRC32.toString());

        // calculate tag with CRC32C algorithm
        ByteString tag = MessageDigestUtil.calculateTag(Algorithm.CRC32, value);

        // to byte[]
        byte[] crc32c = tag.toByteArray();

        // set tag
        simpleEntry1.getEntryMetadata().setTag(crc32c);

        // put entry to store
        client.putForced(simpleEntry1);

        // get entry from store
        Entry entry2 = client.get(key);

        // calculate tag from value
        byte[] calculatedTag = MessageDigestUtil.calculateTag(Algorithm.CRC32,
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

        TagUsageExample tagUsage = new TagUsageExample();

        tagUsage.runExample();
	}
}
