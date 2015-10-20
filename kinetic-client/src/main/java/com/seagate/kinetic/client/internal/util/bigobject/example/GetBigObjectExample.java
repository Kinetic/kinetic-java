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
package com.seagate.kinetic.client.internal.util.bigobject.example;

import java.io.FileOutputStream;

import kinetic.client.ClientConfiguration;

import com.seagate.kinetic.client.internal.util.bigobject.BigObject;

/**
 * An example to get a big object (sequence of entries) from the Kinetic
 * storage.
 * 
 * @author chiaming
 * 
 */
public class GetBigObjectExample {

	/**
	 * print result based on start time, end time, and total bytes.
	 * 
	 * @param start
	 *            start time in milli-seconds for the operation.
	 * @param end
	 *            end time in milli-seconds for the operation.
	 * @param total
	 *            total value bytes for the operation.
	 */
	public static void printResult(String op, long start, long end, double total) {
		double time = (end - start) / 1000.0;

		double avg = (total / time) / (1024 * 1024);

		System.out.println(op + ": total time (secs)= " + time
				+ ", total (bytes) = "
				+ total + ", avg (Mb/sec) = " + avg);
	}

	/**
	 * An example to read a big object (sequence of entries) stored in the
	 * Kinetic system and write it to a file.
	 * <p>
	 * 
	 * @param args
	 *            args[0] is the key space string name, args[1] is the full path
	 *            file name.
	 * 
	 * @throws Exception
	 *             if any errors occurred.
	 */
	public static void main(String[] args) throws Exception {

		if (args.length != 2) {
			throw new java.lang.IllegalArgumentException(
					"missing key_space name and output file name");
		}

		// string key name
		String keyName = args[0];

		// key space for the sequence of entries
		byte[] keySpace = keyName.getBytes("UTF8");

		// full path file name
		String fileName = args[1];

		System.out.println("getting object from Kinetic, key=" + keyName
				+ ", path=" + fileName);

		// client config
		ClientConfiguration config = new ClientConfiguration();

		// file output stream to write the sequence of values
		FileOutputStream fos = new FileOutputStream(fileName);

		// big object util instance
		BigObject bigObject = new BigObject(config);

		// start time
		long start = System.currentTimeMillis();

		// get the sequence of values and write to the output stream
		long total = bigObject.getx(keySpace, fos);

		// end time
		long end = System.currentTimeMillis();

		// print statistics
		printResult("getx", start, end, total);

		bigObject.close();
	}

}
