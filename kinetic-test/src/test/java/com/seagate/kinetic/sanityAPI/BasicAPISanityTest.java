package com.seagate.kinetic.sanityAPI;

import static com.seagate.kinetic.KineticAssertions.assertEntryEquals;
import static com.seagate.kinetic.KineticAssertions.assertListOfArraysEqual;
import static com.seagate.kinetic.KineticTestHelpers.int32;
import static com.seagate.kinetic.KineticTestHelpers.toByteArray;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import kinetic.client.ClientConfiguration;
import kinetic.client.Entry;
import kinetic.client.EntryMetadata;
import kinetic.client.KineticClient;
import kinetic.client.KineticClientFactory;
import kinetic.client.KineticException;
import kinetic.simulator.KineticSimulator;
import kinetic.simulator.SimulatorConfiguration;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.google.common.collect.Lists;

@Test(groups = { "simulator", "drive" })
public class BasicAPISanityTest {
	private SimulatorConfiguration sc;
	private KineticSimulator simulator;
	private ClientConfiguration cc;
	private KineticClient client;
	private Boolean runAgainstExternal = false;
	private final String KEY_PREFIX = "key";
	private final int MAX_KEYS = 1;

	@BeforeClass
	public void beforeClass() throws KineticException {
		String host = System.getProperty("KINETIC_HOST", "localhost");
		int port = Integer.parseInt(System.getProperty("KINETIC_PORT", "8123"));
		int sslPort = Integer.parseInt(System.getProperty("KINETIC_SSL_PORT", "8443"));
		runAgainstExternal = Boolean.parseBoolean(System
				.getProperty("RUN_AGAINST_EXTERNAL"));
		boolean runNioSSL = Boolean.parseBoolean(System
				.getProperty("RUN_NIO_SSL"));
		if (!runAgainstExternal) {
			sc = new SimulatorConfiguration();
			simulator = new KineticSimulator(sc);
		}

		cc = new ClientConfiguration();
		cc.setHost(host);
		cc.setPort(port);
		
		if (runNioSSL){
			cc.setUseSsl(true);
			cc.setPort(sslPort);
		}
		client = KineticClientFactory.createInstance(cc);
	}

	@AfterClass
	public void afterClass() throws KineticException {
		client.close();
		if (!runAgainstExternal) {
			simulator.close();
		}
	}

	private void clean(int maxkeys) throws KineticException {
		byte[] key;

		for (int i = 0; i < maxkeys; i++) {
			key = toByteArray(KEY_PREFIX + i);
			client.deleteForced(key);
		}

	}

	/**
	 * Test put API with a serial of entries. Metadata with the value of tag and
	 * algorithm The test result should be successful and verify the result
	 * returned is the same as put before
	 * <p>
	 *
	 * @throws KineticException
	 *             if any internal error occurred.
	 */
	public void testPut() throws KineticException {
		Entry versionedPut;
		Entry versionedPutReturn;
		byte[] key;
		byte[] value;
		String algorithm = "SHA1";
		Long start = System.nanoTime();

		clean(MAX_KEYS);

		for (int i = 0; i < MAX_KEYS; i++) {
			key = toByteArray(KEY_PREFIX + i);
			value = ByteBuffer.allocate(8).putLong(start + i).array();
			EntryMetadata entryMetadata = new EntryMetadata();
			entryMetadata.setTag(key);
			entryMetadata.setAlgorithm(algorithm);
			versionedPut = new Entry(key, value, entryMetadata);

			versionedPutReturn = client.put(versionedPut, int32(i));
			assertArrayEquals(key, versionedPutReturn.getKey());
			assertArrayEquals(int32(i), versionedPutReturn.getEntryMetadata()
					.getVersion());
			assertArrayEquals(value, versionedPutReturn.getValue());
			assertArrayEquals(key, versionedPutReturn.getEntryMetadata()
					.getTag());
			assertEquals("SHA1", versionedPutReturn.getEntryMetadata()
					.getAlgorithm());
		}

		clean(MAX_KEYS);
	}

	/**
	 * Test get API with a serial of entries. The entries have already existed
	 * in simulator/drive. The test result should be successful and verify the
	 * result returned is the same as put before
	 * <p>
	 *
	 * @throws KineticException
	 *             if any internal error occurred.
	 */
	public void testGet() throws KineticException {
		Entry versionedPut;
		Entry versionedPutReturn;
		Entry versionedGet;
		List<Entry> versionedPutReturnEntry = new ArrayList<Entry>();
		byte[] key;
		byte[] value;
		Long start = System.nanoTime();

		for (int i = 0; i < MAX_KEYS; i++) {
			key = toByteArray(KEY_PREFIX + i);
			value = ByteBuffer.allocate(8).putLong(start + i).array();
			EntryMetadata entryMetadata = new EntryMetadata();
			versionedPut = new Entry(key, value, entryMetadata);

			versionedPutReturn = client.putForced(versionedPut);

			versionedPutReturnEntry.add(versionedPutReturn);
		}

		for (int i = 0; i < MAX_KEYS; i++) {
			key = toByteArray(KEY_PREFIX + i);

			versionedGet = client.get(key);

			assertEntryEquals(versionedGet, versionedPutReturnEntry.get(i));

			client.deleteForced(key);
		}
	}

	/**
	 * Test delete API with a serial of entries. The entries have already
	 * existed in simulator/drive. The test result should be true. Try to get
	 * key to verify the results is null after delete.
	 * <p>
	 *
	 * @throws KineticException
	 *             if any internal error occurred.
	 */
	public void testDelete() throws KineticException {
		Long start = System.nanoTime();

		for (int i = 0; i < MAX_KEYS; i++) {
			byte[] key = toByteArray(KEY_PREFIX + i);
			byte[] value = ByteBuffer.allocate(8).putLong(start + i).array();
			EntryMetadata entryMetadata = new EntryMetadata();
			Entry versionedPut = new Entry(key, value, entryMetadata);

			Entry versionedGet = client.putForced(versionedPut);
			assertTrue(client.delete(versionedGet));
		}
	}

	/**
	 * Test getNext API with a serial of entries. The entries have already
	 * existed in simulator/drive. The test result should be successful.
	 * <p>
	 *
	 * @throws KineticException
	 *             if any internal error occurred.
	 */
	public void testGetNext() throws KineticException {
		long start = System.nanoTime();

		Entry vIn;
		Entry vOut;
		List<Entry> versionedOutList = new ArrayList<Entry>();
		List<byte[]> keyList = new ArrayList<byte[]>();
		for (int i = 0; i < MAX_KEYS + 1; i++) {
			byte[] key = toByteArray(KEY_PREFIX + i);
			byte[] data = ByteBuffer.allocate(8).putLong(start + i).array();
			keyList.add(key);
			EntryMetadata entryMetadata = new EntryMetadata();
			vIn = new Entry(key, data, entryMetadata);

			vOut = client.putForced(vIn);
			versionedOutList.add(vOut);
		}

		for (int i = 0; i < MAX_KEYS; i++) {
			Entry vNext = client.getNext(keyList.get(i));

			assertEntryEquals(versionedOutList.get(i + 1), vNext);
		}

		clean(MAX_KEYS + 1);
	}

	/**
	 * Test getPrevious API with a serial of entries. The entries have already
	 * existed in simulator/drive. The test result should be successful.
	 * <p>
	 *
	 * @throws KineticException
	 *             if any internal error occurred.
	 */
	public void testGetPrevious() throws KineticException {
		long start = System.nanoTime();

		List<Entry> versionedOutList = new ArrayList<Entry>();
		List<byte[]> keyList = new ArrayList<byte[]>();
		for (int i = 0; i < MAX_KEYS + 1; i++) {
			byte[] key = toByteArray(KEY_PREFIX + i);
			byte[] data = ByteBuffer.allocate(8).putLong(start + i).array();
			keyList.add(key);
			EntryMetadata entryMetadata = new EntryMetadata();
			Entry vIn = new Entry(key, data, entryMetadata);
			Entry vOut = client.putForced(vIn);
			versionedOutList.add(vOut);
		}

		for (int i = 1; i < MAX_KEYS + 1; i++) {
			Entry vPre = client.getPrevious(keyList.get(i));

			assertEntryEquals(versionedOutList.get(i - 1), vPre);
		}

		clean(MAX_KEYS + 1);
	}

	/**
	 * Test getMetadata API with a serial of entries. The entries have already
	 * existed in simulator/drive. The test result should be successful.
	 * <p>
	 *
	 * @throws KineticException
	 *             if any internal error occurred.
	 */
	public void testGetMetadata() throws KineticException {
		byte[] newVersion = int32(0);
		long start = System.nanoTime();
		for (int i = 0; i < MAX_KEYS; i++) {
			byte[] key = toByteArray(KEY_PREFIX + i);
			byte[] data = ByteBuffer.allocate(8).putLong(start + i).array();

			EntryMetadata entryMetadata = new EntryMetadata();
			entryMetadata.setVersion(newVersion);
			Entry entry = new Entry(key, data, entryMetadata);
			client.putForced(entry);

			EntryMetadata entryMetadataGet;
			entryMetadataGet = client.getMetadata(key);
			assertArrayEquals(newVersion, entryMetadataGet.getVersion());
			client.deleteForced(key);
		}
	}

	/**
	 * Test getKeyRange API with a serial of entries. The entries have already
	 * existed in simulator/drive. Both startKey and endKey are inclusive. The
	 * test result should be successful.
	 * <p>
	 *
	 * @throws KineticException
	 *             if any internal error occurred.
	 */
	public void testGetKeyRange() throws KineticException {
		List<byte[]> keys = Arrays.asList(toByteArray("00"), toByteArray("01"),
				toByteArray("02"), toByteArray("03"));

		for (byte[] key : keys) {
			client.putForced(new Entry(key, key));
		}

		List<byte[]> returnedKeys = Lists
				.newLinkedList(client.getKeyRange(keys.get(0), true,
						keys.get(keys.size() - 1), true, keys.size()));

		assertListOfArraysEqual(keys, returnedKeys);

		for (byte[] key : keys) {
			client.deleteForced(key);
		}
	}
}
