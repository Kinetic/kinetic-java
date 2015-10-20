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
package com.seagate.kinetic.batchOp;

import static com.seagate.kinetic.KineticTestHelpers.toByteArray;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import kinetic.client.BatchOperation;
import kinetic.client.Entry;
import kinetic.client.KineticClient;
import kinetic.client.KineticClientFactory;
import kinetic.client.KineticException;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.seagate.kinetic.IntegrationTestCase;

@Test(groups = { "simulator" })
public class BatchConcurrentTest extends IntegrationTestCase {
	@Test(dataProvider = "transportProtocolOptions")
	public void testBatchOperation_Concurrent_MultiClients_SameKey_AllSuccess(
			String clientName) {
		int writeThreads = 5;
		CountDownLatch latch = new CountDownLatch(writeThreads);
		ExecutorService pool = Executors.newCachedThreadPool();

		KineticClient kineticClient = null;
		for (int i = 0; i < writeThreads; i++) {
			try {
				kineticClient = KineticClientFactory
						.createInstance(kineticClientConfigutations
								.get(clientName));
			} catch (KineticException e) {
				Assert.fail("Create client throw exception. " + e.getMessage());
			}
			pool.execute(new BatchThread(kineticClient, latch));
		}

		// wait all threads finish
		try {
			latch.await();
		} catch (InterruptedException e) {
			Assert.fail("latch await throw exception. " + e.getMessage());
		}
		pool.shutdown();

	}
}

class BatchThread implements Runnable {
	private final CountDownLatch latch;
	private final KineticClient kineticClient;

	public BatchThread(KineticClient kineticClient, CountDownLatch latch) {
		this.kineticClient = kineticClient;
		this.latch = latch;
	}

	@Override
	public void run() {
		Entry bar = new Entry();
		byte[] barKey = toByteArray("bar");
		bar.setKey(barKey);
		byte[] barValue = toByteArray("barvalue");
		bar.setValue(barValue);
		byte[] barVersion = toByteArray("1234");
		bar.getEntryMetadata().setVersion(barVersion);

		Entry foo = new Entry();
		byte[] fooKey = toByteArray("foo");
		foo.setKey(fooKey);
		byte[] fooValue = toByteArray("foovalue");
		foo.setValue(fooValue);
		byte[] fooVersion = toByteArray("1234");
		foo.getEntryMetadata().setVersion(fooVersion);

		try {
			kineticClient.deleteForced(fooKey);
			kineticClient.deleteForced(barKey);
		} catch (KineticException e) {
			Assert.fail("Clean entry failed. " + e.getMessage());
		}

		try {
			kineticClient.putForced(bar);
		} catch (KineticException e1) {
			Assert.fail("Put entry failed. " + e1.getMessage());
		}

		BatchOperation batch = null;
		try {
			batch = kineticClient.createBatchOperation();
		} catch (KineticException e1) {
			Assert.fail("Create batch throw exception. " + e1.getMessage());
		}

		try {
			batch.putForced(foo);
		} catch (KineticException e1) {
			Assert.fail("Put entry failed. " + e1.getMessage());
		}

		try {
			batch.deleteForced(bar.getKey());
		} catch (KineticException e1) {
			Assert.fail("Delete entry failed. " + e1.getMessage());
		}

		try {
			batch.commit();
		} catch (KineticException e1) {
			Assert.fail("Batch commit throw exception. " + e1.getMessage());
		}

		try {
			kineticClient.deleteForced(fooKey);
			kineticClient.deleteForced(barKey);
		} catch (KineticException e) {
			Assert.fail("Clean entry failed. " + e.getMessage());
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
