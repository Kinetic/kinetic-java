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

public class BatchPerformanceTest extends IntegrationTestCase {
	private static final int TEST_RUN_TIME_IN_MINUTE = 1;
	private static final int MAX_BATCH_COUNT = 5;
	private static final int REPORT_PERIOD_IN_SECONDS = 10;

	@Test(dataProvider = "transportProtocolOptions")
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

	@Test(dataProvider = "transportProtocolOptions")
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
