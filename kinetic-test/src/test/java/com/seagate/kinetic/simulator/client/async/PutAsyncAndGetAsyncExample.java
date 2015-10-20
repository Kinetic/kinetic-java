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
package com.seagate.kinetic.simulator.client.async;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import kinetic.client.AsyncKineticException;
import kinetic.client.CallbackHandler;
import kinetic.client.CallbackResult;
import kinetic.client.ClientConfiguration;
import kinetic.client.Entry;
import kinetic.client.KineticException;

import com.seagate.kinetic.client.internal.DefaultKineticClient;

/**
 * PutAsync and GetAsync example.
 * <p>
 * PutAsync n entries to simulator/drive, then GetAsync to check whether the
 * entries are correctly stored in the system.<br>
 * 1. PutAsync n entries, add each entry and putAsync callbackHandler to a hash
 * map.<br>
 * 2. GetAsync the n entries, in each getAsync callbackHandler, remove the item
 * in hash map with a entry as same as returned entry.<br>
 * 3. Wait and check whether the hash map is empty.
 */
public class PutAsyncAndGetAsyncExample {
    private static final String UTF8 = "UTF-8";

    private static final int ENTRIES_COUNT = 1000;
    private static final int SLEEP_TIME_IN_MILLISECOND = 10;
    private static final int MAX_WAIT_TIME_IN_MILLISECOND = 10000;

    private ClientConfiguration clientConfig = null;
    private DefaultKineticClient kineticClient = null;

    public PutAsyncAndGetAsyncExample() throws Exception {
        this.setUp();
        this.runExample();
    }

    /**
     * Initialize Kinetic client
     * <p>
     * 
     * @throws KineticException
     *             if any internal error occurred.
     */
    public void setUp() throws KineticException {
        this.clientConfig = new ClientConfiguration(System.getProperties());
        kineticClient = new DefaultKineticClient(this.clientConfig);
    }

    /**
     * Instance of run example.<br>
     * 1. PutAsync entries and add them into a hash map.<br>
     * 2. GetAsync entries and check the returned entry whether as same as the
     * put entry, if same, remove the item from hash map.<br>
     * 3. Wait for the hash map empty in a max limited time.
     * <p>
     * 
     * @throws Exception
     *             if any internal error occurred.
     */
    public void runExample() throws Exception {
        Map<String, MyPutAsyncCallbackHandler> handlers = new HashMap<String, MyPutAsyncCallbackHandler>();

        String key = null;
        String value = null;
        Entry entry = null;
        String entryAsString = null;

        // put entries
        for (int i = 0; i < ENTRIES_COUNT; i++) {
            key = "KEY" + i;
            value = "VALUE" + i;
            entryAsString = key + ":" + value;
            entry = new Entry(key.getBytes(UTF8), value.getBytes(UTF8));

            MyPutAsyncCallbackHandler putHandler = new MyPutAsyncCallbackHandler();

            this.kineticClient.putAsync(entry, null, putHandler);

            // Add each put entry and putAsync call back handler into a hash
            // map.
            handlers.put(entryAsString, putHandler);

        }

        // get entries
        for (int i = 0; i < ENTRIES_COUNT; i++) {
            key = "KEY" + i;
            value = "VALUE" + i;
            entry = new Entry(key.getBytes(UTF8), value.getBytes(UTF8));

            // check that whether get returned entry equals put entry in each
            // getCalbackHandler.
            MyGetAsyncCheckCallbackHandler getHandler = new MyGetAsyncCheckCallbackHandler(
                    entry, handlers);
            this.kineticClient.getAsync(key.getBytes(UTF8), getHandler);

        }

        // wait and check the hash map whether empty in a max limited time
        int max_wait_time = 0;
        while (handlers.size() > 0
                && max_wait_time < MAX_WAIT_TIME_IN_MILLISECOND) {
            max_wait_time += SLEEP_TIME_IN_MILLISECOND;
            Thread.sleep(SLEEP_TIME_IN_MILLISECOND);
        }

        if (handlers.size() == 0) {
            System.out.println("Checking: Passed.");
        } else {
            System.out.println("Checking: Failed, HashMap size:"
                    + handlers.size());
        }
    }

    /**
     * Close kinetic client
     * <p>
     * 
     * @throws KineticException
     *             if any internal error occurred.
     */
    public void close() throws KineticException {
        this.kineticClient.close();
    }

    /**
     * Run a instance of PutAsyncAndGetAsyncExample.
     * <p>
     * 
     * @throws Exception
     *             if any internal error occurred.
     */
    public static void main(String[] args) throws Exception {
        @SuppressWarnings("unused")
        PutAsyncAndGetAsyncExample example = new PutAsyncAndGetAsyncExample();
    }

}

/**
 * GetAsync call back handler.
 * <p>
 * GetAsync callbackHandler to check whether the returned entry is the same as
 * put entry.<br>
 * 1. If getAsync success, compare the returned entry with put entry, if same,
 * remove the get returned entry from hash map.<br>
 * 2. If getAsync failed, throw runtime exception.
 */
class MyGetAsyncCheckCallbackHandler implements CallbackHandler<Entry> {
    private Entry entry;
    private Map<String, MyPutAsyncCallbackHandler> handlers;

    public MyGetAsyncCheckCallbackHandler(Entry entry,
            Map<String, MyPutAsyncCallbackHandler> handlers) {
        this.entry = entry;
        this.handlers = handlers;
    }

    @Override
    public void onSuccess(CallbackResult<Entry> result) {
        Entry entryGet = result.getResult();

        String entryGetAsString = new String(entryGet.getKey()) + ":"
                + new String(entry.getValue());

        // check whether the entry I got is as same as the previous put
        if (Arrays.equals(entry.getKey(), entryGet.getKey())
                && Arrays.equals(entry.getValue(), entryGet.getValue())) {
            // remove the entry from put hash map
            handlers.remove(entryGetAsString);

            System.out.println("Entry " + entryGetAsString
                    + " passed the checking.");
        } else {
            throw new RuntimeException(
                    "Can't get the right entry back for the "
                            + new String(entry.getKey()) + " put");
        }
    }

    @Override
    public void onError(AsyncKineticException exception) {
        throw new RuntimeException(exception.getMessage());
    }
}

/**
 * PutAsync call back handler.
 * <p>
 * 1. If getAsync success, do nothing.<br>
 * 2. If getAsync failed, throw runtime exception.
 */
class MyPutAsyncCallbackHandler implements CallbackHandler<Entry> {

    @Override
    public void onSuccess(CallbackResult<Entry> result) {
        // do nothing
    }

    @Override
    public void onError(AsyncKineticException exception) {
        throw new RuntimeException(exception.getMessage());
    }
}
