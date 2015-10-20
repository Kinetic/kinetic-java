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
package com.seagate.kinetic.simulator.client.internal;

import static com.seagate.kinetic.KineticAssertions.assertListOfEntriesEqual;
import static com.seagate.kinetic.KineticTestHelpers.toByteArray;
import static org.testng.AssertJUnit.assertEquals;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

import kinetic.client.Entry;
import kinetic.client.EntryMetadata;
import kinetic.client.KineticException;

import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.jcraft.jsch.JSchException;
import com.seagate.kinetic.AbstractIntegrationTestTarget;
import com.seagate.kinetic.IntegrationTestTargetFactory;
import com.seagate.kinetic.KVGenerator;
import com.seagate.kinetic.client.internal.DefaultKineticClient;

@Test(groups = {"simulator"})
public class KineticImplTest {
	private final static Logger logger = Logger.getLogger(KineticImplTest.class
			.getName());

	private DefaultKineticClient kineticClient;
	private AbstractIntegrationTestTarget testTarget;

	@BeforeMethod
    public void startTestServer() throws InterruptedException,
            KineticException, IOException, JSchException, ExecutionException {
        testTarget = IntegrationTestTargetFactory.createTestTarget(true);
		kineticClient = new DefaultKineticClient(
		        IntegrationTestTargetFactory.createDefaultClientConfig());
	}

	@AfterMethod
    public void stopTestServer() throws Exception {
		kineticClient.close();
		testTarget.shutdown();
	}

	private List<Entry> prepareKeysForGetKeyRange() throws KineticException {
		int keyCount = 15;
		KVGenerator kvGenerator = new KVGenerator();

		kvGenerator.reset();
		List<Entry> vPutList = new ArrayList<Entry>();
		for (int i = 0; i < keyCount; i++) {
			String key = kvGenerator.getNextKey();
			String value = kvGenerator.getValue(key);
			byte[] version = toByteArray("0");
			EntryMetadata entryMetadata = new EntryMetadata();
			Entry v = new Entry(toByteArray(key), toByteArray(value),
					entryMetadata);
			Entry vIn = kineticClient.put(v, version);
			vPutList.add(vIn);
		}

		return vPutList;
	}

	@Test
	public void testIterableGetKeyRange_ReturnsCorrectValues_ForStartEndInclusive()
			throws KineticException, UnsupportedEncodingException {
		List<Entry> vPutList = prepareKeysForGetKeyRange();
		int startIndex = 0;
		int endIndex = 13;
		int expectSize = endIndex - startIndex + 1;

		Iterable<byte[]> keys1 = kineticClient.getKeyRange(
				vPutList.get(startIndex).getKey(), true, vPutList.get(endIndex)
						.getKey(), true, expectSize);
		int pos = 0;
		for (byte[] key : keys1) {
			AssertJUnit.assertArrayEquals(vPutList.get(startIndex + pos).getKey(), key);
			pos++;
		}
		assertEquals(expectSize, pos);
	}

	@Test
	public void testIterableGetKeyRange_ReturnsCorrectValues_ForEndInclusive()
			throws KineticException {
		List<Entry> vPutList = prepareKeysForGetKeyRange();
		int startIndex = 0;
		int endIndex = 13;
		int expectSize = endIndex - startIndex + 1;

		Iterable<byte[]> keys2 = kineticClient.getKeyRange(
				vPutList.get(startIndex).getKey(), false, vPutList
						.get(endIndex).getKey(), true, expectSize);
		int pos = 0;
		for (byte[] key : keys2) {
			AssertJUnit.assertArrayEquals(vPutList.get(startIndex + 1 + pos).getKey(), key);
			pos++;
		}
		assertEquals(expectSize - 1, pos);

	}

	@Test
	public void testIterableGetKeyRange_ReturnsCorrectValues_ForStartInclusive()
			throws KineticException {
		List<Entry> vPutList = prepareKeysForGetKeyRange();
		int startIndex = 0;
		int endIndex = 13;
		int expectSize = endIndex - startIndex + 1;

		Iterable<byte[]> keys3 = kineticClient.getKeyRange(
				vPutList.get(startIndex).getKey(), true, vPutList.get(endIndex)
						.getKey(), false, expectSize);
		int pos = 0;
		for (byte[] key : keys3) {
			AssertJUnit.assertArrayEquals(vPutList.get(startIndex + pos).getKey(), key);
			pos++;
		}
		assertEquals(expectSize - 1, pos);

	}

	@Test
	public void testIterableGetKeyRange_ReturnsCorrectValues_ForNotInclusive()
			throws KineticException {
		List<Entry> vPutList = prepareKeysForGetKeyRange();
		int startIndex = 0;
		int endIndex = 13;
		int expectSize = endIndex - startIndex + 1;
		Iterable<byte[]> keys4 = kineticClient.getKeyRange(
				vPutList.get(startIndex).getKey(), false, vPutList
						.get(endIndex).getKey(), false, expectSize);
		int pos = 0;
		for (byte[] key : keys4) {
			AssertJUnit.assertArrayEquals(vPutList.get(startIndex + 1 + pos).getKey(), key);
			pos++;
		}
		assertEquals(expectSize - 2, pos);
	}

	private List<Entry> prepareKeysForGetRange() throws KineticException {
		int keyCount = 20;

		KVGenerator kvGenerator = new KVGenerator();

		kvGenerator.reset();
		List<Entry> vPutList = new ArrayList<Entry>();
		for (int i = 0; i < keyCount; i++) {
			String key = kvGenerator.getNextKey();
			String value = kvGenerator.getValue(key);
			byte[] version = toByteArray("0");
			EntryMetadata entryMetadata = new EntryMetadata();
			Entry v = new Entry(toByteArray(key), toByteArray(value),
					entryMetadata);

			Entry vIn = kineticClient.put(v, version);
			vPutList.add(vIn);
		}

		return vPutList;
	}

	@Test
	public void testIterableGetRange_ReturnsCorrectValues_ForStartEndInclusive()
			throws KineticException, UnsupportedEncodingException {
		int startIndex = 0;
		int endIndex = 17;
		int expectSize = endIndex - startIndex + 1;
		List<Entry> vPutList = prepareKeysForGetRange();

		startIndex = 0;
		Iterable<Entry> versioneds1 = kineticClient.getRange(
				vPutList.get(startIndex).getKey(), true, vPutList.get(endIndex)
						.getKey(), true);
		int pos = 0;
		for (Entry versioned : versioneds1) {
			AssertJUnit.assertArrayEquals(vPutList.get(startIndex + pos).getKey(),
					versioned.getKey());
			AssertJUnit.assertArrayEquals(vPutList.get(startIndex + pos).getValue(),
					versioned.getValue());
			pos++;
		}
		assertEquals(expectSize, pos);

	}

	@Test
	public void testIterableGetRange_ReturnsCorrectValues_ForEndInclusive()
			throws KineticException, UnsupportedEncodingException {
		int startIndex = 0;
		int endIndex = 17;
		int expectSize = endIndex - startIndex + 1;
		List<Entry> vPutList = prepareKeysForGetRange();

		Iterable<Entry> versioneds2 = kineticClient.getRange(
				vPutList.get(startIndex).getKey(), false, vPutList
						.get(endIndex).getKey(), true);
		int pos = 0;
		for (Entry versioned : versioneds2) {
			AssertJUnit.assertArrayEquals(vPutList.get(startIndex + 1 + pos).getKey(),
					versioned.getKey());
			AssertJUnit.assertArrayEquals(vPutList.get(startIndex + 1 + pos).getValue(),
					versioned.getValue());
			pos++;
		}
		assertEquals(expectSize - 1, pos);

	}

	@Test
	public void testIterableGetRange_ReturnsCorrectValues_ForStartInclusive()
			throws KineticException, UnsupportedEncodingException {
		int startIndex = 0;
		int endIndex = 17;
		int expectSize = endIndex - startIndex + 1;
		List<Entry> vPutList = prepareKeysForGetRange();

		Iterable<Entry> versioneds3 = kineticClient.getRange(
				vPutList.get(startIndex).getKey(), true, vPutList.get(endIndex)
						.getKey(), false);
		int pos = 0;
		for (Entry versioned : versioneds3) {
			AssertJUnit.assertArrayEquals(vPutList.get(startIndex + pos).getKey(),
					versioned.getKey());
			AssertJUnit.assertArrayEquals(vPutList.get(startIndex + pos).getValue(),
					versioned.getValue());
			pos++;
		}
		assertEquals(expectSize - 1, pos);

	}

	@Test
	public void testIterableGetRange_ReturnsCorrectValues_ForNoneInclusive()
			throws KineticException, UnsupportedEncodingException {

		int startIndex = 0;
		int endIndex = 17;
		int expectSize = endIndex - startIndex + 1;
		List<Entry> vPutList = prepareKeysForGetRange();

		Iterable<Entry> versioneds4 = kineticClient.getRange(
				vPutList.get(startIndex).getKey(), false, vPutList
						.get(endIndex).getKey(), false);
		int pos = 0;
		for (Entry versioned : versioneds4) {
			AssertJUnit.assertArrayEquals(vPutList.get(startIndex + 1 + pos).getKey(),
					versioned.getKey());
			AssertJUnit.assertArrayEquals(vPutList.get(startIndex + 1 + pos).getValue(),
					versioned.getValue());
			pos++;
		}
		assertEquals(expectSize - 2, pos);
	}

	@Test
	public void getRangeTest() throws Exception {
		List<Entry> versionedList = new ArrayList<Entry>(10);

		long start = System.nanoTime();

		int max = 10;

		// init and put data entries
		for (int i = 0; i < max; i++) {

			byte[] data = ByteBuffer.allocate(8).putLong(start + i).array();

			logger.info("adding data index=" + i);

			// construct/put data to db
			EntryMetadata entryMetadata = new EntryMetadata();
			Entry versioned = new Entry(data, data, entryMetadata);
			Entry dbVersioned = kineticClient.put(versioned, data);

			logger.info("added data index=" + i + ", key="
					+ ByteBuffer.wrap(data).getLong());

			versionedList.add(dbVersioned);
		}

		// verify iterator
		int startIndex = 2;
		boolean startInclusive = true;
		int endIndex = 7;
		boolean endInclusive = false;

		Iterable<Entry> it = kineticClient.getRange(
				versionedList.get(startIndex).getKey(), startInclusive,
				versionedList.get(endIndex).getKey(), endInclusive);

		assertListOfEntriesEqual(versionedList.subList(startIndex, endIndex),
				it);
	}

	@Test
	public void iteratorTest() throws Exception {
		List<Entry> versionedList = new ArrayList<Entry>(10);

		long start = System.nanoTime();

		int max = 10;

		// init and put data entries
		for (int i = 0; i < max; i++) {

			byte[] data = ByteBuffer.allocate(8).putLong(start + i).array();

			logger.info("adding data index=" + i);

			// construct/put data to db
			EntryMetadata entryMetadata = new EntryMetadata();
			Entry versioned = new Entry(data, data, entryMetadata);
			Entry dbVersioned = kineticClient.put(versioned, data);

			logger.info("added data index=" + i + ", key="
					+ ByteBuffer.wrap(data).getLong());

			versionedList.add(dbVersioned);
		}

		// verify iterator
		int startIndex = 2;
		boolean startInclusive = true;
		int endIndex = 7;
		boolean endInclusive = false;

		Iterable<Entry> it = kineticClient.getRange(
				versionedList.get(startIndex).getKey(), startInclusive,
				versionedList.get(endIndex).getKey(), endInclusive);

		assertListOfEntriesEqual(versionedList.subList(startIndex, endIndex),
				it);
	}

	@Test
	public void iteratorThrowsNoSuchElementExceptionTest() throws Exception {
		List<Entry> versionedList = new ArrayList<Entry>(10);

		long start = System.nanoTime();

		int max = 10;

		// init and put data entries
		for (int i = 0; i < max; i++) {

			byte[] data = ByteBuffer.allocate(8).putLong(start + i).array();

			logger.info("adding data index=" + i);

			// construct/put data to db
			EntryMetadata entryMetadata = new EntryMetadata();
			Entry versioned = new Entry(data, data, entryMetadata);
			Entry dbVersioned = kineticClient.put(versioned, data);

			logger.info("added data index=" + i + ", key="
					+ ByteBuffer.wrap(data).getLong());

			versionedList.add(dbVersioned);
		}

		// verify iterator
		int startIndex = 5;
		boolean startInclusive = true;
		int endIndex = 9;
		boolean endInclusive = false;

		Iterator<Entry> it = kineticClient.getRange(
				versionedList.get(startIndex).getKey(), startInclusive,
				versionedList.get(endIndex).getKey(), endInclusive).iterator();

		int pos = 0;
		while (it.hasNext()) {

			Entry v = it.next();

			// verify key
			AssertJUnit.assertArrayEquals(versionedList.get(startIndex + pos).getKey(),
					v.getKey());

			// verify value
			AssertJUnit.assertArrayEquals(versionedList.get(startIndex + pos).getValue(),
					v.getValue());

			pos++;
		}

		boolean hasNext = it.hasNext();
		AssertJUnit.assertFalse(hasNext);

		try {
			it.next();
			AssertJUnit.fail("API did not throw NoSuchElementException");
		} catch (Exception e) {
		}
	}

	@Test
	public void iteratorWithDeleteEntryTest() throws Exception {
		int max = 20;
		List<Entry> versionedList = new ArrayList<Entry>(max);

		long start = 1000;

		// init and put data entries
		for (int i = 0; i < max; i++) {

			byte[] data = ByteBuffer.allocate(8).putLong(start + i).array();

			logger.info("adding data index=" + i);

			// construct/put data to db
			EntryMetadata entryMetadata = new EntryMetadata();
			Entry versioned = new Entry(data, data, entryMetadata);
			Entry dbVersioned = kineticClient.put(versioned, data);

			logger.info("added data index=" + i + ", key="
					+ ByteBuffer.wrap(data).getLong());

			versionedList.add(dbVersioned);
		}

		// verify iterator
		int startIndex = 3;
		boolean startInclusive = true;
		int endIndex = 19;
		boolean endInclusive = false;

		// the entry at (13+10) will be deleted
		int deleteOffset = 10;

		Iterator<Entry> it = kineticClient.getRange(
				versionedList.get(startIndex).getKey(), startInclusive,
				versionedList.get(endIndex).getKey(), endInclusive).iterator();

		int pos = 0;

		while (it.hasNext()) {

			if (pos == deleteOffset) {
				boolean deleted = kineticClient.delete(versionedList
						.get(startIndex + pos));

				AssertJUnit.assertTrue(deleted);
			}

			Entry v = it.next();

			// skip deleted offset index
			if (pos != deleteOffset) {

				int index = startIndex + pos;
				if (pos > deleteOffset) {
					index++;
				}

				// verify key
				AssertJUnit.assertArrayEquals(String.format(
						"Unexpected key at startIndex=%d pos=%d", startIndex,
						pos), versionedList.get(index).getKey(), v.getKey());

				// verify value
				AssertJUnit.assertArrayEquals(versionedList.get(index).getValue(),
						v.getValue());
			}

			pos++;
		}

		assertEquals(endIndex - startIndex - 1, pos);
	}

	@Test
	public void testGetKeyRangeWithReverse_StartKeyAndEndKeyInclusive_ExpectSizeLessThanRealSize()
			throws KineticException {
		int startIndex = 0;
		int endIndex = 17;
		int expectSize = endIndex - startIndex + 1;
		List<Entry> vPutList = prepareKeysForGetRange();

		List<byte[]> keys = kineticClient.getKeyRangeReversed(
				vPutList.get(startIndex).getKey(), true, vPutList.get(endIndex)
						.getKey(), true, expectSize);
		int pos = 0;
		for (byte[] key : keys) {
			AssertJUnit.assertArrayEquals(vPutList.get(endIndex - pos).getKey(), key);
			pos++;
		}
		assertEquals(expectSize, pos);
	}

	@Test
	public void testGetKeyRangeWithReverse_StartKeyAndEndKeyExclusive_ExpectSizeLessThanRealSize()
			throws KineticException {
		int startIndex = 0;
		int endIndex = 17;
		int expectSize = endIndex - startIndex + 1;
		List<Entry> vPutList = prepareKeysForGetRange();

		List<byte[]> keys = kineticClient.getKeyRangeReversed(
				vPutList.get(startIndex).getKey(), false, vPutList
						.get(endIndex).getKey(), false, expectSize);
		int pos = 0;
		for (byte[] key : keys) {
			AssertJUnit.assertArrayEquals(vPutList.get(endIndex - pos - 1).getKey(), key);
			pos++;
		}
		assertEquals(expectSize - 2, pos);
	}

	@Test
	public void testGetKeyRangeWithReverse_StartKeyInclusiveAndEndKeyExclusive_ExpectSizeLessThanRealSize()
			throws KineticException {
		int startIndex = 0;
		int endIndex = 17;
		int expectSize = endIndex - startIndex + 1;
		List<Entry> vPutList = prepareKeysForGetRange();

		List<byte[]> keys = kineticClient.getKeyRangeReversed(
				vPutList.get(startIndex).getKey(), true, vPutList.get(endIndex)
						.getKey(), false, expectSize);
		int pos = 0;
		for (byte[] key : keys) {
			AssertJUnit.assertArrayEquals(vPutList.get(endIndex - pos - 1).getKey(), key);
			pos++;
		}
		assertEquals(expectSize - 1, pos);
	}

	@Test
	public void testGetKeyRangeWithReverse_StartKeyExclusiveAndEndKeyInclusive_ExpectSizeLessThanRealSize()
			throws KineticException {
		int startIndex = 0;
		int endIndex = 17;
		int expectSize = endIndex - startIndex + 1;
		List<Entry> vPutList = prepareKeysForGetRange();

		List<byte[]> keys = kineticClient.getKeyRangeReversed(
				vPutList.get(startIndex).getKey(), false, vPutList
						.get(endIndex).getKey(), true, expectSize);
		int pos = 0;
		for (byte[] key : keys) {
			AssertJUnit.assertArrayEquals(vPutList.get(endIndex - pos).getKey(), key);
			pos++;
		}
		assertEquals(expectSize - 1, pos);
	}

	@Test
	public void testGetKeyRangeWithReverse_StartKeyAndEndKeyInclusive_ExpectSizeLessThanStartToEndKeySize()
			throws KineticException {
		int startIndex = 0;
		int endIndex = 17;
		int expectSize = endIndex - startIndex - 9;
		List<Entry> vPutList = prepareKeysForGetRange();

		List<byte[]> keys = kineticClient.getKeyRangeReversed(
				vPutList.get(startIndex).getKey(), true, vPutList.get(endIndex)
						.getKey(), true, expectSize);
		int pos = 0;
		for (byte[] key : keys) {
			AssertJUnit.assertArrayEquals(vPutList.get(endIndex - pos).getKey(), key);
			pos++;
		}
		assertEquals(expectSize, pos);
	}

	@Test
	public void testGetKeyRangeWithReverse_StartKeyInclusiveEndKeyExclusive_ExpectSizeLessThanStartToEndKeySize()
			throws KineticException {
		int startIndex = 0;
		int endIndex = 17;
		int expectSize = endIndex - startIndex - 9;
		List<Entry> vPutList = prepareKeysForGetRange();

		List<byte[]> keys = kineticClient.getKeyRangeReversed(
				vPutList.get(startIndex).getKey(), true, vPutList.get(endIndex)
						.getKey(), false, expectSize);
		int pos = 0;
		for (byte[] key : keys) {
			AssertJUnit.assertArrayEquals(vPutList.get(endIndex - pos - 1).getKey(), key);
			pos++;
		}
		assertEquals(expectSize, pos);
	}

	@Test
	public void testGetKeyRangeWithReverse_StartKeyExclusiveEndKeyInclusive_ExpectSizeLessThanStartToEndKeySize()
			throws KineticException {
		int startIndex = 0;
		int endIndex = 17;
		int expectSize = endIndex - startIndex - 9;
		List<Entry> vPutList = prepareKeysForGetRange();

		List<byte[]> keys = kineticClient.getKeyRangeReversed(
				vPutList.get(startIndex).getKey(), false, vPutList
						.get(endIndex).getKey(), true, expectSize);
		int pos = 0;
		for (byte[] key : keys) {
			AssertJUnit.assertArrayEquals(vPutList.get(endIndex - pos).getKey(), key);
			pos++;
		}
		assertEquals(expectSize, pos);
	}

	@Test
	public void testGetKeyRangeWithReverse_StartKeyInclusiveEndKeyInclusive_ExpectSizeLessThanStartToEndKeySize()
			throws KineticException {
		int startIndex = 0;
		int endIndex = 17;
		int expectSize = endIndex - startIndex - 9;
		List<Entry> vPutList = prepareKeysForGetRange();

		List<byte[]> keys = kineticClient.getKeyRangeReversed(
				vPutList.get(startIndex).getKey(), false, vPutList
						.get(endIndex).getKey(), false, expectSize);
		int pos = 0;
		for (byte[] key : keys) {
			AssertJUnit.assertArrayEquals(vPutList.get(endIndex - pos - 1).getKey(), key);
			pos++;
		}
		assertEquals(expectSize, pos);
	}

	@Test
	public void testGetKeyRangeWithReverse_StartKeyAndEndKeyInclusive_ExpectSizeBiggerThanStartToEndKeySize()
			throws KineticException {
		int startIndex = 0;
		int endIndex = 17;
		int expectSize = endIndex - startIndex + 5;
		List<Entry> vPutList = prepareKeysForGetRange();

		List<byte[]> keys = kineticClient.getKeyRangeReversed(
				vPutList.get(startIndex).getKey(), true, vPutList.get(endIndex)
						.getKey(), true, expectSize);
		int pos = 0;
		for (byte[] key : keys) {
			AssertJUnit.assertArrayEquals(vPutList.get(endIndex - pos).getKey(), key);
			pos++;
		}
		assertEquals(endIndex - startIndex + 1, pos);
	}

	@Test
	public void testGetKeyRangeWithReverse_StartKeyInclusiveEndKeyExclusive_ExpectSizeBiggerThanStartToEndKeySize()
			throws KineticException {
		int startIndex = 0;
		int endIndex = 17;
		int expectSize = endIndex - startIndex + 5;
		List<Entry> vPutList = prepareKeysForGetRange();

		List<byte[]> keys = kineticClient.getKeyRangeReversed(
				vPutList.get(startIndex).getKey(), true, vPutList.get(endIndex)
						.getKey(), false, expectSize);
		int pos = 0;
		for (byte[] key : keys) {
			AssertJUnit.assertArrayEquals(vPutList.get(endIndex - pos - 1).getKey(), key);
			pos++;
		}
		assertEquals(endIndex - startIndex, pos);
	}

	@Test
	public void testGetKeyRangeWithReverse_StartKeyExclusiveEndKeyInclusive_ExpectSizeBiggerThanStartToEndKeySize()
			throws KineticException {
		int startIndex = 0;
		int endIndex = 17;
		int expectSize = endIndex - startIndex + 5;
		List<Entry> vPutList = prepareKeysForGetRange();

		List<byte[]> keys = kineticClient.getKeyRangeReversed(
				vPutList.get(startIndex).getKey(), false, vPutList
						.get(endIndex).getKey(), true, expectSize);
		int pos = 0;
		for (byte[] key : keys) {
			AssertJUnit.assertArrayEquals(vPutList.get(endIndex - pos).getKey(), key);
			pos++;
		}
		assertEquals(endIndex - startIndex, pos);
	}

	@Test
	public void testGetKeyRangeWithReverse_StartKeyExclusiveEndKeyExclusive_ExpectSizeBiggerThanStartToEndKeySize()
			throws KineticException {
		int startIndex = 0;
		int endIndex = 17;
		int expectSize = endIndex - startIndex + 5;
		List<Entry> vPutList = prepareKeysForGetRange();

		List<byte[]> keys = kineticClient.getKeyRangeReversed(
				vPutList.get(startIndex).getKey(), false, vPutList
						.get(endIndex).getKey(), false, expectSize);
		int pos = 0;
		for (byte[] key : keys) {
			AssertJUnit.assertArrayEquals(vPutList.get(endIndex - pos - 1).getKey(), key);
			pos++;
		}
		assertEquals(endIndex - startIndex - 1, pos);
	}

	@Test
	public void testGetKeyRangeWithReverse_StartKeyAndEndKeyInclusive_ExpectSizeEqualsRealSize()
			throws KineticException {
		int startIndex = 0;
		int endIndex = 19;
		int expectSize = endIndex - startIndex + 1;
		List<Entry> vPutList = prepareKeysForGetRange();

		List<byte[]> keys = kineticClient.getKeyRangeReversed(
				vPutList.get(startIndex).getKey(), true, vPutList.get(endIndex)
						.getKey(), true, expectSize);
		int pos = 0;
		for (byte[] key : keys) {
			AssertJUnit.assertArrayEquals(vPutList.get(endIndex - pos).getKey(), key);
			pos++;
		}
		assertEquals(expectSize, pos);
	}

	@Test
	public void testGetKeyRangeWithReverse_StartKeyAndEndKeyExclusive_ExpectSizeEqualsRealSize()
			throws KineticException {
		int startIndex = 0;
		int endIndex = 19;
		int expectSize = endIndex - startIndex + 1;
		List<Entry> vPutList = prepareKeysForGetRange();

		List<byte[]> keys = kineticClient.getKeyRangeReversed(
				vPutList.get(startIndex).getKey(), false, vPutList
						.get(endIndex).getKey(), false, expectSize);
		int pos = 0;
		for (byte[] key : keys) {
			AssertJUnit.assertArrayEquals(vPutList.get(endIndex - pos - 1).getKey(), key);
			pos++;
		}
		assertEquals(expectSize - 2, pos);
	}

	@Test
	public void testGetKeyRangeWithReverse_StartKeyInclusiveAndEndKeyExclusive_ExpectSizeEqualsRealSize()
			throws KineticException {
		int startIndex = 0;
		int endIndex = 19;
		int expectSize = endIndex - startIndex + 1;
		List<Entry> vPutList = prepareKeysForGetRange();

		List<byte[]> keys = kineticClient.getKeyRangeReversed(
				vPutList.get(startIndex).getKey(), true, vPutList.get(endIndex)
						.getKey(), false, expectSize);
		int pos = 0;
		for (byte[] key : keys) {
			AssertJUnit.assertArrayEquals(vPutList.get(endIndex - pos - 1).getKey(), key);
			pos++;
		}
		assertEquals(expectSize - 1, pos);
	}

	@Test
	public void testGetKeyRangeWithReverse_StartKeyExclusiveAndEndKeyInclusive_ExpectSizeEqualsRealSize()
			throws KineticException {
		int startIndex = 0;
		int endIndex = 19;
		int expectSize = endIndex - startIndex + 1;
		List<Entry> vPutList = prepareKeysForGetRange();

		List<byte[]> keys = kineticClient.getKeyRangeReversed(
				vPutList.get(startIndex).getKey(), false, vPutList
						.get(endIndex).getKey(), true, expectSize);
		int pos = 0;
		for (byte[] key : keys) {
			AssertJUnit.assertArrayEquals(vPutList.get(endIndex - pos).getKey(), key);
			pos++;
		}
		assertEquals(expectSize - 1, pos);
	}

	@Test
	public void testGetKeyRangeWithReverse_StartKeyAndEndKeyInclusive_ExpectSizeBiggerThanRealSize()
			throws KineticException {
		int startIndex = 0;
		int endIndex = 19;
		int expectSize = endIndex - startIndex + 20;
		List<Entry> vPutList = prepareKeysForGetRange();

		List<byte[]> keys = kineticClient.getKeyRangeReversed(
				vPutList.get(startIndex).getKey(), true, vPutList.get(endIndex)
						.getKey(), true, expectSize);
		int pos = 0;
		for (byte[] key : keys) {
			AssertJUnit.assertArrayEquals(vPutList.get(endIndex - pos).getKey(), key);
			pos++;
		}
		assertEquals(endIndex - startIndex + 1, pos);
	}

	@Test
	public void testGetKeyRangeWithReverse_StartKeyAndEndKeyExclusive_ExpectSizeBiggerThanRealSize()
			throws KineticException {
		int startIndex = 0;
		int endIndex = 19;
		int expectSize = endIndex - startIndex + 20;
		List<Entry> vPutList = prepareKeysForGetRange();

		List<byte[]> keys = kineticClient.getKeyRangeReversed(
				vPutList.get(startIndex).getKey(), false, vPutList
						.get(endIndex).getKey(), false, expectSize);
		int pos = 0;
		for (byte[] key : keys) {
			AssertJUnit.assertArrayEquals(vPutList.get(endIndex - pos - 1).getKey(), key);
			pos++;
		}
		assertEquals(endIndex - startIndex - 1, pos);
	}

	@Test
	public void testGetKeyRangeWithReverse_StartKeyInclusiveAndEndKeyExclusive_ExpectSizeBiggerThanRealSize()
			throws KineticException {
		int startIndex = 0;
		int endIndex = 19;
		int expectSize = endIndex - startIndex + 20;
		List<Entry> vPutList = prepareKeysForGetRange();

		List<byte[]> keys = kineticClient.getKeyRangeReversed(
				vPutList.get(startIndex).getKey(), true, vPutList.get(endIndex)
						.getKey(), false, expectSize);
		int pos = 0;
		for (byte[] key : keys) {
			AssertJUnit.assertArrayEquals(vPutList.get(endIndex - pos - 1).getKey(), key);
			pos++;
		}
		assertEquals(endIndex - startIndex, pos);
	}

	@Test
	public void testGetKeyRangeWithReverse_StartKeyExclusiveAndEndKeyInclusive_ExpectSizeBiggerThanRealSize()
			throws KineticException {
		int startIndex = 0;
		int endIndex = 19;
		int expectSize = endIndex - startIndex + 20;
		List<Entry> vPutList = prepareKeysForGetRange();

		List<byte[]> keys = kineticClient.getKeyRangeReversed(
				vPutList.get(startIndex).getKey(), false, vPutList
						.get(endIndex).getKey(), true, expectSize);
		int pos = 0;
		for (byte[] key : keys) {
			AssertJUnit.assertArrayEquals(vPutList.get(endIndex - pos).getKey(), key);
			pos++;
		}
		assertEquals(endIndex - startIndex, pos);
	}

}
