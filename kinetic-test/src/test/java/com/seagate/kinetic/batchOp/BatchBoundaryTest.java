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

import static com.seagate.kinetic.KineticTestHelpers.toByteArray;
import static org.testng.AssertJUnit.assertTrue;
import kinetic.client.BatchAbortedException;
import kinetic.client.BatchOperation;
import kinetic.client.Entry;
import kinetic.client.EntryMetadata;
import kinetic.client.KineticClient;
import kinetic.client.KineticClientFactory;
import kinetic.client.KineticException;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.seagate.kinetic.IntegrationTestCase;
import com.seagate.kinetic.proto.Kinetic.Command.Status.StatusCode;

@Test(groups = { "simulator" })
public class BatchBoundaryTest extends IntegrationTestCase {
	private final int MAX_VALUE_SIZE = 1024 * 1024;
	private final int MAX_KEY_SIZE = 4096;
	private final int MAX_VERSION_SIZE = 2048;
	private final int MAX_BATCH_OP_NUM = 15;
	private final int MAX_BATCH_PER_CONNECTION_NUM = 5;

	@Test(dataProvider = "transportProtocolOptions")
	public void testBatchOperation_PutAndDeleteForced_PutExceedMaximumKeySizeFailed(
			String clientName) {
		byte[] key = new byte[MAX_KEY_SIZE + 1];
		byte[] value = toByteArray("value");

		Entry entry = new Entry(key, value);

		BatchOperation batch = null;
		try {
			batch = getClient(clientName).createBatchOperation();
		} catch (KineticException e) {
			Assert.fail("Create batch operation throw exception. "
					+ e.getMessage());
		}

		try {
			batch.put(entry, toByteArray("5678"));
		} catch (KineticException e) {
			Assert.fail("Put entry throw exception. " + e.getMessage());
		}

		try {
			batch.commit();
		} catch (BatchAbortedException e) {
			assertTrue(e.getResponseMessage().getCommand().getStatus()
					.getCode().equals(StatusCode.INVALID_BATCH));
			assertTrue(e.getFailedOperationIndex() == 0);

		} catch (KineticException e) {
			Assert.fail("received unexpected exception: " + e.getMessage());
		}
	}

	@Test(dataProvider = "transportProtocolOptions")
	public void testBatchOperation_PutAndDeleteForced_PutExceedMaximumValueSizeFailed(
			String clientName) {
		byte[] key = toByteArray("key");
		byte[] value = new byte[MAX_VALUE_SIZE + 1];

		Entry entry = new Entry(key, value);

		BatchOperation batch = null;
		try {
			batch = getClient(clientName).createBatchOperation();
		} catch (KineticException e) {
			Assert.fail("Create batch operation throw exception. "
					+ e.getMessage());
		}

		try {
			batch.put(entry, toByteArray("5678"));
		} catch (KineticException e) {
			Assert.fail("Put entry throw exception. " + e.getMessage());
		}

		try {
			batch.commit();
		} catch (BatchAbortedException e) {
			assertTrue(e.getResponseMessage().getCommand().getStatus()
					.getCode().equals(StatusCode.INVALID_BATCH));
			assertTrue(e.getFailedOperationIndex() == 0);

		} catch (KineticException e) {
			Assert.fail("received unexpected exception: " + e.getMessage());
		}
	}

	@Test(dataProvider = "transportProtocolOptions")
	public void testBatchOperation_PutAndDeleteForced_PutExceedMaximumDbVersionSizeFailed(
			String clientName) {
		byte[] key = toByteArray("key");
		byte[] value = toByteArray("value");
		byte[] dbVersion = new byte[MAX_VERSION_SIZE + 1];

		EntryMetadata emd = new EntryMetadata();
		emd.setVersion(dbVersion);

		Entry entry = new Entry(key, value, emd);

		try {
			getClient(clientName).deleteForced(key);
		} catch (KineticException e1) {
			Assert.fail("delete entry failed. " + e1.getMessage());
		}

		BatchOperation batch = null;
		try {
			batch = getClient(clientName).createBatchOperation();
		} catch (KineticException e) {
			Assert.fail("Create batch operation throw exception. "
					+ e.getMessage());
		}

		try {
			batch.putForced(entry);
		} catch (KineticException e) {
			Assert.fail("Put entry throw exception. " + e.getMessage());
		}

		try {
			batch.commit();
		} catch (BatchAbortedException e) {
			e.printStackTrace();
			System.out.println(e.getFailedOperationIndex());
			assertTrue(e.getResponseMessage().getCommand().getStatus()
					.getCode().equals(StatusCode.INVALID_BATCH));
			assertTrue(e.getFailedOperationIndex() == 0);

		} catch (KineticException e) {
			Assert.fail("received unexpected exception: " + e.getMessage());
		}

		try {
			getClient(clientName).deleteForced(key);
		} catch (KineticException e1) {
			Assert.fail("delete entry failed. " + e1.getMessage());
		}
	}

	@Test(dataProvider = "transportProtocolOptions")
	public void testBatchOperation_PutAndDeleteForced_PutExceedMaximumNewVersionSizeFailed(
			String clientName) {
		byte[] key = toByteArray("key");
		byte[] value = toByteArray("value");

		Entry entry = new Entry(key, value);
		try {
			getClient(clientName).deleteForced(key);
		} catch (KineticException e1) {
			Assert.fail("delete entry failed. " + e1.getMessage());
		}

		BatchOperation batch = null;
		try {
			batch = getClient(clientName).createBatchOperation();
		} catch (KineticException e) {
			Assert.fail("Create batch operation throw exception. "
					+ e.getMessage());
		}

		try {
			batch.put(entry, new byte[MAX_VERSION_SIZE + 1]);
		} catch (KineticException e) {
			Assert.fail("Put entry throw exception. " + e.getMessage());
		}

		try {
			batch.commit();
		} catch (BatchAbortedException e) {
			assertTrue(e.getResponseMessage().getCommand().getStatus()
					.getCode().equals(StatusCode.INVALID_BATCH));
			assertTrue(e.getFailedOperationIndex() == 0);

		} catch (KineticException e) {
			Assert.fail("received unexpected exception: " + e.getMessage());
		}

		try {
			getClient(clientName).deleteForced(key);
		} catch (KineticException e1) {
			Assert.fail("delete entry failed. " + e1.getMessage());
		}
	}

	@Test(dataProvider = "transportProtocolOptions", enabled = true, priority = 1)
	public void testBatchOperation_BatchCountExceedTheMaxinumNum_ThrowException(
			String clientName) {
		KineticClient kineticClient = creatClient(clientName);
		assertTrue(kineticClient != null);

		Entry foo = getFooEntry();

		try {
			kineticClient.deleteForced(foo.getKey());
		} catch (KineticException e1) {
			Assert.fail("delete entry failed. " + e1.getMessage());
		}

		BatchOperation batch[] = new BatchOperation[MAX_BATCH_PER_CONNECTION_NUM + 1];
		try {
			for (int i = 0; i < MAX_BATCH_PER_CONNECTION_NUM + 1; i++) {
				batch[i] = kineticClient.createBatchOperation();
			}
		} catch (KineticException e) {
			assertTrue(e.getMessage() != null);
		}
	}

	private KineticClient creatClient(String clientName) {
		KineticClient kineticClient = null;
		try {
			kineticClient = KineticClientFactory
					.createInstance(kineticClientConfigutations.get(clientName));
		} catch (KineticException e) {
			Assert.fail("Create client throw exception. " + e.getMessage());
		}
		return kineticClient;
	}

	@Test(dataProvider = "transportProtocolOptions", enabled = true, priority = 2)
	public void testBatchOperation_OperationExceedTheMaxinumNumPerBatch_ThrowException(
			String clientName) {
		KineticClient kineticClient = creatClient(clientName);
		assertTrue(kineticClient != null);

		try {
			for (int i = 0; i < MAX_BATCH_OP_NUM + 1; i++) {
				kineticClient.deleteForced(toByteArray("foo" + i));
			}
		} catch (KineticException e) {
			Assert.fail("Clean entry failed. " + e.getMessage());
		}
	}

	private Entry getFooEntry() {
		Entry foo = new Entry();
		byte[] fooKey = toByteArray("foo");
		foo.setKey(fooKey);
		byte[] fooValue = toByteArray("foovalue");
		foo.setValue(fooValue);
		byte[] fooVersion = toByteArray("1234");
		foo.getEntryMetadata().setVersion(fooVersion);

		return foo;
	}
}
