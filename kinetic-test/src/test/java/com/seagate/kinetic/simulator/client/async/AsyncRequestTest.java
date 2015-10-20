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

import static org.testng.AssertJUnit.assertTrue;
import org.testng.annotations.Test;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

import kinetic.client.CallbackHandler;
import kinetic.client.Entry;
import kinetic.client.EntryMetadata;

import com.google.protobuf.ByteString;
import com.seagate.kinetic.IntegrationTestCase;
import com.seagate.kinetic.client.internal.MessageFactory;
import com.seagate.kinetic.common.lib.KineticMessage;
import com.seagate.kinetic.proto.Kinetic.Command;

import com.seagate.kinetic.proto.Kinetic.Command.KeyValue;
import com.seagate.kinetic.proto.Kinetic.Command.MessageType;
import com.seagate.kinetic.proto.Kinetic.Command.Synchronization;

@Test (groups = {"simulator"})
public class AsyncRequestTest extends IntegrationTestCase {

	Logger logger = Logger.getLogger(AsyncRequestTest.class.getName());

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test(dataProvider = "transportProtocolOptions")
	public void asyncPutTest(String clientName) throws Exception {

		List<byte[]> dataList = new ArrayList<byte[]>(10);
		List<Entry> versionedList = new ArrayList<Entry>(10);

		long start = System.nanoTime();

		int max = 1;

		LinkedBlockingQueue<KineticMessage> lbq = new LinkedBlockingQueue<KineticMessage>();

		// init and put data entries
		for (int i = 0; i < max; i++) {

			byte[] data = ByteBuffer.allocate(8).putLong(start + i).array();

			// add to cache
			dataList.add(data);

			// logger.info("adding data index=" + i);

			// construct/put data to db
			EntryMetadata entryMetadata = new EntryMetadata();
			Entry versioned = new Entry(data, data, entryMetadata);

			// Entry dbVersioned = getClient().put(versioned);
			KineticMessage km = createPutMessage(versioned, null);

			CallbackHandler callback = new MyCallBack(lbq, km);

			this.getClient(clientName).requestAsync(km, callback);

			// logger.info("added data index=" + i);

			versionedList.add(versioned);
		}

		this.checkCallBackMessageSize(lbq, versionedList);

		// verify entry
		for (int i = 0; i < (max); i++) {
			Entry versioned = getClient(clientName).get(versionedList.get(i).getKey());

			assertTrue(Arrays.equals(versionedList.get(i).getKey(),
					versioned.getKey()));

		}

		// clean up
		for (int i = 0; i < max; i++) {
			boolean deleted = getClient(clientName).delete(versionedList.get(i));
			assertTrue(deleted == true);
		}

		// verify clean up
		for (int i = 0; i < max; i++) {
			Entry v = getClient(clientName).get(versionedList.get(i).getKey());
			assertTrue(v == null);
		}

		logger.info("asyncPutTest passed ...");

	}

	private static KineticMessage createPutMessage(Entry versioned,
			byte[] newVersion) {

		KineticMessage km = MessageFactory.createKineticMessageWithBuilder();
		
		Command.Builder commandBuilder = (Command.Builder) km.getCommand();

		commandBuilder.getHeaderBuilder()
		.setMessageType(MessageType.PUT);
		
		commandBuilder.getBodyBuilder().getKeyValueBuilder().setSynchronization(Synchronization.WRITETHROUGH);
		
		KeyValue.Builder kv = commandBuilder.getBodyBuilder()
				.getKeyValueBuilder();

		// ket
		kv.setKey(ByteString.copyFrom(versioned.getKey()));

		// new version
		if (newVersion != null) {
			kv.setNewVersion(ByteString.copyFrom(newVersion));
		}

		// data
		if (versioned.getValue() != null) {
			// message.setValue(ByteString.copyFrom(versioned.getValue()));
			km.setValue(versioned.getValue());
		}

		return km;
	}

	private void checkCallBackMessageSize(
			LinkedBlockingQueue<KineticMessage> lbq,
			List<Entry> versionedList) throws InterruptedException {

		int count = 0;
		while (lbq.size() != versionedList.size()) {
			Thread.sleep(1000);
			count++;

			if (count == 10) {
				throw new RuntimeException("reached max timeout., lbq size="
						+ lbq.size());
			}
		}
	}
}
