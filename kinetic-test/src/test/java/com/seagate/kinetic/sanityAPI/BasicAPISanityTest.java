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
package com.seagate.kinetic.sanityAPI;

import static com.seagate.kinetic.KineticAssertions.assertEntryEquals;
import static com.seagate.kinetic.KineticAssertions.assertListOfArraysEqual;
import static com.seagate.kinetic.KineticTestHelpers.int32;
import static com.seagate.kinetic.KineticTestHelpers.toByteArray;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import kinetic.client.Entry;
import kinetic.client.EntryMetadata;
import kinetic.client.KineticException;

import org.testng.annotations.Test;

import com.google.common.collect.Lists;
import com.seagate.kinetic.IntegrationTestCase;

@Test(groups = { "simulator", "drive" })
public class BasicAPISanityTest extends IntegrationTestCase {
    private final String KEY_PREFIX = "key";
    private final int MAX_KEYS = 1;

    private void clean(String clientName, int maxkeys) throws KineticException {
        byte[] key;

        for (int i = 0; i < maxkeys; i++) {
            key = toByteArray(KEY_PREFIX + i);
            getClient(clientName).deleteForced(key);
        }

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

        clean(clientName, MAX_KEYS);

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

        clean(clientName, MAX_KEYS);
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
    public void testGet(String clientName) throws KineticException {
        Entry versionedPut;
        Entry versionedPutReturn;
        Entry versionedGet;
        List<Entry> versionedPutReturnEntry = new ArrayList<Entry>();
        byte[] key;
        byte[] value;
        Long start = System.nanoTime();

        for (int i = 0; i < MAX_KEYS; i++) {
            key = toByteArray(KEY_PREFIX + i);
            value = ByteBuffer.allocate(8).putLong(start + i).array();
            EntryMetadata entryMetadata = new EntryMetadata();
            versionedPut = new Entry(key, value, entryMetadata);

            versionedPutReturn = getClient(clientName).putForced(versionedPut);

            versionedPutReturnEntry.add(versionedPutReturn);
        }

        for (int i = 0; i < MAX_KEYS; i++) {
            key = toByteArray(KEY_PREFIX + i);

            versionedGet = getClient(clientName).get(key);

            assertEntryEquals(versionedGet, versionedPutReturnEntry.get(i));

            getClient(clientName).deleteForced(key);
        }
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
    public void testDelete(String clientName) throws KineticException {
        Long start = System.nanoTime();

        for (int i = 0; i < MAX_KEYS; i++) {
            byte[] key = toByteArray(KEY_PREFIX + i);
            byte[] value = ByteBuffer.allocate(8).putLong(start + i).array();
            EntryMetadata entryMetadata = new EntryMetadata();
            Entry versionedPut = new Entry(key, value, entryMetadata);

            Entry versionedGet = getClient(clientName).putForced(versionedPut);
            assertTrue(getClient(clientName).delete(versionedGet));
        }
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
    public void testGetNext(String clientName) throws KineticException {
        long start = System.nanoTime();

        Entry vIn;
        Entry vOut;
        List<Entry> versionedOutList = new ArrayList<Entry>();
        List<byte[]> keyList = new ArrayList<byte[]>();
        for (int i = 0; i < MAX_KEYS + 1; i++) {
            byte[] key = toByteArray(KEY_PREFIX + i);
            byte[] data = ByteBuffer.allocate(8).putLong(start + i).array();
            keyList.add(key);
            EntryMetadata entryMetadata = new EntryMetadata();
            vIn = new Entry(key, data, entryMetadata);

            vOut = getClient(clientName).putForced(vIn);
            versionedOutList.add(vOut);
        }

        for (int i = 0; i < MAX_KEYS; i++) {
            Entry vNext = getClient(clientName).getNext(keyList.get(i));

            assertEntryEquals(versionedOutList.get(i + 1), vNext);
        }

        clean(clientName, MAX_KEYS + 1);
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
    public void testGetPrevious(String clientName) throws KineticException {
        long start = System.nanoTime();

        List<Entry> versionedOutList = new ArrayList<Entry>();
        List<byte[]> keyList = new ArrayList<byte[]>();
        for (int i = 0; i < MAX_KEYS + 1; i++) {
            byte[] key = toByteArray(KEY_PREFIX + i);
            byte[] data = ByteBuffer.allocate(8).putLong(start + i).array();
            keyList.add(key);
            EntryMetadata entryMetadata = new EntryMetadata();
            Entry vIn = new Entry(key, data, entryMetadata);
            Entry vOut = getClient(clientName).putForced(vIn);
            versionedOutList.add(vOut);
        }

        for (int i = 1; i < MAX_KEYS + 1; i++) {
            Entry vPre = getClient(clientName).getPrevious(keyList.get(i));

            assertEntryEquals(versionedOutList.get(i - 1), vPre);
        }

        clean(clientName, MAX_KEYS + 1);
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
    public void testGetMetadata(String clientName) throws KineticException {
        byte[] newVersion = int32(0);
        long start = System.nanoTime();
        for (int i = 0; i < MAX_KEYS; i++) {
            byte[] key = toByteArray(KEY_PREFIX + i);
            byte[] data = ByteBuffer.allocate(8).putLong(start + i).array();

            EntryMetadata entryMetadata = new EntryMetadata();
            entryMetadata.setVersion(newVersion);
            Entry entry = new Entry(key, data, entryMetadata);
            getClient(clientName).putForced(entry);

            EntryMetadata entryMetadataGet;
            entryMetadataGet = getClient(clientName).getMetadata(key);
            assertArrayEquals(newVersion, entryMetadataGet.getVersion());
            getClient(clientName).deleteForced(key);
        }
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
    public void testGetKeyRange(String clientName) throws KineticException {
        List<byte[]> keys = Arrays.asList(toByteArray("00"), toByteArray("01"),
                toByteArray("02"), toByteArray("03"));

        for (byte[] key : keys) {
            getClient(clientName).putForced(new Entry(key, key));
        }

        List<byte[]> returnedKeys = Lists.newLinkedList(getClient(clientName)
                .getKeyRange(keys.get(0), true, keys.get(keys.size() - 1),
                        true, keys.size()));

        assertListOfArraysEqual(keys, returnedKeys);

        for (byte[] key : keys) {
            getClient(clientName).deleteForced(key);
        }
    }
}
