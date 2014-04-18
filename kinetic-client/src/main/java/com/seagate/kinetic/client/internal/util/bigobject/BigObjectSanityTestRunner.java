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
package com.seagate.kinetic.client.internal.util.bigobject;

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Arrays;

import kinetic.client.ClientConfiguration;
import kinetic.client.KineticException;

/**
 * Internal test to sanity test BigObject util API.
 * 
 * @author chiaming
 * 
 */
public class BigObjectSanityTestRunner implements Runnable {

	private PipedInputStream pis = null;

	private PipedOutputStream pos = null;

	private static final int CHUNK_SIZE = 1024 * 1024 * 10;

	private int nchunk = 1;

	public BigObjectSanityTestRunner(int mBytes) throws IOException {

		if (mBytes < 10) {
			nchunk = 1;
			System.out.println("testing with 10M size ...");
		} else {
			nchunk = mBytes / 10;
		}

		pis = new PipedInputStream(CHUNK_SIZE);
		pos = new PipedOutputStream(pis);
	}

	public InputStream getInputStream() {
		return pis;
	}

	@Override
	public void run() {

		try {

			long total = 0;

			byte[] bytes = new byte[CHUNK_SIZE];

			for (int i = 0; i < nchunk; i++) {
				Arrays.fill(bytes, (byte) 0x00);
				pos.write(bytes);
				pos.flush();

				total = total + CHUNK_SIZE;
			}

			pos.close();

			System.out.println("closed pos, wrote total (bytes) = " + total);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void main(String[] args) throws IOException, KineticException {

		//
		int size = 10;

		if (args.length >= 1) {
			size = Integer.parseInt(args[0]);
		}


		BigObjectSanityTestRunner tester = new BigObjectSanityTestRunner(
				size);

		InputStream is = tester.getInputStream();

		Thread t = new Thread(tester);
		t.start();

		ClientConfiguration config = new ClientConfiguration();

		if (args.length == 2) {
			config.setHost(args[1]);
		}

		BigObject kc = new BigObject(config);

		byte[] key = "bigObject".getBytes();

		long start = System.currentTimeMillis();
		double total = kc.putx(key, is);

		long end = System.currentTimeMillis();

		double time = (end - start) / 1000.0;

		double avg = (total / time) / (1024 * 1024);

		is.close();

		System.out.println("putx time = " + time + ", total = " + total
				+ ", avg (Mb/s)= " + avg);

		//
		NullOutputStream nos = new NullOutputStream();
		start = System.currentTimeMillis();
		long total2 = kc.getx(key, nos);
		end = System.currentTimeMillis();

		if (total2 != total) {
			throw new RuntimeException("getx total != putx total");
		}

		time = (end - start) / 1000.0;

		avg = (total2 / time) / (1024 * 1024);

		System.out.println("getx time = " + time + ", total = " + total2
				+ ", avg (Mb/s)= "
				+ avg);

		start = System.currentTimeMillis();
		long deletedCount = kc.deletex(key);
		end = System.currentTimeMillis();

		time = (end - start) / 1000.0;

		avg = deletedCount / time;

		System.out.println("deletex time = " + time + ", deletedCount = "
				+ deletedCount + ", avg (ops/s) = " + avg);
	}

}
