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
package com.seagate.kinetic.boundary;

import static com.seagate.kinetic.KineticTestHelpers.cleanData;
import static com.seagate.kinetic.KineticTestHelpers.int32;
import static com.seagate.kinetic.KineticTestHelpers.toByteArray;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import kinetic.client.Entry;
import kinetic.client.EntryMetadata;
import kinetic.client.KineticException;
import kinetic.client.advanced.AdvancedKineticClient;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.common.collect.Lists;
import com.seagate.kinetic.IntegrationTestCase;
import com.seagate.kinetic.IntegrationTestLoggerFactory;

/**
 * Kinetic advanced Client API Boundary Test.
 * <p>
 * Boundary test against advance kinetic client API.
 * <p>
 * 
 * @see AdvancedKineticClient
 * 
 */
@Test(groups = { "simulator", "drive" })
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
    @Test(dataProvider = "transportProtocolOptions")
    public void testGetKeyRangeReversed_Throws_ForStartKeyIsNull(
            String clientName) throws KineticException {
        byte[] key0 = toByteArray("key005");
        byte[] newVersion0 = int32(0);
        byte[] value0 = toByteArray("value005");
        EntryMetadata entryMetadata = new EntryMetadata();
        Entry versioned0 = new Entry(key0, value0, entryMetadata);

        byte[] key1 = toByteArray("key006");
        byte[] newVersion1 = int32(1);
        byte[] value1 = toByteArray("value006");
        EntryMetadata entryMetadata1 = new EntryMetadata();
        Entry versioned1 = new Entry(key1, value1, entryMetadata1);

        cleanData(key0, key1, getClient(clientName));

        getClient(clientName).put(versioned0, newVersion0);
        getClient(clientName).put(versioned1, newVersion1);

        try {
            getClient(clientName).getKeyRangeReversed(null, true, key1, true,
                    10);
            Assert.fail("start key is null, get range reversed failed");
        } catch (KineticException e) {
            assertNull(e.getMessage());
        }

        cleanData(key0, key1, getClient(clientName));

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
    @Test(dataProvider = "transportProtocolOptions")
    public void testGetKeyRangeReversed_Throws_ForEndKeyIsNull(String clientName)
            throws KineticException {
        byte[] key0 = toByteArray("key005");
        byte[] newVersion0 = int32(0);
        byte[] value0 = toByteArray("value005");
        EntryMetadata entryMetadata = new EntryMetadata();
        Entry versioned0 = new Entry(key0, value0, entryMetadata);

        byte[] key1 = toByteArray("key006");
        byte[] newVersion1 = int32(1);
        byte[] value1 = toByteArray("value006");
        EntryMetadata entryMetadata1 = new EntryMetadata();
        Entry versioned1 = new Entry(key1, value1, entryMetadata1);

        cleanData(key0, key1, getClient(clientName));

        getClient(clientName).put(versioned0, newVersion0);
        getClient(clientName).put(versioned1, newVersion1);

        try {
            getClient(clientName).getKeyRangeReversed(key0, true, null, true,
                    10);
            Assert.fail("end key is null, get range reversed failed");
        } catch (KineticException e) {
            assertNull(e.getMessage());
        }

        cleanData(key0, key1, getClient(clientName));

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
    @Test(dataProvider = "transportProtocolOptions")
    public void testGetKeyRangeReversed_ForStartKeyInclusiveEndKeyInclusive_WithStartAndEndKeyNotExistInDB(
            String clientName) throws KineticException {
        byte[] key0 = toByteArray("key005");
        byte[] value0 = toByteArray("value005");
        Entry entry0 = new Entry(key0, value0);

        byte[] key1 = toByteArray("key006");
        byte[] value1 = toByteArray("value006");
        Entry entry1 = new Entry(key1, value1);

        byte[] key2 = toByteArray("key007");
        byte[] value2 = toByteArray("value007");
        Entry entry2 = new Entry(key2, value2);

        byte[] startKey = toByteArray("key00");
        byte[] endKey = toByteArray("key002");

        cleanData(startKey, endKey, getClient(clientName));
        cleanData(key0, key2, getClient(clientName));

        getClient(clientName).put(entry0, null);
        getClient(clientName).put(entry1, null);
        getClient(clientName).put(entry2, null);

        List<byte[]> keys = getClient(clientName).getKeyRangeReversed(startKey,
                true, endKey, true, 10);
        assertEquals(0, keys.size());

        cleanData(key0, key2, getClient(clientName));

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
    @Test(dataProvider = "transportProtocolOptions")
    public void testGetKeyRangeReversed_ForStartKeyExclusiveEndKeyExclusive_WithStartAndEndKeyNotExistInDB(
            String clientName) throws KineticException {
        byte[] key0 = toByteArray("key005");
        byte[] value0 = toByteArray("value005");
        Entry entry0 = new Entry(key0, value0);

        byte[] key1 = toByteArray("key006");
        byte[] value1 = toByteArray("value006");
        Entry entry1 = new Entry(key1, value1);

        byte[] key2 = toByteArray("key007");
        byte[] value2 = toByteArray("value007");
        Entry entry2 = new Entry(key2, value2);

        byte[] startKey = toByteArray("key00");
        byte[] endKey = toByteArray("key002");

        cleanData(startKey, endKey, getClient(clientName));
        cleanData(key0, key2, getClient(clientName));

        getClient(clientName).put(entry0, null);
        getClient(clientName).put(entry1, null);
        getClient(clientName).put(entry2, null);

        List<byte[]> keys = getClient(clientName).getKeyRangeReversed(startKey,
                false, endKey, false, 10);
        assertEquals(0, keys.size());

        cleanData(key0, key2, getClient(clientName));

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
    @Test(dataProvider = "transportProtocolOptions")
    public void testGetKeyRangeReversed_ForStartKeyInclusiveEndKeyExclusive_WithStartAndEndKeyNotExistInDB(
            String clientName) throws KineticException {
        byte[] key0 = toByteArray("key005");
        byte[] value0 = toByteArray("value005");
        Entry entry0 = new Entry(key0, value0);

        byte[] key1 = toByteArray("key006");
        byte[] value1 = toByteArray("value006");
        Entry entry1 = new Entry(key1, value1);

        byte[] key2 = toByteArray("key007");
        byte[] value2 = toByteArray("value007");
        Entry entry2 = new Entry(key2, value2);

        byte[] startKey = toByteArray("key00");
        byte[] endKey = toByteArray("key002");

        cleanData(startKey, endKey, getClient(clientName));
        cleanData(key0, key2, getClient(clientName));

        getClient(clientName).put(entry0, null);
        getClient(clientName).put(entry1, null);
        getClient(clientName).put(entry2, null);

        List<byte[]> keys = getClient(clientName).getKeyRangeReversed(startKey,
                true, endKey, false, 10);
        assertEquals(0, keys.size());

        cleanData(key0, key2, getClient(clientName));

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
    @Test(dataProvider = "transportProtocolOptions")
    public void testGetKeyRangeReversed_ForStartKeyExclusiveEndKeyInclusive_WithStartAndEndKeyNotExistInDB(
            String clientName) throws KineticException {
        byte[] key0 = toByteArray("key005");
        byte[] value0 = toByteArray("value005");
        Entry entry0 = new Entry(key0, value0);

        byte[] key1 = toByteArray("key006");
        byte[] value1 = toByteArray("value006");
        Entry entry1 = new Entry(key1, value1);

        byte[] key2 = toByteArray("key007");
        byte[] value2 = toByteArray("value007");
        Entry entry2 = new Entry(key2, value2);

        byte[] startKey = toByteArray("key00");
        byte[] endKey = toByteArray("key002");

        cleanData(startKey, endKey, getClient(clientName));
        cleanData(key0, key2, getClient(clientName));

        getClient(clientName).put(entry0, null);
        getClient(clientName).put(entry1, null);
        getClient(clientName).put(entry2, null);

        List<byte[]> keys = getClient(clientName).getKeyRangeReversed(startKey,
                false, endKey, true, 10);
        assertEquals(0, keys.size());

        cleanData(key0, key2, getClient(clientName));

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
    @Test(dataProvider = "transportProtocolOptions")
    public void testGetKeyRangeReversed_ForStartKeyExclusiveEndKeyExclusive_WithEndKeyExistsInDB(
            String clientName) throws KineticException {
        byte[] key0 = toByteArray("key005");
        byte[] value0 = toByteArray("value005");
        Entry entry0 = new Entry(key0, value0);

        byte[] key1 = toByteArray("key006");
        byte[] value1 = toByteArray("value006");
        Entry entry1 = new Entry(key1, value1);

        byte[] key2 = toByteArray("key007");
        byte[] value2 = toByteArray("value007");
        Entry entry2 = new Entry(key2, value2);

        byte[] startKey = toByteArray("key00");
        byte[] endKey = key0;

        cleanData(startKey, key2, getClient(clientName));

        getClient(clientName).put(entry0, null);
        getClient(clientName).put(entry1, null);
        getClient(clientName).put(entry2, null);

        List<byte[]> keys = getClient(clientName).getKeyRangeReversed(startKey,
                false, endKey, false, 10);
        assertEquals(0, keys.size());

        cleanData(startKey, key2, getClient(clientName));

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
    @Test(dataProvider = "transportProtocolOptions")
    public void testGetKeyRangeReversed_ForStartKeyInclusiveEndKeyExclusive_WithEndKeyExistsInDB(
            String clientName) throws KineticException {
        byte[] key0 = toByteArray("key005");
        byte[] value0 = toByteArray("value005");
        Entry entry0 = new Entry(key0, value0);

        byte[] key1 = toByteArray("key006");
        byte[] value1 = toByteArray("value006");
        Entry entry1 = new Entry(key1, value1);

        byte[] key2 = toByteArray("key007");
        byte[] value2 = toByteArray("value007");
        Entry entry2 = new Entry(key2, value2);

        byte[] startKey = toByteArray("key00");
        byte[] endKey = key0;

        cleanData(startKey, key2, getClient(clientName));

        getClient(clientName).put(entry0, null);
        getClient(clientName).put(entry1, null);
        getClient(clientName).put(entry2, null);

        List<byte[]> keys = getClient(clientName).getKeyRangeReversed(startKey,
                true, endKey, false, 10);
        assertEquals(0, keys.size());

        cleanData(startKey, key2, getClient(clientName));

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
    @Test(dataProvider = "transportProtocolOptions")
    public void testGetKeyRangeReversed_ForStartKeyExclusiveEndKeyInclusive_WithEndKeyExistsInDB(
            String clientName) throws KineticException {
        byte[] key0 = toByteArray("key005");
        byte[] value0 = toByteArray("value005");
        Entry entry0 = new Entry(key0, value0);

        byte[] key1 = toByteArray("key006");
        byte[] value1 = toByteArray("value006");
        Entry entry1 = new Entry(key1, value1);

        byte[] key2 = toByteArray("key007");
        byte[] value2 = toByteArray("value007");
        Entry entry2 = new Entry(key2, value2);

        byte[] startKey = toByteArray("key00");
        byte[] endKey = key0;

        cleanData(startKey, key2, getClient(clientName));

        getClient(clientName).put(entry0, null);
        getClient(clientName).put(entry1, null);
        getClient(clientName).put(entry2, null);

        List<byte[]> keys = getClient(clientName).getKeyRangeReversed(startKey,
                false, endKey, true, 10);
        assertEquals(1, keys.size());
        assertArrayEquals(key0, keys.get(0));

        cleanData(startKey, key2, getClient(clientName));

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
    @Test(dataProvider = "transportProtocolOptions")
    public void testGetKeyRangeReversed_ForStartKeyInclusiveEndKeyInclusive_WithEndKeyExistsInDB(
            String clientName) throws KineticException {
        byte[] key0 = toByteArray("key005");
        byte[] value0 = toByteArray("value005");
        Entry entry0 = new Entry(key0, value0);

        byte[] key1 = toByteArray("key006");
        byte[] value1 = toByteArray("value006");
        Entry entry1 = new Entry(key1, value1);

        byte[] key2 = toByteArray("key007");
        byte[] value2 = toByteArray("value007");
        Entry entry2 = new Entry(key2, value2);

        byte[] startKey = toByteArray("key00");
        byte[] endKey = key0;

        cleanData(startKey, key2, getClient(clientName));

        getClient(clientName).put(entry0, null);
        getClient(clientName).put(entry1, null);
        getClient(clientName).put(entry2, null);

        List<byte[]> keys = getClient(clientName).getKeyRangeReversed(startKey,
                true, endKey, true, 10);
        assertEquals(1, keys.size());
        assertArrayEquals(key0, keys.get(0));

        cleanData(startKey, key2, getClient(clientName));

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
    @Test(dataProvider = "transportProtocolOptions")
    public void testGetKeyRangeReversed_ForStartKeyExclusiveEndKeyExclusive_WithEndKeyIsTheSecondKeyExistsInDB(
            String clientName) throws KineticException {
        byte[] key0 = toByteArray("key005");
        byte[] value0 = toByteArray("value005");
        Entry entry0 = new Entry(key0, value0);

        byte[] key1 = toByteArray("key006");
        byte[] value1 = toByteArray("value006");
        Entry entry1 = new Entry(key1, value1);

        byte[] key2 = toByteArray("key007");
        byte[] value2 = toByteArray("value007");
        Entry entry2 = new Entry(key2, value2);

        byte[] startKey = toByteArray("key00");
        byte[] endKey = key1;

        cleanData(startKey, key2, getClient(clientName));

        getClient(clientName).put(entry0, null);
        getClient(clientName).put(entry1, null);
        getClient(clientName).put(entry2, null);

        List<byte[]> keys = getClient(clientName).getKeyRangeReversed(startKey,
                false, endKey, false, 10);
        assertEquals(1, keys.size());
        assertArrayEquals(key0, keys.get(0));

        cleanData(startKey, key2, getClient(clientName));

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
    @Test(dataProvider = "transportProtocolOptions")
    public void testGetKeyRangeReversed_ForStartKeyInclusiveEndKeyExclusive_WithEndKeyIsTheSecondKeyExistsInDB(
            String clientName) throws KineticException {
        byte[] key0 = toByteArray("key005");
        byte[] value0 = toByteArray("value005");
        Entry entry0 = new Entry(key0, value0);

        byte[] key1 = toByteArray("key006");
        byte[] value1 = toByteArray("value006");
        Entry entry1 = new Entry(key1, value1);

        byte[] key2 = toByteArray("key007");
        byte[] value2 = toByteArray("value007");
        Entry entry2 = new Entry(key2, value2);

        byte[] startKey = toByteArray("key00");
        byte[] endKey = key1;

        cleanData(startKey, key2, getClient(clientName));

        getClient(clientName).put(entry0, null);
        getClient(clientName).put(entry1, null);
        getClient(clientName).put(entry2, null);

        List<byte[]> keys = getClient(clientName).getKeyRangeReversed(startKey,
                true, endKey, false, 10);
        assertEquals(1, keys.size());
        assertArrayEquals(key0, keys.get(0));

        cleanData(startKey, key2, getClient(clientName));

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
    @Test(dataProvider = "transportProtocolOptions")
    public void testGetKeyRangeReversed_ForStartKeyExclusiveEndKeyInclusive_WithEndKeyIsTheSecondKeyExistsInDB(
            String clientName) throws KineticException {
        byte[] key0 = toByteArray("key005");
        byte[] value0 = toByteArray("value005");
        Entry entry0 = new Entry(key0, value0);

        byte[] key1 = toByteArray("key006");
        byte[] value1 = toByteArray("value006");
        Entry entry1 = new Entry(key1, value1);

        byte[] key2 = toByteArray("key007");
        byte[] value2 = toByteArray("value007");
        Entry entry2 = new Entry(key2, value2);

        byte[] startKey = toByteArray("key00");
        byte[] endKey = key1;

        cleanData(startKey, key2, getClient(clientName));

        getClient(clientName).put(entry0, null);
        getClient(clientName).put(entry1, null);
        getClient(clientName).put(entry2, null);

        List<byte[]> keys = getClient(clientName).getKeyRangeReversed(startKey,
                false, endKey, true, 10);
        assertEquals(2, keys.size());
        assertArrayEquals(key1, keys.get(0));
        assertArrayEquals(key0, keys.get(1));

        cleanData(startKey, key2, getClient(clientName));

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
    @Test(dataProvider = "transportProtocolOptions")
    public void testGetKeyRangeReversed_ForStartKeyInclusiveEndKeyInclusive_WithEndKeyIsTheSecondKeyExistsInDB(
            String clientName) throws KineticException {
        byte[] key0 = toByteArray("key005");
        byte[] value0 = toByteArray("value005");
        Entry entry0 = new Entry(key0, value0);

        byte[] key1 = toByteArray("key006");
        byte[] value1 = toByteArray("value006");
        Entry entry1 = new Entry(key1, value1);

        byte[] key2 = toByteArray("key007");
        byte[] value2 = toByteArray("value007");
        Entry entry2 = new Entry(key2, value2);

        byte[] startKey = toByteArray("key00");
        byte[] endKey = key1;

        cleanData(startKey, key2, getClient(clientName));

        getClient(clientName).put(entry0, null);
        getClient(clientName).put(entry1, null);
        getClient(clientName).put(entry2, null);

        List<byte[]> keys = getClient(clientName).getKeyRangeReversed(startKey,
                true, endKey, true, 10);
        assertEquals(2, keys.size());
        assertArrayEquals(key1, keys.get(0));
        assertArrayEquals(key0, keys.get(1));

        cleanData(startKey, key2, getClient(clientName));

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
    @Test(dataProvider = "transportProtocolOptions")
    public void testGetKeyRangeReversed_ForStartKeyExclusiveEndKeyExclusive_WithEndKeyIsTheLastKeyExistsInDB(
            String clientName) throws KineticException {
        byte[] key0 = toByteArray("key005");
        byte[] value0 = toByteArray("value005");
        Entry entry0 = new Entry(key0, value0);

        byte[] key1 = toByteArray("key006");
        byte[] value1 = toByteArray("value006");
        Entry entry1 = new Entry(key1, value1);

        byte[] key2 = toByteArray("key007");
        byte[] value2 = toByteArray("value007");
        Entry entry2 = new Entry(key2, value2);

        byte[] startKey = toByteArray("key00");
        byte[] endKey = key2;

        cleanData(startKey, key2, getClient(clientName));

        getClient(clientName).put(entry0, null);
        getClient(clientName).put(entry1, null);
        getClient(clientName).put(entry2, null);

        List<byte[]> keys = getClient(clientName).getKeyRangeReversed(startKey,
                false, endKey, false, 10);
        assertEquals(2, keys.size());
        assertArrayEquals(key1, keys.get(0));
        assertArrayEquals(key0, keys.get(1));

        cleanData(startKey, key2, getClient(clientName));

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
    @Test(dataProvider = "transportProtocolOptions")
    public void testGetKeyRangeReversed_ForStartKeyInclusiveEndKeyExclusive_WithEndKeyIsTheLastKeyExistsInDB(
            String clientName) throws KineticException {
        byte[] key0 = toByteArray("key005");
        byte[] value0 = toByteArray("value005");
        Entry entry0 = new Entry(key0, value0);

        byte[] key1 = toByteArray("key006");
        byte[] value1 = toByteArray("value006");
        Entry entry1 = new Entry(key1, value1);

        byte[] key2 = toByteArray("key007");
        byte[] value2 = toByteArray("value007");
        Entry entry2 = new Entry(key2, value2);

        byte[] startKey = toByteArray("key00");
        byte[] endKey = key2;

        cleanData(startKey, key2, getClient(clientName));

        getClient(clientName).put(entry0, null);
        getClient(clientName).put(entry1, null);
        getClient(clientName).put(entry2, null);

        List<byte[]> keys = getClient(clientName).getKeyRangeReversed(startKey,
                true, endKey, false, 10);
        assertEquals(2, keys.size());
        assertArrayEquals(key1, keys.get(0));
        assertArrayEquals(key0, keys.get(1));

        cleanData(startKey, key2, getClient(clientName));

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
    @Test(dataProvider = "transportProtocolOptions")
    public void testGetKeyRangeReversed_ForStartKeyExclusiveEndKeyInclusive_WithEndKeyIsTheLastKeyExistsInDB(
            String clientName) throws KineticException {
        byte[] key0 = toByteArray("key005");
        byte[] value0 = toByteArray("value005");
        Entry entry0 = new Entry(key0, value0);

        byte[] key1 = toByteArray("key006");
        byte[] value1 = toByteArray("value006");
        Entry entry1 = new Entry(key1, value1);

        byte[] key2 = toByteArray("key007");
        byte[] value2 = toByteArray("value007");
        Entry entry2 = new Entry(key2, value2);

        byte[] startKey = toByteArray("key00");
        byte[] endKey = key2;

        cleanData(startKey, key2, getClient(clientName));

        getClient(clientName).put(entry0, null);
        getClient(clientName).put(entry1, null);
        getClient(clientName).put(entry2, null);

        List<byte[]> keys = getClient(clientName).getKeyRangeReversed(startKey,
                false, endKey, true, 10);
        assertEquals(3, keys.size());
        assertArrayEquals(key2, keys.get(0));
        assertArrayEquals(key1, keys.get(1));
        assertArrayEquals(key0, keys.get(2));

        cleanData(startKey, key2, getClient(clientName));

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
    @Test(dataProvider = "transportProtocolOptions")
    public void testGetKeyRangeReversed_ForStartKeyInclusiveEndKeyInclusive_WithEndKeyIsTheLastKeyExistsInDB(
            String clientName) throws KineticException {
        byte[] key0 = toByteArray("key005");
        byte[] value0 = toByteArray("value005");
        Entry entry0 = new Entry(key0, value0);

        byte[] key1 = toByteArray("key006");
        byte[] value1 = toByteArray("value006");
        Entry entry1 = new Entry(key1, value1);

        byte[] key2 = toByteArray("key007");
        byte[] value2 = toByteArray("value007");
        Entry entry2 = new Entry(key2, value2);

        byte[] startKey = toByteArray("key00");
        byte[] endKey = key2;

        cleanData(startKey, key2, getClient(clientName));

        getClient(clientName).put(entry0, null);
        getClient(clientName).put(entry1, null);
        getClient(clientName).put(entry2, null);

        List<byte[]> keys = getClient(clientName).getKeyRangeReversed(startKey,
                true, endKey, true, 10);
        assertEquals(3, keys.size());
        assertArrayEquals(key2, keys.get(0));
        assertArrayEquals(key1, keys.get(1));
        assertArrayEquals(key0, keys.get(2));

        cleanData(startKey, key2, getClient(clientName));

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
    @Test(dataProvider = "transportProtocolOptions")
    public void testGetKeyRangeReversed_ForStartKeyExclusiveEndKeyExclusive_WithStartKeyIsTheFirstKeyAndEndKeyIsTheSecondKeyInDB(
            String clientName) throws KineticException {
        byte[] key0 = toByteArray("key005");
        byte[] value0 = toByteArray("value005");
        Entry entry0 = new Entry(key0, value0);

        byte[] key1 = toByteArray("key006");
        byte[] value1 = toByteArray("value006");
        Entry entry1 = new Entry(key1, value1);

        byte[] key2 = toByteArray("key007");
        byte[] value2 = toByteArray("value007");
        Entry entry2 = new Entry(key2, value2);

        cleanData(key0, key2, getClient(clientName));

        getClient(clientName).put(entry0, null);
        getClient(clientName).put(entry1, null);
        getClient(clientName).put(entry2, null);

        byte[] startKey = key0;
        byte[] endKey = key1;

        List<byte[]> keys = getClient(clientName).getKeyRangeReversed(startKey,
                false, endKey, false, 10);
        assertEquals(0, keys.size());

        cleanData(key0, key2, getClient(clientName));

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
    @Test(dataProvider = "transportProtocolOptions")
    public void gtestGetKeyRangeReversed_ForStartKeyInclusiveEndKeyExclusive_WithStartKeyIsTheFirstKeyAndEndKeyIsTheSecondKeyInDB(
            String clientName) throws KineticException {
        byte[] key0 = toByteArray("key005");
        byte[] value0 = toByteArray("value005");
        Entry entry0 = new Entry(key0, value0);

        byte[] key1 = toByteArray("key006");
        byte[] value1 = toByteArray("value006");
        Entry entry1 = new Entry(key1, value1);

        byte[] key2 = toByteArray("key007");
        byte[] value2 = toByteArray("value007");
        Entry entry2 = new Entry(key2, value2);

        cleanData(key0, key2, getClient(clientName));

        getClient(clientName).put(entry0, null);
        getClient(clientName).put(entry1, null);
        getClient(clientName).put(entry2, null);

        byte[] startKey = key0;
        byte[] endKey = key1;

        List<byte[]> keys = getClient(clientName).getKeyRangeReversed(startKey,
                true, endKey, false, 10);
        assertEquals(1, keys.size());
        assertArrayEquals(key0, keys.get(0));

        cleanData(key0, key2, getClient(clientName));

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
    @Test(dataProvider = "transportProtocolOptions")
    public void testgetKeyRangeReversed_ForStartKeyExclusiveEndKeyInclusive_WithStartKeyIsTheFirstKeyAndEndKeyIsTheSecondKeyInDB(
            String clientName) throws KineticException {
        byte[] key0 = toByteArray("key005");
        byte[] value0 = toByteArray("value005");
        Entry entry0 = new Entry(key0, value0);

        byte[] key1 = toByteArray("key006");
        byte[] value1 = toByteArray("value006");
        Entry entry1 = new Entry(key1, value1);

        byte[] key2 = toByteArray("key007");
        byte[] value2 = toByteArray("value007");
        Entry entry2 = new Entry(key2, value2);

        cleanData(key0, key2, getClient(clientName));

        getClient(clientName).put(entry0, null);
        getClient(clientName).put(entry1, null);
        getClient(clientName).put(entry2, null);

        byte[] startKey = key0;
        byte[] endKey = key1;

        List<byte[]> keys = getClient(clientName).getKeyRangeReversed(startKey,
                false, endKey, true, 10);
        assertEquals(1, keys.size());
        assertArrayEquals(key1, keys.get(0));

        cleanData(key0, key2, getClient(clientName));

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
    @Test(dataProvider = "transportProtocolOptions")
    public void testGetKeyRangeReversed_ForStartKeyInclusiveEndKeyInclusive_WithStartKeyIsTheFirstKeyAndEndKeyIsTheSecondKeyInDB(
            String clientName) throws KineticException {
        byte[] key0 = toByteArray("key005");
        byte[] value0 = toByteArray("value005");
        Entry entry0 = new Entry(key0, value0);

        byte[] key1 = toByteArray("key006");
        byte[] value1 = toByteArray("value006");
        Entry entry1 = new Entry(key1, value1);

        byte[] key2 = toByteArray("key007");
        byte[] value2 = toByteArray("value007");
        Entry entry2 = new Entry(key2, value2);

        cleanData(key0, key2, getClient(clientName));

        getClient(clientName).put(entry0, null);
        getClient(clientName).put(entry1, null);
        getClient(clientName).put(entry2, null);

        byte[] startKey = key0;
        byte[] endKey = key1;

        List<byte[]> keys = getClient(clientName).getKeyRangeReversed(startKey,
                true, endKey, true, 10);
        assertEquals(2, keys.size());
        assertArrayEquals(key1, keys.get(0));
        assertArrayEquals(key0, keys.get(1));

        cleanData(key0, key2, getClient(clientName));

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
    @Test(dataProvider = "transportProtocolOptions")
    public void testGetKeyRangeReversed_ForStartKeyExclusiveEndKeyExclusive_WithStartKeyIsTheFirstKeyAndEndKeyIsTheLastKeyInDB(
            String clientName) throws KineticException {
        byte[] key0 = toByteArray("key005");
        byte[] value0 = toByteArray("value005");
        Entry entry0 = new Entry(key0, value0);

        byte[] key1 = toByteArray("key006");
        byte[] value1 = toByteArray("value006");
        Entry entry1 = new Entry(key1, value1);

        byte[] key2 = toByteArray("key007");
        byte[] value2 = toByteArray("value007");
        Entry entry2 = new Entry(key2, value2);

        cleanData(key0, key2, getClient(clientName));

        getClient(clientName).put(entry0, null);
        getClient(clientName).put(entry1, null);
        getClient(clientName).put(entry2, null);

        byte[] startKey = key0;
        byte[] endKey = key2;

        List<byte[]> keys = getClient(clientName).getKeyRangeReversed(startKey,
                false, endKey, false, 10);
        assertEquals(1, keys.size());
        assertArrayEquals(key1, keys.get(0));

        cleanData(key0, key2, getClient(clientName));

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
    @Test(dataProvider = "transportProtocolOptions")
    public void testGetKeyRangeReversed_ForStartKeyInclusiveEndKeyExclusive_WithStartKeyIsTheFirstKeyAndEndKeyIsTheLastKeyInDB(
            String clientName) throws KineticException {
        byte[] key0 = toByteArray("key005");
        byte[] value0 = toByteArray("value005");
        Entry entry0 = new Entry(key0, value0);

        byte[] key1 = toByteArray("key006");
        byte[] value1 = toByteArray("value006");
        Entry entry1 = new Entry(key1, value1);

        byte[] key2 = toByteArray("key007");
        byte[] value2 = toByteArray("value007");
        Entry entry2 = new Entry(key2, value2);

        cleanData(key0, key2, getClient(clientName));

        getClient(clientName).put(entry0, null);
        getClient(clientName).put(entry1, null);
        getClient(clientName).put(entry2, null);

        byte[] startKey = key0;
        byte[] endKey = key2;

        List<byte[]> keys = getClient(clientName).getKeyRangeReversed(startKey,
                true, endKey, false, 10);
        assertEquals(2, keys.size());
        assertArrayEquals(key1, keys.get(0));
        assertArrayEquals(key0, keys.get(1));

        cleanData(key0, key2, getClient(clientName));

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
    @Test(dataProvider = "transportProtocolOptions")
    public void testGetKeyRangeReversed_ForStartKeyExclusiveEndKeyInclusive_WithStartKeyIsTheFirstKeyAndEndKeyIsTheLastKeyInDB(
            String clientName) throws KineticException {
        byte[] key0 = toByteArray("key005");
        byte[] value0 = toByteArray("value005");
        Entry entry0 = new Entry(key0, value0);

        byte[] key1 = toByteArray("key006");
        byte[] value1 = toByteArray("value006");
        Entry entry1 = new Entry(key1, value1);

        byte[] key2 = toByteArray("key007");
        byte[] value2 = toByteArray("value007");
        Entry entry2 = new Entry(key2, value2);

        cleanData(key0, key2, getClient(clientName));

        getClient(clientName).put(entry0, null);
        getClient(clientName).put(entry1, null);
        getClient(clientName).put(entry2, null);

        byte[] startKey = key0;
        byte[] endKey = key2;

        List<byte[]> keys = getClient(clientName).getKeyRangeReversed(startKey,
                false, endKey, true, 10);
        assertEquals(2, keys.size());
        assertArrayEquals(key2, keys.get(0));
        assertArrayEquals(key1, keys.get(1));

        cleanData(key0, key2, getClient(clientName));

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
    @Test(dataProvider = "transportProtocolOptions")
    public void testGetKeyRangeReversed_ForStartKeyInclusiveEndKeyInclusive_WithStartKeyIsTheFirstKeyAndEndKeyIsTheLastKeyInDB(
            String clientName) throws KineticException {
        byte[] key0 = toByteArray("key005");
        byte[] value0 = toByteArray("value005");
        Entry entry0 = new Entry(key0, value0);

        byte[] key1 = toByteArray("key006");
        byte[] value1 = toByteArray("value006");
        Entry entry1 = new Entry(key1, value1);

        byte[] key2 = toByteArray("key007");
        byte[] value2 = toByteArray("value007");
        Entry entry2 = new Entry(key2, value2);

        cleanData(key0, key2, getClient(clientName));

        getClient(clientName).put(entry0, null);
        getClient(clientName).put(entry1, null);
        getClient(clientName).put(entry2, null);

        byte[] startKey = key0;
        byte[] endKey = key2;

        List<byte[]> keys = getClient(clientName).getKeyRangeReversed(startKey,
                true, endKey, true, 10);
        assertEquals(3, keys.size());
        assertArrayEquals(key2, keys.get(0));
        assertArrayEquals(key1, keys.get(1));
        assertArrayEquals(key0, keys.get(2));

        cleanData(key0, key2, getClient(clientName));

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
    @Test(dataProvider = "transportProtocolOptions")
    public void testGetKeyRangeReversed_ForStartKeyExclusiveEndKeyExclusive_WithStartKeyIsTheSecondKeyAndEndKeyIsTheLastKeyInDB(
            String clientName) throws KineticException {
        byte[] key0 = toByteArray("key005");
        byte[] value0 = toByteArray("value005");
        Entry entry0 = new Entry(key0, value0);

        byte[] key1 = toByteArray("key006");
        byte[] value1 = toByteArray("value006");
        Entry entry1 = new Entry(key1, value1);

        byte[] key2 = toByteArray("key007");
        byte[] value2 = toByteArray("value007");
        Entry entry2 = new Entry(key2, value2);

        cleanData(key0, key2, getClient(clientName));

        getClient(clientName).put(entry0, null);
        getClient(clientName).put(entry1, null);
        getClient(clientName).put(entry2, null);

        byte[] startKey = key1;
        byte[] endKey = key2;

        List<byte[]> keys = getClient(clientName).getKeyRangeReversed(startKey,
                false, endKey, false, 10);
        assertEquals(0, keys.size());

        cleanData(key0, key2, getClient(clientName));

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
    @Test(dataProvider = "transportProtocolOptions")
    public void testGetKeyRangeReversed_ForStartKeyInclusiveEndKeyExclusive_WithStartKeyIsTheSecondKeyAndEndKeyIsTheLastKeyInDB(
            String clientName) throws KineticException {
        byte[] key0 = toByteArray("key005");
        byte[] value0 = toByteArray("value005");
        Entry entry0 = new Entry(key0, value0);

        byte[] key1 = toByteArray("key006");
        byte[] value1 = toByteArray("value006");
        Entry entry1 = new Entry(key1, value1);

        byte[] key2 = toByteArray("key007");
        byte[] value2 = toByteArray("value007");
        Entry entry2 = new Entry(key2, value2);

        cleanData(key0, key2, getClient(clientName));

        getClient(clientName).put(entry0, null);
        getClient(clientName).put(entry1, null);
        getClient(clientName).put(entry2, null);

        byte[] startKey = key1;
        byte[] endKey = key2;

        List<byte[]> keys = getClient(clientName).getKeyRangeReversed(startKey,
                true, endKey, false, 10);
        assertEquals(1, keys.size());
        assertArrayEquals(key1, keys.get(0));

        cleanData(key0, key2, getClient(clientName));

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
    @Test(dataProvider = "transportProtocolOptions")
    public void testGetKeyRangeReversed_ForStartKeyExclusiveEndKeyInclusive_WithStartKeyIsTheSecondKeyAndEndKeyIsTheLastKeyInDB(
            String clientName) throws KineticException {
        byte[] key0 = toByteArray("key005");
        byte[] value0 = toByteArray("value005");
        Entry entry0 = new Entry(key0, value0);

        byte[] key1 = toByteArray("key006");
        byte[] value1 = toByteArray("value006");
        Entry entry1 = new Entry(key1, value1);

        byte[] key2 = toByteArray("key007");
        byte[] value2 = toByteArray("value007");
        Entry entry2 = new Entry(key2, value2);

        cleanData(key0, key2, getClient(clientName));

        getClient(clientName).put(entry0, null);
        getClient(clientName).put(entry1, null);
        getClient(clientName).put(entry2, null);

        byte[] startKey = key1;
        byte[] endKey = key2;

        List<byte[]> keys = getClient(clientName).getKeyRangeReversed(startKey,
                false, endKey, true, 10);
        assertEquals(1, keys.size());
        assertArrayEquals(key2, keys.get(0));

        cleanData(key0, key2, getClient(clientName));

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
    @Test(dataProvider = "transportProtocolOptions")
    public void testGetKeyRangeReversed_ForStartKeyInclusiveEndKeyInclusive_WithStartKeyIsTheSecondKeyAndEndKeyIsTheLastKeyInDB(
            String clientName) throws KineticException {
        byte[] key0 = toByteArray("key005");
        byte[] value0 = toByteArray("value005");
        Entry entry0 = new Entry(key0, value0);

        byte[] key1 = toByteArray("key006");
        byte[] value1 = toByteArray("value006");
        Entry entry1 = new Entry(key1, value1);

        byte[] key2 = toByteArray("key007");
        byte[] value2 = toByteArray("value007");
        Entry entry2 = new Entry(key2, value2);

        cleanData(key0, key2, getClient(clientName));

        getClient(clientName).put(entry0, null);
        getClient(clientName).put(entry1, null);
        getClient(clientName).put(entry2, null);

        byte[] startKey = key1;
        byte[] endKey = key2;

        List<byte[]> keys = getClient(clientName).getKeyRangeReversed(startKey,
                true, endKey, true, 10);
        assertEquals(2, keys.size());
        assertArrayEquals(key2, keys.get(0));
        assertArrayEquals(key1, keys.get(1));

        cleanData(key0, key2, getClient(clientName));

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
    @Test(dataProvider = "transportProtocolOptions")
    public void testGetKeyRangeReversed_ForStartKeyExclusiveEndKeyExclusive_WithStartKeyIsTheLastKeyInDB(
            String clientName) throws KineticException {
        byte[] key0 = toByteArray("key005");
        byte[] value0 = toByteArray("value005");
        Entry entry0 = new Entry(key0, value0);

        byte[] key1 = toByteArray("key006");
        byte[] value1 = toByteArray("value006");
        Entry entry1 = new Entry(key1, value1);

        byte[] key2 = toByteArray("key007");
        byte[] value2 = toByteArray("value007");
        Entry entry2 = new Entry(key2, value2);

        byte[] startKey = key2;
        byte[] endKey = toByteArray("key09");

        cleanData(key0, endKey, getClient(clientName));

        getClient(clientName).put(entry0, null);
        getClient(clientName).put(entry1, null);
        getClient(clientName).put(entry2, null);

        List<byte[]> keys = getClient(clientName).getKeyRangeReversed(startKey,
                false, endKey, false, 10);
        assertEquals(0, keys.size());

        cleanData(key0, endKey, getClient(clientName));

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
    @Test(dataProvider = "transportProtocolOptions")
    public void testGetKeyRangeReversed_ForStartKeyInclusiveEndKeyExclusive_WithStartKeyIsTheLastKeyInDB(
            String clientName) throws KineticException {
        byte[] key0 = toByteArray("key005");
        byte[] value0 = toByteArray("value005");
        Entry entry0 = new Entry(key0, value0);

        byte[] key1 = toByteArray("key006");
        byte[] value1 = toByteArray("value006");
        Entry entry1 = new Entry(key1, value1);

        byte[] key2 = toByteArray("key007");
        byte[] value2 = toByteArray("value007");
        Entry entry2 = new Entry(key2, value2);

        byte[] startKey = key2;
        byte[] endKey = toByteArray("key09");

        cleanData(key0, endKey, getClient(clientName));

        getClient(clientName).put(entry0, null);
        getClient(clientName).put(entry1, null);
        getClient(clientName).put(entry2, null);

        List<byte[]> keys = getClient(clientName).getKeyRangeReversed(startKey,
                true, endKey, false, 10);
        assertEquals(1, keys.size());
        assertArrayEquals(key2, keys.get(0));

        cleanData(key0, endKey, getClient(clientName));

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
    @Test(dataProvider = "transportProtocolOptions")
    public void testGetKeyRangeReversed_ForStartKeyExclusiveEndKeyInclusive_WithStartKeyIsTheLastKeyInDB(
            String clientName) throws KineticException {
        byte[] key0 = toByteArray("key005");
        byte[] value0 = toByteArray("value005");
        Entry entry0 = new Entry(key0, value0);

        byte[] key1 = toByteArray("key006");
        byte[] value1 = toByteArray("value006");
        Entry entry1 = new Entry(key1, value1);

        byte[] key2 = toByteArray("key007");
        byte[] value2 = toByteArray("value007");
        Entry entry2 = new Entry(key2, value2);

        byte[] startKey = key2;
        byte[] endKey = toByteArray("key09");

        cleanData(key0, endKey, getClient(clientName));

        getClient(clientName).put(entry0, null);
        getClient(clientName).put(entry1, null);
        getClient(clientName).put(entry2, null);

        List<byte[]> keys = getClient(clientName).getKeyRangeReversed(startKey,
                false, endKey, true, 10);
        assertEquals(0, keys.size());

        cleanData(key0, endKey, getClient(clientName));

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
    @Test(dataProvider = "transportProtocolOptions")
    public void testGetKeyRangeReversed_ForStartKeyInclusiveEndKeyInclusive_WithStartKeyIsTheLastKeyInDB(
            String clientName) throws KineticException {
        byte[] key0 = toByteArray("key005");
        byte[] value0 = toByteArray("value005");
        Entry entry0 = new Entry(key0, value0);

        byte[] key1 = toByteArray("key006");
        byte[] value1 = toByteArray("value006");
        Entry entry1 = new Entry(key1, value1);

        byte[] key2 = toByteArray("key007");
        byte[] value2 = toByteArray("value007");
        Entry entry2 = new Entry(key2, value2);

        byte[] startKey = key2;
        byte[] endKey = toByteArray("key09");

        cleanData(key0, endKey, getClient(clientName));

        getClient(clientName).put(entry0, null);
        getClient(clientName).put(entry1, null);
        getClient(clientName).put(entry2, null);

        List<byte[]> keys = getClient(clientName).getKeyRangeReversed(startKey,
                true, endKey, true, 10);
        assertEquals(1, keys.size());
        assertArrayEquals(key2, keys.get(0));

        cleanData(key0, endKey, getClient(clientName));

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
    @Test(dataProvider = "transportProtocolOptions")
    public void testGetKeyRangeReversed_ForStartKeyExclusiveEndKeyExclusive_WithStartKeyBiggerThanTheLastKeyInDB(
            String clientName) throws KineticException {
        byte[] key0 = toByteArray("key005");
        byte[] value0 = toByteArray("value005");
        Entry entry0 = new Entry(key0, value0);

        byte[] key1 = toByteArray("key006");
        byte[] value1 = toByteArray("value006");
        Entry entry1 = new Entry(key1, value1);

        byte[] key2 = toByteArray("key007");
        byte[] value2 = toByteArray("value007");
        Entry entry2 = new Entry(key2, value2);

        byte[] startKey = toByteArray("key09");
        byte[] endKey = toByteArray("key11");

        cleanData(key0, endKey, getClient(clientName));

        getClient(clientName).put(entry0, null);
        getClient(clientName).put(entry1, null);
        getClient(clientName).put(entry2, null);

        List<byte[]> keys = getClient(clientName).getKeyRangeReversed(startKey,
                false, endKey, false, 10);
        assertEquals(0, keys.size());

        cleanData(key0, endKey, getClient(clientName));

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
    @Test(dataProvider = "transportProtocolOptions")
    public void testGetKeyRangeReversed_ForStartKeyInclusiveEndKeyExclusive_WithStartKeyBiggerThanTheLastKeyInDB(
            String clientName) throws KineticException {
        byte[] key0 = toByteArray("key005");
        byte[] value0 = toByteArray("value005");
        Entry entry0 = new Entry(key0, value0);

        byte[] key1 = toByteArray("key006");
        byte[] value1 = toByteArray("value006");
        Entry entry1 = new Entry(key1, value1);

        byte[] key2 = toByteArray("key007");
        byte[] value2 = toByteArray("value007");
        Entry entry2 = new Entry(key2, value2);

        byte[] startKey = toByteArray("key09");
        byte[] endKey = toByteArray("key11");

        cleanData(key0, endKey, getClient(clientName));

        getClient(clientName).put(entry0, null);
        getClient(clientName).put(entry1, null);
        getClient(clientName).put(entry2, null);

        List<byte[]> keys = getClient(clientName).getKeyRangeReversed(startKey,
                true, endKey, false, 10);
        assertEquals(0, keys.size());

        cleanData(key0, endKey, getClient(clientName));

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
    @Test(dataProvider = "transportProtocolOptions")
    public void testGetKeyRangeReversed_ForStartKeyExclusiveEndKeyInclusive_WithStartKeyBiggerThanTheLastKeyInDB(
            String clientName) throws KineticException {
        byte[] key0 = toByteArray("key005");
        byte[] value0 = toByteArray("value005");
        Entry entry0 = new Entry(key0, value0);

        byte[] key1 = toByteArray("key006");
        byte[] value1 = toByteArray("value006");
        Entry entry1 = new Entry(key1, value1);

        byte[] key2 = toByteArray("key007");
        byte[] value2 = toByteArray("value007");
        Entry entry2 = new Entry(key2, value2);

        byte[] startKey = toByteArray("key09");
        byte[] endKey = toByteArray("key11");

        cleanData(key0, endKey, getClient(clientName));

        getClient(clientName).put(entry0, null);
        getClient(clientName).put(entry1, null);
        getClient(clientName).put(entry2, null);

        List<byte[]> keys = getClient(clientName).getKeyRangeReversed(startKey,
                false, endKey, true, 10);
        assertEquals(0, keys.size());

        cleanData(key0, endKey, getClient(clientName));

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
    @Test(dataProvider = "transportProtocolOptions")
    public void testGetKeyRangeReversed_ForStartKeyInclusiveEndKeyInclusive_WithStartKeyBiggerThanTheLastKeyInDB(
            String clientName) throws KineticException {
        byte[] key0 = toByteArray("key005");
        byte[] value0 = toByteArray("value005");
        Entry entry0 = new Entry(key0, value0);

        byte[] key1 = toByteArray("key006");
        byte[] value1 = toByteArray("value006");
        Entry entry1 = new Entry(key1, value1);

        byte[] key2 = toByteArray("key007");
        byte[] value2 = toByteArray("value007");
        Entry entry2 = new Entry(key2, value2);

        byte[] startKey = toByteArray("key09");
        byte[] endKey = toByteArray("key11");

        cleanData(key0, endKey, getClient(clientName));

        getClient(clientName).put(entry0, null);
        getClient(clientName).put(entry1, null);
        getClient(clientName).put(entry2, null);

        List<byte[]> keys = getClient(clientName).getKeyRangeReversed(startKey,
                true, endKey, true, 10);
        assertEquals(0, keys.size());

        cleanData(key0, endKey, getClient(clientName));

        logger.info(this.testEndInfo());
    }

    /**
     * Test getKeyRangeReversed API: startKey equals endKey, startKey inclusive
     * and endKey inclusive, should return endKey.
     * <p>
     *
     * @throws KineticException
     *             if any internal error occurred.
     */
    @Test(dataProvider = "transportProtocolOptions")
    public void testGetKeyRangeReversed_StartKeyEqualsEndKey_StartKeyInclusiveEndKeyInclusive(
            String clientName) throws KineticException {
        List<byte[]> keys = Arrays.asList(toByteArray("00"), toByteArray("01"),
                toByteArray("02"), toByteArray("03"), toByteArray("04"),
                toByteArray("05"), toByteArray("06"), toByteArray("07"),
                toByteArray("08"), toByteArray("09"), toByteArray("10"),
                toByteArray("11"), toByteArray("12"), toByteArray("13"),
                toByteArray("14"));

        cleanData(toByteArray("00"), toByteArray("14"), getClient(clientName));

        for (byte[] key : keys) {
            getClient(clientName).putForced(new Entry(key, key));
        }

        List<byte[]> returnedKeys = Lists.newLinkedList(getClient(clientName)
                .getKeyRangeReversed(keys.get(0), true, keys.get(0), true,
                        keys.size() - 1));

        assertEquals(1, returnedKeys.size());
        assertArrayEquals(keys.get(0), returnedKeys.get(0));

        cleanData(toByteArray("00"), toByteArray("14"), getClient(clientName));

        logger.info(this.testEndInfo());
    }

    /**
     * Test getKeyRangeReversed API: startKey equals endKey, startKey exclusive
     * and endKey inclusive, should return empty list.
     * <p>
     *
     * @throws KineticException
     *             if any internal error occurred.
     */
    @Test(dataProvider = "transportProtocolOptions")
    public void testGetKeyRangeReversed_StartKeyEqualsEndKey_StartKeyExclusiveEndKeyInclusive(
            String clientName) throws KineticException {
        List<byte[]> keys = Arrays.asList(toByteArray("00"), toByteArray("01"),
                toByteArray("02"), toByteArray("03"), toByteArray("04"),
                toByteArray("05"), toByteArray("06"), toByteArray("07"),
                toByteArray("08"), toByteArray("09"), toByteArray("10"),
                toByteArray("11"), toByteArray("12"), toByteArray("13"),
                toByteArray("14"));

        cleanData(toByteArray("00"), toByteArray("14"), getClient(clientName));

        for (byte[] key : keys) {
            getClient(clientName).putForced(new Entry(key, key));
        }

        List<byte[]> returnedKeys = Lists.newLinkedList(getClient(clientName)
                .getKeyRangeReversed(keys.get(0), false, keys.get(0), true,
                        keys.size() - 1));

        assertEquals(0, returnedKeys.size());

        cleanData(toByteArray("00"), toByteArray("14"), getClient(clientName));

        logger.info(this.testEndInfo());
    }

    /**
     * Test getKeyRangeReversed API: startKey equals endKey, startKey inclusive
     * and endKey exclusive, should return empty list.
     * <p>
     *
     * @throws KineticException
     *             if any internal error occurred.
     */
    @Test(dataProvider = "transportProtocolOptions")
    public void testGetKeyRangeReversed_StartKeyEqualsEndKey_StartKeyinclusiveEndKeyexclusive(
            String clientName) throws KineticException {
        List<byte[]> keys = Arrays.asList(toByteArray("00"), toByteArray("01"),
                toByteArray("02"), toByteArray("03"), toByteArray("04"),
                toByteArray("05"), toByteArray("06"), toByteArray("07"),
                toByteArray("08"), toByteArray("09"), toByteArray("10"),
                toByteArray("11"), toByteArray("12"), toByteArray("13"),
                toByteArray("14"));

        cleanData(toByteArray("00"), toByteArray("14"), getClient(clientName));

        for (byte[] key : keys) {
            getClient(clientName).putForced(new Entry(key, key));
        }

        List<byte[]> returnedKeys = Lists.newLinkedList(getClient(clientName)
                .getKeyRangeReversed(keys.get(0), true, keys.get(0), false,
                        keys.size() - 1));

        assertEquals(0, returnedKeys.size());

        cleanData(toByteArray("00"), toByteArray("14"), getClient(clientName));

        logger.info(this.testEndInfo());
    }

    /**
     * Test getKeyRangeReversed API: startKey equals endKey, startKey exclusive
     * and endKey exclusive, should return empty.
     * <p>
     *
     * @throws KineticException
     *             if any internal error occurred.
     */
    @Test(dataProvider = "transportProtocolOptions")
    public void testGetKeyRangeReversed_StartKeyEqualsEndKey_StartKeyexclusiveEndKeyexclusive(
            String clientName) throws KineticException {
        List<byte[]> keys = Arrays.asList(toByteArray("00"), toByteArray("01"),
                toByteArray("02"), toByteArray("03"), toByteArray("04"),
                toByteArray("05"), toByteArray("06"), toByteArray("07"),
                toByteArray("08"), toByteArray("09"), toByteArray("10"),
                toByteArray("11"), toByteArray("12"), toByteArray("13"),
                toByteArray("14"));

        cleanData(toByteArray("00"), toByteArray("14"), getClient(clientName));

        for (byte[] key : keys) {
            getClient(clientName).putForced(new Entry(key, key));
        }

        List<byte[]> returnedKeys = Lists.newLinkedList(getClient(clientName)
                .getKeyRangeReversed(keys.get(0), false, keys.get(0), false,
                        keys.size() - 1));

        assertEquals(0, returnedKeys.size());

        cleanData(toByteArray("00"), toByteArray("14"), getClient(clientName));

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
    @Test(dataProvider = "transportProtocolOptions")
    public void testGetKeyRangeReversed_ForNoDataInDB(String clientName)
            throws KineticException {
        byte[] startKey = toByteArray("key005");
        byte[] endKey = toByteArray("key006");

        cleanData(startKey, endKey, getClient(clientName));

        List<byte[]> keys = getClient(clientName).getKeyRangeReversed(startKey,
                true, endKey, true, 10);
        assertEquals(0, keys.size());
        assertTrue(keys.isEmpty());

        logger.info(this.testEndInfo());
    }
}
