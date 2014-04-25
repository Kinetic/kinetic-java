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
package com.seagate.kinetic.boundary;

import static com.seagate.kinetic.KineticTestHelpers.int32;
import static com.seagate.kinetic.KineticTestHelpers.toByteArray;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.logging.Logger;

import kinetic.client.Entry;
import kinetic.client.EntryMetadata;
import kinetic.client.KineticException;
import kinetic.client.advanced.AdvancedKineticClient;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.seagate.kinetic.IntegrationTestCase;
import com.seagate.kinetic.IntegrationTestLoggerFactory;
import com.seagate.kinetic.KineticTestRunner;

/**
 * Kinetic advanced Client API Boundary Test.
 * <p>
 * Boundary test against advance kinetic client API.
 * <p>
 * 
 * @see AdvancedKineticClient
 * 
 */
@RunWith(KineticTestRunner.class)
public class AdvancedAPIBoundaryTest extends IntegrationTestCase {
	private static final Logger logger = IntegrationTestLoggerFactory
			.getLogger(AdvancedAPIBoundaryTest.class.getName());

	/**
	 * GetKeyRangeReversed, startKey is null, the result should be thrown
	 * KineticException.
	 * <p>
	 * 
	 * @throws KineticException
	 *             if any internal error occurred.
	 */
	@Test
	public void testGetKeyRangeReversed_Throws_ForStartKeyIsNull()
			throws KineticException {
		byte[] key0 = toByteArray("key00000000000");
		byte[] newVersion0 = int32(0);
		byte[] value0 = toByteArray("value00000000000");
		EntryMetadata entryMetadata = new EntryMetadata();
		Entry versioned0 = new Entry(key0, value0, entryMetadata);

		byte[] key1 = toByteArray("key00000000001");
		byte[] newVersion1 = int32(1);
		byte[] value1 = toByteArray("value00000000001");
		EntryMetadata entryMetadata1 = new EntryMetadata();
		Entry versioned1 = new Entry(key1, value1, entryMetadata1);

		getClient().put(versioned0, newVersion0);
		getClient().put(versioned1, newVersion1);

		try {
			getClient().getKeyRangeReversed(null, true, key1, true, 10);
			fail("start key is null, get range reversed failed");
		} catch (KineticException e) {
			assertNull(e.getMessage());
		}

		logger.info(this.testEndInfo());
	}

	/**
	 * GetKeyRangeReversed, endKey is null, the result should be thrown
	 * KineticException.
	 * <p>
	 * 
	 * @throws KineticException
	 *             if any internal error occurred.
	 */
	@Test
	public void testGetKeyRangeReversed_Throws_ForEndKeyIsNull()
			throws KineticException {
		byte[] key0 = toByteArray("key00000000000");
		byte[] newVersion0 = int32(0);
		byte[] value0 = toByteArray("value00000000000");
		EntryMetadata entryMetadata = new EntryMetadata();
		Entry versioned0 = new Entry(key0, value0, entryMetadata);

		byte[] key1 = toByteArray("key00000000001");
		byte[] newVersion1 = int32(1);
		byte[] value1 = toByteArray("value00000000001");
		EntryMetadata entryMetadata1 = new EntryMetadata();
		Entry versioned1 = new Entry(key1, value1, entryMetadata1);

		getClient().put(versioned0, newVersion0);
		getClient().put(versioned1, newVersion1);

		try {
			getClient().getKeyRangeReversed(key0, true, null, true, 10);
			fail("end key is null, get range reversed failed");
		} catch (KineticException e) {
			assertNull(e.getMessage());
		}

		logger.info(this.testEndInfo());
	}

	/**
	 * GetKeyRangeReversed, startKey and endKey are inclusive, but they do not
	 * exist in simulator/drive, the result of key list should be empty.
	 * <p>
	 * 
	 * @throws KineticException
	 *             if any internal error occurred.
	 */
	@Test
	public void testGetKeyRangeReversed_ForStartKeyInclusiveEndKeyInclusive_WithStartAndEndKeyNotExistInDB()
			throws KineticException {
		byte[] key0 = toByteArray("key005");
		byte[] value0 = toByteArray("value005");
		Entry entry0 = new Entry(key0, value0);

		byte[] key1 = toByteArray("key006");
		byte[] value1 = toByteArray("value006");
		Entry entry1 = new Entry(key1, value1);

		byte[] key2 = toByteArray("key007");
		byte[] value2 = toByteArray("value007");
		Entry entry2 = new Entry(key2, value2);

		getClient().put(entry0, null);
		getClient().put(entry1, null);
		getClient().put(entry2, null);

		byte[] startKey = toByteArray("key00");
		byte[] endKey = toByteArray("key002");

		List<byte[]> keys = getClient().getKeyRangeReversed(startKey, true,
				endKey, true, 10);
		assertEquals(0, keys.size());

		logger.info(this.testEndInfo());
	}

	/**
	 * GetKeyRangeReversed, startKey and endKey are exclusive, but they do not
	 * exist in simulator/drive, the result of key list should be empty.
	 * <p>
	 * 
	 * @throws KineticException
	 *             if any internal error occurred.
	 */
	@Test
	public void testGetKeyRangeReversed_ForStartKeyExclusiveEndKeyExclusive_WithStartAndEndKeyNotExistInDB()
			throws KineticException {
		byte[] key0 = toByteArray("key005");
		byte[] value0 = toByteArray("value005");
		Entry entry0 = new Entry(key0, value0);

		byte[] key1 = toByteArray("key006");
		byte[] value1 = toByteArray("value006");
		Entry entry1 = new Entry(key1, value1);

		byte[] key2 = toByteArray("key007");
		byte[] value2 = toByteArray("value007");
		Entry entry2 = new Entry(key2, value2);

		getClient().put(entry0, null);
		getClient().put(entry1, null);
		getClient().put(entry2, null);

		byte[] startKey = toByteArray("key00");
		byte[] endKey = toByteArray("key002");

		List<byte[]> keys = getClient().getKeyRangeReversed(startKey, false,
				endKey, false, 10);
		assertEquals(0, keys.size());

		logger.info(this.testEndInfo());
	}

	/**
	 * GetKeyRangeReversed, startKey is inclusive, endKey is exclusive, but they
	 * do not exist in simulator/drive, the result of key list should be empty.
	 * <p>
	 * 
	 * @throws KineticException
	 *             if any internal error occurred.
	 */
	@Test
	public void testGetKeyRangeReversed_ForStartKeyInclusiveEndKeyExclusive_WithStartAndEndKeyNotExistInDB()
			throws KineticException {
		byte[] key0 = toByteArray("key005");
		byte[] value0 = toByteArray("value005");
		Entry entry0 = new Entry(key0, value0);

		byte[] key1 = toByteArray("key006");
		byte[] value1 = toByteArray("value006");
		Entry entry1 = new Entry(key1, value1);

		byte[] key2 = toByteArray("key007");
		byte[] value2 = toByteArray("value007");
		Entry entry2 = new Entry(key2, value2);

		getClient().put(entry0, null);
		getClient().put(entry1, null);
		getClient().put(entry2, null);

		byte[] startKey = toByteArray("key00");
		byte[] endKey = toByteArray("key002");

		List<byte[]> keys = getClient().getKeyRangeReversed(startKey, true,
				endKey, false, 10);
		assertEquals(0, keys.size());

		logger.info(this.testEndInfo());
	}

	/**
	 * GetKeyRangeReversed, startKey is inclusive, endKey is exclusive, but they
	 * do not exist in simulator/drive, the result of key list should be empty.
	 * <p>
	 * 
	 * @throws KineticException
	 *             if any internal error occurred.
	 */
	@Test
	public void testGetKeyRangeReversed_ForStartKeyExclusiveEndKeyInclusive_WithStartAndEndKeyNotExistInDB()
			throws KineticException {
		byte[] key0 = toByteArray("key005");
		byte[] value0 = toByteArray("value005");
		Entry entry0 = new Entry(key0, value0);

		byte[] key1 = toByteArray("key006");
		byte[] value1 = toByteArray("value006");
		Entry entry1 = new Entry(key1, value1);

		byte[] key2 = toByteArray("key007");
		byte[] value2 = toByteArray("value007");
		Entry entry2 = new Entry(key2, value2);

		getClient().put(entry0, null);
		getClient().put(entry1, null);
		getClient().put(entry2, null);

		byte[] startKey = toByteArray("key00");
		byte[] endKey = toByteArray("key002");

		List<byte[]> keys = getClient().getKeyRangeReversed(startKey, false,
				endKey, true, 10);
		assertEquals(0, keys.size());

		logger.info(this.testEndInfo());
	}

	/**
	 * GetKeyRangeReversed, startKey and endKey are exclusive, only endKey
	 * exists in simulator/drive, the result of key list should be empty.
	 * <p>
	 * 
	 * @throws KineticException
	 *             if any internal error occurred.
	 */
	@Test
	public void testGetKeyRangeReversed_ForStartKeyExclusiveEndKeyExclusive_WithEndKeyExistsInDB()
			throws KineticException {
		byte[] key0 = toByteArray("key005");
		byte[] value0 = toByteArray("value005");
		Entry entry0 = new Entry(key0, value0);

		byte[] key1 = toByteArray("key006");
		byte[] value1 = toByteArray("value006");
		Entry entry1 = new Entry(key1, value1);

		byte[] key2 = toByteArray("key007");
		byte[] value2 = toByteArray("value007");
		Entry entry2 = new Entry(key2, value2);

		getClient().put(entry0, null);
		getClient().put(entry1, null);
		getClient().put(entry2, null);

		byte[] startKey = toByteArray("key00");
		byte[] endKey = key0;

		List<byte[]> keys = getClient().getKeyRangeReversed(startKey, false,
				endKey, false, 10);
		assertEquals(0, keys.size());

		logger.info(this.testEndInfo());
	}

	/**
	 * GetKeyRangeReversed, startKey is inclusive and endKey is exclusive, only
	 * endKey exists in simulator/drive, the result of key list should be empty.
	 * <p>
	 * 
	 * @throws KineticException
	 *             if any internal error occurred.
	 */
	@Test
	public void testGetKeyRangeReversed_ForStartKeyInclusiveEndKeyExclusive_WithEndKeyExistsInDB()
			throws KineticException {
		byte[] key0 = toByteArray("key005");
		byte[] value0 = toByteArray("value005");
		Entry entry0 = new Entry(key0, value0);

		byte[] key1 = toByteArray("key006");
		byte[] value1 = toByteArray("value006");
		Entry entry1 = new Entry(key1, value1);

		byte[] key2 = toByteArray("key007");
		byte[] value2 = toByteArray("value007");
		Entry entry2 = new Entry(key2, value2);

		getClient().put(entry0, null);
		getClient().put(entry1, null);
		getClient().put(entry2, null);

		byte[] startKey = toByteArray("key00");
		byte[] endKey = key0;

		List<byte[]> keys = getClient().getKeyRangeReversed(startKey, true,
				endKey, false, 10);
		assertEquals(0, keys.size());

		logger.info(this.testEndInfo());
	}

	/**
	 * GetKeyRangeReversed, startKey is exclusive and endKey is inclusive, only
	 * endKey exists in simulator/drive, the result of key list should include
	 * end key.
	 * <p>
	 * 
	 * @throws KineticException
	 *             if any internal error occurred.
	 */
	@Test
	public void testGetKeyRangeReversed_ForStartKeyExclusiveEndKeyInclusive_WithEndKeyExistsInDB()
			throws KineticException {
		byte[] key0 = toByteArray("key005");
		byte[] value0 = toByteArray("value005");
		Entry entry0 = new Entry(key0, value0);

		byte[] key1 = toByteArray("key006");
		byte[] value1 = toByteArray("value006");
		Entry entry1 = new Entry(key1, value1);

		byte[] key2 = toByteArray("key007");
		byte[] value2 = toByteArray("value007");
		Entry entry2 = new Entry(key2, value2);

		getClient().put(entry0, null);
		getClient().put(entry1, null);
		getClient().put(entry2, null);

		byte[] startKey = toByteArray("key00");
		byte[] endKey = key0;

		List<byte[]> keys = getClient().getKeyRangeReversed(startKey, false,
				endKey, true, 10);
		assertEquals(1, keys.size());
		assertArrayEquals(key0, keys.get(0));

		logger.info(this.testEndInfo());
	}

	/**
	 * GetKeyRangeReversed, startKey and endKey is inclusive, only endKey exists
	 * in simulator/drive, the result of key list should include end key.
	 * <p>
	 * 
	 * @throws KineticException
	 *             if any internal error occurred.
	 */
	@Test
	public void testGetKeyRangeReversed_ForStartKeyInclusiveEndKeyInclusive_WithEndKeyExistsInDB()
			throws KineticException {
		byte[] key0 = toByteArray("key005");
		byte[] value0 = toByteArray("value005");
		Entry entry0 = new Entry(key0, value0);

		byte[] key1 = toByteArray("key006");
		byte[] value1 = toByteArray("value006");
		Entry entry1 = new Entry(key1, value1);

		byte[] key2 = toByteArray("key007");
		byte[] value2 = toByteArray("value007");
		Entry entry2 = new Entry(key2, value2);

		getClient().put(entry0, null);
		getClient().put(entry1, null);
		getClient().put(entry2, null);

		byte[] startKey = toByteArray("key00");
		byte[] endKey = key0;

		List<byte[]> keys = getClient().getKeyRangeReversed(startKey, true,
				endKey, true, 10);
		assertEquals(1, keys.size());
		assertArrayEquals(key0, keys.get(0));

		logger.info(this.testEndInfo());
	}

	/**
	 * GetKeyRangeReversed, startKey and endKey is inclusive, only endKey is the
	 * second key exists in simulator/drive, the result of key list should
	 * include the first key existed in simulator/drive.
	 * <p>
	 * 
	 * @throws KineticException
	 *             if any internal error occurred.
	 */
	@Test
	public void testGetKeyRangeReversed_ForStartKeyExclusiveEndKeyExclusive_WithEndKeyIsTheSecondKeyExistsInDB()
			throws KineticException {
		byte[] key0 = toByteArray("key005");
		byte[] value0 = toByteArray("value005");
		Entry entry0 = new Entry(key0, value0);

		byte[] key1 = toByteArray("key006");
		byte[] value1 = toByteArray("value006");
		Entry entry1 = new Entry(key1, value1);

		byte[] key2 = toByteArray("key007");
		byte[] value2 = toByteArray("value007");
		Entry entry2 = new Entry(key2, value2);

		getClient().put(entry0, null);
		getClient().put(entry1, null);
		getClient().put(entry2, null);

		byte[] startKey = toByteArray("key00");
		byte[] endKey = key1;

		List<byte[]> keys = getClient().getKeyRangeReversed(startKey, false,
				endKey, false, 10);
		assertEquals(1, keys.size());
		assertArrayEquals(key0, keys.get(0));

		logger.info(this.testEndInfo());
	}

	/**
	 * GetKeyRangeReversed, startKey is inclusive and endKey is exclusive, only
	 * endKey is the second key exists in simulator/drive, the result of key
	 * list should include the first key existed in simulator/drive.
	 * <p>
	 * 
	 * @throws KineticException
	 *             if any internal error occurred.
	 */
	@Test
	public void testGetKeyRangeReversed_ForStartKeyInclusiveEndKeyExclusive_WithEndKeyIsTheSecondKeyExistsInDB()
			throws KineticException {
		byte[] key0 = toByteArray("key005");
		byte[] value0 = toByteArray("value005");
		Entry entry0 = new Entry(key0, value0);

		byte[] key1 = toByteArray("key006");
		byte[] value1 = toByteArray("value006");
		Entry entry1 = new Entry(key1, value1);

		byte[] key2 = toByteArray("key007");
		byte[] value2 = toByteArray("value007");
		Entry entry2 = new Entry(key2, value2);

		getClient().put(entry0, null);
		getClient().put(entry1, null);
		getClient().put(entry2, null);

		byte[] startKey = toByteArray("key00");
		byte[] endKey = key1;

		List<byte[]> keys = getClient().getKeyRangeReversed(startKey, true,
				endKey, false, 10);
		assertEquals(1, keys.size());
		assertArrayEquals(key0, keys.get(0));

		logger.info(this.testEndInfo());
	}

	/**
	 * GetKeyRangeReversed, startKey is exclusive and endKey is inclusive, only
	 * endKey is the second key exists in simulator/drive, the result of key
	 * list should include the first key existed in simulator/drive and endKey.
	 * <p>
	 * 
	 * @throws KineticException
	 *             if any internal error occurred.
	 */
	@Test
	public void testGetKeyRangeReversed_ForStartKeyExclusiveEndKeyInclusive_WithEndKeyIsTheSecondKeyExistsInDB()
			throws KineticException {
		byte[] key0 = toByteArray("key005");
		byte[] value0 = toByteArray("value005");
		Entry entry0 = new Entry(key0, value0);

		byte[] key1 = toByteArray("key006");
		byte[] value1 = toByteArray("value006");
		Entry entry1 = new Entry(key1, value1);

		byte[] key2 = toByteArray("key007");
		byte[] value2 = toByteArray("value007");
		Entry entry2 = new Entry(key2, value2);

		getClient().put(entry0, null);
		getClient().put(entry1, null);
		getClient().put(entry2, null);

		byte[] startKey = toByteArray("key00");
		byte[] endKey = key1;

		List<byte[]> keys = getClient().getKeyRangeReversed(startKey, false,
				endKey, true, 10);
		assertEquals(2, keys.size());
		assertArrayEquals(key1, keys.get(0));
		assertArrayEquals(key0, keys.get(1));

		logger.info(this.testEndInfo());
	}

	/**
	 * GetKeyRangeReversed, startKey is inclusive and endKey is inclusive, only
	 * endKey is the second key exists in simulator/drive, the result of key
	 * list should include the first key existed in simulator/drive and endKey.
	 * <p>
	 * 
	 * @throws KineticException
	 *             if any internal error occurred.
	 */
	@Test
	public void testGetKeyRangeReversed_ForStartKeyInclusiveEndKeyInclusive_WithEndKeyIsTheSecondKeyExistsInDB()
			throws KineticException {
		byte[] key0 = toByteArray("key005");
		byte[] value0 = toByteArray("value005");
		Entry entry0 = new Entry(key0, value0);

		byte[] key1 = toByteArray("key006");
		byte[] value1 = toByteArray("value006");
		Entry entry1 = new Entry(key1, value1);

		byte[] key2 = toByteArray("key007");
		byte[] value2 = toByteArray("value007");
		Entry entry2 = new Entry(key2, value2);

		getClient().put(entry0, null);
		getClient().put(entry1, null);
		getClient().put(entry2, null);

		byte[] startKey = toByteArray("key00");
		byte[] endKey = key1;

		List<byte[]> keys = getClient().getKeyRangeReversed(startKey, true,
				endKey, true, 10);
		assertEquals(2, keys.size());
		assertArrayEquals(key1, keys.get(0));
		assertArrayEquals(key0, keys.get(1));

		logger.info(this.testEndInfo());
	}

	/**
	 * GetKeyRangeReversed, startKey and endKey is exclusive, only endKey is the
	 * last key exists in simulator/drive, the result of key list should include
	 * the key existed in simulator/drive without the endKey.
	 * <p>
	 * 
	 * @throws KineticException
	 *             if any internal error occurred.
	 */
	@Test
	public void testGetKeyRangeReversed_ForStartKeyExclusiveEndKeyExclusive_WithEndKeyIsTheLastKeyExistsInDB()
			throws KineticException {
		byte[] key0 = toByteArray("key005");
		byte[] value0 = toByteArray("value005");
		Entry entry0 = new Entry(key0, value0);

		byte[] key1 = toByteArray("key006");
		byte[] value1 = toByteArray("value006");
		Entry entry1 = new Entry(key1, value1);

		byte[] key2 = toByteArray("key007");
		byte[] value2 = toByteArray("value007");
		Entry entry2 = new Entry(key2, value2);

		getClient().put(entry0, null);
		getClient().put(entry1, null);
		getClient().put(entry2, null);

		byte[] startKey = toByteArray("key00");
		byte[] endKey = key2;

		List<byte[]> keys = getClient().getKeyRangeReversed(startKey, false,
				endKey, false, 10);
		assertEquals(2, keys.size());
		assertArrayEquals(key1, keys.get(0));
		assertArrayEquals(key0, keys.get(1));

		logger.info(this.testEndInfo());
	}

	/**
	 * GetKeyRangeReversed, startKey is inclusive and endKey is exclusive, only
	 * endKey is the last key exists in simulator/drive, the result of key list
	 * should include the key existed in simulator/drive without the endKey.
	 * <p>
	 * 
	 * @throws KineticException
	 *             if any internal error occurred.
	 */
	@Test
	public void testGetKeyRangeReversed_ForStartKeyInclusiveEndKeyExclusive_WithEndKeyIsTheLastKeyExistsInDB()
			throws KineticException {
		byte[] key0 = toByteArray("key005");
		byte[] value0 = toByteArray("value005");
		Entry entry0 = new Entry(key0, value0);

		byte[] key1 = toByteArray("key006");
		byte[] value1 = toByteArray("value006");
		Entry entry1 = new Entry(key1, value1);

		byte[] key2 = toByteArray("key007");
		byte[] value2 = toByteArray("value007");
		Entry entry2 = new Entry(key2, value2);

		getClient().put(entry0, null);
		getClient().put(entry1, null);
		getClient().put(entry2, null);

		byte[] startKey = toByteArray("key00");
		byte[] endKey = key2;

		List<byte[]> keys = getClient().getKeyRangeReversed(startKey, true,
				endKey, false, 10);
		assertEquals(2, keys.size());
		assertArrayEquals(key1, keys.get(0));
		assertArrayEquals(key0, keys.get(1));

		logger.info(this.testEndInfo());
	}

	/**
	 * GetKeyRangeReversed, startKey is exclusive and endKey is inclusive, only
	 * endKey is the last key exists in simulator/drive, the result of key list
	 * should include the key existed in simulator/drive.
	 * <p>
	 * 
	 * @throws KineticException
	 *             if any internal error occurred.
	 */
	@Test
	public void testGetKeyRangeReversed_ForStartKeyExclusiveEndKeyInclusive_WithEndKeyIsTheLastKeyExistsInDB()
			throws KineticException {
		byte[] key0 = toByteArray("key005");
		byte[] value0 = toByteArray("value005");
		Entry entry0 = new Entry(key0, value0);

		byte[] key1 = toByteArray("key006");
		byte[] value1 = toByteArray("value006");
		Entry entry1 = new Entry(key1, value1);

		byte[] key2 = toByteArray("key007");
		byte[] value2 = toByteArray("value007");
		Entry entry2 = new Entry(key2, value2);

		getClient().put(entry0, null);
		getClient().put(entry1, null);
		getClient().put(entry2, null);

		byte[] startKey = toByteArray("key00");
		byte[] endKey = key2;

		List<byte[]> keys = getClient().getKeyRangeReversed(startKey, false,
				endKey, true, 10);
		assertEquals(3, keys.size());
		assertArrayEquals(key2, keys.get(0));
		assertArrayEquals(key1, keys.get(1));
		assertArrayEquals(key0, keys.get(2));

		logger.info(this.testEndInfo());
	}

	/**
	 * GetKeyRangeReversed, startKey and endKey is inclusive, only endKey is the
	 * last key exists in simulator/drive, the result of key list should include
	 * the key existed in simulator/drive.
	 * <p>
	 * 
	 * @throws KineticException
	 *             if any internal error occurred.
	 */
	@Test
	public void testGetKeyRangeReversed_ForStartKeyInclusiveEndKeyInclusive_WithEndKeyIsTheLastKeyExistsInDB()
			throws KineticException {
		byte[] key0 = toByteArray("key005");
		byte[] value0 = toByteArray("value005");
		Entry entry0 = new Entry(key0, value0);

		byte[] key1 = toByteArray("key006");
		byte[] value1 = toByteArray("value006");
		Entry entry1 = new Entry(key1, value1);

		byte[] key2 = toByteArray("key007");
		byte[] value2 = toByteArray("value007");
		Entry entry2 = new Entry(key2, value2);

		getClient().put(entry0, null);
		getClient().put(entry1, null);
		getClient().put(entry2, null);

		byte[] startKey = toByteArray("key00");
		byte[] endKey = key2;

		List<byte[]> keys = getClient().getKeyRangeReversed(startKey, true,
				endKey, true, 10);
		assertEquals(3, keys.size());
		assertArrayEquals(key2, keys.get(0));
		assertArrayEquals(key1, keys.get(1));
		assertArrayEquals(key0, keys.get(2));

		logger.info(this.testEndInfo());
	}

	/**
	 * GetKeyRangeReversed, startKey and endKey is exclusive, startKey is the
	 * first key and endKey is the second key exist in simulator/drive, the
	 * result of key list should empty.
	 * <p>
	 * 
	 * @throws KineticException
	 *             if any internal error occurred.
	 */
	@Test
	public void testGetKeyRangeReversed_ForStartKeyExclusiveEndKeyExclusive_WithStartKeyIsTheFirstKeyAndEndKeyIsTheSecondKeyInDB()
			throws KineticException {
		byte[] key0 = toByteArray("key005");
		byte[] value0 = toByteArray("value005");
		Entry entry0 = new Entry(key0, value0);

		byte[] key1 = toByteArray("key006");
		byte[] value1 = toByteArray("value006");
		Entry entry1 = new Entry(key1, value1);

		byte[] key2 = toByteArray("key007");
		byte[] value2 = toByteArray("value007");
		Entry entry2 = new Entry(key2, value2);

		getClient().put(entry0, null);
		getClient().put(entry1, null);
		getClient().put(entry2, null);

		byte[] startKey = key0;
		byte[] endKey = key1;

		List<byte[]> keys = getClient().getKeyRangeReversed(startKey, false,
				endKey, false, 10);
		assertEquals(0, keys.size());

		logger.info(this.testEndInfo());
	}

	/**
	 * GetKeyRangeReversed, startKey is inclusive and endKey is exclusive,
	 * startKey is the first key and endKey is the second key exist in
	 * simulator/drive, the result of key list should include the first key.
	 * <p>
	 * 
	 * @throws KineticException
	 *             if any internal error occurred.
	 */
	@Test
	public void gtestGetKeyRangeReversed_ForStartKeyInclusiveEndKeyExclusive_WithStartKeyIsTheFirstKeyAndEndKeyIsTheSecondKeyInDB()
			throws KineticException {
		byte[] key0 = toByteArray("key005");
		byte[] value0 = toByteArray("value005");
		Entry entry0 = new Entry(key0, value0);

		byte[] key1 = toByteArray("key006");
		byte[] value1 = toByteArray("value006");
		Entry entry1 = new Entry(key1, value1);

		byte[] key2 = toByteArray("key007");
		byte[] value2 = toByteArray("value007");
		Entry entry2 = new Entry(key2, value2);

		getClient().put(entry0, null);
		getClient().put(entry1, null);
		getClient().put(entry2, null);

		byte[] startKey = key0;
		byte[] endKey = key1;

		List<byte[]> keys = getClient().getKeyRangeReversed(startKey, true,
				endKey, false, 10);
		assertEquals(1, keys.size());
		assertArrayEquals(key0, keys.get(0));

		logger.info(this.testEndInfo());
	}

	/**
	 * GetKeyRangeReversed, startKey is exclusive and endKey is inclusive,
	 * startKey is the first key and endKey is the second key exist in
	 * simulator/drive, the result of key list should include endKey existed in
	 * simulator/drive.
	 * <p>
	 * 
	 * @throws KineticException
	 *             if any internal error occurred.
	 */
	@Test
	public void testgetKeyRangeReversed_ForStartKeyExclusiveEndKeyInclusive_WithStartKeyIsTheFirstKeyAndEndKeyIsTheSecondKeyInDB()
			throws KineticException {
		byte[] key0 = toByteArray("key005");
		byte[] value0 = toByteArray("value005");
		Entry entry0 = new Entry(key0, value0);

		byte[] key1 = toByteArray("key006");
		byte[] value1 = toByteArray("value006");
		Entry entry1 = new Entry(key1, value1);

		byte[] key2 = toByteArray("key007");
		byte[] value2 = toByteArray("value007");
		Entry entry2 = new Entry(key2, value2);

		getClient().put(entry0, null);
		getClient().put(entry1, null);
		getClient().put(entry2, null);

		byte[] startKey = key0;
		byte[] endKey = key1;

		List<byte[]> keys = getClient().getKeyRangeReversed(startKey, false,
				endKey, true, 10);
		assertEquals(1, keys.size());
		assertArrayEquals(key1, keys.get(0));

		logger.info(this.testEndInfo());
	}

	/**
	 * GetKeyRangeReversed, startKey and endKey are inclusive, startKey is the
	 * first key and endKey is the second key exist in simulator/drive, the
	 * result of key list should include startKey and endKey existed in
	 * simulator/drive.
	 * <p>
	 * 
	 * @throws KineticException
	 *             if any internal error occurred.
	 */
	@Test
	public void testGetKeyRangeReversed_ForStartKeyInclusiveEndKeyInclusive_WithStartKeyIsTheFirstKeyAndEndKeyIsTheSecondKeyInDB()
			throws KineticException {
		byte[] key0 = toByteArray("key005");
		byte[] value0 = toByteArray("value005");
		Entry entry0 = new Entry(key0, value0);

		byte[] key1 = toByteArray("key006");
		byte[] value1 = toByteArray("value006");
		Entry entry1 = new Entry(key1, value1);

		byte[] key2 = toByteArray("key007");
		byte[] value2 = toByteArray("value007");
		Entry entry2 = new Entry(key2, value2);

		getClient().put(entry0, null);
		getClient().put(entry1, null);
		getClient().put(entry2, null);

		byte[] startKey = key0;
		byte[] endKey = key1;

		List<byte[]> keys = getClient().getKeyRangeReversed(startKey, true,
				endKey, true, 10);
		assertEquals(2, keys.size());
		assertArrayEquals(key1, keys.get(0));
		assertArrayEquals(key0, keys.get(1));

		logger.info(this.testEndInfo());
	}

	/**
	 * GetKeyRangeReversed, startKey and endKey are exclusive, startKey is the
	 * first key and endKey is the last key exist in simulator/drive, the result
	 * of key list should include keys existed in simulator/drive without
	 * startKey and endKey .
	 * <p>
	 * 
	 * @throws KineticException
	 *             if any internal error occurred.
	 */
	@Test
	public void testGetKeyRangeReversed_ForStartKeyExclusiveEndKeyExclusive_WithStartKeyIsTheFirstKeyAndEndKeyIsTheLastKeyInDB()
			throws KineticException {
		byte[] key0 = toByteArray("key005");
		byte[] value0 = toByteArray("value005");
		Entry entry0 = new Entry(key0, value0);

		byte[] key1 = toByteArray("key006");
		byte[] value1 = toByteArray("value006");
		Entry entry1 = new Entry(key1, value1);

		byte[] key2 = toByteArray("key007");
		byte[] value2 = toByteArray("value007");
		Entry entry2 = new Entry(key2, value2);

		getClient().put(entry0, null);
		getClient().put(entry1, null);
		getClient().put(entry2, null);

		byte[] startKey = key0;
		byte[] endKey = key2;

		List<byte[]> keys = getClient().getKeyRangeReversed(startKey, false,
				endKey, false, 10);
		assertEquals(1, keys.size());
		assertArrayEquals(key1, keys.get(0));

		logger.info(this.testEndInfo());
	}

	/**
	 * GetKeyRangeReversed, startKey is inclusive and endKey is exclusive,
	 * startKey is the first key and endKey is the last key exist in
	 * simulator/drive, the result of key list should include keys existed in
	 * simulator/drive without endKey.
	 * <p>
	 * 
	 * @throws KineticException
	 *             if any internal error occurred.
	 */
	@Test
	public void testGetKeyRangeReversed_ForStartKeyInclusiveEndKeyExclusive_WithStartKeyIsTheFirstKeyAndEndKeyIsTheLastKeyInDB()
			throws KineticException {
		byte[] key0 = toByteArray("key005");
		byte[] value0 = toByteArray("value005");
		Entry entry0 = new Entry(key0, value0);

		byte[] key1 = toByteArray("key006");
		byte[] value1 = toByteArray("value006");
		Entry entry1 = new Entry(key1, value1);

		byte[] key2 = toByteArray("key007");
		byte[] value2 = toByteArray("value007");
		Entry entry2 = new Entry(key2, value2);

		getClient().put(entry0, null);
		getClient().put(entry1, null);
		getClient().put(entry2, null);

		byte[] startKey = key0;
		byte[] endKey = key2;

		List<byte[]> keys = getClient().getKeyRangeReversed(startKey, true,
				endKey, false, 10);
		assertEquals(2, keys.size());
		assertArrayEquals(key1, keys.get(0));
		assertArrayEquals(key0, keys.get(1));

		logger.info(this.testEndInfo());
	}

	/**
	 * GetKeyRangeReversed, startKey is exclusive and endKey is inclusive,
	 * startKey is the first key and endKey is the last key exist in
	 * simulator/drive, the result of key list should include keys existed in
	 * simulator/drive without startKey.
	 * <p>
	 * 
	 * @throws KineticException
	 *             if any internal error occurred.
	 */
	@Test
	public void testGetKeyRangeReversed_ForStartKeyExclusiveEndKeyInclusive_WithStartKeyIsTheFirstKeyAndEndKeyIsTheLastKeyInDB()
			throws KineticException {
		byte[] key0 = toByteArray("key005");
		byte[] value0 = toByteArray("value005");
		Entry entry0 = new Entry(key0, value0);

		byte[] key1 = toByteArray("key006");
		byte[] value1 = toByteArray("value006");
		Entry entry1 = new Entry(key1, value1);

		byte[] key2 = toByteArray("key007");
		byte[] value2 = toByteArray("value007");
		Entry entry2 = new Entry(key2, value2);

		getClient().put(entry0, null);
		getClient().put(entry1, null);
		getClient().put(entry2, null);

		byte[] startKey = key0;
		byte[] endKey = key2;

		List<byte[]> keys = getClient().getKeyRangeReversed(startKey, false,
				endKey, true, 10);
		assertEquals(2, keys.size());
		assertArrayEquals(key2, keys.get(0));
		assertArrayEquals(key1, keys.get(1));

		logger.info(this.testEndInfo());
	}

	/**
	 * GetKeyRangeReversed, startKey and endKey are inclusive, startKey is the
	 * first key and endKey is the last key exist in simulator/drive, the result
	 * of key list should include all keys existed in simulator/drive.
	 * <p>
	 * 
	 * @throws KineticException
	 *             if any internal error occurred.
	 */
	@Test
	public void testGetKeyRangeReversed_ForStartKeyInclusiveEndKeyInclusive_WithStartKeyIsTheFirstKeyAndEndKeyIsTheLastKeyInDB()
			throws KineticException {
		byte[] key0 = toByteArray("key005");
		byte[] value0 = toByteArray("value005");
		Entry entry0 = new Entry(key0, value0);

		byte[] key1 = toByteArray("key006");
		byte[] value1 = toByteArray("value006");
		Entry entry1 = new Entry(key1, value1);

		byte[] key2 = toByteArray("key007");
		byte[] value2 = toByteArray("value007");
		Entry entry2 = new Entry(key2, value2);

		getClient().put(entry0, null);
		getClient().put(entry1, null);
		getClient().put(entry2, null);

		byte[] startKey = key0;
		byte[] endKey = key2;

		List<byte[]> keys = getClient().getKeyRangeReversed(startKey, true,
				endKey, true, 10);
		assertEquals(3, keys.size());
		assertArrayEquals(key2, keys.get(0));
		assertArrayEquals(key1, keys.get(1));
		assertArrayEquals(key0, keys.get(2));

		logger.info(this.testEndInfo());
	}

	/**
	 * GetKeyRangeReversed, startKey and endKey are exclusive, startKey is the
	 * second key and endKey is the last key exist in simulator/drive, the
	 * result of key list should be empty.
	 * <p>
	 * 
	 * @throws KineticException
	 *             if any internal error occurred.
	 */
	@Test
	public void testGetKeyRangeReversed_ForStartKeyExclusiveEndKeyExclusive_WithStartKeyIsTheSecondKeyAndEndKeyIsTheLastKeyInDB()
			throws KineticException {
		byte[] key0 = toByteArray("key005");
		byte[] value0 = toByteArray("value005");
		Entry entry0 = new Entry(key0, value0);

		byte[] key1 = toByteArray("key006");
		byte[] value1 = toByteArray("value006");
		Entry entry1 = new Entry(key1, value1);

		byte[] key2 = toByteArray("key007");
		byte[] value2 = toByteArray("value007");
		Entry entry2 = new Entry(key2, value2);

		getClient().put(entry0, null);
		getClient().put(entry1, null);
		getClient().put(entry2, null);

		byte[] startKey = key1;
		byte[] endKey = key2;

		List<byte[]> keys = getClient().getKeyRangeReversed(startKey, false,
				endKey, false, 10);
		assertEquals(0, keys.size());

		logger.info(this.testEndInfo());
	}

	/**
	 * GetKeyRangeReversed, startKey is inclusive and endKey is exclusive,
	 * startKey is the second key and endKey is the last key exist in
	 * simulator/drive, the result of key list should include startKey.
	 * <p>
	 * 
	 * @throws KineticException
	 *             if any internal error occurred.
	 */
	@Test
	public void testGetKeyRangeReversed_ForStartKeyInclusiveEndKeyExclusive_WithStartKeyIsTheSecondKeyAndEndKeyIsTheLastKeyInDB()
			throws KineticException {
		byte[] key0 = toByteArray("key005");
		byte[] value0 = toByteArray("value005");
		Entry entry0 = new Entry(key0, value0);

		byte[] key1 = toByteArray("key006");
		byte[] value1 = toByteArray("value006");
		Entry entry1 = new Entry(key1, value1);

		byte[] key2 = toByteArray("key007");
		byte[] value2 = toByteArray("value007");
		Entry entry2 = new Entry(key2, value2);

		getClient().put(entry0, null);
		getClient().put(entry1, null);
		getClient().put(entry2, null);

		byte[] startKey = key1;
		byte[] endKey = key2;

		List<byte[]> keys = getClient().getKeyRangeReversed(startKey, true,
				endKey, false, 10);
		assertEquals(1, keys.size());
		assertArrayEquals(key1, keys.get(0));

		logger.info(this.testEndInfo());
	}

	/**
	 * GetKeyRangeReversed, startKey is exclusive and endKey is inclusive,
	 * startKey is the second key and endKey is the last key exist in
	 * simulator/drive, the result of key list should include endKey.
	 * <p>
	 * 
	 * @throws KineticException
	 *             if any internal error occurred.
	 */
	@Test
	public void testGetKeyRangeReversed_ForStartKeyExclusiveEndKeyInclusive_WithStartKeyIsTheSecondKeyAndEndKeyIsTheLastKeyInDB()
			throws KineticException {
		byte[] key0 = toByteArray("key005");
		byte[] value0 = toByteArray("value005");
		Entry entry0 = new Entry(key0, value0);

		byte[] key1 = toByteArray("key006");
		byte[] value1 = toByteArray("value006");
		Entry entry1 = new Entry(key1, value1);

		byte[] key2 = toByteArray("key007");
		byte[] value2 = toByteArray("value007");
		Entry entry2 = new Entry(key2, value2);

		getClient().put(entry0, null);
		getClient().put(entry1, null);
		getClient().put(entry2, null);

		byte[] startKey = key1;
		byte[] endKey = key2;

		List<byte[]> keys = getClient().getKeyRangeReversed(startKey, false,
				endKey, true, 10);
		assertEquals(1, keys.size());
		assertArrayEquals(key2, keys.get(0));

		logger.info(this.testEndInfo());
	}

	/**
	 * GetKeyRangeReversed, startKey and endKey are inclusive, startKey is the
	 * second key and endKey is the last key exist in simulator/drive, the
	 * result of key list should include startKey and endKey.
	 * <p>
	 * 
	 * @throws KineticException
	 *             if any internal error occurred.
	 */
	@Test
	public void testGetKeyRangeReversed_ForStartKeyInclusiveEndKeyInclusive_WithStartKeyIsTheSecondKeyAndEndKeyIsTheLastKeyInDB()
			throws KineticException {
		byte[] key0 = toByteArray("key005");
		byte[] value0 = toByteArray("value005");
		Entry entry0 = new Entry(key0, value0);

		byte[] key1 = toByteArray("key006");
		byte[] value1 = toByteArray("value006");
		Entry entry1 = new Entry(key1, value1);

		byte[] key2 = toByteArray("key007");
		byte[] value2 = toByteArray("value007");
		Entry entry2 = new Entry(key2, value2);

		getClient().put(entry0, null);
		getClient().put(entry1, null);
		getClient().put(entry2, null);

		byte[] startKey = key1;
		byte[] endKey = key2;

		List<byte[]> keys = getClient().getKeyRangeReversed(startKey, true,
				endKey, true, 10);
		assertEquals(2, keys.size());
		assertArrayEquals(key2, keys.get(0));
		assertArrayEquals(key1, keys.get(1));

		logger.info(this.testEndInfo());
	}

	/**
	 * GetKeyRangeReversed, startKey and endKey are exclusive, startKey is the
	 * last key in simulator/drive, the result of key list should include be
	 * empty.
	 * <p>
	 * 
	 * @throws KineticException
	 *             if any internal error occurred.
	 */
	@Test
	public void testGetKeyRangeReversed_ForStartKeyExclusiveEndKeyExclusive_WithStartKeyIsTheLastKeyInDB()
			throws KineticException {
		byte[] key0 = toByteArray("key005");
		byte[] value0 = toByteArray("value005");
		Entry entry0 = new Entry(key0, value0);

		byte[] key1 = toByteArray("key006");
		byte[] value1 = toByteArray("value006");
		Entry entry1 = new Entry(key1, value1);

		byte[] key2 = toByteArray("key007");
		byte[] value2 = toByteArray("value007");
		Entry entry2 = new Entry(key2, value2);

		getClient().put(entry0, null);
		getClient().put(entry1, null);
		getClient().put(entry2, null);

		byte[] startKey = key2;
		byte[] endKey = toByteArray("key09");

		List<byte[]> keys = getClient().getKeyRangeReversed(startKey, false,
				endKey, false, 10);
		assertEquals(0, keys.size());

		logger.info(this.testEndInfo());
	}

	/**
	 * GetKeyRangeReversed, startKey is inclusive and endKey is exclusive,
	 * startKey is the last key in simulator/drive, the result of key list
	 * should include the last key.
	 * <p>
	 * 
	 * @throws KineticException
	 *             if any internal error occurred.
	 */
	@Test
	public void testGetKeyRangeReversed_ForStartKeyInclusiveEndKeyExclusive_WithStartKeyIsTheLastKeyInDB()
			throws KineticException {
		byte[] key0 = toByteArray("key005");
		byte[] value0 = toByteArray("value005");
		Entry entry0 = new Entry(key0, value0);

		byte[] key1 = toByteArray("key006");
		byte[] value1 = toByteArray("value006");
		Entry entry1 = new Entry(key1, value1);

		byte[] key2 = toByteArray("key007");
		byte[] value2 = toByteArray("value007");
		Entry entry2 = new Entry(key2, value2);

		getClient().put(entry0, null);
		getClient().put(entry1, null);
		getClient().put(entry2, null);

		byte[] startKey = key2;
		byte[] endKey = toByteArray("key09");

		List<byte[]> keys = getClient().getKeyRangeReversed(startKey, true,
				endKey, false, 10);
		assertEquals(1, keys.size());
		assertArrayEquals(key2, keys.get(0));

		logger.info(this.testEndInfo());
	}

	/**
	 * GetKeyRangeReversed, startKey is exclusive and endKey is inclusive,
	 * startKey is the last key in simulator/drive, the result of key list
	 * should be empty.
	 * <p>
	 * 
	 * @throws KineticException
	 *             if any internal error occurred.
	 */
	@Test
	public void testGetKeyRangeReversed_ForStartKeyExclusiveEndKeyInclusive_WithStartKeyIsTheLastKeyInDB()
			throws KineticException {
		byte[] key0 = toByteArray("key005");
		byte[] value0 = toByteArray("value005");
		Entry entry0 = new Entry(key0, value0);

		byte[] key1 = toByteArray("key006");
		byte[] value1 = toByteArray("value006");
		Entry entry1 = new Entry(key1, value1);

		byte[] key2 = toByteArray("key007");
		byte[] value2 = toByteArray("value007");
		Entry entry2 = new Entry(key2, value2);

		getClient().put(entry0, null);
		getClient().put(entry1, null);
		getClient().put(entry2, null);

		byte[] startKey = key2;
		byte[] endKey = toByteArray("key09");

		List<byte[]> keys = getClient().getKeyRangeReversed(startKey, false,
				endKey, true, 10);
		assertEquals(0, keys.size());

		logger.info(this.testEndInfo());
	}

	/**
	 * GetKeyRangeReversed, startKey and endKey are inclusive, startKey is the
	 * last key in simulator/drive, the result of key list should include the
	 * last key.
	 * <p>
	 * 
	 * @throws KineticException
	 *             if any internal error occurred.
	 */
	@Test
	public void testGetKeyRangeReversed_ForStartKeyInclusiveEndKeyInclusive_WithStartKeyIsTheLastKeyInDB()
			throws KineticException {
		byte[] key0 = toByteArray("key005");
		byte[] value0 = toByteArray("value005");
		Entry entry0 = new Entry(key0, value0);

		byte[] key1 = toByteArray("key006");
		byte[] value1 = toByteArray("value006");
		Entry entry1 = new Entry(key1, value1);

		byte[] key2 = toByteArray("key007");
		byte[] value2 = toByteArray("value007");
		Entry entry2 = new Entry(key2, value2);

		getClient().put(entry0, null);
		getClient().put(entry1, null);
		getClient().put(entry2, null);

		byte[] startKey = key2;
		byte[] endKey = toByteArray("key09");

		List<byte[]> keys = getClient().getKeyRangeReversed(startKey, true,
				endKey, true, 10);
		assertEquals(1, keys.size());
		assertArrayEquals(key2, keys.get(0));

		logger.info(this.testEndInfo());
	}

	/**
	 * GetKeyRangeReversed, startKey and endKey are exclusive, startKey is the
	 * last key in simulator/drive, the result of key list should be empty.
	 * <p>
	 * 
	 * @throws KineticException
	 *             if any internal error occurred.
	 */
	@Test
	public void testGetKeyRangeReversed_ForStartKeyExclusiveEndKeyExclusive_WithStartKeyBiggerThanTheLastKeyInDB()
			throws KineticException {
		byte[] key0 = toByteArray("key005");
		byte[] value0 = toByteArray("value005");
		Entry entry0 = new Entry(key0, value0);

		byte[] key1 = toByteArray("key006");
		byte[] value1 = toByteArray("value006");
		Entry entry1 = new Entry(key1, value1);

		byte[] key2 = toByteArray("key007");
		byte[] value2 = toByteArray("value007");
		Entry entry2 = new Entry(key2, value2);

		getClient().put(entry0, null);
		getClient().put(entry1, null);
		getClient().put(entry2, null);

		byte[] startKey = toByteArray("key09");
		byte[] endKey = toByteArray("key11");

		List<byte[]> keys = getClient().getKeyRangeReversed(startKey, false,
				endKey, false, 10);
		assertEquals(0, keys.size());

		logger.info(this.testEndInfo());
	}

	/**
	 * GetKeyRangeReversed, startKey is inclusive and endKey is exclusive,
	 * startKey is bigger than the last key in simulator/drive, the result of
	 * key list should be empty.
	 * <p>
	 * 
	 * @throws KineticException
	 *             if any internal error occurred.
	 */
	@Test
	public void testGetKeyRangeReversed_ForStartKeyInclusiveEndKeyExclusive_WithStartKeyBiggerThanTheLastKeyInDB()
			throws KineticException {
		byte[] key0 = toByteArray("key005");
		byte[] value0 = toByteArray("value005");
		Entry entry0 = new Entry(key0, value0);

		byte[] key1 = toByteArray("key006");
		byte[] value1 = toByteArray("value006");
		Entry entry1 = new Entry(key1, value1);

		byte[] key2 = toByteArray("key007");
		byte[] value2 = toByteArray("value007");
		Entry entry2 = new Entry(key2, value2);

		getClient().put(entry0, null);
		getClient().put(entry1, null);
		getClient().put(entry2, null);

		byte[] startKey = toByteArray("key09");
		byte[] endKey = toByteArray("key11");

		List<byte[]> keys = getClient().getKeyRangeReversed(startKey, true,
				endKey, false, 10);
		assertEquals(0, keys.size());

		logger.info(this.testEndInfo());
	}

	/**
	 * GetKeyRangeReversed, startKey is exclusive and endKey is inclusive,
	 * startKey is bigger than the last key in simulator/drive, the result of
	 * key list should be empty.
	 * <p>
	 * 
	 * @throws KineticException
	 *             if any internal error occurred.
	 */
	@Test
	public void testGetKeyRangeReversed_ForStartKeyExclusiveEndKeyInclusive_WithStartKeyBiggerThanTheLastKeyInDB()
			throws KineticException {
		byte[] key0 = toByteArray("key005");
		byte[] value0 = toByteArray("value005");
		Entry entry0 = new Entry(key0, value0);

		byte[] key1 = toByteArray("key006");
		byte[] value1 = toByteArray("value006");
		Entry entry1 = new Entry(key1, value1);

		byte[] key2 = toByteArray("key007");
		byte[] value2 = toByteArray("value007");
		Entry entry2 = new Entry(key2, value2);

		getClient().put(entry0, null);
		getClient().put(entry1, null);
		getClient().put(entry2, null);

		byte[] startKey = toByteArray("key09");
		byte[] endKey = toByteArray("key11");

		List<byte[]> keys = getClient().getKeyRangeReversed(startKey, false,
				endKey, true, 10);
		assertEquals(0, keys.size());

		logger.info(this.testEndInfo());
	}

	/**
	 * GetKeyRangeReversed, startKey and endKey are inclusive, startKey is
	 * bigger than the last key in simulator/drive, the result of key list
	 * should be empty.
	 * <p>
	 * 
	 * @throws KineticException
	 *             if any internal error occurred.
	 */
	@Test
	public void testGetKeyRangeReversed_ForStartKeyInclusiveEndKeyInclusive_WithStartKeyBiggerThanTheLastKeyInDB()
			throws KineticException {
		byte[] key0 = toByteArray("key005");
		byte[] value0 = toByteArray("value005");
		Entry entry0 = new Entry(key0, value0);

		byte[] key1 = toByteArray("key006");
		byte[] value1 = toByteArray("value006");
		Entry entry1 = new Entry(key1, value1);

		byte[] key2 = toByteArray("key007");
		byte[] value2 = toByteArray("value007");
		Entry entry2 = new Entry(key2, value2);

		getClient().put(entry0, null);
		getClient().put(entry1, null);
		getClient().put(entry2, null);

		byte[] startKey = toByteArray("key09");
		byte[] endKey = toByteArray("key11");

		List<byte[]> keys = getClient().getKeyRangeReversed(startKey, true,
				endKey, true, 10);
		assertEquals(0, keys.size());

		logger.info(this.testEndInfo());
	}

	/**
	 * GetKeyRangeReversed, no data is stored in simulator/drive, the result of
	 * key list should be empty.
	 * <p>
	 * 
	 * @throws KineticException
	 *             if any internal error occurred.
	 */
	@Test
	public void testGetKeyRangeReversed_ForNoDataInDB() throws KineticException {
		byte[] startKey = toByteArray("key09");
		byte[] endKey = toByteArray("key11");

		List<byte[]> keys = getClient().getKeyRangeReversed(startKey, true,
				endKey, true, 10);
		assertEquals(0, keys.size());
		assertTrue(keys.isEmpty());

		logger.info(this.testEndInfo());
	}
}
