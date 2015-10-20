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
package com.seagate.kinetic.batchOp;

import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

import kinetic.client.AsyncKineticException;
import kinetic.client.BatchOperation;
import kinetic.client.CallbackHandler;
import kinetic.client.CallbackResult;
import kinetic.client.Entry;
import kinetic.client.KineticException;
import kinetic.client.advanced.AdvancedKineticClient;
import kinetic.client.advanced.PersistOption;

abstract class BatchTestThread extends Thread {
	protected static final int MAX_VALUE_SIZE_IN_KB = 1024;
	protected static final int MAX_BATCH_COUNT_MINUS_ONE = 14;
	protected static final int DEFAULT_ROUNDS = 1;
	protected final AdvancedKineticClient kineticClient;
	protected boolean stop = false;
	protected Random random = new Random();

	protected BatchTestThread(String name, AdvancedKineticClient kineticClient) {
		super(name);
		this.kineticClient = kineticClient;
	}

	@Override
	public void run() {
		while (!stop) {
			loopTask();
		}
	}

	public void shutdown() {
		stop = true;
	}

	abstract void loopTask();
}

class BatchPutDeleteThread extends BatchTestThread {
	public BatchPutDeleteThread(String name, AdvancedKineticClient kineticClient) {
		super(name, kineticClient);
	}

	@Override
	void loopTask() {
		try {
			BatchOperation batch = null;
			Entry entry = null;
			byte[] key = null;
			byte[] value = null;
			int rounds = random.nextInt(DEFAULT_ROUNDS) + 1;
			int operations = random.nextInt(MAX_BATCH_COUNT_MINUS_ONE) + 1;
			for (int i = 0; i < rounds; i++) {
				batch = kineticClient.createBatchOperation();
				for (int j = 0; j < operations; j++) {
					key = (this.getName() + i + "_" + j).getBytes();
					value = new byte[(random.nextInt(MAX_VALUE_SIZE_IN_KB) + 1) << 10];
					entry = new Entry(key, value);
					batch.putForced(entry);
				}
				batch.commit();

				batch = kineticClient.createBatchOperation();
				for (int j = 0; j < operations; j++) {
					key = (this.getName() + i + "_" + j).getBytes();
					batch.deleteForced(key);
				}
				batch.commit();
			}
		} catch (KineticException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

class PureBatchPutThread extends BatchTestThread {
	private static final int ONE_MEGA = 1048576;
	private static AtomicLong totalPutInBytes = new AtomicLong(0);
	private byte[] ONE_MEGA_VALUE = new byte[ONE_MEGA];

	// Once enabling stat, data size will be set as 1M.
	private boolean enableStat = false;

	public PureBatchPutThread(String name, AdvancedKineticClient kineticClient) {
		super(name, kineticClient);
	}

	public PureBatchPutThread(String name, AdvancedKineticClient kineticClient,
			boolean enableStat) {
		super(name, kineticClient);
		this.enableStat = enableStat;
	}

	public static long totalPutBytes() {
		return totalPutInBytes.get();
	}

	@Override
	void loopTask() {
		try {
			BatchOperation batch = null;
			Entry entry = null;
			byte[] key = null;
			byte[] value = null;
			int rounds = random.nextInt(DEFAULT_ROUNDS) + 1;
			int operations = enableStat ? (MAX_BATCH_COUNT_MINUS_ONE + 1)
					: random.nextInt(MAX_BATCH_COUNT_MINUS_ONE) + 1;
			for (int i = 0; i < rounds; i++) {
				batch = kineticClient.createBatchOperation();
				for (int j = 0; j < operations; j++) {
					key = (this.getName() + i + "_" + j).getBytes();
					if (enableStat) {
						value = ONE_MEGA_VALUE;
					} else {
						value = new byte[((random.nextInt(MAX_VALUE_SIZE_IN_KB) + 1) << 10)];
					}
					entry = new Entry(key, value);
					batch.putForced(entry);
				}
				batch.commit();
				if (enableStat)
					totalPutInBytes.getAndAdd(operations * ONE_MEGA);
			}
		} catch (KineticException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

class BatchPutGetThread extends BatchTestThread {
	public BatchPutGetThread(String name, AdvancedKineticClient kineticClient) {
		super(name, kineticClient);
	}

	@Override
	void loopTask() {
		try {
			BatchOperation batch = null;
			Entry entry = null;
			byte[] key = null;
			byte[] value = null;
			int rounds = random.nextInt(DEFAULT_ROUNDS) + 1;
			int operations = random.nextInt(MAX_BATCH_COUNT_MINUS_ONE) + 1;
			int values_size[] = new int[operations];
			for (int i = 0; i < rounds; i++) {
				batch = kineticClient.createBatchOperation();
				for (int j = 0; j < operations; j++) {
					key = (this.getName() + i + "_" + j).getBytes();
					value = new byte[(random.nextInt(MAX_VALUE_SIZE_IN_KB) + 1) << 10];
					values_size[j] = value.length;
					entry = new Entry(key, value);
					batch.putForced(entry);
				}
				batch.commit();

				for (int j = 0; j < operations; j++) {
					key = (this.getName() + i + "_" + j).getBytes();
					entry = kineticClient.get(key);

					if (values_size[j] != entry.getValue().length) {
						throw new KineticException("wrong value size");
					}
				}
			}
		} catch (KineticException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

class PurePutThread extends BatchTestThread {
	private static final int ONE_MEGA = 1048576;
	private static AtomicLong totalPutInBytes = new AtomicLong(0);
	// Once enabling stat, data size will be set as 1M.
	private boolean enableStat = false;
	private byte[] ONE_MEGA_VALUE = new byte[ONE_MEGA];

	public PurePutThread(String name, AdvancedKineticClient kineticClient) {
		super(name, kineticClient);
	}

	public PurePutThread(String name, AdvancedKineticClient kineticClient,
			boolean enableStat) {
		super(name, kineticClient);
		this.enableStat = enableStat;
	}

	public static long totalPutBytes() {
		return totalPutInBytes.get();
	}

	@Override
	void loopTask() {
		Entry entry = null;
		byte[] key = null;
		byte[] value = null;
		int rounds = random.nextInt(DEFAULT_ROUNDS) + 1;
		int operations = enableStat ? (MAX_BATCH_COUNT_MINUS_ONE + 1) : random
				.nextInt(MAX_BATCH_COUNT_MINUS_ONE) + 1;
		for (int i = 0; i < rounds; i++) {
			for (int j = 0; j < operations; j++) {
				key = (this.getName() + i + "_" + j).getBytes();
				if (enableStat) {
					value = ONE_MEGA_VALUE;
				} else {
					value = new byte[((random.nextInt(MAX_VALUE_SIZE_IN_KB) + 1) << 10)];
				}

				entry = new Entry(key, value);
				try {
					if (enableStat) {
						kineticClient.putForced(entry, PersistOption.ASYNC);
						// kineticClient.putForcedAsync(entry, handler);
					} else {
						kineticClient.putForced(entry);
					}
				} catch (KineticException e) {
					e.printStackTrace();
				} finally {
					if (enableStat)
						totalPutInBytes.getAndAdd(ONE_MEGA);
				}
			}

		}
	}

	class MyPutCallbackHandler implements CallbackHandler<Entry> {

		@Override
		public void onSuccess(CallbackResult<Entry> result) {
			totalPutInBytes.getAndAdd(ONE_MEGA);
		}

		@Override
		public void onError(AsyncKineticException exception) {
			totalPutInBytes.getAndAdd(ONE_MEGA);
		}
	}
}

class PutBatchDeleteThread extends BatchTestThread {
	public PutBatchDeleteThread(String name, AdvancedKineticClient kineticClient) {
		super(name, kineticClient);
	}

	@Override
	void loopTask() {
		BatchOperation batch = null;
		Entry entry = null;
		byte[] key = null;
		byte[] value = null;
		int rounds = random.nextInt(DEFAULT_ROUNDS) + 1;
		int operations = random.nextInt(MAX_BATCH_COUNT_MINUS_ONE) + 1;
		for (int i = 0; i < rounds; i++) {
			for (int j = 0; j < operations; j++) {
				key = (this.getName() + i + "_" + j).getBytes();
				value = new byte[(random.nextInt(MAX_VALUE_SIZE_IN_KB) + 1) << 10];
				entry = new Entry(key, value);
				try {
					kineticClient.putForced(entry);
				} catch (KineticException e) {
					e.printStackTrace();
				}
			}

			try {
				batch = kineticClient.createBatchOperation();
				for (int j = 0; j < operations; j++) {
					key = (this.getName() + i + "_" + j).getBytes();
					batch.deleteForced(key);
				}
				batch.commit();
			} catch (KineticException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
