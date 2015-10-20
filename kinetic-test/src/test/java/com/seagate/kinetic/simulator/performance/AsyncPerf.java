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
package com.seagate.kinetic.simulator.performance;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;
import org.testng.Assert;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import kinetic.client.CallbackHandler;
import kinetic.client.ClientConfiguration;
import kinetic.client.Entry;
import kinetic.client.EntryMetadata;
import kinetic.client.KineticClient;
import kinetic.client.KineticClientFactory;
import kinetic.client.KineticException;

import com.google.protobuf.ByteString;
import com.seagate.kinetic.client.internal.DefaultKineticClient;
import com.seagate.kinetic.client.internal.MessageFactory;
import com.seagate.kinetic.common.lib.KineticMessage;
import com.seagate.kinetic.proto.Kinetic.Command;

import com.seagate.kinetic.proto.Kinetic.Command.KeyValue;
import com.seagate.kinetic.proto.Kinetic.Command.MessageType;

/**
 *
 * Async put performance test
 * <p>
 *
 * @author Chenchong(Emma) Li
 *
 */
public class AsyncPerf {
	private static final Logger logger = Logger.getLogger(AsyncPerf.class
			.getName());

	private static ClientConfiguration clientConfig = null;
	private static KVGenerator kvGenerator = null;
	private static final String INIT_VERSION = "0";
	private static final int WARM_UP_COUNT = 1000;
	private static int VALUE_SIZE = 1024;
	private static int OPERATE_COUNT = 10000;
	private static int REPEAT_COUNT = 10;
	private static int PORT = 8123;

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void main(String[] args) {
		if (args.length != 0 && args.length != 4) {
			System.out.println("Parameters error!!!");
			System.out.println("Usage:");
			System.out
			.println("AsyncPerf [Data_Size] [Operation_Count] [Repeat_Count] [Client_Port]");
			System.out.println("Welcome to try again.");
			return;

		}
		if (args.length > 0 && args.length == 4) {
			VALUE_SIZE = Integer.parseInt(args[0]);
			OPERATE_COUNT = Integer.parseInt(args[1]);
			REPEAT_COUNT = Integer.parseInt(args[2]);
			PORT = Integer.parseInt(args[3]);
			System.out.println("Data_Size=" + VALUE_SIZE);
			System.out.println("Operation_Count=" + OPERATE_COUNT);
			System.out.println("Repeat_Count=" + REPEAT_COUNT);
			System.out.println("Client_Port=" + PORT);

		}

		byte[] value = ByteBuffer.allocate(VALUE_SIZE).array();

		try {
			kvGenerator = new KVGenerator();
			clientConfig = new ClientConfiguration(System.getProperties());
			clientConfig.setPort(PORT);

			// warm up
			DefaultKineticClient kineticClient = new DefaultKineticClient(
					clientConfig);
			kvGenerator.reset();
			LinkedBlockingQueue<KineticMessage> lbq = new LinkedBlockingQueue<KineticMessage>();
			for (int warm = 0; warm < WARM_UP_COUNT; warm++) {

				String key = kvGenerator.getNextKey();
				// String value = kvGenerator.getValue(key);

				// logger.info("adding warm data index=" + warm);

				EntryMetadata entryMetadata = new EntryMetadata();
				Entry versioned = new Entry(toByteArray(key), value,
						entryMetadata);
				
				KineticMessage km = createPutMessage(versioned,
						toByteArray(INIT_VERSION));

				//KineticMessage km = new KineticMessage();
				//km.setMessage(m);

				CallbackHandler callback = new MyCallBack(lbq, km);

				kineticClient.requestAsync(km, callback);
			}
			while (lbq.size() != WARM_UP_COUNT) {
			}
			kineticClient.close();
			clear(WARM_UP_COUNT);
			logger.info("Warm up number=" + WARM_UP_COUNT
					+ ", warm up end, perf test begin!");

			// Test begin
			long totalRepeatAsyncTime = 0;
			for (int i = 0; i < REPEAT_COUNT; i++) {
				kineticClient = new DefaultKineticClient(clientConfig);

				kvGenerator.reset();
				lbq = new LinkedBlockingQueue<KineticMessage>();

				long asyncStartTime = System.currentTimeMillis();
				for (int j = 0; j < OPERATE_COUNT; j++) {
					String key = kvGenerator.getNextKey();
					// String value = kvGenerator.getValue(key);

					// logger.info("adding data index=" + j);

					EntryMetadata entryMetadata = new EntryMetadata();
					Entry versioned = new Entry(toByteArray(key), value,
							entryMetadata);
					
					KineticMessage km = createPutMessage(versioned,
							toByteArray(INIT_VERSION));

					//KineticMessage km = new KineticMessage();
					//km.setMessage(m);

					CallbackHandler callback = new MyCallBack(lbq, km);

					kineticClient.requestAsync(km, callback);
				}

				while (lbq.size() != OPERATE_COUNT) {
				}

				long asyncEndTime = System.currentTimeMillis();
				long totalAsyncTime = asyncEndTime - asyncStartTime;
				logger.info("The " + i
						+ " times async, total operation number="
						+ OPERATE_COUNT + ", take time=" + totalAsyncTime
						+ "ms");
				totalRepeatAsyncTime += totalAsyncTime;
				clear(OPERATE_COUNT);
				kineticClient.close();
			}

			long averageAsynTime = totalRepeatAsyncTime / REPEAT_COUNT;
			long messagePerSec = OPERATE_COUNT * 1000 / averageAsynTime;
			logger.info("Repeat " + REPEAT_COUNT + " times, "
					+ "async , total operation number=" + OPERATE_COUNT
					+ ", take average time=" + averageAsynTime + "ms");
			logger.info("Async put (" + VALUE_SIZE + "B value) average = "
					+ messagePerSec + " ops/sec");

		} catch (UnsupportedEncodingException e) {
			logger.log(Level.SEVERE, e.getMessage());
		} catch (KineticException e) {
			logger.log(Level.SEVERE, e.getMessage());
		}

	}

	private static KineticMessage createPutMessage(Entry versioned,
			byte[] newVersion) {
	    
	    KineticMessage km = MessageFactory.createKineticMessageWithBuilder();
	    
		//Message.Builder message = Message.newBuilder();
		//KineticMessage km = new KineticMessage();
		//km.setMessage(message);

	    Command.Builder commandBuilder = (Command.Builder) km.getCommand();
	    
		commandBuilder.getHeaderBuilder()
		.setMessageType(MessageType.PUT);
		
		KeyValue.Builder kv = commandBuilder.getBodyBuilder()
				.getKeyValueBuilder();

		kv.setKey(ByteString.copyFrom(versioned.getKey()));

		if (newVersion != null) {
			kv.setNewVersion(ByteString.copyFrom(newVersion));
		}

		if (versioned.getValue() != null) {
			// message.setValue(ByteString.copyFrom(versioned.getValue()));
			km.setValue(versioned.getValue());
		}

		return km;
	}

	private static void clear(int count) throws KineticException,
	UnsupportedEncodingException {

		KineticClient kineticClient = KineticClientFactory
				.createInstance(clientConfig);
		String key;
		kvGenerator.reset();
		for (int i = 0; i < count; i++) {
			key = kvGenerator.getNextKey();
			delete(kineticClient, toByteArray(key));
		}
		kineticClient.close();
	}

	// clear all the versonedEntry
	private static void delete(KineticClient kineticClient, byte[] key) {
		Entry versionedEntry = null;
		try {
			versionedEntry = kineticClient.get(key);
		} catch (KineticException e) {
			Assert.fail("get key " + new String(key) + " failed, " + e.getMessage());
		} catch (Exception e) {
			Assert.fail("get key " + new String(key) + " failed, " + e.getMessage());
		}

		if (null != versionedEntry && null != versionedEntry.getKey()) {
			try {
				assertTrue(kineticClient.delete(versionedEntry));
				assertEquals(null, kineticClient.get(key));
			} catch (KineticException e) {
				Assert.fail("delete key " + new String(key) + " failed, "
						+ e.getMessage());
			} catch (Exception e) {
				Assert.fail("delete key " + new String(key) + " failed, "
						+ e.getMessage());
			}
		} else {
			logger.info("no key: " + new String(key));
		}
	}

	// generator the key value, support reset and getNextKey and getValue
	static class KVGenerator {
		private String keyPrefix = "key";
		private String valuePrefix = "value";
		private int start = 0;
		private int current = 0;
		private final int alignLength = ("" + Integer.MAX_VALUE).length() + 1;

		public KVGenerator() {

		}

		private String align(int id) {
			String idAsString = "" + id;
			int length = idAsString.length();
			for (int i = 0; i < alignLength - length; i++) {
				idAsString = "0" + idAsString;
			}
			return idAsString;
		}

		public KVGenerator(int start) {
			this.start = start;
			this.current = start;
		}

		public KVGenerator(String keyPrefix, String valuePrefix, int start) {
			this.keyPrefix = keyPrefix;
			this.valuePrefix = valuePrefix;
			this.start = start;
			this.current = start;
		}

		public void reset() {
			this.current = start;
		}

		public synchronized String getNextKey() {
			if (current >= Integer.MAX_VALUE) {
				throw new RuntimeException("out of keys");
			}
			return keyPrefix + align(current++);
		}

		public String getValue(String key) {
			int keyId = Integer.parseInt(key.replaceAll(keyPrefix, ""));
			return valuePrefix + keyId;
		}
	}

	// convert String to byte[]
	private static byte[] toByteArray(String s)
			throws UnsupportedEncodingException {
		return s.getBytes("utf8");
	}
}
