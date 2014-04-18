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
