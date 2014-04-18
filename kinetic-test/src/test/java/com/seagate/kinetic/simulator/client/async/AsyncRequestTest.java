package com.seagate.kinetic.simulator.client.async;

import static org.junit.Assert.assertTrue;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

import kinetic.client.CallbackHandler;
import kinetic.client.Entry;
import kinetic.client.EntryMetadata;

import org.junit.Test;

import com.google.protobuf.ByteString;
import com.seagate.kinetic.IntegrationTestCase;
import com.seagate.kinetic.common.lib.KineticMessage;
import com.seagate.kinetic.proto.Kinetic.Message;
import com.seagate.kinetic.proto.Kinetic.Message.KeyValue;
import com.seagate.kinetic.proto.Kinetic.Message.MessageType;

public class AsyncRequestTest extends IntegrationTestCase {

	Logger logger = Logger.getLogger(AsyncRequestTest.class.getName());

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void asyncPutTest() throws Exception {

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

			this.getClient().requestAsync(km, callback);

			// logger.info("added data index=" + i);

			versionedList.add(versioned);
		}

		this.checkCallBackMessageSize(lbq, versionedList);

		// verify entry
		for (int i = 0; i < (max); i++) {
			Entry versioned = getClient().get(versionedList.get(i).getKey());

			assertTrue(Arrays.equals(versionedList.get(i).getKey(),
					versioned.getKey()));

		}

		// clean up
		for (int i = 0; i < max; i++) {
			boolean deleted = getClient().delete(versionedList.get(i));
			assertTrue(deleted == true);
		}

		// verify clean up
		for (int i = 0; i < max; i++) {
			Entry v = getClient().get(versionedList.get(i).getKey());
			assertTrue(v == null);
		}

		logger.info("asyncPutTest passed ...");

	}

	private static KineticMessage createPutMessage(Entry versioned,
			byte[] newVersion) {

		KineticMessage km = new KineticMessage();
		Message.Builder message = Message.newBuilder();
		km.setMessage(message);

		message.getCommandBuilder().getHeaderBuilder()
		.setMessageType(MessageType.PUT);
		KeyValue.Builder kv = message.getCommandBuilder().getBodyBuilder()
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
