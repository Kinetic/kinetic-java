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

import org.testng.annotations.Test;
import org.testng.Assert;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;

import kinetic.client.ClientConfiguration;
import kinetic.client.Entry;
import kinetic.client.EntryMetadata;
import kinetic.client.KineticClient;
import kinetic.client.KineticClientFactory;
import kinetic.client.KineticException;
import kinetic.simulator.KineticSimulator;
import kinetic.simulator.SimulatorConfiguration;

/**
 * 
 * performance test, include nio client/server, sync and async permute.
 * <p>
 * 
 * @author Chenchong(Emma) Li
 * 
 */
public class Performance {
	private static final Logger logger = Logger.getLogger(Performance.class
			.getName());

	private static final String INIT_VERSION = "0";
	private static final int WARM_UP_COUNT = 5000;
	private static ClientConfiguration clientConfig = null;
	private static SimulatorConfiguration serverConfig = null;
	private static KineticSimulator kineticServer = null;
	private static KineticClient kineticClient = null;
	private static KVGenerator kvGenerator = null;
	private static CountDownLatch signal = null;
	private static List<String> testResultInfo = null;
	private static byte[] VALUE = null;
	private static int VALUE_SIZE = 1024;

	private static int OPERATE_COUNT = 10000;
	private static int REPEAT_COUNT = 10;
	private static int PORT = 8123;

	public static void main(String[] args) throws InterruptedException {
		if (args.length != 0 && args.length != 4) {
			System.out.println("Parameters error!!!");
			System.out.println("Usage:");
			System.out
					.println("PerfTest [Data_Size] [Operation_Count] [Repeat_Count] [Client_Port]");
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

		VALUE = ByteBuffer.allocate(VALUE_SIZE).array();
		testResultInfo = new ArrayList<String>();

		kvGenerator = new KVGenerator();
		clientConfig = new ClientConfiguration(System.getProperties());
		clientConfig.setPort(PORT);

		boolean clientNioFlag;
		boolean serverNioFlag;

		// start server nio=f
		serverNioFlag = false;
		startServer(serverNioFlag);

		// sync=f, nio(c/s)=f/f
		clientNioFlag = false;
		asyncPerf(clientNioFlag, serverNioFlag);

		// sync=f, nio(c/s)=t/f
		clientNioFlag = true;
		asyncPerf(clientNioFlag, serverNioFlag);

		// sync=t, nio(c/s)=f/f
		clientNioFlag = false;
		syncPerf(clientNioFlag, serverNioFlag);

		// sync=t, nio(c/s)=t/f
		clientNioFlag = true;
		syncPerf(clientNioFlag, serverNioFlag);
		closeServer();

		// start server nio=t
		serverNioFlag = true;
		startServer(serverNioFlag);

		// sync=f, nio(c/s)=f/t
		clientNioFlag = false;
		asyncPerf(clientNioFlag, serverNioFlag);

		// sync=f, nio(c/s)=t/t
		clientNioFlag = true;
		asyncPerf(clientNioFlag, serverNioFlag);

		// sync=t, nio(c/s)=f/t
		clientNioFlag = false;
		syncPerf(clientNioFlag, serverNioFlag);

		// sync=t, nio(c/s)=t/t
		clientNioFlag = true;
		syncPerf(clientNioFlag, serverNioFlag);
		closeServer();

		printTestResult();

	}

	private static void asyncPerf(boolean clientNioFlag, boolean serverNioFlag)
			throws InterruptedException {
		try {
			createClient(clientNioFlag);

			// warm up
			asyncWarmUp();

			// Test begin
			clear(OPERATE_COUNT, kineticClient);
			long totalRepeatAsyncTime = 0;
			for (int i = 0; i < REPEAT_COUNT; i++) {

				kvGenerator.reset();
				signal = new CountDownLatch(OPERATE_COUNT);
				MyCallBackHandler myCallback = new MyCallBackHandler(signal);
				long asyncStartTime = System.currentTimeMillis();
				for (int j = 0; j < OPERATE_COUNT; j++) {
					String key = kvGenerator.getNextKey();

					// logger.info("adding data index=" + j);

					EntryMetadata entryMetadata = new EntryMetadata();
					Entry entryPut = new Entry(toByteArray(key), VALUE,
							entryMetadata);

					kineticClient.putAsync(entryPut,
							toByteArray(INIT_VERSION), myCallback);
				}
				signal.await();

				long asyncEndTime = System.currentTimeMillis();
				long totalAsyncTime = asyncEndTime - asyncStartTime;
				long messagePerSec = OPERATE_COUNT * 1000 / totalAsyncTime;

				logger.info(testResult(false, clientNioFlag, serverNioFlag,
						OPERATE_COUNT, totalAsyncTime, messagePerSec));

				totalRepeatAsyncTime += totalAsyncTime;
				clear(OPERATE_COUNT, kineticClient);

			}
			kineticClient.close();

			long averageAsynTime = totalRepeatAsyncTime / REPEAT_COUNT;
			long messagePerSec = OPERATE_COUNT * 1000 / averageAsynTime;

			String testInfo = testResult(false, clientNioFlag, serverNioFlag,
					OPERATE_COUNT, averageAsynTime, messagePerSec);
			logger.info(testInfo);

			testResultInfo.add(testInfo);

		} catch (UnsupportedEncodingException e) {
			logger.log(Level.SEVERE, e.getMessage());
		} catch (KineticException e) {
			logger.log(Level.SEVERE, e.getMessage());
		}

	}

	private static void syncPerf(boolean clientNioFlag, boolean serverNioFlag) {
		try {
			createClient(clientNioFlag);

			// warm up
			syncWarmUp();

			// Test begin
			clear(OPERATE_COUNT, kineticClient);
			long totalRepeatSyncTime = 0;
			for (int i = 0; i < REPEAT_COUNT; i++) {

				kvGenerator.reset();

				long syncStartTime = System.currentTimeMillis();
				for (int j = 0; j < OPERATE_COUNT; j++) {
					String key = kvGenerator.getNextKey();

					// logger.info("adding data index=" + j);
					EntryMetadata entryMetadata = new EntryMetadata();
					Entry entryPut = new Entry(toByteArray(key), VALUE,
							entryMetadata);

					kineticClient.put(entryPut, toByteArray(INIT_VERSION));
				}

				long syncEndTime = System.currentTimeMillis();
				long totalSyncTime = syncEndTime - syncStartTime;
				long messagePerSec = OPERATE_COUNT * 1000 / totalSyncTime;

				logger.info(testResult(true, clientNioFlag, serverNioFlag,
						OPERATE_COUNT, totalSyncTime, messagePerSec));

				totalRepeatSyncTime += totalSyncTime;
				clear(OPERATE_COUNT, kineticClient);
			}

			kineticClient.close();

			long averageSynTime = totalRepeatSyncTime / REPEAT_COUNT;
			long messagePerSec = OPERATE_COUNT * 1000 / averageSynTime;

			String testInfo = testResult(true, clientNioFlag, serverNioFlag,
					OPERATE_COUNT, averageSynTime, messagePerSec);
			logger.info(testInfo);

			testResultInfo.add(testInfo);

		} catch (UnsupportedEncodingException e) {
			logger.log(Level.SEVERE, e.getMessage());
		} catch (KineticException e) {
			logger.log(Level.SEVERE, e.getMessage());
		}

	}

	/*
	 * async warm up
	 */
	private static void asyncWarmUp() throws UnsupportedEncodingException,
			KineticException, InterruptedException {
		// warm up
		clear(WARM_UP_COUNT, kineticClient);

		final CountDownLatch warmUpSignal = new CountDownLatch(WARM_UP_COUNT);
		kvGenerator.reset();
		MyCallBackHandler myWarmUpCallback = new MyCallBackHandler(warmUpSignal);
		for (int warm = 0; warm < WARM_UP_COUNT; warm++) {

			String key = kvGenerator.getNextKey();

			// logger.info("adding warm data index=" + warm);

			EntryMetadata entryMetadata = new EntryMetadata();
			Entry entryPut = new Entry(toByteArray(key), VALUE, entryMetadata);

			kineticClient.putAsync(entryPut, toByteArray(INIT_VERSION),
					myWarmUpCallback);
		}
		warmUpSignal.await();

		clear(WARM_UP_COUNT, kineticClient);

		logger.info("Warm up number=" + WARM_UP_COUNT
				+ ", warm up end, perf test begin!");
	}

	/*
	 * sync warm up
	 */
	private static void syncWarmUp() throws UnsupportedEncodingException,
			KineticException {

		clear(WARM_UP_COUNT, kineticClient);
		kvGenerator.reset();
		for (int warm = 0; warm < WARM_UP_COUNT; warm++) {

			String key = kvGenerator.getNextKey();

			// logger.info("adding warm data index=" + warm);

			EntryMetadata entryMetadata = new EntryMetadata();
			Entry entryPut = new Entry(toByteArray(key), VALUE, entryMetadata);

			kineticClient.put(entryPut, toByteArray(INIT_VERSION));
		}

		clear(WARM_UP_COUNT, kineticClient);
		logger.info("Warm up number=" + WARM_UP_COUNT
				+ ", warm up end, perf test begin!");
	}

	private static KineticClient createClient(boolean clientNioFlag)
			throws KineticException {

		if (clientNioFlag) {
			kineticClient = KineticClientFactory
					.createInstance(clientConfig);
		} else {
			clientConfig.setUseNio(false);
			kineticClient = KineticClientFactory
					.createInstance(clientConfig);
		}

		return kineticClient;
	}

	private static void startServer(boolean serverNioFlag)
			throws InterruptedException {
		serverConfig = new SimulatorConfiguration();
		serverConfig.put(SimulatorConfiguration.PERSIST_HOME, "performance");
		serverConfig.setPort(PORT);
		if (serverNioFlag) {
			kineticServer = new KineticSimulator(serverConfig);
			Thread.sleep(200);
		} else {
			serverConfig.setUseNio(serverNioFlag);
			kineticServer = new KineticSimulator(serverConfig);
			Thread.sleep(200);
		}
	}

	private static void closeServer() {
		if (null != kineticServer) {
			kineticServer.close();
		}
	}

	private static void printTestResult() {

		if (!testResultInfo.isEmpty() && null != testResultInfo
				&& 0 < testResultInfo.size()) {
			for (int i = 0; i < testResultInfo.size(); i++) {
				// logger.info(testResultInfo.get(i));
				System.out.println(testResultInfo.get(i));
			}
		}
	}

	@Test(enabled = false)
    private static String testResult(boolean syncFlag, boolean clientNioFlag,
			boolean serverNioFlag, int operationCount, long totalAsyncTime,
			long messagePerSec) {
		String syncFlagS;
		String clientNioFlagS;
		String serverNioFlagS;
		if (syncFlag) {
			syncFlagS = "T";
		} else {
			syncFlagS = "F";
		}

		if (clientNioFlag) {
			clientNioFlagS = "T";
		} else {
			clientNioFlagS = "F";
		}

		if (serverNioFlag) {
			serverNioFlagS = "T";
		} else {
			serverNioFlagS = "F";
		}
		String resultInfo = "Sync=" + syncFlagS + ", Nio(C/S)="
				+ clientNioFlagS + "/" + serverNioFlagS + ", Size="
				+ VALUE_SIZE + "B, #OP=" + operationCount + ", Time="
				+ totalAsyncTime + "ms, #Avg=" + messagePerSec + "op/sec";

		return resultInfo;
	}

	private static void clear(int count, KineticClient kineticClient)
			throws KineticException, UnsupportedEncodingException {
		String key;
		kvGenerator.reset();
		for (int i = 0; i < count; i++) {
			key = kvGenerator.getNextKey();
			delete(kineticClient, toByteArray(key));
		}
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
				kineticClient.delete(versionedEntry);
				// logger.info("delete key: " + new String(key));
			} catch (KineticException e) {
				Assert.fail("delete key " + new String(key) + " failed, "
						+ e.getMessage());
			} catch (Exception e) {
				Assert.fail("delete key " + new String(key) + " failed, "
						+ e.getMessage());
			}
		} else {
			// logger.info("no key: " + new String(key));
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
