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

import kinetic.client.ClientConfiguration;

import com.seagate.kinetic.client.internal.util.bigobject.BigObject;

/**
 * An example to delete a big object (sequence of entries) in the Kinetic
 * storage.
 * 
 * @author chiaming
 * 
 */
public class DeleteBigObjectExample {

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

		double avg = (total / time);

		System.out.println("deletx time (secs)= " + time
				+ ", total (entries) deleted = " + total
				+ ", avg time (entries/sec)=" + avg);
	}

	/**
	 * An example to delete a big object (sequence of entries) in the Kinetic
	 * storage.
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

		if (args.length != 1) {
			throw new java.lang.IllegalArgumentException(
					"missing file_name and key_space name");
		}

		// string key name
		String keyName = args[0];

		// key space to the big object
		byte[] keySpace = keyName.getBytes("UTF8");

		// client config
		ClientConfiguration config = new ClientConfiguration();

		// big object instance
		BigObject bigObject = new BigObject(config);

		// start time
		long start = System.currentTimeMillis();

		// delete big object
		double total = bigObject.deletex(keySpace);

		// end time
		long end = System.currentTimeMillis();

		// print statistics
		printResult(start, end, total);

		// close util
		bigObject.close();
	}

}
