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

package com.seagate.kinetic.example.client.async;

import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

import kinetic.client.ClientConfiguration;
import kinetic.client.Entry;
import kinetic.client.KineticClient;
import kinetic.client.KineticClientFactory;
import kinetic.client.KineticException;

/**
 * Kinetic Asynchronous API with SSL/TLS usage sample code.
 * <p>
 * This example assumes that a simulator is running on the localhost:8443
 * (SSL/TLS port)
 * <p>
 * This example performs the following operations in a loop
 * <ul>
 * <li>1. start Kinetic client with SSL/TLS enabled.
 * <li>2. (forced) put entry asynchronously ("hello", "world");
 * <li>3. get entry asynchronously ("hello");
 * <li>4. (forced) delete entry asynchronously ("hello");
 * <li>5. repeat step #2 to step #4 until reached MAX_ITERATION (1000).
 * <li>6. wait for operation to complete
 * <li>7. close Kinetic client.
 * </ul>
 */
public class AsyncApiUsage {

	// iteration to put messages to the kinetic store
	public static final int MAX_ITERATION = 1000;

	// String to byte[] encoding
	public static final String UTF8 = "utf8";

	// kinetic client
	private KineticClient client = null;

	// cache to store async put entry
	private final Map<String, Entry> map = new ConcurrentHashMap<String, Entry>();

	// async put callback handler
	private PutAsyncCallbackHandler putCallbackHandler = null;

	// async delete callback handler
	private DeleteAsyncCallbackHandler deleteCallbackHandler = null;

	// async get callback handler
	private GetCallbackHandler getCallbackHandler = null;

	// count down signal for async put/get/delete
	private CountDownLatch doneSignal = null;

	/**
	 * Start the async API usage example.
	 * 
	 * @throws KineticException
	 *             if any Kinetic internal error occurred.
	 * @throws InterruptedException
	 *             if the example is interrupted before it is completed.
	 */
	public void runExample() throws KineticException, InterruptedException {

		// Client configuration and initialization
		ClientConfiguration clientConfig = new ClientConfiguration();

		// set use SSL/TLS
		clientConfig.setUseSsl(true);

		// set SSL/TLS service port
		clientConfig.setPort(8443);

		client = KineticClientFactory.createInstance(clientConfig);

		// count down signal -- total 3 asynchronous operations
		doneSignal = new CountDownLatch(3 * MAX_ITERATION);

		// async put putCallbackHandler handler
		putCallbackHandler = new PutAsyncCallbackHandler(this);

		// async get callback handler
		getCallbackHandler = new GetCallbackHandler(this);

		// async delete putCallbackHandler handler
		deleteCallbackHandler = new DeleteAsyncCallbackHandler(this);

		// put a simple entry asynchronously, this starts async loops.
		this.putSimpleEntry();

		// wait for async put/get/delete to complete
		this.doneSignal.await();

		// close kinetic client
		this.client.close();
	}

	/**
	 * put a simple entry asynchronously
	 * 
	 * @throws KineticException
	 *             any kinetic internal error occurred
	 */
	private void putSimpleEntry() {
		// create a simple key/value entry
		Entry simpleEntry = createSimpleEntry("hello", "world");

		/**
		 * added the entry to cache, this will be deleted when received
		 * putCallbackHandler confirmation.
		 */
		this.map.put(bytesToString(simpleEntry.getKey()), simpleEntry);

		/**
		 * put forced asynchronously. Upon completion, the
		 * PutAsyncCallbackHandler#onSuccess() is invoked by kinetic client
		 * runtime.
		 */
		try {
			client.putForcedAsync(simpleEntry, putCallbackHandler);
		} catch (KineticException e) {
			e.printStackTrace();
		}
	}

	/**
	 * utility method to create a simple entry.
	 * 
	 * @param key
	 *            string key
	 * @param value
	 *            string value
	 * @return kinetic entry
	 * 
	 * @throws UnsupportedEncodingException
	 */
	public static Entry createSimpleEntry(String key, String value) {

		Entry entry = null;

		try {
			// get key bytes
			byte[] keyBytes = key.getBytes(UTF8);

			// get value bytes
			byte[] valueBytes = value.getBytes(UTF8);

			// create entry
			entry = new Entry(keyBytes, valueBytes);

			// return simple entry
			return entry;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return entry;
	}

	/**
	 * This method is called by the PutAsyncCallbackHandler when received put
	 * callback successfully.
	 * 
	 * @param entry
	 *            the entry that was put successfully to the kinetic store.
	 */
	public void asyncPutInStore(Entry entry) {

		// count down operation for put successfully
		this.doneSignal.countDown();

		try {
			// get entry form cache, the entry has successfully put in store
			Entry eInMap = map.get(bytesToString(entry.getKey()));

			if (eInMap == null) {
				System.out.println("cannot find entry from cache ...");
			} else {
				System.out
				.println("put successful, found entry from cache, calling get async ...");

				// get the entry asynchronously from kinetic store
				this.client.getAsync(entry.getKey(), getCallbackHandler);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * This method is called by the GetCallbackHandler when received an
	 * asynchronous get entry.
	 * 
	 * @param entry
	 *            received from asynchronous get callback handler.
	 */
	public void asyncGetReceived(Entry entry) {

		// get entry form cache, the entry has successfully put in store
		Entry eInMap = map.get(bytesToString(entry.getKey()));

		if (eInMap == null) {
			System.out.println("cannot find entry from cache ...");
		} else {
			// count down signal for async get operation
			this.doneSignal.countDown();

			System.out
			.println("get async received entry, calling async delete ...");

			// delete the entry
			this.deleteEntry(entry.getKey());
		}
	}

	/**
	 * async delete an entry.
	 * 
	 * @param key
	 *            the key of the entry to be deleted asynchronously
	 */
	private void deleteEntry(byte[] key) {
		try {
			// async forced delete
			this.client.deleteForcedAsync(key, this.deleteCallbackHandler);
		} catch (KineticException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * This method is called when the DeleteAsyncCallbackHandler received
	 * confirmation that an entry is deleted.
	 */
	public void asyncDeletedInStore(byte[] key) {

		// remove entry form cache, the entry has successfully put in store
		Entry eInMap = map.remove(bytesToString(key));

		if (eInMap == null) {
			System.out.println("error remove entry from cache ...");
		} else {
			// count down so that we can exit
			this.doneSignal.countDown();

			System.out.println("async delete successfully ..., done counter="
					+ this.doneSignal.getCount());
		}

		if (this.doneSignal.getCount() > 0) {
			// a new iteration of async put/get/delete
			this.putSimpleEntry();
		}

	}

	/**
	 * convert byte[] to string using UTF8 encoding.
	 * 
	 * @param bytes
	 *            byte[] to be converted to string.
	 * 
	 * @return the string representation of the specified byte[]
	 */
	private static String bytesToString(byte[] bytes) {

		try {
			return new String(bytes, UTF8);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

	public static void main(String[] args) throws KineticException,
	InterruptedException {
		AsyncApiUsage asyncUsage = new AsyncApiUsage();

		asyncUsage.runExample();
	}

}
