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
import java.util.logging.Logger;

import kinetic.client.ClientConfiguration;
import kinetic.client.Entry;
import kinetic.client.EntryMetadata;
import kinetic.client.KineticClient;
import kinetic.client.KineticClientFactory;
import kinetic.client.KineticException;

import com.google.common.collect.Lists;
import com.seagate.kinetic.IntegrationTestLoggerFactory;

public class BasicAPISanityExample {
    private static final Logger logger = IntegrationTestLoggerFactory
            .getLogger(BasicAPISanityExample.class.getName());

    private ClientConfiguration cconfig;
    private KineticClient client;
    private String host = System.getProperty("KINETIC_HOST", "127.0.0.1");
    private int port = Integer.parseInt(System.getProperty("KINEITC_PORT",
            "8123"));
    private int sslPort = Integer.parseInt(System.getProperty(
            "KINEITC_SSL_PORT", "8443"));
    private boolean useSsl = Boolean.parseBoolean(System.getProperty(
            "RUN_SSL_TEST", "false"));
    private final String KEY_PREFIX = "key";
    private final int MAX_KEYS = 1;

    public BasicAPISanityExample() throws KineticException {
        cconfig = new ClientConfiguration();
        cconfig.setHost(host);
        cconfig.setPort(port);
        if (useSsl) {
            cconfig.setUseSsl(useSsl);
            cconfig.setPort(sslPort);
        }

        client = KineticClientFactory.createInstance(cconfig);

    }

    private void clean(int maxkeys) throws KineticException {
        byte[] key;

        for (int i = 0; i < maxkeys; i++) {
            key = toByteArray(KEY_PREFIX + i);
            client.deleteForced(key);
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
    public void testPut() throws KineticException {
        Entry versionedPut;
        Entry versionedPutReturn;
        byte[] key;
        byte[] value;
        String algorithm = "SHA1";
        Long start = System.nanoTime();

        clean(MAX_KEYS);

        for (int i = 0; i < MAX_KEYS; i++) {
            key = toByteArray(KEY_PREFIX + i);
            value = ByteBuffer.allocate(8).putLong(start + i).array();
            EntryMetadata entryMetadata = new EntryMetadata();
            entryMetadata.setTag(key);
            entryMetadata.setAlgorithm(algorithm);
            versionedPut = new Entry(key, value, entryMetadata);

            versionedPutReturn = client.put(versionedPut, int32(i));
            assertArrayEquals(key, versionedPutReturn.getKey());
            assertArrayEquals(int32(i), versionedPutReturn.getEntryMetadata()
                    .getVersion());
            assertArrayEquals(value, versionedPutReturn.getValue());
            assertArrayEquals(key, versionedPutReturn.getEntryMetadata()
                    .getTag());
            assertEquals("SHA1", versionedPutReturn.getEntryMetadata()
                    .getAlgorithm());
        }

        clean(MAX_KEYS);
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
    public void testGet() throws KineticException {
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

            versionedPutReturn = client.putForced(versionedPut);

            versionedPutReturnEntry.add(versionedPutReturn);
        }

        for (int i = 0; i < MAX_KEYS; i++) {
            key = toByteArray(KEY_PREFIX + i);

            versionedGet = client.get(key);

            assertEntryEquals(versionedGet, versionedPutReturnEntry.get(i));

            client.deleteForced(key);
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
    public void testDelete() throws KineticException {
        Long start = System.nanoTime();

        for (int i = 0; i < MAX_KEYS; i++) {
            byte[] key = toByteArray(KEY_PREFIX + i);
            byte[] value = ByteBuffer.allocate(8).putLong(start + i).array();
            EntryMetadata entryMetadata = new EntryMetadata();
            Entry versionedPut = new Entry(key, value, entryMetadata);

            Entry versionedGet = client.putForced(versionedPut);
            assertTrue(client.delete(versionedGet));
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
    public void testGetNext() throws KineticException {
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

            vOut = client.putForced(vIn);
            versionedOutList.add(vOut);
        }

        for (int i = 0; i < MAX_KEYS; i++) {
            Entry vNext = client.getNext(keyList.get(i));

            assertEntryEquals(versionedOutList.get(i + 1), vNext);
        }

        clean(MAX_KEYS + 1);
    }

    /**
     * Test getPrevious API with a serial of entries. The entries have already
     * existed in simulator/drive. The test result should be successful.
     * <p>
     *
     * @throws KineticException
     *             if any internal error occurred.
     */
    public void testGetPrevious() throws KineticException {
        long start = System.nanoTime();

        List<Entry> versionedOutList = new ArrayList<Entry>();
        List<byte[]> keyList = new ArrayList<byte[]>();
        for (int i = 0; i < MAX_KEYS + 1; i++) {
            byte[] key = toByteArray(KEY_PREFIX + i);
            byte[] data = ByteBuffer.allocate(8).putLong(start + i).array();
            keyList.add(key);
            EntryMetadata entryMetadata = new EntryMetadata();
            Entry vIn = new Entry(key, data, entryMetadata);
            Entry vOut = client.putForced(vIn);
            versionedOutList.add(vOut);
        }

        for (int i = 1; i < MAX_KEYS + 1; i++) {
            Entry vPre = client.getPrevious(keyList.get(i));

            assertEntryEquals(versionedOutList.get(i - 1), vPre);
        }

        clean(MAX_KEYS + 1);
    }

    /**
     * Test getMetadata API with a serial of entries. The entries have already
     * existed in simulator/drive. The test result should be successful.
     * <p>
     *
     * @throws KineticException
     *             if any internal error occurred.
     */
    public void testGetMetadata() throws KineticException {
        byte[] newVersion = int32(0);
        long start = System.nanoTime();
        for (int i = 0; i < MAX_KEYS; i++) {
            byte[] key = toByteArray(KEY_PREFIX + i);
            byte[] data = ByteBuffer.allocate(8).putLong(start + i).array();

            EntryMetadata entryMetadata = new EntryMetadata();
            entryMetadata.setVersion(newVersion);
            Entry entry = new Entry(key, data, entryMetadata);
            client.putForced(entry);

            EntryMetadata entryMetadataGet;
            entryMetadataGet = client.getMetadata(key);
            assertArrayEquals(newVersion, entryMetadataGet.getVersion());
            client.deleteForced(key);
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
    public void testGetKeyRange() throws KineticException {
        List<byte[]> keys = Arrays.asList(toByteArray("00"), toByteArray("01"),
                toByteArray("02"), toByteArray("03"));

        for (byte[] key : keys) {
            client.putForced(new Entry(key, key));
        }

        List<byte[]> returnedKeys = Lists
                .newLinkedList(client.getKeyRange(keys.get(0), true,
                        keys.get(keys.size() - 1), true, keys.size()));

        assertListOfArraysEqual(keys, returnedKeys);

        for (byte[] key : keys) {
            client.deleteForced(key);
        }
    }

    private void close() throws KineticException {
        client.close();
    }

    public static void main(String[] args) {
        BasicAPISanityExample example = null;
        try {
            example = new BasicAPISanityExample();
            example.testDelete();
            example.testGet();
            example.testGetKeyRange();
            example.testGetMetadata();
            example.testGetNext();
            example.testGetPrevious();
            example.testPut();
        } catch (KineticException e) {
            logger.severe(e.getMessage());
        } finally {
            try {
                example.close();
            } catch (KineticException e) {
                logger.severe(e.getMessage());
            }
        }
    }
}
