/**
 * 
 * Copyright (C) 2014 Seagate Technology.
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 */
package com.seagate.kinetic.batchOp;

import java.util.concurrent.TimeUnit;

import kinetic.client.KineticClientFactory;
import kinetic.client.KineticException;
import kinetic.client.advanced.AdvancedKineticClient;

import org.testng.annotations.Test;

import com.seagate.kinetic.IntegrationTestCase;

public class BatchStressTest extends IntegrationTestCase {
	private static final int TEST_RUN_TIME_IN_MINUTE = 1;
	private static final int MAX_BATCH_COUNT = 1;

	@Test(dataProvider = "transportProtocolOptions")
	public void testBatchOperation_WithPureBatchPutUsingSingleThread(
			String clientName) {
		AdvancedKineticClient client = getClient(clientName);
		BatchTestThread thread = new PureBatchPutThread("ST_PBP", client);
		thread.start();

		try {
			TimeUnit.MINUTES.sleep(TEST_RUN_TIME_IN_MINUTE);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			thread.shutdown();
		}

		try {
			thread.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test(dataProvider = "transportProtocolOptions")
	public void testUuidFileStore_WithPurePutUsingSingleThread(String clientName) {
		AdvancedKineticClient client = getClient(clientName);
		BatchTestThread thread = new PurePutThread("ST_PP", client);
		thread.start();

		try {
			TimeUnit.MINUTES.sleep(TEST_RUN_TIME_IN_MINUTE);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			thread.shutdown();
		}

		try {
			thread.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test(dataProvider = "transportProtocolOptions")
	public void testBatchOperation_WithBatchPutAndDeleteUsingSingleThread(
			String clientName) {
		AdvancedKineticClient client = getClient(clientName);
		BatchTestThread thread = new BatchPutDeleteThread("ST_BPD", client);
		thread.start();

		try {
			TimeUnit.MINUTES.sleep(TEST_RUN_TIME_IN_MINUTE);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			thread.shutdown();
		}

		try {
			thread.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test(dataProvider = "transportProtocolOptions")
	public void testBatchOperation_WithBatchPutAndGetUsingSingleThread(
			String clientName) {
		AdvancedKineticClient client = getClient(clientName);
		BatchTestThread thread = new BatchPutGetThread("ST_BPG", client);
		thread.start();

		try {
			TimeUnit.MINUTES.sleep(TEST_RUN_TIME_IN_MINUTE);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			thread.shutdown();
		}

		try {
			thread.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test(dataProvider = "transportProtocolOptions")
	public void testBatchOperation_WithPutAndBatchDeleteUsingSingleThread(
			String clientName) {
		AdvancedKineticClient client = getClient(clientName);
		BatchTestThread thread = new PutBatchDeleteThread("ST_PBD", client);
		thread.start();

		try {
			TimeUnit.MINUTES.sleep(TEST_RUN_TIME_IN_MINUTE);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			thread.shutdown();
		}

		try {
			thread.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test(dataProvider = "transportProtocolOptions")
	public void testBatchOperation_WithPureBatchPutUsingMultipleThreads(
			String clientName) {
		try {
			AdvancedKineticClient kineticClient = null;
			BatchTestThread threads[] = new BatchTestThread[MAX_BATCH_COUNT];
			for (int i = 0; i < MAX_BATCH_COUNT; i++) {
				kineticClient = (AdvancedKineticClient) KineticClientFactory
						.createInstance(kineticClientConfigutations
								.get(clientName));
				threads[i] = new PureBatchPutThread("MT_PBP" + i + "_",
						kineticClient);
				threads[i].start();
			}

			try {
				TimeUnit.MINUTES.sleep(TEST_RUN_TIME_IN_MINUTE);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				for (int i = 0; i < MAX_BATCH_COUNT; i++) {
					threads[i].shutdown();
					try {
						threads[i].join();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}

		} catch (KineticException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test(dataProvider = "transportProtocolOptions")
	public void testUuidFileStore_WithPurePutUsingMultipleThreads(
			String clientName) {
		try {
			AdvancedKineticClient kineticClient = null;
			BatchTestThread threads[] = new BatchTestThread[MAX_BATCH_COUNT];
			for (int i = 0; i < MAX_BATCH_COUNT; i++) {
				kineticClient = (AdvancedKineticClient) KineticClientFactory
						.createInstance(kineticClientConfigutations
								.get(clientName));
				threads[i] = new PurePutThread("MT_PP" + i + "_", kineticClient);
				threads[i].start();
			}

			try {
				TimeUnit.MINUTES.sleep(TEST_RUN_TIME_IN_MINUTE);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				for (int i = 0; i < MAX_BATCH_COUNT; i++) {
					threads[i].shutdown();
					try {
						threads[i].join();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}

		} catch (KineticException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test(dataProvider = "transportProtocolOptions")
	public void testBatchOperation_WithBatchPutAndDeleteUsingMultipleThreads(
			String clientName) {
		try {
			AdvancedKineticClient kineticClient = null;
			BatchTestThread threads[] = new BatchTestThread[MAX_BATCH_COUNT];
			for (int i = 0; i < MAX_BATCH_COUNT; i++) {
				kineticClient = (AdvancedKineticClient) KineticClientFactory
						.createInstance(kineticClientConfigutations
								.get(clientName));
				threads[i] = new BatchPutDeleteThread("MT_BPD" + i + "_",
						kineticClient);
				threads[i].start();
			}

			try {
				TimeUnit.MINUTES.sleep(TEST_RUN_TIME_IN_MINUTE);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				for (int i = 0; i < MAX_BATCH_COUNT; i++) {
					threads[i].shutdown();
					try {
						threads[i].join();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}

		} catch (KineticException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test(dataProvider = "transportProtocolOptions")
	public void testBatchOperation_WithBatchPutAndGetUsingMultipleThreads(
			String clientName) {
		try {
			AdvancedKineticClient kineticClient = null;
			BatchTestThread threads[] = new BatchTestThread[MAX_BATCH_COUNT];
			for (int i = 0; i < MAX_BATCH_COUNT; i++) {
				kineticClient = (AdvancedKineticClient) KineticClientFactory
						.createInstance(kineticClientConfigutations
								.get(clientName));
				threads[i] = new BatchPutGetThread("MT_BPG" + i + "_",
						kineticClient);
				threads[i].start();
			}

			try {
				TimeUnit.MINUTES.sleep(TEST_RUN_TIME_IN_MINUTE);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				for (int i = 0; i < MAX_BATCH_COUNT; i++) {
					threads[i].shutdown();
					try {
						threads[i].join();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}

		} catch (KineticException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test(dataProvider = "transportProtocolOptions")
	public void testBatchOperation_WithPutAndBatchDeleteUsingMultipleThreads(
			String clientName) {
		try {
			AdvancedKineticClient kineticClient = null;
			BatchTestThread threads[] = new BatchTestThread[MAX_BATCH_COUNT];
			for (int i = 0; i < MAX_BATCH_COUNT; i++) {
				kineticClient = (AdvancedKineticClient) KineticClientFactory
						.createInstance(kineticClientConfigutations
								.get(clientName));
				threads[i] = new PutBatchDeleteThread("MT_PBD" + i + "_",
						kineticClient);
				threads[i].start();
			}

			try {
				TimeUnit.MINUTES.sleep(TEST_RUN_TIME_IN_MINUTE);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				for (int i = 0; i < MAX_BATCH_COUNT; i++) {
					threads[i].shutdown();
					try {
						threads[i].join();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}

		} catch (KineticException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
