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

import java.util.concurrent.TimeUnit;

import kinetic.client.KineticClientFactory;
import kinetic.client.KineticException;
import kinetic.client.advanced.AdvancedKineticClient;

import org.testng.annotations.Test;

import com.seagate.kinetic.IntegrationTestCase;

public class BatchPerformanceTest extends IntegrationTestCase {
	private static final int TEST_RUN_TIME_IN_MINUTE = 1;
	private static final int MAX_BATCH_COUNT = 5;
	private static final int REPORT_PERIOD_IN_SECONDS = 10;

	@Test(dataProvider = "transportProtocolOptions", enabled = false)
	public void testBatchPerformance_WithPureBatchPutUsingMultipleThreads(
			String clientName) {
		long start, end;
		double mbps = 0;
		System.out.println("Start batch performance test...");
		try {
			AdvancedKineticClient kineticClient = null;
			PureBatchPutThread threads[] = new PureBatchPutThread[MAX_BATCH_COUNT];
			for (int i = 0; i < MAX_BATCH_COUNT; i++) {
				kineticClient = (AdvancedKineticClient) KineticClientFactory
						.createInstance(kineticClientConfigutations
								.get(clientName));
				threads[i] = new PureBatchPutThread("MT_PBP" + i + "_",
						kineticClient, true);
				threads[i].start();
			}
			start = System.currentTimeMillis();

			try {
				int report_rounds = (60 * TEST_RUN_TIME_IN_MINUTE)
						/ REPORT_PERIOD_IN_SECONDS;
				long time_spent_in_seconds = 0;
				for (int i = 0; i < report_rounds; i++) {
					TimeUnit.SECONDS.sleep(REPORT_PERIOD_IN_SECONDS);
					end = System.currentTimeMillis();
					time_spent_in_seconds = (end - start) / 1000;
					mbps = ((double) (PureBatchPutThread.totalPutBytes()) / time_spent_in_seconds) / 1024 / 1024;
					System.out.println("Runned " + time_spent_in_seconds
							+ " seconds, MBPS: " + mbps);
				}
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

	@Test(dataProvider = "transportProtocolOptions", enabled = false)
	public void testUuidFileStorePerformance_WithPurePutUsingMultipleThreads(
			String clientName) {
		long start, end;
		double mbps = 0;
		System.out.println("Start uuid mode put performance test...");
		try {
			AdvancedKineticClient kineticClient = null;
			PurePutThread threads[] = new PurePutThread[MAX_BATCH_COUNT];
			for (int i = 0; i < MAX_BATCH_COUNT; i++) {
				kineticClient = (AdvancedKineticClient) KineticClientFactory
						.createInstance(kineticClientConfigutations
								.get(clientName));
				threads[i] = new PurePutThread("MT_PP" + i + "_",
						kineticClient, true);
				threads[i].start();
			}
			start = System.currentTimeMillis();

			try {
				int report_rounds = (60 * TEST_RUN_TIME_IN_MINUTE)
						/ REPORT_PERIOD_IN_SECONDS;
				long time_spent_in_seconds = 0;
				for (int i = 0; i < report_rounds; i++) {
					TimeUnit.SECONDS.sleep(REPORT_PERIOD_IN_SECONDS);
					end = System.currentTimeMillis();
					time_spent_in_seconds = (end - start) / 1000;
					mbps = ((double) (PurePutThread.totalPutBytes()) / time_spent_in_seconds) / 1024 / 1024;
					System.out.println("Runned " + time_spent_in_seconds
							+ " seconds, MBPS: " + mbps);
				}
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
