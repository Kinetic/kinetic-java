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
package com.seagate.kinetic.performance;

import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;
import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;

import kinetic.client.AsyncKineticException;
import kinetic.client.CallbackHandler;
import kinetic.client.CallbackResult;
import kinetic.client.Entry;
import kinetic.client.EntryMetadata;
import kinetic.client.KineticException;
import kinetic.client.advanced.AdvancedKineticClient;
import kinetic.client.advanced.PersistOption;

import com.seagate.kinetic.IntegrationTestCase;
import com.seagate.kinetic.IntegrationTestLoggerFactory;
import com.seagate.kinetic.KVGenerator;

/**
 * 
 * Micro performance test benchmark.
 * <p>
 * Test put performance with sync and async API.
 * <p>
 * 
 */
public class microPerfTest extends IntegrationTestCase {
	private static final Logger logger = IntegrationTestLoggerFactory
			.getLogger(microPerfTest.class.getName());

	private static AdvancedKineticClient client = null;
	private static KVGenerator kvGenerator = null;
	private static byte[] newVersion = null;
	private static CountDownLatch signal = null;

	/**
	 * Initialize a key/value pair generator
	 * <p>
	 */
	@BeforeMethod
    public void setUp() throws Exception {
		kvGenerator = new KVGenerator();
		newVersion = "0".getBytes();
		
	}

	/**
	 * Test four scenario：
	 * <p>
	 * Sync put (default persist option is sync)
	 * <p>
	 * Async put (default persist option is sync)
	 * <p>
	 * Sync put with persist option is async
	 * <P>
	 * Async put with persist option is sync
	 * 
	 * 
	 * @throws KineticException
	 *             if any internal error occurred.
	 * @throws InterruptedException
	 *             if thread is interrupted, or the specified waiting time
	 *             elapses.
	 */
	@Test(dataProvider = "transportProtocolOptions")
	public void microTest(String clientName) throws KineticException, InterruptedException {
	    client = getClient(clientName);
		int op_count = 1000;
		microAsync("Async Run1", op_count, 1024);
		microAsync("Async Run2", op_count, 16 * 1024);
		microAsync("Async Run3", op_count, 32 * 1024);

		System.out.println("");

		microSync("Sync Run1", op_count, 1024);
		microSync("Sync Run2", op_count, 16 * 1024);
		microSync("Sync Run3", op_count, 32 * 1024);

		System.out.println("");

		microAsyncWithPersistOption("Async Persist_ASYNC Run1", op_count, 1024);
		microAsyncWithPersistOption("Async Persist_ASYNC Run2", op_count,
				16 * 1024);
		microAsyncWithPersistOption("Async Persist_ASYNC Run3", op_count,
				32 * 1024);

		System.out.println("");

		microSyncWithPersistOption("Sync Persist_ASYNC Run1", op_count, 1024);
		microSyncWithPersistOption("Sync Persist_ASYNC Run2", op_count,
				16 * 1024);
		microSyncWithPersistOption("Sync Persist_ASYNC Run3", op_count,
				32 * 1024);
		
		logger.info(this.testEndInfo());

	}

	/**
	 * Async test with default persist option.
	 * <p>
	 * 
	 * 
	 * @throws KineticException
	 *             if any internal error occurred.
	 * @throws InterruptedException
	 *             if thread is interrupted, or the specified waiting time
	 *             elapses.
	 */
	private void microAsync(String name, int count, int size)
			throws KineticException, InterruptedException {
		deleteAll(count);

		signal = new CountDownLatch(count);
		byte[] value = ByteBuffer.allocate(size).array();
		kvGenerator.reset();
		long timeStart = System.nanoTime();
		for (int i = 0; i < count; i++) {
			String key = kvGenerator.getNextKey();
			EntryMetadata emd = new EntryMetadata();
			Entry entry = new Entry(key.getBytes(), value, emd);

			client.putAsync(entry, newVersion, new CallbackHandler<Entry>() {

				@Override
				public void onSuccess(CallbackResult<Entry> result) {
					signal.countDown();
				}

				@Override
				public void onError(AsyncKineticException exception) {
					signal.countDown();
					throw new RuntimeException("put async exception"
							+ exception.getMessage());
				}

			});

		}
		signal.await();
		long timeEnd = System.nanoTime();

		double ave = ((timeEnd - timeStart) * 1e-6 / count);
		printAverageTime(name, size / 1024, count, ave);

		deleteAll(count);
	}

	/**
	 * Sync test with default persist option.
	 * <p>
	 * 
	 * 
	 * @throws KineticException
	 *             if any internal error occurred.
	 * @throws InterruptedException
	 *             if thread is interrupted, or the specified waiting time
	 *             elapses.
	 */
	private void microSync(String name, int count, int size)
			throws KineticException, InterruptedException {
		deleteAll(count);

		byte[] value = ByteBuffer.allocate(size).array();
		kvGenerator.reset();
		long timeStart = System.nanoTime();
		for (int i = 0; i < count; i++) {
			String key = kvGenerator.getNextKey();
			EntryMetadata emd = new EntryMetadata();
			Entry entry = new Entry(key.getBytes(), value, emd);

			client.put(entry, newVersion);

		}
		long timeEnd = System.nanoTime();

		double ave = ((timeEnd - timeStart) * 1e-6 / count);
		printAverageTime(name, size / 1024, count, ave);

		deleteAll(count);
	}

	/**
	 * Async test with tag and default persist option.
	 * <p>
	 * 
	 * 
	 * @throws KineticException
	 *             if any internal error occurred.
	 * @throws InterruptedException
	 *             if thread is interrupted, or the specified waiting time
	 *             elapses.
	 */
	@SuppressWarnings("unused")
	private void microAsyncWithTag(String name, int count, int size)
			throws KineticException, InterruptedException {
		deleteAll(count);

		signal = new CountDownLatch(count);
		byte[] value = ByteBuffer.allocate(size).array();
		byte[] tag = "tag".getBytes();
		kvGenerator.reset();
		long timeStart = System.nanoTime();
		for (int i = 0; i < count; i++) {
			String key = kvGenerator.getNextKey();
			EntryMetadata emd = new EntryMetadata();
			emd.setTag(tag);
			Entry entry = new Entry(key.getBytes(), value, emd);

			client.putAsync(entry, newVersion, new CallbackHandler<Entry>() {

				@Override
				public void onSuccess(CallbackResult<Entry> result) {
					signal.countDown();
				}

				@Override
				public void onError(AsyncKineticException exception) {
					signal.countDown();
					throw new RuntimeException("put async exception"
							+ exception.getMessage());
				}

			});

		}
		signal.await();
		long timeEnd = System.nanoTime();

		double ave = ((timeEnd - timeStart) * 1e-6 / count);
		printAverageTime(name, size / 1024, count, ave);

		deleteAll(count);
	}

	/**
	 * Sync test with tag and default persist option.
	 * <p>
	 * 
	 * 
	 * @throws KineticException
	 *             if any internal error occurred.
	 * @throws InterruptedException
	 *             if thread is interrupted, or the specified waiting time
	 *             elapses.
	 */
	@SuppressWarnings("unused")
	private void microSyncWithTag(String name, int count, int size)
			throws KineticException, InterruptedException {
		deleteAll(count);

		byte[] value = ByteBuffer.allocate(size).array();
		byte[] tag = "tag".getBytes();
		kvGenerator.reset();
		long timeStart = System.nanoTime();
		for (int i = 0; i < count; i++) {
			String key = kvGenerator.getNextKey();
			EntryMetadata emd = new EntryMetadata();
			emd.setTag(tag);
			Entry entry = new Entry(key.getBytes(), value, emd);

			client.put(entry, newVersion);

		}
		long timeEnd = System.nanoTime();

		double ave = ((timeEnd - timeStart) * 1e-6 / count);
		printAverageTime(name, size / 1024, count, ave);

		deleteAll(count);
	}

	/**
	 * Async test with persist option is async.
	 * <p>
	 * 
	 * 
	 * @throws KineticException
	 *             if any internal error occurred.
	 * @throws InterruptedException
	 *             if thread is interrupted, or the specified waiting time
	 *             elapses.
	 */
	private void microAsyncWithPersistOption(String name, int count, int size)
			throws KineticException, InterruptedException {
		deleteAll(count);

		signal = new CountDownLatch(count);
		byte[] value = ByteBuffer.allocate(size).array();
		kvGenerator.reset();
		long timeStart = System.nanoTime();
		for (int i = 0; i < count; i++) {
			String key = kvGenerator.getNextKey();
			EntryMetadata emd = new EntryMetadata();
			Entry entry = new Entry(key.getBytes(), value, emd);

			client.putAsync(entry, newVersion, PersistOption.ASYNC,
					new CallbackHandler<Entry>() {

						@Override
						public void onSuccess(CallbackResult<Entry> result) {
							signal.countDown();
						}

						@Override
						public void onError(AsyncKineticException exception) {
							signal.countDown();
							throw new RuntimeException("put async exception"
									+ exception.getMessage());
						}

					});

		}
		signal.await();
		long timeEnd = System.nanoTime();

		double ave = ((timeEnd - timeStart) * 1e-6 / count);
		printAverageTime(name, size / 1024, count, ave);

		deleteAll(count);
	}

	/**
	 * Sync test with persist option is async.
	 * <p>
	 * 
	 * 
	 * @throws KineticException
	 *             if any internal error occurred.
	 * @throws InterruptedException
	 *             if thread is interrupted, or the specified waiting time
	 *             elapses.
	 */
	private void microSyncWithPersistOption(String name, int count, int size)
			throws KineticException, InterruptedException {
		deleteAll(count);

		byte[] value = ByteBuffer.allocate(size).array();
		kvGenerator.reset();
		long timeStart = System.nanoTime();
		for (int i = 0; i < count; i++) {
			String key = kvGenerator.getNextKey();
			EntryMetadata emd = new EntryMetadata();
			Entry entry = new Entry(key.getBytes(), value, emd);

			client.put(entry, newVersion, PersistOption.ASYNC);

		}
		long timeEnd = System.nanoTime();

		double ave = ((timeEnd - timeStart) * 1e-6 / count);
		printAverageTime(name, size / 1024, count, ave);

		deleteAll(count);
	}

	/**
	 * Print test result.
	 * <p>
	 */
	private void printAverageTime(String name, int size, int count, double ave) {
		double speed = size / ave;
		System.out
				.printf("%s: size: %5dKB  #op: %2d  ave_op: %#3.3fms  speed: %#3.2fMB/s\n",
						name, size, count, ave, speed);
	}

	/**
	 * Delete all items after one performance loop.
	 * <p>
	 */
	private void deleteAll(int count) throws KineticException {
		kvGenerator.reset();
		for (int i = 0; i < count; i++) {
			String key = kvGenerator.getNextKey();
			client.deleteForced(key.getBytes());
		}
	}

}
