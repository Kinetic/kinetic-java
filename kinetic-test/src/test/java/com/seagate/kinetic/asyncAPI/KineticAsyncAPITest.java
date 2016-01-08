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
package com.seagate.kinetic.asyncAPI;

import static com.seagate.kinetic.KineticAssertions.assertKeyNotFound;
import static com.seagate.kinetic.KineticAssertions.assertListOfArraysEqual;
import static com.seagate.kinetic.KineticTestHelpers.buildSuccessOnlyCallbackHandler;
import static com.seagate.kinetic.KineticTestHelpers.cleanData;
import static com.seagate.kinetic.KineticTestHelpers.int32;
import static com.seagate.kinetic.KineticTestHelpers.toByteArray;
import static com.seagate.kinetic.KineticTestHelpers.waitForLatch;
import static com.seagate.kinetic.KineticTestHelpers.cleanKVGenData;
import static com.seagate.kinetic.KineticTestHelpers.cleanPreviousData;
import static com.seagate.kinetic.KineticTestHelpers.cleanNextData;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;

import kinetic.client.CallbackHandler;
import kinetic.client.CallbackResult;
import kinetic.client.Entry;
import kinetic.client.EntryMetadata;
import kinetic.client.KineticClient;
import kinetic.client.KineticException;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.seagate.kinetic.IntegrationTestCase;
import com.seagate.kinetic.IntegrationTestLoggerFactory;
import com.seagate.kinetic.KVGenerator;
import com.seagate.kinetic.KineticTestHelpers;
import com.seagate.kinetic.KineticTestHelpers.SuccessAsyncHandler;

/**
 * Kinetic Client Asynchronous API.
 * <p>
 * Asynchronous API include:
 * <p>
 * putAsync(Entry entry, byte[] newVersion, CallbackHandler<Entry> handler)
 * <p>
 * putForcedAsync(Entry entry, CallbackHandler<Entry> handler)
 * <p>
 * getAsync(byte[] key, CallbackHandler<Entry> handler)
 * <p>
 * deleteAsync(Entry entry, CallbackHandler<Boolean> handler)
 * <p>
 * deleteForcedAsync(byte[] key, CallbackHandler<Boolean> handler)
 * <p>
 * getNextAsync(byte[] key, CallbackHandler<Entry> handler)
 * <p>
 * getPreviousAsync(byte[] key, CallbackHandler<Entry> handler)
 * <p>
 * getKeyRangeAsync(byte[] startKey, boolean startKeyInclusive, byte[] endKey,
 * boolean endKeyInclusive, int maxKeys, CallbackHandler<List<byte[]>> handler)
 * <p>
 * getMetadataAsync(byte[] key, CallbackHandler<EntryMetadata> handler)
 * <p>
 * 
 * @see KineticClient
 * 
 */

@Test(groups = { "simulator", "drive" })
public class KineticAsyncAPITest extends IntegrationTestCase {
    private static final Logger logger = IntegrationTestLoggerFactory
            .getLogger(KineticAsyncAPITest.class.getName());

    private KVGenerator kvGenerator;
    private final int MAX_KEYS = 10;

    /**
     * Initialize a key/value pair generator
     * <p>
     */
    @BeforeMethod
    public void setUp() throws IOException, InterruptedException {
        kvGenerator = new KVGenerator();
    }

    /**
     * Test putAsync with a serial of entries. The test result should be
     * successful and verify the result returned is the same as put before
     * <p>
     * 
     * @throws KineticException
     *             if any internal error occurred.
     * @throws InterruptedException
     *             if thread is interrupted, or the specified waiting time
     *             elapses.
     */
    @Test(dataProvider = "transportProtocolOptions")
    public void testPutAsync(String clientName)
            throws UnsupportedEncodingException, KineticException,
            InterruptedException {
        byte[] newVersion = int32(0);
        final List<Entry> putReturnList = new ArrayList<Entry>(MAX_KEYS);

        final CountDownLatch putSignal = new CountDownLatch(MAX_KEYS);

        List<String> keySList = new ArrayList<String>();
        List<String> valueSList = new ArrayList<String>();
        List<EntryMetadata> emdOfList = new ArrayList<EntryMetadata>();

        cleanKVGenData(MAX_KEYS, getClient(clientName));

        for (int i = 0; i < MAX_KEYS; i++) {
            String keyS = kvGenerator.getNextKey();
            String valueS = kvGenerator.getValue(keyS);

            keySList.add(keyS);
            valueSList.add(valueS);

            EntryMetadata entryMetadata = new EntryMetadata();
            entryMetadata.setTag(keyS.getBytes());
            entryMetadata.setAlgorithm("SHA1");

            emdOfList.add(entryMetadata);
        }

        for (int i = 0; i < MAX_KEYS; i++) {
            Entry entryPut = new Entry(toByteArray(keySList.get(i)),
                    toByteArray(valueSList.get(i)), emdOfList.get(i));
            CallbackHandler<Entry> handler = buildSuccessOnlyCallbackHandler(new KineticTestHelpers.SuccessAsyncHandler<Entry>() {
                @Override
                public void onSuccess(CallbackResult<Entry> result) {
                    putReturnList.add(result.getResult());
                    putSignal.countDown();
                }
            });

            getClient(clientName).putAsync(entryPut, newVersion, handler);
        }

        waitForLatch(putSignal);
        assertEquals(MAX_KEYS, putReturnList.size());
        for (int i = 0; i < MAX_KEYS; i++) {
            assertTrue(keySList.contains(new String(putReturnList.get(i)
                    .getKey())));
            assertTrue(valueSList.contains(new String(putReturnList.get(i)
                    .getValue())));
            assertTrue(Arrays.equals(newVersion, putReturnList.get(i)
                    .getEntryMetadata().getVersion()));
            assertTrue(keySList.contains(new String(putReturnList.get(i)
                    .getEntryMetadata().getTag())));
        }

        cleanKVGenData(MAX_KEYS, getClient(clientName));

        logger.info(this.testEndInfo());
    }

    /**
     * Test putAsync API with a serial of entries. Metadata with the value of
     * algorithm, without algorithm The test result should be successful and
     * verify the result returned is the same as put before
     * <p>
     * 
     * @throws KineticException
     *             if any internal error occurred.
     * @throws InterruptedException
     *             if thread is interrupted, or the specified waiting time
     *             elapses.
     */
    @Test(dataProvider = "transportProtocolOptions")
    public void testPutAsync_WithoutTag(String clientName)
            throws UnsupportedEncodingException, KineticException,
            InterruptedException {
        byte[] newVersion = int32(0);
        final List<Entry> putReturnList = new ArrayList<Entry>(MAX_KEYS);

        final CountDownLatch putSignal = new CountDownLatch(MAX_KEYS);

        List<String> keySList = new ArrayList<String>();
        List<String> valueSList = new ArrayList<String>();
        List<EntryMetadata> emdOfList = new ArrayList<EntryMetadata>();

        cleanKVGenData(MAX_KEYS, getClient(clientName));

        for (int i = 0; i < MAX_KEYS; i++) {
            String keyS = kvGenerator.getNextKey();
            String valueS = kvGenerator.getValue(keyS);

            keySList.add(keyS);
            valueSList.add(valueS);

            EntryMetadata entryMetadata = new EntryMetadata();
            entryMetadata.setAlgorithm("SHA1");

            emdOfList.add(entryMetadata);
        }

        for (int i = 0; i < MAX_KEYS; i++) {
            Entry entryPut = new Entry(toByteArray(keySList.get(i)),
                    toByteArray(valueSList.get(i)), emdOfList.get(i));
            CallbackHandler<Entry> handler = buildSuccessOnlyCallbackHandler(new KineticTestHelpers.SuccessAsyncHandler<Entry>() {
                @Override
                public void onSuccess(CallbackResult<Entry> result) {
                    putReturnList.add(result.getResult());
                    putSignal.countDown();
                }
            });

            getClient(clientName).putAsync(entryPut, newVersion, handler);
        }

        waitForLatch(putSignal);
        assertEquals(MAX_KEYS, putReturnList.size());
        for (int i = 0; i < MAX_KEYS; i++) {
            assertTrue(keySList.contains(new String(putReturnList.get(i)
                    .getKey())));
            assertTrue(valueSList.contains(new String(putReturnList.get(i)
                    .getValue())));
            assertTrue(Arrays.equals(newVersion, putReturnList.get(i)
                    .getEntryMetadata().getVersion()));
        }

        cleanKVGenData(MAX_KEYS, getClient(clientName));

        logger.info(this.testEndInfo());

    }

    /**
     * Test getAsync API with a serial of entries. The entries have already
     * existed in simulator/drive. The test result should be successful and
     * verify the result returned is the same as put before
     * <p>
     * 
     * @throws KineticException
     *             if any internal error occurred.
     * @throws InterruptedException
     *             if thread is interrupted, or the specified waiting time
     *             elapses.
     */
    @Test(dataProvider = "transportProtocolOptions")
    public void testGetAsync_ReturnsExistingKeys(String clientName)
            throws UnsupportedEncodingException, KineticException,
            InterruptedException {
        byte[] newVersion = int32(0);
        final List<Entry> getReturnList = new ArrayList<Entry>(MAX_KEYS);

        final CountDownLatch putSignal = new CountDownLatch(MAX_KEYS);
        final CountDownLatch getSignal = new CountDownLatch(MAX_KEYS);

        List<String> keySList = new ArrayList<String>();
        List<String> valueSList = new ArrayList<String>();
        EntryMetadata entryMetadata = new EntryMetadata();

        cleanKVGenData(MAX_KEYS, getClient(clientName));

        for (int i = 0; i < MAX_KEYS; i++) {
            String keyS = kvGenerator.getNextKey();
            String valueS = kvGenerator.getValue(keyS);

            keySList.add(keyS);
            valueSList.add(valueS);
        }

        for (int i = 0; i < MAX_KEYS; i++) {
            Entry entryPut = new Entry(toByteArray(keySList.get(i)),
                    toByteArray(valueSList.get(i)), entryMetadata);
            CallbackHandler<Entry> handler = buildSuccessOnlyCallbackHandler(new SuccessAsyncHandler<Entry>() {
                @Override
                public void onSuccess(CallbackResult<Entry> result) {
                    putSignal.countDown();
                }
            });

            getClient(clientName).putAsync(entryPut, newVersion, handler);
        }

        waitForLatch(putSignal);

        for (int i = 0; i < MAX_KEYS; i++) {
            CallbackHandler<Entry> handler = buildSuccessOnlyCallbackHandler(new SuccessAsyncHandler<Entry>() {
                @Override
                public void onSuccess(CallbackResult<Entry> result) {
                    getReturnList.add(result.getResult());
                    getSignal.countDown();
                }
            });

            getClient(clientName).getAsync(toByteArray(keySList.get(i)),
                    handler);
        }
        waitForLatch(getSignal);

        assertTrue(getReturnList.size() == MAX_KEYS);
        for (int i = 0; i < MAX_KEYS; i++) {
            assertTrue(keySList.contains(new String(getReturnList.get(i)
                    .getKey())));
            assertTrue(valueSList.contains(new String(getReturnList.get(i)
                    .getValue())));
            assertTrue(Arrays.equals(newVersion, getReturnList.get(i)
                    .getEntryMetadata().getVersion()));
        }

        cleanKVGenData(MAX_KEYS, getClient(clientName));

        logger.info(this.testEndInfo());
    }

    /**
     * Test getAsync API with a entry. The entry has not existed in
     * simulator/drive. The test result should be null.
     * <p>
     * 
     * @throws KineticException
     *             if any internal error occurred.
     * @throws InterruptedException
     *             if thread is interrupted, or the specified waiting time
     *             elapses.
     */
    @Test(dataProvider = "transportProtocolOptions")
    public void testGetAsync_ReturnsNull_ForNonExistingKey(String clientName)
            throws KineticException, InterruptedException {
        final List<Entry> getReturnList = new ArrayList<Entry>(1);
        final CountDownLatch latch = new CountDownLatch(1);

        byte[] nonExistingKey = toByteArray("qwertyuio");
        getClient(clientName).deleteForced(nonExistingKey);

        CallbackHandler<Entry> handler = buildSuccessOnlyCallbackHandler(new SuccessAsyncHandler<Entry>() {
            @Override
            public void onSuccess(CallbackResult<Entry> result) {
                getReturnList.add(result.getResult());
                latch.countDown();
            }
        });

        getClient(clientName).getAsync(nonExistingKey, handler);

        waitForLatch(latch);

        assertNull(getReturnList.get(0));

        logger.info(this.testEndInfo());
    }

    /**
     * Test deleteAsync API with a serial of entries. The entries have already
     * existed in simulator/drive. The test result should be true. Try to get
     * key to verify the results is null after delete.
     * <p>
     * 
     * @throws KineticException
     *             if any internal error occurred.
     * @throws InterruptedException
     *             if thread is interrupted, or the specified waiting time
     *             elapses.
     */
    @Test(dataProvider = "transportProtocolOptions")
    public void testDeleteAsync_DeletesExistingKeys(String clientName)
            throws UnsupportedEncodingException, KineticException,
            InterruptedException {
        byte[] newVersion = int32(0);
        final List<Boolean> deleteReturnList = new ArrayList<Boolean>(MAX_KEYS);
        final List<Entry> getReturnList = new ArrayList<Entry>(MAX_KEYS);

        final CountDownLatch putSignal = new CountDownLatch(MAX_KEYS);
        final CountDownLatch getSignal = new CountDownLatch(MAX_KEYS);
        final CountDownLatch deleteSignal = new CountDownLatch(MAX_KEYS);

        List<String> keySList = new ArrayList<String>();
        List<String> valueSList = new ArrayList<String>();
        EntryMetadata entryMetadata = new EntryMetadata();

        cleanKVGenData(MAX_KEYS, getClient(clientName));

        for (int i = 0; i < MAX_KEYS; i++) {
            String keyS = kvGenerator.getNextKey();
            String valueS = kvGenerator.getValue(keyS);

            keySList.add(keyS);
            valueSList.add(valueS);
        }

        for (int i = 0; i < MAX_KEYS; i++) {
            Entry entryPut = new Entry(toByteArray(keySList.get(i)),
                    toByteArray(valueSList.get(i)), entryMetadata);
            CallbackHandler<Entry> handler = buildSuccessOnlyCallbackHandler(new SuccessAsyncHandler<Entry>() {
                @Override
                public void onSuccess(CallbackResult<Entry> result) {
                    putSignal.countDown();
                }
            });

            getClient(clientName).putAsync(entryPut, newVersion, handler);
        }
        waitForLatch(putSignal);

        for (int i = 0; i < MAX_KEYS; i++) {
            CallbackHandler<Boolean> handler = buildSuccessOnlyCallbackHandler(new KineticTestHelpers.SuccessAsyncHandler<Boolean>() {
                @Override
                public void onSuccess(CallbackResult<Boolean> result) {
                    deleteReturnList.add(result.getResult());
                    deleteSignal.countDown();
                }
            });

            byte[] key = toByteArray(keySList.get(i));
            byte[] value = toByteArray(valueSList.get(i));
            EntryMetadata entryMetadataDelete = new EntryMetadata();
            entryMetadataDelete.setVersion(newVersion);
            Entry deleteEntry = new Entry(key, value, entryMetadataDelete);

            getClient(clientName).deleteAsync(deleteEntry, handler);
        }
        waitForLatch(deleteSignal);

        assertEquals(MAX_KEYS, deleteReturnList.size());
        for (int i = 0; i < MAX_KEYS; i++) {
            assertTrue(deleteReturnList.get(i));
        }

        // verify the delete result
        for (int i = 0; i < MAX_KEYS; i++) {
            CallbackHandler<Entry> handler = buildSuccessOnlyCallbackHandler(new SuccessAsyncHandler<Entry>() {
                @Override
                public void onSuccess(CallbackResult<Entry> result) {
                    getReturnList.add(result.getResult());
                    getSignal.countDown();
                }
            });

            getClient(clientName).getAsync(toByteArray(keySList.get(i)),
                    handler);
        }
        waitForLatch(getSignal);

        for (int i = 0; i < MAX_KEYS; i++) {
            assertNull(getReturnList.get(i));
        }

        cleanKVGenData(MAX_KEYS, getClient(clientName));

        logger.info(this.testEndInfo());
    }

    /**
     * Test deleteAsync API with a entry has not existed in simulator/drive. The
     * test result should be false.
     * <p>
     * 
     * @throws KineticException
     *             if any internal error occurred.
     * @throws InterruptedException
     *             if thread is interrupted, or the specified waiting time
     *             elapses.
     */
    @Test(dataProvider = "transportProtocolOptions")
    public void testDeleteAsync_ReturnsNull_ForNonExistingKey(String clientName)
            throws KineticException, InterruptedException {
        byte[] newVersion = int32(0);
        final List<Boolean> deleteReturnList = new ArrayList<Boolean>();
        final CountDownLatch deleteSignal = new CountDownLatch(1);

        CallbackHandler<Boolean> handler = buildSuccessOnlyCallbackHandler(new SuccessAsyncHandler<Boolean>() {
            @Override
            public void onSuccess(CallbackResult<Boolean> result) {
                deleteReturnList.add(result.getResult());
                deleteSignal.countDown();
            }
        });

        byte[] key = toByteArray("@#$%2345");
        byte[] value = toByteArray("qwer");
        EntryMetadata entryMetadataDelete = new EntryMetadata();
        entryMetadataDelete.setVersion(newVersion);
        Entry deleteEntry = new Entry(key, value, entryMetadataDelete);

        // make sure the entry does not exist in db
        assertKeyNotFound(getClient(clientName), key);

        getClient(clientName).deleteAsync(deleteEntry, handler);

        waitForLatch(deleteSignal);

        assertFalse(deleteReturnList.get(0));

        logger.info(this.testEndInfo());
    }

    /**
     * Test getNextAsync API with a serial of entries. The entries have already
     * existed in simulator/drive. The test result should be successful.
     * <p>
     * 
     * @throws KineticException
     *             if any internal error occurred.
     * @throws InterruptedException
     *             if thread is interrupted, or the specified waiting time
     *             elapses.
     */
    @Test(dataProvider = "transportProtocolOptions")
    public void testGetNextAsync(String clientName)
            throws UnsupportedEncodingException, KineticException,
            InterruptedException {
        byte[] newVersion = int32(0);
        final List<Entry> putList = new ArrayList<Entry>(MAX_KEYS);
        final List<Entry> eGetNextList = new ArrayList<Entry>(MAX_KEYS);
        final List<Entry> eGetNextList1 = new ArrayList<Entry>();

        final CountDownLatch putSignal = new CountDownLatch(MAX_KEYS);
        final CountDownLatch getNextSignal = new CountDownLatch(MAX_KEYS - 1);
        final CountDownLatch getNextSignal1 = new CountDownLatch(1);

        List<String> keySList = new ArrayList<String>();
        List<String> valueSList = new ArrayList<String>();
        EntryMetadata entryMetadata = new EntryMetadata();

        cleanKVGenData(MAX_KEYS, getClient(clientName));

        for (int i = 0; i < MAX_KEYS; i++) {
            String keyS = kvGenerator.getNextKey();
            String valueS = kvGenerator.getValue(keyS);

            keySList.add(keyS);
            valueSList.add(valueS);

            entryMetadata.setVersion(newVersion);
            entryMetadata.setAlgorithm("SHA1");
        }

        entryMetadata = new EntryMetadata();
        for (int i = 0; i < MAX_KEYS; i++) {
            Entry entryPut = new Entry(toByteArray(keySList.get(i)),
                    toByteArray(valueSList.get(i)), entryMetadata);
            CallbackHandler<Entry> handler = buildSuccessOnlyCallbackHandler(new SuccessAsyncHandler<Entry>() {
                @Override
                public void onSuccess(CallbackResult<Entry> result) {
                    putList.add(result.getResult());
                    putSignal.countDown();
                }
            });

            getClient(clientName).putAsync(entryPut, newVersion, handler);
        }
        waitForLatch(putSignal);
        assertEquals(MAX_KEYS, putList.size());

        for (int i = 0; i < MAX_KEYS - 1; i++) {

            CallbackHandler<Entry> handler = buildSuccessOnlyCallbackHandler(new SuccessAsyncHandler<Entry>() {
                @Override
                public void onSuccess(CallbackResult<Entry> result) {
                    eGetNextList.add(result.getResult());

                    getNextSignal.countDown();
                }
            });
            getClient(clientName).getNextAsync(toByteArray(keySList.get(i)),
                    handler);
        }

        waitForLatch(getNextSignal);

        for (int i = 0; i < MAX_KEYS - 1; i++) {
            assertTrue(keySList.contains(new String(eGetNextList.get(i)
                    .getKey())));
            assertTrue(valueSList.contains(new String(eGetNextList.get(i)
                    .getValue())));
            assertTrue(Arrays.equals(newVersion, eGetNextList.get(i)
                    .getEntryMetadata().getVersion()));
        }
        assertEquals(MAX_KEYS - 1, eGetNextList.size());

        CallbackHandler<Entry> handler = buildSuccessOnlyCallbackHandler(new SuccessAsyncHandler<Entry>() {
            @Override
            public void onSuccess(CallbackResult<Entry> result) {
                eGetNextList1.add(result.getResult());
                getNextSignal1.countDown();
            }
        });
        getClient(clientName).getNextAsync(
                toByteArray(keySList.get(MAX_KEYS - 1)), handler);
        waitForLatch(getNextSignal1);
        assertNull(eGetNextList1.get(0));

        cleanKVGenData(MAX_KEYS, getClient(clientName));

        logger.info(this.testEndInfo());
    }

    /**
     * Test getNextAsync API with the last entry existed in simulator/drive. The
     * test result should be null.
     * <p>
     * 
     * @throws KineticException
     *             if any internal error occurred.
     * @throws InterruptedException
     *             if thread is interrupted, or the specified waiting time
     *             elapses.
     */
    @Test(dataProvider = "transportProtocolOptions")
    public void testGetNextAysnc_ReturnsNull_ForLastKey(String clientName)
            throws InterruptedException, KineticException {
        final List<Entry> eGetNextList = new ArrayList<Entry>();
        final CountDownLatch getNextSignal = new CountDownLatch(1);

        byte[] lastKey = toByteArray("lastkey");
        cleanNextData(lastKey, getClient(clientName));
        getClient(clientName).putForced(
                new Entry(lastKey, toByteArray("lastvalue")));

        CallbackHandler<Entry> handler = buildSuccessOnlyCallbackHandler(new SuccessAsyncHandler<Entry>() {
            @Override
            public void onSuccess(CallbackResult<Entry> result) {
                eGetNextList.add(result.getResult());
                getNextSignal.countDown();
            }
        });
        getClient(clientName).getNextAsync(lastKey, handler);
        waitForLatch(getNextSignal);
        assertNull(eGetNextList.get(0));

        getClient(clientName).deleteForced(lastKey);

        logger.info(this.testEndInfo());
    }

    /**
     * Test getPreviousAsync API with a serial of entries. The entries have
     * already existed in simulator/drive. The test result should be successful.
     * <p>
     * 
     * @throws KineticException
     *             if any internal error occurred.
     * @throws InterruptedException
     *             if thread is interrupted, or the specified waiting time
     *             elapses.
     */
    @Test(dataProvider = "transportProtocolOptions")
    public void testGetPreviousAsync_ReturnsPreviousValue(String clientName)
            throws UnsupportedEncodingException, KineticException,
            InterruptedException {
        byte[] newVersion = int32(0);
        final List<Entry> putList = new ArrayList<Entry>(MAX_KEYS);
        final List<Entry> eGetPreviousList = new ArrayList<Entry>(MAX_KEYS);

        final CountDownLatch putSignal = new CountDownLatch(MAX_KEYS);
        final CountDownLatch getPreviousSignal = new CountDownLatch(
                MAX_KEYS - 1);

        List<String> keySList = new ArrayList<String>();
        List<String> valueSList = new ArrayList<String>();
        EntryMetadata entryMetadata = new EntryMetadata();

        cleanKVGenData(MAX_KEYS, getClient(clientName));

        for (int i = 0; i < MAX_KEYS; i++) {
            String keyS = kvGenerator.getNextKey();
            String valueS = kvGenerator.getValue(keyS);

            keySList.add(keyS);
            valueSList.add(valueS);

            entryMetadata.setVersion(newVersion);
            entryMetadata.setAlgorithm("SHA1");
        }

        entryMetadata = new EntryMetadata();
        for (int i = 0; i < MAX_KEYS; i++) {
            Entry entryPut = new Entry(toByteArray(keySList.get(i)),
                    toByteArray(valueSList.get(i)), entryMetadata);
            CallbackHandler<Entry> handler = buildSuccessOnlyCallbackHandler(new SuccessAsyncHandler<Entry>() {
                @Override
                public void onSuccess(CallbackResult<Entry> result) {
                    putList.add(result.getResult());
                    putSignal.countDown();
                }
            });

            getClient(clientName).putAsync(entryPut, newVersion, handler);
        }

        waitForLatch(putSignal);
        assertEquals(MAX_KEYS, putList.size());

        for (int i = 1; i < MAX_KEYS; i++) {

            CallbackHandler<Entry> handler = buildSuccessOnlyCallbackHandler(new SuccessAsyncHandler<Entry>() {
                @Override
                public void onSuccess(CallbackResult<Entry> result) {
                    eGetPreviousList.add(result.getResult());

                    getPreviousSignal.countDown();
                }
            });
            getClient(clientName).getPreviousAsync(
                    toByteArray(keySList.get(i)), handler);
        }

        waitForLatch(getPreviousSignal);

        for (int i = 0; i < MAX_KEYS - 1; i++) {
            assertTrue(keySList.contains(new String(eGetPreviousList.get(i)
                    .getKey())));
            assertTrue(valueSList.contains(new String(eGetPreviousList.get(i)
                    .getValue())));
            assertTrue(Arrays.equals(newVersion, eGetPreviousList.get(i)
                    .getEntryMetadata().getVersion()));
        }
        assertEquals(MAX_KEYS - 1, eGetPreviousList.size());

        cleanKVGenData(MAX_KEYS, getClient(clientName));

        logger.info(this.testEndInfo());
    }

    /**
     * Test getPreviousAsync API with the first entry existed in
     * simulator/drive. The test result should be null.
     * <p>
     * 
     * @throws KineticException
     *             if any internal error occurred.
     * @throws InterruptedException
     *             if thread is interrupted, or the specified waiting time
     *             elapses.
     */
    @Test(dataProvider = "transportProtocolOptions")
    public void testGetPreviousAysnc_ReturnsNull_ForFirstKey(String clientName)
            throws InterruptedException, KineticException {
        final List<Entry> eGetPreviousList = new ArrayList<Entry>();
        final CountDownLatch getPreviousSignal = new CountDownLatch(1);

        byte[] firstKey = toByteArray("firstkey");
        cleanPreviousData(firstKey, getClient(clientName));
        getClient(clientName).putForced(
                new Entry(firstKey, toByteArray("firstvalue")));

        CallbackHandler<Entry> handler = buildSuccessOnlyCallbackHandler(new SuccessAsyncHandler<Entry>() {
            @Override
            public void onSuccess(CallbackResult<Entry> result) {
                eGetPreviousList.add(result.getResult());
                getPreviousSignal.countDown();
            }
        });
        getClient(clientName).getPreviousAsync(firstKey, handler);
        waitForLatch(getPreviousSignal);
        assertNull(eGetPreviousList.get(0));

        getClient(clientName).deleteForced(firstKey);

        logger.info(this.testEndInfo());
    }

    /**
     * Test getMetadataAsync API with a serial of entries. The entries have
     * already existed in simulator/drive. The test result should be successful.
     * <p>
     * 
     * @throws KineticException
     *             if any internal error occurred.
     * @throws InterruptedException
     *             if thread is interrupted, or the specified waiting time
     *             elapses.
     */
    @Test(dataProvider = "transportProtocolOptions")
    public void testGetMetadataAsync_ReturnsExistingKeys(String clientName)
            throws UnsupportedEncodingException, KineticException,
            InterruptedException {
        byte[] newVersion = int32(0);
        String algorithm = "SHA1";
        final List<EntryMetadata> getMetadataReturnList = new ArrayList<EntryMetadata>(
                MAX_KEYS);

        final CountDownLatch putSignal = new CountDownLatch(MAX_KEYS);
        final CountDownLatch getSignal = new CountDownLatch(MAX_KEYS);

        List<String> keySList = new ArrayList<String>();
        List<String> valueSList = new ArrayList<String>();
        List<String> tagSList = new ArrayList<String>();
        List<EntryMetadata> entryMetadataList = new ArrayList<EntryMetadata>();

        cleanKVGenData(MAX_KEYS, getClient(clientName));

        for (int i = 0; i < MAX_KEYS; i++) {
            String keyS = kvGenerator.getNextKey();
            String valueS = kvGenerator.getValue(keyS);
            String tagS = "tag" + i;

            keySList.add(keyS);
            valueSList.add(valueS);
            tagSList.add(tagS);

            EntryMetadata entryMetadata = new EntryMetadata();
            entryMetadata.setAlgorithm(algorithm);
            entryMetadata.setTag(tagS.getBytes());

            entryMetadataList.add(entryMetadata);

        }

        for (int i = 0; i < MAX_KEYS; i++) {
            Entry entryPut = new Entry(toByteArray(keySList.get(i)),
                    toByteArray(valueSList.get(i)), entryMetadataList.get(i));
            CallbackHandler<Entry> handler = buildSuccessOnlyCallbackHandler(new SuccessAsyncHandler<Entry>() {
                @Override
                public void onSuccess(CallbackResult<Entry> result) {
                    putSignal.countDown();
                }
            });

            getClient(clientName).putAsync(entryPut, newVersion, handler);
        }

        waitForLatch(putSignal);

        for (int i = 0; i < MAX_KEYS; i++) {
            CallbackHandler<EntryMetadata> handler = buildSuccessOnlyCallbackHandler(new SuccessAsyncHandler<EntryMetadata>() {
                @Override
                public void onSuccess(CallbackResult<EntryMetadata> result) {
                    getMetadataReturnList.add(result.getResult());
                    getSignal.countDown();
                }
            });

            getClient(clientName).getMetadataAsync(
                    toByteArray(keySList.get(i)), handler);
        }
        waitForLatch(getSignal);

        assertTrue(getMetadataReturnList.size() == MAX_KEYS);
        for (int i = 0; i < MAX_KEYS; i++) {
            assertTrue(algorithm.equals(getMetadataReturnList.get(i)
                    .getAlgorithm()));
            assertTrue(tagSList.contains(new String(getMetadataReturnList
                    .get(i).getTag())));
            assertArrayEquals(newVersion, getMetadataReturnList.get(i)
                    .getVersion());
        }

        cleanKVGenData(MAX_KEYS, getClient(clientName));

        logger.info(this.testEndInfo());
    }

    /**
     * Test getMetadataAsync API with a serial of entries. The entries have not
     * existed in simulator/drive. The test result should be null.
     * <p>
     * 
     * @throws KineticException
     *             if any internal error occurred.
     * @throws InterruptedException
     *             if thread is interrupted, or the specified waiting time
     *             elapses.
     */
    @Test(dataProvider = "transportProtocolOptions")
    public void testGetMetadataAsync_ReturnsNull_ForNonExistingKey(
            String clientName) throws KineticException, InterruptedException {
        final List<EntryMetadata> eGetMetadataList = new ArrayList<EntryMetadata>();
        final CountDownLatch latch = new CountDownLatch(1);
        
        byte[] nonExistingKey = toByteArray("qwertyuio");
        getClient(clientName).deleteForced(nonExistingKey);

        CallbackHandler<EntryMetadata> handler = buildSuccessOnlyCallbackHandler(new SuccessAsyncHandler<EntryMetadata>() {
            @Override
            public void onSuccess(CallbackResult<EntryMetadata> result) {
                eGetMetadataList.add(result.getResult());
                latch.countDown();
            }
        });

        getClient(clientName).getMetadataAsync(nonExistingKey, handler);

        waitForLatch(latch);
        assertNull(eGetMetadataList.get(0));

        getClient(clientName).deleteForced(nonExistingKey);

        logger.info(this.testEndInfo());

    }

    /**
     * Test getKeyRangeAsync API with a serial of entries. The entries have
     * already existed in simulator/drive. Both startKey and endKey are
     * inclusive. The test result should be successful.
     * <p>
     * 
     * @throws KineticException
     *             if any internal error occurred.
     * @throws InterruptedException
     *             if thread is interrupted, or the specified waiting time
     *             elapses.
     */
    @Test(dataProvider = "transportProtocolOptions")
    public void testGetKeyRangeAsync_ReturnsCorrectResults_ForStartEndInclusive(
            String clientName) throws KineticException,
            UnsupportedEncodingException, InterruptedException {
        List<byte[]> keys = Arrays.asList(toByteArray("00"), toByteArray("01"),
                toByteArray("02"), toByteArray("03"), toByteArray("04"),
                toByteArray("05"), toByteArray("06"), toByteArray("07"),
                toByteArray("08"), toByteArray("09"), toByteArray("10"),
                toByteArray("11"), toByteArray("12"), toByteArray("13"),
                toByteArray("14"));

        byte[] newVersion = int32(0);
        final List<Entry> putList = new ArrayList<Entry>();
        final List<byte[]> keyRangeList = new ArrayList<byte[]>();
        final CountDownLatch putSignal = new CountDownLatch(keys.size());
        final CountDownLatch getKeyRangeSignal = new CountDownLatch(1);
        
        cleanData(toByteArray("00"), toByteArray("14"), getClient(clientName));

        for (byte[] key : keys) {
            CallbackHandler<Entry> handler = buildSuccessOnlyCallbackHandler(new SuccessAsyncHandler<Entry>() {
                @Override
                public void onSuccess(CallbackResult<Entry> result) {
                    putList.add(result.getResult());
                    putSignal.countDown();
                }
            });

            getClient(clientName).putAsync(new Entry(key, key), newVersion,
                    handler);
        }

        waitForLatch(putSignal);
        assertEquals(keys.size(), putList.size());

        CallbackHandler<List<byte[]>> handler = buildSuccessOnlyCallbackHandler(new SuccessAsyncHandler<List<byte[]>>() {
            @Override
            public void onSuccess(CallbackResult<List<byte[]>> result) {
                for (byte[] key : result.getResult()) {
                    keyRangeList.add(key);
                }
                getKeyRangeSignal.countDown();
            }
        });

        getClient(clientName).getKeyRangeAsync(keys.get(0), true,
                keys.get(keys.size() - 1), true, keys.size(), handler);

        waitForLatch(getKeyRangeSignal);

        assertListOfArraysEqual(keys, keyRangeList);
        
        cleanData(toByteArray("00"), toByteArray("14"), getClient(clientName));

        logger.info(this.testEndInfo());
    }

    /**
     * Test getKeyRangeAsync API with a serial of entries. The entries have
     * already existed in simulator/drive. Both startKey and endKey are
     * exclusive. The test result should be successful.
     * <p>
     * 
     * @throws KineticException
     *             if any internal error occurred.
     * @throws InterruptedException
     *             if thread is interrupted, or the specified waiting time
     *             elapses.
     */
    @Test(dataProvider = "transportProtocolOptions")
    public void testGetKeyRangeAsync_ReturnsCorrectResults_ForStartEndExclusive(
            String clientName) throws KineticException,
            UnsupportedEncodingException, InterruptedException {
        List<byte[]> keys = Arrays.asList(toByteArray("00"), toByteArray("01"),
                toByteArray("02"), toByteArray("03"), toByteArray("04"),
                toByteArray("05"), toByteArray("06"), toByteArray("07"),
                toByteArray("08"), toByteArray("09"), toByteArray("10"),
                toByteArray("11"), toByteArray("12"), toByteArray("13"),
                toByteArray("14"));

        cleanData(toByteArray("00"), toByteArray("14"), getClient(clientName));
        
        byte[] newVersion = int32(0);
        final List<Entry> putList = new ArrayList<Entry>();
        final List<byte[]> keyRangeList = new ArrayList<byte[]>();
        final CountDownLatch putSignal = new CountDownLatch(keys.size());
        final CountDownLatch getKeyRangeSignal = new CountDownLatch(1);

        for (byte[] key : keys) {
            CallbackHandler<Entry> handler = buildSuccessOnlyCallbackHandler(new SuccessAsyncHandler<Entry>() {
                @Override
                public void onSuccess(CallbackResult<Entry> result) {
                    putList.add(result.getResult());
                    putSignal.countDown();
                }
            });

            getClient(clientName).putAsync(new Entry(key, key), newVersion,
                    handler);
        }

        waitForLatch(putSignal);
        assertEquals(keys.size(), putList.size());

        CallbackHandler<List<byte[]>> handler = buildSuccessOnlyCallbackHandler(new SuccessAsyncHandler<List<byte[]>>() {
            @Override
            public void onSuccess(CallbackResult<List<byte[]>> result) {
                for (byte[] key : result.getResult()) {
                    keyRangeList.add(key);
                }
                getKeyRangeSignal.countDown();
            }
        });

        getClient(clientName).getKeyRangeAsync(keys.get(0), false,
                keys.get(keys.size() - 1), false, keys.size(), handler);

        waitForLatch(getKeyRangeSignal);

        assertEquals(keys.size() - 2, keyRangeList.size());
        assertListOfArraysEqual(keys.subList(1, keys.size() - 1), keyRangeList);
        
        cleanData(toByteArray("00"), toByteArray("14"), getClient(clientName));

        logger.info(this.testEndInfo());
    }

    /**
     * Test getKeyRangeAsync API with a serial of entries. The entries have
     * already existed in simulator/drive. StartKey inclusive but endKey are
     * exclusive. The test result should be successful.
     * <p>
     * 
     * @throws KineticException
     *             if any internal error occurred.
     * @throws InterruptedException
     *             if thread is interrupted, or the specified waiting time
     *             elapses.
     */
    @Test(dataProvider = "transportProtocolOptions")
    public void testGetKeyRangeAsync_ReturnsCorrectResults_ForStartInclusiveEndExclusive(
            String clientName) throws KineticException,
            UnsupportedEncodingException, InterruptedException {
        List<byte[]> keys = Arrays.asList(toByteArray("00"), toByteArray("01"),
                toByteArray("02"), toByteArray("03"), toByteArray("04"),
                toByteArray("05"), toByteArray("06"), toByteArray("07"),
                toByteArray("08"), toByteArray("09"), toByteArray("10"),
                toByteArray("11"), toByteArray("12"), toByteArray("13"),
                toByteArray("14"));
        
        cleanData(toByteArray("00"), toByteArray("14"), getClient(clientName));

        byte[] newVersion = int32(0);
        final List<Entry> putList = new ArrayList<Entry>();
        final List<byte[]> keyRangeList = new ArrayList<byte[]>();
        final CountDownLatch putSignal = new CountDownLatch(keys.size());
        final CountDownLatch getKeyRangeSignal = new CountDownLatch(1);

        for (byte[] key : keys) {
            CallbackHandler<Entry> handler = buildSuccessOnlyCallbackHandler(new SuccessAsyncHandler<Entry>() {
                @Override
                public void onSuccess(CallbackResult<Entry> result) {
                    putList.add(result.getResult());
                    putSignal.countDown();
                }
            });

            getClient(clientName).putAsync(new Entry(key, key), newVersion,
                    handler);
        }

        waitForLatch(putSignal);
        assertEquals(keys.size(), putList.size());

        CallbackHandler<List<byte[]>> handler = buildSuccessOnlyCallbackHandler(new SuccessAsyncHandler<List<byte[]>>() {
            @Override
            public void onSuccess(CallbackResult<List<byte[]>> result) {
                for (byte[] key : result.getResult()) {
                    keyRangeList.add(key);
                }
                getKeyRangeSignal.countDown();
            }
        });

        getClient(clientName).getKeyRangeAsync(keys.get(0), true,
                keys.get(keys.size() - 1), false, keys.size(), handler);

        waitForLatch(getKeyRangeSignal);

        assertEquals(keys.size() - 1, keyRangeList.size());
        assertListOfArraysEqual(keys.subList(0, keys.size() - 1), keyRangeList);
        
        cleanData(toByteArray("00"), toByteArray("14"), getClient(clientName));

        logger.info(this.testEndInfo());
    }

    /**
     * Test getKeyRangeAsync API with a serial of entries. The entries have
     * already existed in simulator/drive. StartKey exclusive but endKey are
     * inclusive. The test result should be successful.
     * <p>
     * 
     * @throws KineticException
     *             if any internal error occurred.
     * @throws InterruptedException
     *             if thread is interrupted, or the specified waiting time
     *             elapses.
     */
    @Test(dataProvider = "transportProtocolOptions")
    public void testGetKeyRangeAsync_ReturnsCorrectResults_ForStartExclusiveEndInclusive(
            String clientName) throws KineticException,
            UnsupportedEncodingException, InterruptedException {
        List<byte[]> keys = Arrays.asList(toByteArray("00"), toByteArray("01"),
                toByteArray("02"), toByteArray("03"), toByteArray("04"),
                toByteArray("05"), toByteArray("06"), toByteArray("07"),
                toByteArray("08"), toByteArray("09"), toByteArray("10"),
                toByteArray("11"), toByteArray("12"), toByteArray("13"),
                toByteArray("14"));

        cleanData(toByteArray("00"), toByteArray("14"), getClient(clientName));
        
        byte[] newVersion = int32(0);
        final List<Entry> putList = new ArrayList<Entry>();
        final List<byte[]> keyRangeList = new ArrayList<byte[]>();
        final CountDownLatch putSignal = new CountDownLatch(keys.size());
        final CountDownLatch getKeyRangeSignal = new CountDownLatch(1);

        for (byte[] key : keys) {
            CallbackHandler<Entry> handler = buildSuccessOnlyCallbackHandler(new SuccessAsyncHandler<Entry>() {
                @Override
                public void onSuccess(CallbackResult<Entry> result) {
                    putList.add(result.getResult());
                    putSignal.countDown();
                }
            });

            getClient(clientName).putAsync(new Entry(key, key), newVersion,
                    handler);
        }

        waitForLatch(putSignal);
        assertEquals(keys.size(), putList.size());

        CallbackHandler<List<byte[]>> handler = buildSuccessOnlyCallbackHandler(new SuccessAsyncHandler<List<byte[]>>() {
            @Override
            public void onSuccess(CallbackResult<List<byte[]>> result) {
                for (byte[] key : result.getResult()) {
                    keyRangeList.add(key);
                }
                getKeyRangeSignal.countDown();
            }
        });

        getClient(clientName).getKeyRangeAsync(keys.get(0), false,
                keys.get(keys.size() - 1), true, keys.size(), handler);

        waitForLatch(getKeyRangeSignal);

        assertEquals(keys.size() - 1, keyRangeList.size());
        assertListOfArraysEqual(keys.subList(1, keys.size()), keyRangeList);
        
        cleanData(toByteArray("00"), toByteArray("14"), getClient(clientName));

        logger.info(this.testEndInfo());
    }

    /**
     * Test putForcedAsync with a serial of entries. The test result should be
     * successful and verify the result get is the same as put before
     * <p>
     * 
     * @throws KineticException
     *             if any internal error occurred.
     * @throws InterruptedException
     *             if thread is interrupted, or the specified waiting time
     *             elapses.
     */
    @Test(dataProvider = "transportProtocolOptions")
    public void testPutForcedAsync(String clientName)
            throws UnsupportedEncodingException, KineticException,
            InterruptedException {
        byte[] newVersion = int32(0);

        final CountDownLatch putSignal = new CountDownLatch(MAX_KEYS);

        List<String> keySList = new ArrayList<String>();
        List<String> valueSList = new ArrayList<String>();
        List<EntryMetadata> emdOfList = new ArrayList<EntryMetadata>();
        
        cleanKVGenData(MAX_KEYS, getClient(clientName));

        for (int i = 0; i < MAX_KEYS; i++) {
            String keyS = kvGenerator.getNextKey();
            String valueS = kvGenerator.getValue(keyS);

            keySList.add(keyS);
            valueSList.add(valueS);

            EntryMetadata entryMetadata = new EntryMetadata();
            entryMetadata.setTag(keyS.getBytes());
            entryMetadata.setAlgorithm("SHA1");

            emdOfList.add(entryMetadata);
        }

        for (int i = 0; i < MAX_KEYS; i++) {
            Entry entryPut = new Entry(toByteArray(keySList.get(i)),
                    toByteArray(valueSList.get(i)), emdOfList.get(i));
            CallbackHandler<Entry> handler = buildSuccessOnlyCallbackHandler(new KineticTestHelpers.SuccessAsyncHandler<Entry>() {
                @Override
                public void onSuccess(CallbackResult<Entry> result) {
                    putSignal.countDown();
                }
            });

            getClient(clientName).putAsync(entryPut, newVersion, handler);
        }

        waitForLatch(putSignal);

        final List<Entry> putReturnList = new ArrayList<Entry>(MAX_KEYS);
        final CountDownLatch putForcedSignal = new CountDownLatch(MAX_KEYS);

        byte[] forcedVersion = int32(1);
        emdOfList = new ArrayList<EntryMetadata>();
        byte[] tag = toByteArray("tag");
        for (int i = 0; i < MAX_KEYS; i++) {

            EntryMetadata entryMetadata = new EntryMetadata();
            entryMetadata.setVersion(forcedVersion);
            entryMetadata.setTag(tag);
            entryMetadata.setAlgorithm("SHA1");

            emdOfList.add(entryMetadata);
        }

        for (int i = 0; i < MAX_KEYS; i++) {
            Entry entryPut = new Entry(toByteArray(keySList.get(i)),
                    toByteArray(valueSList.get(i)), emdOfList.get(i));
            CallbackHandler<Entry> handler = buildSuccessOnlyCallbackHandler(new KineticTestHelpers.SuccessAsyncHandler<Entry>() {
                @Override
                public void onSuccess(CallbackResult<Entry> result) {
                    putReturnList.add(result.getResult());
                    putForcedSignal.countDown();
                }
            });

            getClient(clientName).putForcedAsync(entryPut, handler);
        }

        waitForLatch(putForcedSignal);
        assertEquals(MAX_KEYS, putReturnList.size());

        for (int i = 0; i < MAX_KEYS; i++) {
            Entry getReturned = getClient(clientName).get(
                    toByteArray(keySList.get(i)));

            assertTrue(keySList.contains(new String(getReturned.getKey())));
            assertTrue(valueSList.contains(new String(getReturned.getValue())));
            assertTrue(Arrays.equals(forcedVersion, getReturned
                    .getEntryMetadata().getVersion()));
            assertTrue(Arrays.equals(tag, getReturned.getEntryMetadata()
                    .getTag()));
        }
        
        cleanKVGenData(MAX_KEYS, getClient(clientName));

        logger.info(this.testEndInfo());
    }

    /**
     * Test deleteForcedAsync with a serial of entries. The entries have existed
     * in simulator/drive.The test result should be successful and verify the
     * result get is null.
     * <p>
     * 
     * @throws KineticException
     *             if any internal error occurred.
     * @throws InterruptedException
     *             if thread is interrupted, or the specified waiting time
     *             elapses.
     */
    @Test(dataProvider = "transportProtocolOptions")
    public void testDeleteForcedAsync_RemovesExistingKeys(String clientName)
            throws UnsupportedEncodingException, KineticException,
            InterruptedException {
        byte[] newVersion = int32(0);

        final CountDownLatch putSignal = new CountDownLatch(MAX_KEYS);

        List<String> keySList = new ArrayList<String>();
        List<String> valueSList = new ArrayList<String>();
        List<EntryMetadata> emdOfList = new ArrayList<EntryMetadata>();
        
        cleanKVGenData(MAX_KEYS, getClient(clientName));

        for (int i = 0; i < MAX_KEYS; i++) {
            String keyS = kvGenerator.getNextKey();
            String valueS = kvGenerator.getValue(keyS);

            keySList.add(keyS);
            valueSList.add(valueS);

            EntryMetadata entryMetadata = new EntryMetadata();
            entryMetadata.setTag(keyS.getBytes());
            entryMetadata.setAlgorithm("SHA1");

            emdOfList.add(entryMetadata);
        }

        for (int i = 0; i < MAX_KEYS; i++) {
            Entry entryPut = new Entry(toByteArray(keySList.get(i)),
                    toByteArray(valueSList.get(i)), emdOfList.get(i));
            CallbackHandler<Entry> handler = buildSuccessOnlyCallbackHandler(new KineticTestHelpers.SuccessAsyncHandler<Entry>() {
                @Override
                public void onSuccess(CallbackResult<Entry> result) {
                    putSignal.countDown();
                }
            });

            getClient(clientName).putAsync(entryPut, newVersion, handler);
        }

        waitForLatch(putSignal);

        final List<Boolean> deleteReturnList = new ArrayList<Boolean>(MAX_KEYS);
        final CountDownLatch deleteForcedSignal = new CountDownLatch(MAX_KEYS);

        for (int i = 0; i < MAX_KEYS; i++) {
            byte[] key = toByteArray(keySList.get(i));

            CallbackHandler<Boolean> handler = buildSuccessOnlyCallbackHandler(new KineticTestHelpers.SuccessAsyncHandler<Boolean>() {
                @Override
                public void onSuccess(CallbackResult<Boolean> result) {
                    deleteReturnList.add(result.getResult());
                    deleteForcedSignal.countDown();
                }
            });

            getClient(clientName).deleteForcedAsync(key, handler);
        }

        waitForLatch(deleteForcedSignal);
        assertEquals(MAX_KEYS, deleteReturnList.size());

        for (int i = 0; i < MAX_KEYS; i++) {
            assertTrue(deleteReturnList.get(i));

            Entry getReturned = getClient(clientName).get(
                    toByteArray(keySList.get(i)));

            assertNull(getReturned);
        }
        
        cleanKVGenData(MAX_KEYS, getClient(clientName));

        logger.info(this.testEndInfo());
    }

    /**
     * Test deleteForcedAsync with a serial of entries. The entries have not
     * existed in simulator/drive.The test result should be successful and
     * verify the result get is null.
     * <p>
     * 
     * @throws KineticException
     *             if any internal error occurred.
     * @throws InterruptedException
     *             if thread is interrupted, or the specified waiting time
     *             elapses.
     */
    @Test(dataProvider = "transportProtocolOptions")
    public void testDeleteForcedAsync_Succeeds_IfKeysDontExist(String clientName)
            throws UnsupportedEncodingException, KineticException,
            InterruptedException {
        List<String> keySList = new ArrayList<String>();
        
        cleanKVGenData(MAX_KEYS, getClient(clientName));

        for (int i = 0; i < MAX_KEYS; i++) {
            String keyS = kvGenerator.getNextKey();

            keySList.add(keyS);
        }

        final List<Boolean> deleteReturnList = new ArrayList<Boolean>(MAX_KEYS);
        final CountDownLatch deleteForcedSignal = new CountDownLatch(MAX_KEYS);

        for (int i = 0; i < MAX_KEYS; i++) {
            byte[] key = toByteArray(keySList.get(i));

            CallbackHandler<Boolean> handler = buildSuccessOnlyCallbackHandler(new KineticTestHelpers.SuccessAsyncHandler<Boolean>() {
                @Override
                public void onSuccess(CallbackResult<Boolean> result) {
                    deleteReturnList.add(result.getResult());
                    deleteForcedSignal.countDown();
                }
            });

            getClient(clientName).deleteForcedAsync(key, handler);
        }

        waitForLatch(deleteForcedSignal);
        assertEquals(MAX_KEYS, deleteReturnList.size());

        for (int i = 0; i < MAX_KEYS; i++) {
            assertTrue(deleteReturnList.get(i));

            Entry getReturned = getClient(clientName).get(
                    toByteArray(keySList.get(i)));

            assertNull(getReturned);
        }
        
        cleanKVGenData(MAX_KEYS, getClient(clientName));

        logger.info(this.testEndInfo());
    }
}
