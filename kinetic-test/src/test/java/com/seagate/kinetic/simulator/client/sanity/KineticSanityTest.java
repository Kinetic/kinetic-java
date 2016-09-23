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
package com.seagate.kinetic.simulator.client.sanity;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;
import static com.seagate.kinetic.KineticTestHelpers.toByteArray;

import java.io.StringWriter;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import kinetic.client.AsyncKineticException;
import kinetic.client.CallbackHandler;
import kinetic.client.CallbackResult;
import kinetic.client.Entry;
import kinetic.client.EntryMetadata;
import kinetic.client.KineticException;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.seagate.kinetic.IntegrationTestCase;
import com.seagate.kinetic.proto.Kinetic.Command.Algorithm;

@Test(groups = {"simulator"})
public class KineticSanityTest extends IntegrationTestCase {

	Logger logger = Logger.getLogger(KineticSanityTest.class.getName());

	@Test(dataProvider = "transportProtocolOptions")
	public void sanityTest(String clientName) throws Exception {
		/**
		 * start playing with the APIs.
		 */
		Random random = new Random();
		byte[] key = new byte[20];
		random.nextBytes(key);

		byte[] value = toByteArray("world");

		byte[] initVersion = new byte[20];
		random.nextBytes(initVersion);

		EntryMetadata entryMetadata = new EntryMetadata();
		entryMetadata.setAlgorithm(Algorithm.SHA2.toString());

		Entry versioned = new Entry(key, value, entryMetadata);

		/**
		 * put key/value and validate.
		 */
		Entry dbVersioned = getClient(clientName).put(versioned, initVersion);

		Entry vFromDb = getClient(clientName).get(key);

		assertTrue(Arrays.equals(vFromDb.getValue(), value));

		/**
		 * get metadata only
		 */
		EntryMetadata metadata = getClient(clientName).getMetadata(key);

		assertTrue(Arrays.equals(metadata.getVersion(), initVersion));

		assertTrue(Algorithm.SHA2.toString().equals(
				metadata.getAlgorithm().toString()));

		byte[] newWorld = toByteArray("new world");

		// set value to "new world" for the entry.
		dbVersioned.setValue(newWorld);
		EntryMetadata entryMetadata1 = new EntryMetadata();
		entryMetadata1.setVersion(initVersion);
		dbVersioned.setEntryMetadata(entryMetadata1);

		/**
		 * update entry with new version in the persistent store
		 */
		Entry dbVersioned2 = getClient(clientName).put(dbVersioned, toByteArray("2"));

		assertTrue(Arrays.equals(dbVersioned2.getEntryMetadata().getVersion(),
				toByteArray("2")));

		// read from db
		Entry v2FromDb = getClient(clientName).get(key);

		// validate value was put successfully
		assertTrue(Arrays.equals(v2FromDb.getValue(), newWorld));

		/**
		 * get metadata only
		 */
		EntryMetadata metadata2 = getClient(clientName).getMetadata(key);

		assertTrue(Arrays.equals(metadata2.getVersion(), toByteArray("2")));

		logger.info("put/get twice successfully, deleting entry ...");

		// delete entry
		getClient(clientName).delete(dbVersioned2);

		// read and validate deleted
		Entry versionedGetFromDb = getClient(clientName).get(key);

		assertTrue(versionedGetFromDb == null);

		logger.info("sanity test for put/get/delete passed");
	}

	@Test(dataProvider = "transportProtocolOptions")
	public void noopTest(String clientName) throws Exception {
		/**
		 * noop test
		 */
		long time = getClient(clientName).noop();

		/**
		 * noop returns successfully with round-trip time in milliseconds.
		 * Otherwise, an exception is thrown.
		 */
		assertTrue(time >= 0);
	}

	@Test(dataProvider = "transportProtocolOptions")
	public void maxValueSizeTest(String clientName) throws Exception {

		byte[] value = new byte[(1024 * 1024) + 1];

		EntryMetadata entryMetadata = new EntryMetadata();
		entryMetadata.setAlgorithm(Algorithm.SHA1.toString());

		Entry entry = new Entry("maxValueSizeTest".getBytes(), value,
				entryMetadata);

		try {
			getClient(clientName).putForced(entry);
			throw new RuntimeException("expect exception is not thrown");
		} catch (KineticException e) {
			e.printStackTrace();
			// expected exception was thrown
			logger.info("received expected exception: " + e.getMessage());
		}
	}

	@Test(dataProvider = "transportProtocolOptions")
	public void putForcedTest(String clientName) throws Exception {
		/**
		 * start playing with the APIs.
		 */
		Random random = new Random();
		byte[] key = new byte[20];
		random.nextBytes(key);

		byte[] value = toByteArray("initValue");

		byte[] initVersion = new byte[20];
		random.nextBytes(initVersion);

		EntryMetadata entryMetadata = new EntryMetadata();
		entryMetadata.setAlgorithm(Algorithm.SHA1.toString());

		Entry versioned = new Entry(key, value, entryMetadata);

		/**
		 * put key/value and validate.
		 */
		Entry dbVersioned = getClient(clientName).put(versioned, initVersion);

		Entry vFromDb = getClient(clientName).get(key);

		assertTrue(Arrays.equals(vFromDb.getValue(), value));

		// do forced put
		byte[] forcedPutValue = toByteArray("ForcedPutValue");

		byte[] forcedPutVersion = new byte[20];
		random.nextBytes(forcedPutVersion);

		EntryMetadata entryMetadata2 = new EntryMetadata();
		entryMetadata2.setAlgorithm(Algorithm.SHA1.toString());

		// Entry versioned2 = new Entry(key, value, entryMetadata2);
		entryMetadata2.setVersion(forcedPutVersion);

		dbVersioned.setEntryMetadata(entryMetadata2);

		dbVersioned.setValue(forcedPutValue);

		/**
		 * forced put
		 */
		Entry dbVersioned2 = getClient(clientName).putForced(dbVersioned);

		assertTrue(Arrays.equals(dbVersioned2.getEntryMetadata().getVersion(),
				forcedPutVersion));

		// read from db
		Entry v2FromDb = getClient(clientName).get(key);

		// validate value was put successfully
		assertTrue(Arrays.equals(v2FromDb.getValue(), forcedPutValue));

		logger.info("put/get twice successfully, deleting entry ...");

		// delete entry
		boolean deleted = getClient(clientName).deleteForced(key);

		assertTrue(deleted == true);

		boolean deleted2 = getClient(clientName).deleteForced(key);

		assertTrue(deleted2 == true);

		Entry entry3 = getClient(clientName).get(key);

		assertTrue(entry3 == null);

		logger.info("forced put/delete passed");
	}

	@Test(dataProvider = "transportProtocolOptions")
	public void getAsyncTest(String clientName) throws Exception {

		int max = 10;

		List<Entry> masterList = new ArrayList<Entry>(max);
		List<String> dataList = new ArrayList<String>(max);

		// for put reply
		final List<Entry> putList = new ArrayList<Entry>(max);

		final List<Entry> getList = new ArrayList<Entry>(max);

		long start = System.nanoTime();

		// get signal
		final CountDownLatch getSignal = new CountDownLatch(max);

		// put signal
		final CountDownLatch putSignal = new CountDownLatch(max);

		// init and put data entries
		for (int i = 0; i < max; i++) {

			byte[] data = ByteBuffer.allocate(8).putLong(start + i).array();

			dataList.add(new String(data));

			logger.info("adding data index=" + i);

			// construct/put data to db
			EntryMetadata entryMetadata = new EntryMetadata();
			Entry versioned = new Entry(data, data, entryMetadata);

			EntryMetadata masterMetadata = new EntryMetadata();
			masterMetadata.setVersion(data);

			Entry masterEntry = new Entry(data, data, masterMetadata);
			masterList.add(masterEntry);

			getClient(clientName).putAsync(versioned, data, new CallbackHandler<Entry>() {

                @Override
                public void onSuccess(CallbackResult<Entry> result) {
                    // add to list

                    logger.fine(" putAsync received: "
                            + result.getResponseMessage());

                    putList.add(result.getResult());

                    putSignal.countDown();
                }

                @Override
                public void onError(AsyncKineticException exception) {
                    // TODO Auto-generated method stub
                    exception.printStackTrace();
                }

            });
		}

		putSignal.await(10, TimeUnit.SECONDS);
		assertEquals(max, putList.size());

		// The following validation assumes messages are in serial order per I/O
		// socket.
		for (int i = 0; i < max; i++) {
			assertTrue(dataList.contains(new String(putList.get(i).getKey())));
			assertTrue(dataList.contains(new String(putList.get(i).getValue())));
			assertTrue(dataList.contains(new String(putList.get(i)
					.getEntryMetadata().getVersion())));
		}

		// getAsync
		for (int i = 0; i < max; i++) {

			final byte[] key = putList.get(i).getKey();

			this.getClient(clientName).getAsync(key, new CallbackHandler<Entry>() {

                @Override
                public void onSuccess(CallbackResult<Entry> result) {

                    logger.fine(" getAsync received: "
                            + result.getResponseMessage());

                    getList.add(result.getResult());

                    getSignal.countDown();
                }

                @Override
                public void onError(AsyncKineticException exception) {
                    logger.log(Level.WARNING, exception.getMessage(), exception);
                }

            });
		}

		getSignal.await(10, TimeUnit.SECONDS);

		assertEquals(max, getList.size());

		// The following validation assumes messages are in serial order per I/O
		// socket.
		for (int i = 0; i < max; i++) {
			assertTrue(dataList.contains(new String(putList.get(i).getKey())));
			assertTrue(dataList.contains(new String(putList.get(i).getValue())));
			assertTrue(dataList.contains(new String(putList.get(i)
					.getEntryMetadata().getVersion())));
		}

		// clean up
		for (int i = 0; i < max; i++) {
			boolean deleted = getClient(clientName).delete(putList.get(i));
			assertTrue(deleted == true);
		}

		// verify clean up
		for (int i = 0; i < max; i++) {
			Entry v = getClient(clientName).get(putList.get(i).getKey());
			assertTrue(v == null);
		}

		logger.info("getAsyncTest passed ...");
	}

	@Test(dataProvider = "transportProtocolOptions")
	public void getNextTest(String clientName) throws Exception {

		List<byte[]> dataList = new ArrayList<byte[]>(10);
		List<Entry> versionedList = new ArrayList<Entry>(10);

		long start = System.nanoTime();

		int max = 10;

		// init and put data entries
		for (int i = 0; i < max; i++) {

			byte[] data = ByteBuffer.allocate(8).putLong(start + i).array();

			// add to cache
			dataList.add(data);

			logger.info("adding data index=" + i);

			// construct/put data to db
			EntryMetadata entryMetadata = new EntryMetadata();
			Entry versioned = new Entry(data, data, entryMetadata);
			Entry dbVersioned = getClient(clientName).put(versioned, data);

			logger.info("added data index=" + i);

			versionedList.add(dbVersioned);
		}

		// verify getNext
		for (int i = 0; i < (max - 1); i++) {
			Entry nextVersioned = getClient(clientName).getNext(versionedList.get(i)
                    .getKey());
			byte[] nextKey = nextVersioned.getKey();

			assertTrue(Arrays
					.equals(nextKey, versionedList.get(i + 1).getKey()));

		}

		try {
			Entry nextVersioned = getClient(clientName).getNext(versionedList.get(max - 1)
                    .getKey());

			assertTrue(nextVersioned == null);
		} catch (Exception e) {
			Assert.fail("caught exception: " + e.getMessage());
		}

		// clean up
		for (int i = 0; i < max; i++) {
			boolean deleted = getClient(clientName).delete(versionedList.get(i));
			assertTrue(deleted == true);
		}

		// verify clean up
		for (int i = 0; i < max; i++) {
			Entry v = getClient(clientName).get(versionedList.get(i).getKey());
			assertTrue(v == null);
		}

		logger.info("getNextTest passed ...");

	}

	@Test(dataProvider = "transportProtocolOptions")
	public void getPreviousTest(String clientName) throws Exception {

		List<byte[]> dataList = new ArrayList<byte[]>(10);
		List<Entry> versionedList = new ArrayList<Entry>(10);

		long start = System.nanoTime();

		int max = 10;

		// init and put entry
		for (int i = 0; i < max; i++) {

			byte[] data = ByteBuffer.allocate(8).putLong(start + i).array();

			// add to cache
			dataList.add(data);

			logger.info("adding data index=" + i);

			// construct/put data to db
			EntryMetadata entryMetadata = new EntryMetadata();
			Entry versioned = new Entry(data, data, entryMetadata);
			Entry dbVersioned = getClient(clientName).put(versioned, data);

			logger.info("added data index=" + i);

			versionedList.add(dbVersioned);
		}

		// verify get previous
		for (int i = 0; i < max - 1; i++) {

			Entry previousVersioned = getClient(clientName).getPrevious(versionedList.get(
                    max - i - 1).getKey());

			byte[] previousKey = previousVersioned.getKey();

			assertTrue(Arrays.equals(previousKey, versionedList
                    .get(max - i - 2).getKey()));

		}

		try {
			Entry previousVersioned = getClient(clientName).getPrevious(versionedList.get(
                    0).getKey());

			assertTrue(previousVersioned == null);

		} catch (Exception e) {
			// must not throw exception
			Assert.fail("caught exception: " + e.getClass().getName());
		}

		// clean up
		for (int i = 0; i < max; i++) {
			boolean deleted = getClient(clientName).delete(versionedList.get(i));
			assertTrue(deleted == true);
		}

		// verify clean up
		for (int i = 0; i < max; i++) {
			Entry v = getClient(clientName).get(versionedList.get(i).getKey());
			assertTrue(v == null);
		}

		logger.info("getPreviousTest passed ...");

	}

	@Test
	public void permuteTest() {

		// create a list from 1 to 20 in order
		List<Integer> l = new LinkedList<Integer>();
		for (int i = 0; i < 20; i++) {
			l.add(i + 1);
		}
		logger.info(toString("Original List: ", l));

		// Permute that list
		@SuppressWarnings("unchecked")
		List<Integer> pl = permute(l);

		// Write it out...
		logger.info(toString("Permuted List: ", pl));

		// sort it back...
		ArrayList<Integer> al = new ArrayList<Integer>();
		for (int i = 0; i < 20; i++) {
			al.add(0);
		}
		for (int x : pl) {
			if (0 != al.set(x - 1, x))
				Assert.fail("over wrote entry " + x);
		}
		logger.info(toString("Stored   List: ", al));

		// make sure they are equal
		assertTrue(Arrays.equals(l.toArray(), al.toArray()));

	}

	// The following randomly permutes of a list.
	// this algorithm is from Knuth volume 2, algorithm 3.4.2.P
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private List permute(List<?> l) {
		Random r = new Random();
		List f = new ArrayList(l);
		for (int j = f.size() - 1; j > 1; j--) {
			int k = (int) (r.nextFloat() * j);
			f.set(k, f.set(j, f.get(k)));
		}
		return f;
	}

	// helper to print out the members of a list
	private String toString(String s1, List<?> l) {
		StringWriter s = new StringWriter();
		s.write(s1);
		for (Object x : l) {
			s.write(" " + x);
		}
		return s.toString();
	}

	// TODO modify
	// @Test
	// public void jimsGetNextTest() throws Exception {
	//
	// // Create an ordered list of 120 keys. This will be used to check
	// // the ordering of the results.
	// List<Entry> ordered = new LinkedList<Entry>();
	// for (byte i = 0; i < 20; i++) {
	// ordered.add(new Entry(new byte[] { 0, i }, null, null));
	// for (byte j = 0; j < 5; j++) {
	// ordered.add(new Entry(new byte[] { 0, i, j }, null, null));
	// }
	// }
	//
	// for (Entry v : ordered) {
	// getClient().delete(v);
	// }
	//
	// @SuppressWarnings("unchecked")
	// List<Entry> permuted = permute(ordered);
	//
	// for (Entry v : permuted) {
	// getClient().put(v);
	// }
	//
	// // check various values of n.
	// Iterable<byte[]> read = getClient().getKeyRange(new byte[] { 0 }, true,
	// new byte[] { 1 }, true);
	//
	// int pos = 0;
	// for (byte[] key : read) {
	// Arrays.equals(ordered.get(pos++).getKey(), key);
	// }
	//
	// for (Entry v : ordered) {
	// assertTrue(getClient().delete(v));
	// }
	// }

	@Test(dataProvider = "transportProtocolOptions")
	public void getKeyRangeTest(String clientName) throws Exception {

		List<byte[]> dataList = new ArrayList<byte[]>(10);
		List<Entry> versionedList = new ArrayList<Entry>(10);

		long start = System.nanoTime();

		int max = 10;

		// init and put data entries
		for (int i = 0; i < max; i++) {

			byte[] data = ByteBuffer.allocate(8).putLong(start + i).array();

			// add to cache
			dataList.add(data);

			logger.info("adding data index=" + i);

			// construct/put data to db
			EntryMetadata entryMetadata = new EntryMetadata();
			Entry versioned = new Entry(data, data, entryMetadata);
			Entry dbVersioned = getClient(clientName).put(versioned, data);

			logger.info("added data index=" + i + ", key="
					+ ByteBuffer.wrap(data).getLong());

			versionedList.add(dbVersioned);
		}

		// verify getRange
		int startIndex = 3;
		boolean startInclusive = true;
		int endIndex = 7;
		boolean endInclusive = false;
		int expectReturnSize = endIndex - startIndex;

		List<byte[]> rangeKeys = getClient(clientName).getKeyRange(
                versionedList.get(startIndex).getKey(), startInclusive,
                versionedList.get(endIndex).getKey(), endInclusive,
                expectReturnSize);

		logger.info("get key range finished");

		int pos = 0;
		for (byte[] key : rangeKeys) {

			assertTrue(Arrays.equals(key, versionedList.get(startIndex + pos)
					.getKey()));
			pos++;
		}

		assertTrue(pos == expectReturnSize);
		// clean up
		for (int i = 0; i < max; i++) {
			boolean deleted = getClient(clientName).delete(versionedList.get(i));
			assertTrue(deleted == true);
		}

		// verify clean up
		for (int i = 0; i < max; i++) {
			Entry v = getClient(clientName).get(versionedList.get(i).getKey());
			assertTrue(v == null);
		}

		logger.info("getRangeTest passed ...");

	}
}
