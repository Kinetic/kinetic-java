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
import org.testng.annotations.BeforeMethod;
import org.testng.Assert;
import static com.seagate.kinetic.KineticTestHelpers.toByteArray;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
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
import com.seagate.kinetic.KVGenerator;
import com.seagate.kinetic.client.internal.DefaultKineticClient;

/**
 * 
 * Multi client(thread) concurrent put different entry into simulator/drive.
 * Verify the put result and put order.
 * <p>
 * 
 */
@Test(groups = {"simulator", "drive"})
public class KineticClientConcurrentTest extends IntegrationTestCase {
	private static final Logger logger = IntegrationTestLoggerFactory
			.getLogger(KineticClientConcurrentTest.class.getName());

	private final byte[] INIT_VERSION = "0".getBytes();
	private KVGenerator kvGenerator;
	private final int writeThreads = 2;
	private final int writesEachThread = 300;

	/**
	 * 
	 * Initialize a key/value generator.
	 * 
	 */
	@BeforeMethod
    public void setUp() throws KineticException, IOException,
			InterruptedException {
		kvGenerator = new KVGenerator();
	}

	/**
	 * 
	 * Concurrent threads put test.
	 * 
	 * @throws KineticException
	 *             if any kinetic internal error occurred.
	 * @throws InterruptedException
	 *             if any interrupt error occurred.
	 */
	@Test(dataProvider = "transportProtocolOptions")
	public void concurrentTest(String clientName) throws InterruptedException, KineticException,
			UnsupportedEncodingException {
		int totalWrites = writeThreads * writesEachThread;
		CountDownLatch latch = new CountDownLatch(writeThreads);
		ExecutorService pool = Executors.newCachedThreadPool();
		
		cleanData(totalWrites, getClient(clientName));

		// thread pool generate threads(concurrent client number)
		// logger.info("launch " + writeThreads + " write threads to write "
		// + totalWrites + " kv pairs");
		kvGenerator = new KVGenerator();
		kvGenerator.reset();
		KineticClient kineticClient;
		for (int i = 0; i < writeThreads; i++) {
			kineticClient = KineticClientFactory
					.createInstance(kineticClientConfigutations.get(clientName));
			pool.execute(new WriteThread(kineticClient, kvGenerator,
					writesEachThread, latch));
		}

		// wait all threads finish
		latch.await();
		pool.shutdown();

		// reset kvGenerator
		kvGenerator.reset();
		kineticClient = KineticClientFactory.createInstance(getClientConfig());

		// verify results until all WriteThread finished
		// logger.info("verify the kv pairs.....");
		String key = "";
		String rightValue = "";
		String returnValue = "";
		Entry returnVersioned = null;

		for (int i = 0; i < totalWrites; i++) {
			// logger.info("key=" + key + ", rightValue=" + rightValue
			// + ", returnValue=" + returnValue);
			key = kvGenerator.getNextKey();
			rightValue = kvGenerator.getValue(key);
			returnVersioned = kineticClient.get(toByteArray(key));
			if (returnVersioned == null) {
				Assert.fail("return null when get key " + key);
			} else {
				if (returnVersioned.getValue() == null) {
					Assert.fail("the value is null when get key " + key);
				} else {
					returnValue = new String(returnVersioned.getValue());
					assertEquals(rightValue, returnValue);
				}
			}
		}

		kineticClient.close();

		// record the startKey and endKey
		String endKey = key;
		kvGenerator.reset();
		String startKey = kvGenerator.getNextKey();

		// verify that the keys are sorted
		// logger.info("verify the data orders");
		DefaultKineticClient defaultKineticClient = new DefaultKineticClient(
				getClientConfig());
		Iterator<Entry> iterator = defaultKineticClient.getRange(
				startKey.getBytes(), true, endKey.getBytes(), true).iterator();
		String currentKey = "";
		String nextKey = "";
		int count = 0;

		Entry currentVersioned = null;
		if (iterator.hasNext()) {
			currentVersioned = iterator.next();
			if (currentVersioned == null) {
				Assert.fail("iterator has null element");
			} else {
				if (currentVersioned.getKey() == null) {
					Assert.fail("the key is null");
				} else {
					currentKey = new String(currentVersioned.getKey());
					++count;
				}

			}

		}

		while (iterator.hasNext()) {
			nextKey = new String(iterator.next().getKey());
			assertTrue(currentKey.compareTo(nextKey) < 0);
			currentKey = nextKey;
			++count;
		}

		// check the amount of kv pairs
		assertTrue(count == totalWrites);

		defaultKineticClient.close();
		
		cleanData(totalWrites, getClient(clientName));

		logger.info(this.testEndInfo());
	}
	
    private void cleanData(int dataCount, KineticClient client)
            throws KineticException {
	    KVGenerator kvGenerator = new KVGenerator();
	    for(int i = 0; i < dataCount; i++){
	        client.deleteForced(toByteArray(kvGenerator.getNextKey()));
	    }
	}

	/**
	 * 
	 * Every thread(client) execute writeCount number
	 * <p>
	 * 
	 */
	class WriteThread implements Runnable {
		private int writeCount = 0;
		private final CountDownLatch latch;
		private final KVGenerator kvGenerator;
		private final KineticClient kineticClient;

		public WriteThread(KineticClient kineticClient,
				KVGenerator kvGenerator, int writeCount, CountDownLatch latch) {
			this.kineticClient = kineticClient;
			this.kvGenerator = kvGenerator;
			this.writeCount = writeCount;
			this.latch = latch;
		}

		@Override
		public void run() {
			String key = "";
			String value = "";
			byte[] version = INIT_VERSION;

			for (int i = 0; i < writeCount; i++) {
				key = kvGenerator.getNextKey();
				value = kvGenerator.getValue(key);
				try {
					EntryMetadata entryMetadata = new EntryMetadata();
					kineticClient.put(new Entry(toByteArray(key),
							toByteArray(value), entryMetadata), version);
				} catch (KineticException e) {
					Assert.fail("put key=" + key + ", value=" + value + " failed, "
							+ e.getMessage());
				} catch (Exception e) {
					Assert.fail("put key=" + key + ", value=" + value + " failed, "
							+ e.getMessage());
				}
			}

			try {
				kineticClient.close();
			} catch (KineticException e) {
				Assert.fail("close kineticClient failed, " + e.getMessage());
			} catch (Exception e) {
				Assert.fail("close kineticClient failed, " + e.getMessage());
			}

			// latch count down
			latch.countDown();
		}
	}
}
