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

import java.io.FileInputStream;

import kinetic.client.ClientConfiguration;

import com.seagate.kinetic.client.internal.util.bigobject.BigObject;

/**
 * An example to store a big file to the Kinetic storage.
 * 
 * @author chiaming
 * 
 * @see BigObject#putx(byte[], java.io.InputStream)
 */
public class PutBigObjectExample {

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
	public static void printResult(long start, long end, double total) {
		double time = (end - start) / 1000.0;

		double avg = (total / time) / (1024 * 1024);

		System.out.println("putx time (secs)= " + time + ", total (bytes) = "
				+ total + ", avg (Mb/sec) = " + avg);
	}

	/**
	 * An example to read a file and store it in the Kinetic storage.
	 * <p>
	 * 
	 * @param args
	 *            args[0] is the key space string name, args[1] is the full path
	 *            file name to the file be stored in the Kinetic storage.
	 * 
	 * @throws Exception
	 *             if any errors occurred.
	 */
	public static void main(String[] args) throws Exception {

		if (args.length != 2) {
			throw new java.lang.IllegalArgumentException(
					"missing file_name and key_space name");
		}

		// string file name
		String keyName = args[0];

		// key space for the big object
		byte[] keySpace = keyName.getBytes("UTF8");

		// full path file name
		String fullPathFileName = args[1];

		// client config
		ClientConfiguration config = new ClientConfiguration();

		// big object util
		BigObject bigObject = new BigObject(config);

		// file input stream
		FileInputStream fis = new FileInputStream(fullPathFileName);

		// start time
		long start = System.currentTimeMillis();

		// putx operation
		double total = bigObject.putx(keySpace, fis);

		// end time
		long end = System.currentTimeMillis();

		// print statistics
		printResult(start, end, total);

		// close input stream
		fis.close();

		// close big object util
		bigObject.close();
	}

}
