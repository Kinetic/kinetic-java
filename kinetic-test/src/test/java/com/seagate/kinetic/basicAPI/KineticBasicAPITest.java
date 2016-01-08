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
package com.seagate.kinetic.basicAPI;

import static com.seagate.kinetic.KineticAssertions.assertEntryEquals;
import static com.seagate.kinetic.KineticAssertions.assertKeyNotFound;
import static com.seagate.kinetic.KineticAssertions.assertListOfArraysEqual;
import static com.seagate.kinetic.KineticTestHelpers.int32;
import static com.seagate.kinetic.KineticTestHelpers.toByteArray;
import static com.seagate.kinetic.KineticTestHelpers.cleanData;
import static com.seagate.kinetic.KineticTestHelpers.cleanKVGenData;
import static com.seagate.kinetic.KineticTestHelpers.cleanNextData;
import static com.seagate.kinetic.KineticTestHelpers.cleanPreviousData;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import kinetic.client.Entry;
import kinetic.client.EntryMetadata;
import kinetic.client.KineticClient;
import kinetic.client.KineticException;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.collect.Lists;
import com.seagate.kinetic.IntegrationTestCase;
import com.seagate.kinetic.IntegrationTestLoggerFactory;
import com.seagate.kinetic.KVGenerator;

/**
 * Kinetic Client Basic API Test.
 * <p>
 * Basic API include:
 * <p>
 * put(Entry entry, byte[] newVersion)
 * <p>
 * putForced(Entry entry)
 * <p>
 * get(byte[] key)
 * <p>
 * delete(Entry entry)
 * <p>
 * deleteForced(byte[] key)
 * <p>
 * getNext(byte[] key)
 * <p>
 * getPrevious(byte[] key)
 * <p>
 * getKeyRange(byte[] startKey, boolean startKeyInclusive, byte[] endKey,
 * boolean endKeyInclusive, int maxKeys)
 * <p>
 * getMetadata(byte[] key)
 * <p>
 *
 * @see KineticClient
 *
 */

@Test(groups = { "simulator", "drive" })
public class KineticBasicAPITest extends IntegrationTestCase {
    private static final Logger logger = IntegrationTestLoggerFactory
            .getLogger(KineticBasicAPITest.class.getName());
    private KVGenerator kvGenerator;
    private final String KEY_PREFIX = "key";
    private final int MAX_KEYS = 3;

    /**
     * Initialize a KVGenerator to generate key/value for the test
     * <p>
     *
     */
    @BeforeMethod
    public void setUp() {
        kvGenerator = new KVGenerator();
    }

    /**
     * Test put API with a serial of entries. Metadata with the value of tag and
     * algorithm The test result should be successful and verify the result
     * returned is the same as put before
     * <p>
     *
     * @throws KineticException
     *             if any internal error occurred.
     */
    @Test(dataProvider = "transportProtocolOptions")
    public void testPut(String clientName) throws KineticException {
        Entry versionedPut;
        Entry versionedPutReturn;
        byte[] key;
        byte[] value;
        String algorithm = "SHA1";
        Long start = System.nanoTime();

        cleanData(MAX_KEYS, getClient(clientName));

        for (int i = 0; i < MAX_KEYS; i++) {
            key = toByteArray(KEY_PREFIX + i);
            value = ByteBuffer.allocate(8).putLong(start + i).array();
            EntryMetadata entryMetadata = new EntryMetadata();
            entryMetadata.setTag(key);
            entryMetadata.setAlgorithm(algorithm);
            versionedPut = new Entry(key, value, entryMetadata);

            versionedPutReturn = getClient(clientName).put(versionedPut,
                    int32(i));
            assertArrayEquals(key, versionedPutReturn.getKey());
            assertArrayEquals(int32(i), versionedPutReturn.getEntryMetadata()
                    .getVersion());
            assertArrayEquals(value, versionedPutReturn.getValue());
            assertArrayEquals(key, versionedPutReturn.getEntryMetadata()
                    .getTag());
            assertEquals("SHA1", versionedPutReturn.getEntryMetadata()
                    .getAlgorithm());
        }

        cleanData(MAX_KEYS, getClient(clientName));

        logger.info(this.testEndInfo());
    }

    /**
     * Test put API with a serial of entries. Metadata with the value of
     * algorithm, without algorithm The test result should be successful and
     * verify the result returned is the same as put before
     * <p>
     *
     * @throws KineticException
     *             if any internal error occurred.
     */
    @Test(dataProvider = "transportProtocolOptions")
    public void testPut_MetadataWithoutTag(String clientName)
            throws KineticException {
        Entry versionedPut;
        Entry versionedPutReturn;
        byte[] key;
        byte[] value;
        String algorithm = "SHA1";
        Long start = System.nanoTime();

        cleanData(MAX_KEYS, getClient(clientName));

        for (int i = 0; i < MAX_KEYS; i++) {
            key = toByteArray(KEY_PREFIX + i);
            value = ByteBuffer.allocate(8).putLong(start + i).array();
            EntryMetadata entryMetadata = new EntryMetadata();
            entryMetadata.setAlgorithm(algorithm);
            versionedPut = new Entry(key, value, entryMetadata);

            versionedPutReturn = getClient(clientName).put(versionedPut,
                    int32(i));
            assertArrayEquals(key, versionedPutReturn.getKey());
            assertArrayEquals(int32(i), versionedPutReturn.getEntryMetadata()
                    .getVersion());
            assertArrayEquals(value, versionedPutReturn.getValue());
            assertArrayEquals(null, versionedPutReturn.getEntryMetadata()
                    .getTag());
            assertEquals("SHA1", versionedPutReturn.getEntryMetadata()
                    .getAlgorithm());
        }

        cleanData(MAX_KEYS, getClient(clientName));

        logger.info(this.testEndInfo());
    }

    /**
     * Test get API with a serial of entries. The entries have already existed
     * in simulator/drive. The test result should be successful and verify the
     * result returned is the same as put before
     * <p>
     *
     * @throws KineticException
     *             if any internal error occurred.
     */
    @Test(dataProvider = "transportProtocolOptions")
    public void testGet_ReturnsExistingValues(String clientName)
            throws KineticException {
        Entry versionedPut;
        Entry versionedPutReturn;
        Entry versionedGet;
        List<Entry> versionedPutReturnEntry = new ArrayList<Entry>();
        byte[] key;
        byte[] value;
        Long start = System.nanoTime();

        cleanData(MAX_KEYS, getClient(clientName));

        for (int i = 0; i < MAX_KEYS; i++) {
            key = toByteArray(KEY_PREFIX + i);
            value = ByteBuffer.allocate(8).putLong(start + i).array();
            EntryMetadata entryMetadata = new EntryMetadata();
            versionedPut = new Entry(key, value, entryMetadata);

            versionedPutReturn = getClient(clientName).put(versionedPut,
                    int32(i));

            versionedPutReturnEntry.add(versionedPutReturn);
        }

        for (int i = 0; i < MAX_KEYS; i++) {
            key = toByteArray(KEY_PREFIX + i);

            versionedGet = getClient(clientName).get(key);

            assertEntryEquals(versionedGet, versionedPutReturnEntry.get(i));
        }

        cleanData(MAX_KEYS, getClient(clientName));

        logger.info(this.testEndInfo());
    }

    /**
     * Test get API with a serial of entries. The entries haven't existed in
     * simulator/drive. The test result should be null or throw
     * AssertionFailedError exception with message of "Expect key not exist"
     * <p>
     *
     * @throws KineticException
     *             if any internal error occurred.
     */
    @Test(dataProvider = "transportProtocolOptions")
    public void testGet_ReturnsNullForNonExistingKeys(String clientName)
            throws KineticException {
        cleanData(MAX_KEYS, getClient(clientName));

        for (int i = 0; i < MAX_KEYS; i++) {
            byte[] key = toByteArray(KEY_PREFIX + i);
            assertKeyNotFound(getClient(clientName), key);
        }

        cleanData(MAX_KEYS, getClient(clientName));

        logger.info(this.testEndInfo());
    }

    /**
     * Test delete API with a serial of entries. The entries have already
     * existed in simulator/drive. The test result should be true. Try to get
     * key to verify the results is null after delete.
     * <p>
     *
     * @throws KineticException
     *             if any internal error occurred.
     */
    @Test(dataProvider = "transportProtocolOptions")
    public void testDelete_DeletesExistingKeys(String clientName)
            throws KineticException {
        Long start = System.nanoTime();

        cleanData(MAX_KEYS, getClient(clientName));

        for (int i = 0; i < MAX_KEYS; i++) {
            byte[] key = toByteArray(KEY_PREFIX + i);
            byte[] value = ByteBuffer.allocate(8).putLong(start + i).array();
            EntryMetadata entryMetadata = new EntryMetadata();
            Entry versionedPut = new Entry(key, value, entryMetadata);

            Entry versionedGet = getClient(clientName).put(versionedPut,
                    int32(i));
            assertTrue(getClient(clientName).delete(versionedGet));
        }

        for (int i = 0; i < MAX_KEYS; i++) {
            assertKeyNotFound(getClient(clientName),
                    toByteArray(KEY_PREFIX + i));
        }

        cleanData(MAX_KEYS, getClient(clientName));

        logger.info(this.testEndInfo());
    }

    /**
     * Test delete API with a entry has not existed in simulator/drive. The test
     * result should be false.
     * <p>
     *
     * @throws KineticException
     *             if any internal error occurred.
     */
    @Test(dataProvider = "transportProtocolOptions")
    public void testDelete_ReturnsFalse_ForMissingKey(String clientName)
            throws KineticException {
        byte[] key = toByteArray("asdfgh#$@257");
        assertKeyNotFound(getClient(clientName), key);

        // delete key which does not exist in Simulator/Drive
        byte[] value = toByteArray("value");
        EntryMetadata entryMetadata = new EntryMetadata();
        entryMetadata.setVersion(int32(0));
        Entry versioned = new Entry(key, value, entryMetadata);
        assertFalse(getClient(clientName).delete(versioned));

        logger.info(this.testEndInfo());
    }

    /**
     * Test getNext API with a serial of entries. The entries have already
     * existed in simulator/drive. The test result should be successful.
     * <p>
     *
     * @throws KineticException
     *             if any internal error occurred.
     */
    @Test(dataProvider = "transportProtocolOptions")
    public void testGetNext_ReturnsNextValue(String clientName)
            throws KineticException {
        long start = System.nanoTime();

        Entry vIn;
        Entry vOut;
        List<Entry> versionedOutList = new ArrayList<Entry>();
        List<byte[]> keyList = new ArrayList<byte[]>();

        cleanData(MAX_KEYS, getClient(clientName));

        for (int i = 0; i < MAX_KEYS; i++) {
            byte[] key = toByteArray(KEY_PREFIX + i);
            byte[] data = ByteBuffer.allocate(8).putLong(start + i).array();
            keyList.add(key);
            EntryMetadata entryMetadata = new EntryMetadata();
            vIn = new Entry(key, data, entryMetadata);

            vOut = getClient(clientName).put(vIn, data);
            versionedOutList.add(vOut);
        }

        for (int i = 0; i < MAX_KEYS - 1; i++) {
            Entry vNext = getClient(clientName).getNext(keyList.get(i));

            assertEntryEquals(versionedOutList.get(i + 1), vNext);
        }

        cleanData(MAX_KEYS, getClient(clientName));

        logger.info(this.testEndInfo());
    }

    /**
     * Test getNext API with the last entry existed in simulator/drive. The test
     * result should be null.
     * <p>
     *
     * @throws KineticException
     *             if any internal error occurred.
     */
    @Test(dataProvider = "transportProtocolOptions")
    public void testGetNext_ReturnsNull_IfLastValue(String clientName)
            throws KineticException {
        byte[] key = toByteArray("key");
        cleanNextData(key, getClient(clientName));
        getClient(clientName).putForced(new Entry(key, toByteArray("value")));
        assertNull(getClient(clientName).getNext(key));

        getClient(clientName).deleteForced(key);
        logger.info(this.testEndInfo());
    }

    /**
     * Test getPrevious API with a serial of entries. The entries have already
     * existed in simulator/drive. The test result should be successful.
     * <p>
     *
     * @throws KineticException
     *             if any internal error occurred.
     */
    @Test(dataProvider = "transportProtocolOptions")
    public void testGetPrevious_ReturnsPreviousValues(String clientName)
            throws KineticException {
        long start = System.nanoTime();

        List<Entry> versionedOutList = new ArrayList<Entry>();
        List<byte[]> keyList = new ArrayList<byte[]>();

        cleanData(MAX_KEYS, getClient(clientName));

        for (int i = 0; i < MAX_KEYS; i++) {
            byte[] key = toByteArray(KEY_PREFIX + i);
            byte[] data = ByteBuffer.allocate(8).putLong(start + i).array();
            keyList.add(key);
            EntryMetadata entryMetadata = new EntryMetadata();
            Entry vIn = new Entry(key, data, entryMetadata);
            Entry vOut = getClient(clientName).put(vIn, data);
            versionedOutList.add(vOut);
        }

        for (int i = 1; i < MAX_KEYS; i++) {
            Entry vPre = getClient(clientName).getPrevious(keyList.get(i));

            assertEntryEquals(versionedOutList.get(i - 1), vPre);
        }

        cleanData(MAX_KEYS, getClient(clientName));

        logger.info(this.testEndInfo());
    }

    /**
     * Test getPrevious API with the first entry existed in simulator/drive. The
     * test result should be null.
     * <p>
     *
     * @throws KineticException
     *             if any internal error occurred.
     */
    @Test(dataProvider = "transportProtocolOptions")
    public void testGetPrevious_ReturnsNull_IfFirstKey(String clientName)
            throws KineticException {
        byte[] key = toByteArray("key");

        cleanPreviousData(key, getClient(clientName));

        getClient(clientName).putForced(new Entry(key, toByteArray("value")));
        Entry v = getClient(clientName).getPrevious(key);
        assertEquals(null, v);

        getClient(clientName).deleteForced(key);
        logger.info(this.testEndInfo());
    }

    /**
     * Test getMetadata API with a serial of entries. The entries have already
     * existed in simulator/drive. The test result should be successful.
     * <p>
     *
     * @throws KineticException
     *             if any internal error occurred.
     */
    @Test(dataProvider = "transportProtocolOptions")
    public void testGetMetadata_ReturnsMetadata_ForExistingKey(String clientName)
            throws KineticException {
        byte[] newVersion = int32(0);
        // String alg = Message.Algorithm.INVALID_ALGORITHM.toString();

        cleanKVGenData(MAX_KEYS, getClient(clientName));

        for (int i = 0; i < MAX_KEYS; i++) {
            String keyS = kvGenerator.getNextKey();
            String valueS = kvGenerator.getValue(keyS);

            EntryMetadata entryMetadata = new EntryMetadata();
            Entry entry = new Entry(toByteArray(keyS), toByteArray(valueS),
                    entryMetadata);
            getClient(clientName).put(entry, newVersion);

            EntryMetadata entryMetadataGet;
            entryMetadataGet = getClient(clientName).getMetadata(
                    toByteArray(keyS));
            assertArrayEquals(newVersion, entryMetadataGet.getVersion());

            // XXX chiaming 04/12/2013: default enum is not valid, need to be
            // evaluated.
            // assertEquals(alg, entryMetadataGet.getAlgorithm());
        }

        cleanKVGenData(MAX_KEYS, getClient(clientName));

        logger.info(this.testEndInfo());
    }

    /**
     * Test getMetadata API with a serial of entries. The entries have not
     * existed in simulator/drive. The test result should be null.
     * <p>
     *
     * @throws KineticException
     *             if any internal error occurred.
     */
    @Test(dataProvider = "transportProtocolOptions")
    public void testGetMetadata_ReturnsNull_ForNonexistingKey(String clientName)
            throws KineticException {
        byte[] key = toByteArray("@#$%2345");
        getClient(clientName).deleteForced(key);

        assertNull(getClient(clientName).get(key));
        assertNull(getClient(clientName).getMetadata(key));

        getClient(clientName).deleteForced(key);

        logger.info(this.testEndInfo());
    }

    /**
     * Test getKeyRange API with a serial of entries. The entries have already
     * existed in simulator/drive. Both startKey and endKey are inclusive. The
     * test result should be successful.
     * <p>
     *
     * @throws KineticException
     *             if any internal error occurred.
     */
    @Test(dataProvider = "transportProtocolOptions")
    public void testGetKeyRange_ReturnsCorrectResults_ForStartEndInclusive(
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
                .getKeyRange(keys.get(0), true, keys.get(keys.size() - 1),
                        true, keys.size()));

        assertListOfArraysEqual(keys, returnedKeys);

        cleanData(toByteArray("00"), toByteArray("14"), getClient(clientName));

        logger.info(this.testEndInfo());
    }

    /**
     * Test getKeyRange API with a serial of entries. The entries have already
     * existed in simulator/drive. StartKey is exclusive but endKey is
     * inclusive. The test result should be successful.
     * <p>
     *
     * @throws KineticException
     *             if any internal error occurred.
     */
    @Test(dataProvider = "transportProtocolOptions")
    public void testGetKeyRange_ReturnsCorrectResults_ForEndInclusive(
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
                .getKeyRange(keys.get(0), false, keys.get(keys.size() - 1),
                        true, keys.size() - 1));

        assertListOfArraysEqual(keys.subList(1, keys.size()), returnedKeys);

        cleanData(toByteArray("00"), toByteArray("14"), getClient(clientName));

        logger.info(this.testEndInfo());
    }

    /**
     * Test getKeyRange API with a serial of entries. The entries have already
     * existed in simulator/drive. StartKey is inclusive but endKey is
     * exclusive. The test result should be successful.
     * <p>
     *
     * @throws KineticException
     *             if any internal error occurred.
     */
    @Test(dataProvider = "transportProtocolOptions")
    public void testGetKeyRange_ReturnsCorrectResults_ForStartInclusive(
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
                .getKeyRange(keys.get(0), true, keys.get(keys.size() - 1),
                        false, keys.size() - 1));

        assertListOfArraysEqual(keys.subList(0, keys.size() - 1), returnedKeys);

        cleanData(toByteArray("00"), toByteArray("14"), getClient(clientName));

        logger.info(this.testEndInfo());
    }

    /**
     * Test getKeyRange API with a serial of entries. The entries have already
     * existed in simulator/drive. Both startKey and endKey are exclusive. The
     * test result should be successful.
     * <p>
     *
     * @throws KineticException
     *             if any internal error occurred.
     */
    @Test(dataProvider = "transportProtocolOptions")
    public void testGetKeyRange_ReturnsCorrectResults_ForNoneInclusive(
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
                .getKeyRange(keys.get(0), false, keys.get(keys.size() - 1),
                        false, keys.size() - 1));

        assertListOfArraysEqual(keys.subList(1, keys.size() - 1), returnedKeys);

        cleanData(toByteArray("00"), toByteArray("14"), getClient(clientName));

        logger.info(this.testEndInfo());
    }

    /**
     * Test getKeyRange API result order with 0~200 hex. The entries have
     * already existed in simulator/drive. For instance, 0x00 < 0x10 < 0x2a <
     * 0xbf
     * <p>
     *
     * @throws KineticException
     *             if any internal error occurred.
     */
    @Test(dataProvider = "transportProtocolOptions", enabled = false)
    public void testGetKeyRange_VerifyOrder_For0To200(String clientName)
            throws KineticException {
        List<byte[]> keys = new ArrayList<byte[]>();

        String hex = null;
        for (int i = 0; i < 200; i++) {
            if (i < 16)
                hex = "0x0" + Integer.toHexString(i);
            else
                hex = "0x" + Integer.toHexString(i);
            byte[] key = toByteArray(hex);

            getClient(clientName).put(
                    new Entry(key, Integer.toString(i).getBytes()), null);

            keys.add(key);
        }

        List<byte[]> keysRange = getClient(clientName).getKeyRange(
                new byte[] {}, true, toByteArray("0xff"), true, 200);

        assertEquals(200, keysRange.size());

        assertListOfArraysEqual(keys, keysRange);

        logger.info(this.testEndInfo());
    }

    /**
     * Test putForced API result with a serial entries. The entries have already
     * existed in simulator/drive. Give new entry with db version different with
     * version in simulator/drive, the test result should be successful.
     * <p>
     *
     * @throws KineticException
     *             if any internal error occurred.
     */
    @Test(dataProvider = "transportProtocolOptions")
    public void testPutForced(String clientName) throws KineticException {
        Long start = System.nanoTime();

        cleanData(MAX_KEYS, getClient(clientName));

        for (int i = 0; i < MAX_KEYS; i++) {
            byte[] key = toByteArray(KEY_PREFIX + i);
            byte[] value = ByteBuffer.allocate(8).putLong(start + i).array();
            EntryMetadata entryMetadata = new EntryMetadata();
            Entry versionedPut = new Entry(key, value, entryMetadata);

            getClient(clientName).put(versionedPut, int32(i));
        }

        start = System.nanoTime();
        byte[] version = int32(8);
        for (int i = 0; i < MAX_KEYS; i++) {
            byte[] key = toByteArray(KEY_PREFIX + i);
            byte[] value = ByteBuffer.allocate(8).putLong(start + i).array();

            EntryMetadata entryMetadata = new EntryMetadata();

            entryMetadata.setVersion(version);

            Entry versionedPutForced = new Entry(key, value, entryMetadata);

            getClient(clientName).putForced(versionedPutForced);

            Entry entryGet = getClient(clientName).get(key);
            assertArrayEquals(key, entryGet.getKey());
            assertArrayEquals(version, entryGet.getEntryMetadata().getVersion());
            assertArrayEquals(value, entryGet.getValue());
        }

        cleanData(MAX_KEYS, getClient(clientName));

        logger.info(this.testEndInfo());
    }

    /**
     * Test deleteForced API result with a serial entries. The entries have
     * already existed in simulator/drive. The test result should be true.
     * Verify get the key is null after deleteForced.
     * <p>
     *
     * @throws KineticException
     *             if any internal error occurred.
     */
    @Test(dataProvider = "transportProtocolOptions")
    public void testDeleteForced_RemovesExistingKeys(String clientName)
            throws KineticException {
        Long start = System.nanoTime();

        cleanData(MAX_KEYS, getClient(clientName));

        for (int i = 0; i < MAX_KEYS; i++) {
            byte[] key = toByteArray(KEY_PREFIX + i);
            byte[] value = ByteBuffer.allocate(8).putLong(start + i).array();
            EntryMetadata entryMetadata = new EntryMetadata();
            Entry versionedPut = new Entry(key, value, entryMetadata);

            getClient(clientName).put(versionedPut, int32(i));

            assertTrue(getClient(clientName).deleteForced(key));
            assertKeyNotFound(getClient(clientName), key);
        }

        cleanData(MAX_KEYS, getClient(clientName));

        logger.info(this.testEndInfo());
    }

    /**
     * Test deleteForced API result with entry. The entry has not existed in
     * simulator/drive. The test result should be true.
     * <p>
     *
     * @throws KineticException
     *             if any internal error occurred.
     */
    @Test(dataProvider = "transportProtocolOptions")
    public void testDeleteForced_Succeeds_IfKeysDontExist(String clientName)
            throws KineticException {
        getClient(clientName).deleteForced(toByteArray("***345@#$"));

        assertTrue(getClient(clientName).deleteForced(toByteArray("***345@#$")));

        logger.info(this.testEndInfo());
    }
}
