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
