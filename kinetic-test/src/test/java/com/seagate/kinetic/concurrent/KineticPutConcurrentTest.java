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
package com.seagate.kinetic.concurrent;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;
import org.testng.annotations.Test;
import org.testng.Assert;
import static com.seagate.kinetic.KineticTestHelpers.toByteArray;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import kinetic.client.Entry;
import kinetic.client.EntryMetadata;
import kinetic.client.KineticClient;
import kinetic.client.KineticClientFactory;
import kinetic.client.KineticException;

import com.seagate.kinetic.IntegrationTestCase;
import com.seagate.kinetic.IntegrationTestLoggerFactory;

/**
 * 
 * Multi client(thread) concurrent put the same key with increased version
 * number into simulator/drive. Verify the put result and put order.
 * <p>
 * 
 */
@Test(groups = {"simulator", "drive"})
public class KineticPutConcurrentTest extends IntegrationTestCase {
	private static final Logger logger = IntegrationTestLoggerFactory
			.getLogger(KineticPutConcurrentTest.class.getName());

	private int writeThreads = 2;
	private int writesEachThread = 100;

	/**
	 * 
	 * Concurrent threads put same key test.
	 * 
	 * @throws KineticException
	 *             if any kinetic internal error occurred.
	 * @throws InterruptedException
	 *             if any interrupt error occurred.
	 */
	@Test(dataProvider = "transportProtocolOptions")
	public void sameKeyConcurrentPutTest(String clientName) throws KineticException,
			InterruptedException {
		String key = "samekey";
		CountDownLatch latch = new CountDownLatch(writeThreads);
		ExecutorService pool = Executors.newCachedThreadPool();
		
		getClient(clientName).deleteForced(toByteArray(key));

		// thread pool generate threads(concurrent client number)
		KineticClient kineticClient = null;
		for (int i = 0; i < writeThreads; i++) {
			kineticClient = KineticClientFactory
					.createInstance(kineticClientConfigutations.get(clientName));
			
			// every threads write using the same key
			pool.execute(new SameKeyWriteThread(kineticClient, key,
					writesEachThread, latch));
		}

		// wait all threads finish
		latch.await();
		pool.shutdown();
		
//		kineticClient.deleteForced(toByteArray(key));

		logger.info(this.testEndInfo());
	}

	/**
	 * 
	 * Every thread(client) execute writeCount number
	 * <p>
	 * 
	 */
	class SameKeyWriteThread implements Runnable {
		private int writeCount = 0;
		private CountDownLatch latch;
		private String key;
		private KineticClient kineticClient;

		public SameKeyWriteThread(KineticClient kineticClient, String key,
				int writeCount, CountDownLatch latch) {
			this.kineticClient = kineticClient;
			this.key = key;
			this.writeCount = writeCount;
			this.latch = latch;
		}

		@Override
		public void run() {
			int count = 0;
			int versionAsInt = 0;
			byte[] dbVersion = null;
			Entry vPut = null;
			Entry vPutReturn = null;
			Entry vGetReturn = null;
			byte[] version = null;

			while (true) {
				try {
					version = ("" + versionAsInt).getBytes();
					EntryMetadata entryMetadata = new EntryMetadata();
					entryMetadata.setVersion(dbVersion);
					vPut = new Entry(toByteArray(key), version, entryMetadata);

					vPutReturn = kineticClient.put(vPut, version);

					assertEquals(new String(vPutReturn.getEntryMetadata()
							.getVersion()), new String(vPutReturn.getValue()));

					// logger.info("put version: " + new String(version)
					// + " success");

					dbVersion = vPutReturn.getEntryMetadata().getVersion();
					versionAsInt++;
					count++;

				} catch (KineticException e1) {
					// logger.info("put operation expected version mismatch exception, "
					// + e1.getMessage());

					try {
						vGetReturn = kineticClient.get(key.getBytes());
						versionAsInt = Integer.parseInt(new String(vGetReturn
								.getEntryMetadata().getVersion()));

						assertTrue(versionAsInt >= Integer.parseInt(new String(
								version)));

						dbVersion = vGetReturn.getEntryMetadata().getVersion();
						versionAsInt++;
						count++;

					} catch (KineticException e) {
						Assert.fail("fail to get the version" + e.getMessage());
					}
				} finally {
					if (count == writeCount) {
						break;
					}
				}
			}

			try {
				kineticClient.close();
			} catch (KineticException e) {
				Assert.fail("close kineticClient failed, " + e.getMessage());
			}

			latch.countDown();
		}
	}
}
