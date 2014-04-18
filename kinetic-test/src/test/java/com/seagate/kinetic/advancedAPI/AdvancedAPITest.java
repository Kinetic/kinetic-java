package com.seagate.kinetic.advancedAPI;

import static com.seagate.kinetic.KineticAssertions.assertKeyNotFound;
import static com.seagate.kinetic.KineticTestHelpers.buildSuccessOnlyCallbackHandler;
import static com.seagate.kinetic.KineticTestHelpers.int32;
import static com.seagate.kinetic.KineticTestHelpers.toByteArray;
import static com.seagate.kinetic.KineticTestHelpers.waitForLatch;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNull;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertArrayEquals;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;

import kinetic.client.CallbackHandler;
import kinetic.client.CallbackResult;
import kinetic.client.Entry;
import kinetic.client.EntryMetadata;
import kinetic.client.KineticException;
import kinetic.client.advanced.AdvancedKineticClient;
import kinetic.client.advanced.PersistOption;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.seagate.kinetic.IntegrationTestCase;
import com.seagate.kinetic.IntegrationTestLoggerFactory;
import com.seagate.kinetic.KVGenerator;
import com.seagate.kinetic.KineticTestHelpers;
import com.seagate.kinetic.KineticTestHelpers.SuccessAsyncHandler;
import com.seagate.kinetic.KineticTestRunner;

/**
 * Advanced Kinetic Client Basic API Test.
 * <p>
 * Advanced kinetic API include:
 * <p>
 * delete(Entry entry, PersistOption option)
 * <p>
 * deleteAsync(Entry entry, PersistOption option, CallbackHandler<Boolean>
 * handler)
 * <p>
 * deleteForced(byte[] key, PersistOption option)
 * <p>
 * deleteForcedAsync(byte[] key, PersistOption option, CallbackHandler<Boolean>
 * handler)
 * <p>
 * put(Entry entry, byte[] newVersion, PersistOption option)
 * <p>
 * putAsync(Entry entry, byte[] newVersion, PersistOption option,
 * CallbackHandler<Entry> handler)
 * <p>
 * putForced(Entry entry, PersistOption option)
 * <p>
 * putForcedAsync(Entry entry, PersistOption option, CallbackHandler<Entry>
 * handler)
 * <p>
 * getKeyRangeReversed(byte[] startKey, boolean startKeyInclusive, byte[]
 * endKey, boolean endKeyInclusive, int maxKeys)
 * <p>
 * getKeyRangeReversedAsync(byte[] startKey, boolean startKeyInclusive, byte[]
 * endKey, boolean endKeyInclusive, int maxKeys, CallbackHandler<List<byte[]>>
 * handler)
 * <p>
 * 
 * @see AdvancedKineticClient
 * 
 */
@RunWith(KineticTestRunner.class)
public class AdvancedAPITest extends IntegrationTestCase {
	private static final Logger logger = IntegrationTestLoggerFactory
			.getLogger(AdvancedAPITest.class.getName());

	int MAX_KEYS = 10;
	String KEY_PREFIX = "key";
	private KVGenerator kvGenerator;

	/**
	 * Initialize a key/value pair generator
	 * <p>
	 */
	@Before
	public void setUp() throws IOException, InterruptedException {
		kvGenerator = new KVGenerator();
	}

	/**
	 * Test put API with a serial of entries. Persist option is Async. Metadata
	 * with the value of tag and algorithm The test result should be successful
	 * and verify the result returned is the same as put before
	 * <p>
	 * 
	 * @throws KineticException
	 *             if any internal error occurred.
	 */
	@Test
	public void testPutWithPersistOption_Async() throws KineticException {
		PersistOption option = PersistOption.ASYNC;

		Entry versionedPut;
		Entry versionedGetReturn;
		byte[] key;
		byte[] value;
		String algorithm = "SHA1";
		Long start = System.nanoTime();

		for (int i = 0; i < MAX_KEYS; i++) {
			key = toByteArray(KEY_PREFIX + i);
			value = ByteBuffer.allocate(8).putLong(start + i).array();
			EntryMetadata entryMetadata = new EntryMetadata();
			entryMetadata.setTag(key);
			entryMetadata.setAlgorithm(algorithm);
			versionedPut = new Entry(key, value, entryMetadata);

			getClient().put(versionedPut, int32(i), option);

			versionedGetReturn = getClient().get(key);
			assertArrayEquals(key, versionedGetReturn.getKey());
			assertArrayEquals(int32(i), versionedGetReturn.getEntryMetadata()
					.getVersion());
			assertArrayEquals(value, versionedGetReturn.getValue());
			assertArrayEquals(key, versionedGetReturn.getEntryMetadata()
					.getTag());
			assertEquals("SHA1", versionedGetReturn.getEntryMetadata()
					.getAlgorithm());
		}

		logger.info(this.testEndInfo());
	}

	/**
	 * Test put API with a serial of entries. Persist option is Sync. Metadata
	 * with the value of tag and algorithm The test result should be successful
	 * and verify the result returned is the same as put before
	 * <p>
	 * 
	 * @throws KineticException
	 *             if any internal error occurred.
	 */
	@Test
	public void testPutWithPersistOption_Sync() throws KineticException {
		PersistOption option = PersistOption.SYNC;

		Entry versionedPut;
		Entry versionedGetReturn;
		byte[] key;
		byte[] value;
		String algorithm = "SHA1";
		Long start = System.nanoTime();

		for (int i = 0; i < MAX_KEYS; i++) {
			key = toByteArray(KEY_PREFIX + i);
			value = ByteBuffer.allocate(8).putLong(start + i).array();
			EntryMetadata entryMetadata = new EntryMetadata();
			entryMetadata.setTag(key);
			entryMetadata.setAlgorithm(algorithm);
			versionedPut = new Entry(key, value, entryMetadata);

			getClient().put(versionedPut, int32(i), option);

			versionedGetReturn = getClient().get(key);
			assertArrayEquals(key, versionedGetReturn.getKey());
			assertArrayEquals(int32(i), versionedGetReturn.getEntryMetadata()
					.getVersion());
			assertArrayEquals(value, versionedGetReturn.getValue());
			assertArrayEquals(key, versionedGetReturn.getEntryMetadata()
					.getTag());
			assertEquals("SHA1", versionedGetReturn.getEntryMetadata()
					.getAlgorithm());
		}

		logger.info(this.testEndInfo());
	}

	/**
	 * Test put API with a serial of entries. Persist option is Flush. Metadata
	 * with the value of tag and algorithm The test result should be successful
	 * and verify the result returned is the same as put before
	 * <p>
	 * 
	 * @throws KineticException
	 *             if any internal error occurred.
	 */
	@Test
	public void testPutWithPersistOption_Flush() throws KineticException {
		PersistOption option = PersistOption.FLUSH;

		Entry versionedPut;
		Entry versionedGetReturn;
		byte[] key;
		byte[] value;
		String algorithm = "SHA1";
		Long start = System.nanoTime();

		for (int i = 0; i < MAX_KEYS; i++) {
			key = toByteArray(KEY_PREFIX + i);
			value = ByteBuffer.allocate(8).putLong(start + i).array();
			EntryMetadata entryMetadata = new EntryMetadata();
			entryMetadata.setTag(key);
			entryMetadata.setAlgorithm(algorithm);
			versionedPut = new Entry(key, value, entryMetadata);

			getClient().put(versionedPut, int32(i), option);

			versionedGetReturn = getClient().get(key);
			assertArrayEquals(key, versionedGetReturn.getKey());
			assertArrayEquals(int32(i), versionedGetReturn.getEntryMetadata()
					.getVersion());
			assertArrayEquals(value, versionedGetReturn.getValue());
			assertArrayEquals(key, versionedGetReturn.getEntryMetadata()
					.getTag());
			assertEquals("SHA1", versionedGetReturn.getEntryMetadata()
					.getAlgorithm());
		}

		logger.info(this.testEndInfo());
	}

	/**
	 * Test putAsync with a serial of entries. Persist option is ASYNC. The test
	 * result should be successful and verify the result returned is the same as
	 * put before
	 * <p>
	 * 
	 * @throws KineticException
	 *             if any internal error occurred.
	 * @throws InterruptedException
	 *             if thread is interrupted, or the specified waiting time
	 *             elapses.
	 */
	@Test
	public void testPutAsyncWithPersistOption_Async()
			throws UnsupportedEncodingException, KineticException,
			InterruptedException {
		PersistOption option = PersistOption.ASYNC;
		byte[] newVersion = int32(0);
		final List<Entry> putReturnList = new ArrayList<Entry>(MAX_KEYS);

		final CountDownLatch putSignal = new CountDownLatch(MAX_KEYS);

		List<String> keySList = new ArrayList<String>();
		List<String> valueSList = new ArrayList<String>();
		List<EntryMetadata> emdOfList = new ArrayList<EntryMetadata>();

		for (int i = 0; i < MAX_KEYS; i++) {
			String keyS = kvGenerator.getNextKey();
			String valueS = kvGenerator.getValue(keyS);

			keySList.add(keyS);
			valueSList.add(valueS);

			EntryMetadata entryMetadata = new EntryMetadata();
			entryMetadata.setTag(keyS.getBytes());
			entryMetadata.setAlgorithm("SHA1");

			emdOfList.add(entryMetadata);
		}

		for (int i = 0; i < MAX_KEYS; i++) {
			Entry entryPut = new Entry(toByteArray(keySList.get(i)),
					toByteArray(valueSList.get(i)), emdOfList.get(i));
			CallbackHandler<Entry> handler = buildSuccessOnlyCallbackHandler(new KineticTestHelpers.SuccessAsyncHandler<Entry>() {
				@Override
				public void onSuccess(CallbackResult<Entry> result) {
					putReturnList.add(result.getResult());
					putSignal.countDown();
				}
			});

			getClient().putAsync(entryPut, newVersion, option, handler);
		}

		waitForLatch(putSignal);
		assertEquals(MAX_KEYS, putReturnList.size());
		for (int i = 0; i < MAX_KEYS; i++) {
			assertTrue(keySList.contains(new String(putReturnList.get(i)
					.getKey())));
			assertTrue(valueSList.contains(new String(putReturnList.get(i)
					.getValue())));
			assertTrue(Arrays.equals(newVersion, putReturnList.get(i)
					.getEntryMetadata().getVersion()));
			assertTrue(keySList.contains(new String(putReturnList.get(i)
					.getEntryMetadata().getTag())));
		}

		logger.info(this.testEndInfo());
	}

	/**
	 * Test putAsync with a serial of entries. Persist option is SYNC. The test
	 * result should be successful and verify the result returned is the same as
	 * put before
	 * <p>
	 * 
	 * @throws KineticException
	 *             if any internal error occurred.
	 * @throws InterruptedException
	 *             if thread is interrupted, or the specified waiting time
	 *             elapses.
	 */
	@Test
	public void testPutAsyncWithPersistOption_Sync()
			throws UnsupportedEncodingException, KineticException,
			InterruptedException {
		PersistOption option = PersistOption.SYNC;
		byte[] newVersion = int32(0);
		final List<Entry> putReturnList = new ArrayList<Entry>(MAX_KEYS);

		final CountDownLatch putSignal = new CountDownLatch(MAX_KEYS);

		List<String> keySList = new ArrayList<String>();
		List<String> valueSList = new ArrayList<String>();
		List<EntryMetadata> emdOfList = new ArrayList<EntryMetadata>();

		for (int i = 0; i < MAX_KEYS; i++) {
			String keyS = kvGenerator.getNextKey();
			String valueS = kvGenerator.getValue(keyS);

			keySList.add(keyS);
			valueSList.add(valueS);

			EntryMetadata entryMetadata = new EntryMetadata();
			entryMetadata.setTag(keyS.getBytes());
			entryMetadata.setAlgorithm("SHA1");

			emdOfList.add(entryMetadata);
		}

		for (int i = 0; i < MAX_KEYS; i++) {
			Entry entryPut = new Entry(toByteArray(keySList.get(i)),
					toByteArray(valueSList.get(i)), emdOfList.get(i));
			CallbackHandler<Entry> handler = buildSuccessOnlyCallbackHandler(new KineticTestHelpers.SuccessAsyncHandler<Entry>() {
				@Override
				public void onSuccess(CallbackResult<Entry> result) {
					putReturnList.add(result.getResult());
					putSignal.countDown();
				}
			});

			getClient().putAsync(entryPut, newVersion, option, handler);
		}

		waitForLatch(putSignal);
		assertEquals(MAX_KEYS, putReturnList.size());
		for (int i = 0; i < MAX_KEYS; i++) {
			assertTrue(keySList.contains(new String(putReturnList.get(i)
					.getKey())));
			assertTrue(valueSList.contains(new String(putReturnList.get(i)
					.getValue())));
			assertTrue(Arrays.equals(newVersion, putReturnList.get(i)
					.getEntryMetadata().getVersion()));
			assertTrue(keySList.contains(new String(putReturnList.get(i)
					.getEntryMetadata().getTag())));
		}

		logger.info(this.testEndInfo());
	}

	/**
	 * Test putAsync with a serial of entries. Persist option is Flush. The test
	 * result should be successful and verify the result returned is the same as
	 * put before
	 * <p>
	 * 
	 * @throws KineticException
	 *             if any internal error occurred.
	 * @throws InterruptedException
	 *             if thread is interrupted, or the specified waiting time
	 *             elapses.
	 */
	@Test
	public void testPutAsyncWithPersistOption_Flush()
			throws UnsupportedEncodingException, KineticException,
			InterruptedException {
		PersistOption option = PersistOption.FLUSH;
		byte[] newVersion = int32(0);
		final List<Entry> putReturnList = new ArrayList<Entry>(MAX_KEYS);

		final CountDownLatch putSignal = new CountDownLatch(MAX_KEYS);

		List<String> keySList = new ArrayList<String>();
		List<String> valueSList = new ArrayList<String>();
		List<EntryMetadata> emdOfList = new ArrayList<EntryMetadata>();

		for (int i = 0; i < MAX_KEYS; i++) {
			String keyS = kvGenerator.getNextKey();
			String valueS = kvGenerator.getValue(keyS);

			keySList.add(keyS);
			valueSList.add(valueS);

			EntryMetadata entryMetadata = new EntryMetadata();
			entryMetadata.setTag(keyS.getBytes());
			entryMetadata.setAlgorithm("SHA1");

			emdOfList.add(entryMetadata);
		}

		for (int i = 0; i < MAX_KEYS; i++) {
			Entry entryPut = new Entry(toByteArray(keySList.get(i)),
					toByteArray(valueSList.get(i)), emdOfList.get(i));
			CallbackHandler<Entry> handler = buildSuccessOnlyCallbackHandler(new KineticTestHelpers.SuccessAsyncHandler<Entry>() {
				@Override
				public void onSuccess(CallbackResult<Entry> result) {
					putReturnList.add(result.getResult());
					putSignal.countDown();
				}
			});

			getClient().putAsync(entryPut, newVersion, option, handler);
		}

		waitForLatch(putSignal);
		assertEquals(MAX_KEYS, putReturnList.size());
		for (int i = 0; i < MAX_KEYS; i++) {
			assertTrue(keySList.contains(new String(putReturnList.get(i)
					.getKey())));
			assertTrue(valueSList.contains(new String(putReturnList.get(i)
					.getValue())));
			assertTrue(Arrays.equals(newVersion, putReturnList.get(i)
					.getEntryMetadata().getVersion()));
			assertTrue(keySList.contains(new String(putReturnList.get(i)
					.getEntryMetadata().getTag())));
		}

		logger.info(this.testEndInfo());
	}

	/**
	 * Test putForced API result with a serial entries. Persist Option is Async.
	 * The entries have already existed in simulator/drive. Give new entry with
	 * db version different with version in simulator/drive, the test result
	 * should be successful.
	 * <p>
	 * 
	 * @throws KineticException
	 *             if any internal error occurred.
	 */
	@Test
	public void testPutForcedWithPersistOption_Async() throws KineticException {
		PersistOption option = PersistOption.ASYNC;
		Long start = System.nanoTime();

		for (int i = 0; i < MAX_KEYS; i++) {
			byte[] key = toByteArray(KEY_PREFIX + i);
			byte[] value = ByteBuffer.allocate(8).putLong(start + i).array();
			EntryMetadata entryMetadata = new EntryMetadata();
			Entry versionedPut = new Entry(key, value, entryMetadata);

			getClient().put(versionedPut, int32(i));
		}

		start = System.nanoTime();
		byte[] version = int32(8);
		for (int i = 0; i < MAX_KEYS; i++) {
			byte[] key = toByteArray(KEY_PREFIX + i);
			byte[] value = ByteBuffer.allocate(8).putLong(start + i).array();

			EntryMetadata entryMetadata = new EntryMetadata();

			entryMetadata.setVersion(version);

			Entry versionedPutForced = new Entry(key, value, entryMetadata);

			getClient().putForced(versionedPutForced, option);

			Entry entryGet = getClient().get(key);
			assertArrayEquals(key, entryGet.getKey());
			assertArrayEquals(version, entryGet.getEntryMetadata().getVersion());
			assertArrayEquals(value, entryGet.getValue());
		}

		logger.info(this.testEndInfo());
	}

	/**
	 * Test putForced API result with a serial entries. Persist Option is Sync.
	 * The entries have already existed in simulator/drive. Give new entry with
	 * db version different with version in simulator/drive, the test result
	 * should be successful.
	 * <p>
	 * 
	 * @throws KineticException
	 *             if any internal error occurred.
	 */
	@Test
	public void testPutForcedWithPersistOption_Sync() throws KineticException {
		PersistOption option = PersistOption.SYNC;
		Long start = System.nanoTime();

		for (int i = 0; i < MAX_KEYS; i++) {
			byte[] key = toByteArray(KEY_PREFIX + i);
			byte[] value = ByteBuffer.allocate(8).putLong(start + i).array();
			EntryMetadata entryMetadata = new EntryMetadata();
			Entry versionedPut = new Entry(key, value, entryMetadata);

			getClient().put(versionedPut, int32(i));
		}

		start = System.nanoTime();
		byte[] version = int32(8);
		for (int i = 0; i < MAX_KEYS; i++) {
			byte[] key = toByteArray(KEY_PREFIX + i);
			byte[] value = ByteBuffer.allocate(8).putLong(start + i).array();

			EntryMetadata entryMetadata = new EntryMetadata();

			entryMetadata.setVersion(version);

			Entry versionedPutForced = new Entry(key, value, entryMetadata);

			getClient().putForced(versionedPutForced, option);

			Entry entryGet = getClient().get(key);
			assertArrayEquals(key, entryGet.getKey());
			assertArrayEquals(version, entryGet.getEntryMetadata().getVersion());
			assertArrayEquals(value, entryGet.getValue());
		}

		logger.info(this.testEndInfo());
	}

	/**
	 * Test putForced API result with a serial entries. Persist Option is Flush.
	 * The entries have already existed in simulator/drive. Give new entry with
	 * db version different with version in simulator/drive, the test result
	 * should be successful.
	 * <p>
	 * 
	 * @throws KineticException
	 *             if any internal error occurred.
	 */
	@Test
	public void testPutForcedWithPersistOption_Flush() throws KineticException {
		PersistOption option = PersistOption.FLUSH;
		Long start = System.nanoTime();

		for (int i = 0; i < MAX_KEYS; i++) {
			byte[] key = toByteArray(KEY_PREFIX + i);
			byte[] value = ByteBuffer.allocate(8).putLong(start + i).array();
			EntryMetadata entryMetadata = new EntryMetadata();
			Entry versionedPut = new Entry(key, value, entryMetadata);

			getClient().put(versionedPut, int32(i));
		}

		start = System.nanoTime();
		byte[] version = int32(8);
		for (int i = 0; i < MAX_KEYS; i++) {
			byte[] key = toByteArray(KEY_PREFIX + i);
			byte[] value = ByteBuffer.allocate(8).putLong(start + i).array();

			EntryMetadata entryMetadata = new EntryMetadata();

			entryMetadata.setVersion(version);

			Entry versionedPutForced = new Entry(key, value, entryMetadata);

			getClient().putForced(versionedPutForced, option);

			Entry entryGet = getClient().get(key);
			assertArrayEquals(key, entryGet.getKey());
			assertArrayEquals(version, entryGet.getEntryMetadata().getVersion());
			assertArrayEquals(value, entryGet.getValue());
		}

		logger.info(this.testEndInfo());
	}

	/**
	 * Test putForcedAsync with a serial of entries. Persist option is Async.
	 * The test result should be successful and verify the result get is the
	 * same as put before
	 * <p>
	 * 
	 * @throws KineticException
	 *             if any internal error occurred.
	 * @throws InterruptedException
	 *             if thread is interrupted, or the specified waiting time
	 *             elapses.
	 */
	@Test
	public void testPutForcedAsyncWithPersistOption_Async()
			throws UnsupportedEncodingException, KineticException,
			InterruptedException {
		PersistOption option = PersistOption.ASYNC;
		byte[] newVersion = int32(0);

		final CountDownLatch putSignal = new CountDownLatch(MAX_KEYS);

		List<String> keySList = new ArrayList<String>();
		List<String> valueSList = new ArrayList<String>();
		List<EntryMetadata> emdOfList = new ArrayList<EntryMetadata>();

		for (int i = 0; i < MAX_KEYS; i++) {
			String keyS = kvGenerator.getNextKey();
			String valueS = kvGenerator.getValue(keyS);

			keySList.add(keyS);
			valueSList.add(valueS);

			EntryMetadata entryMetadata = new EntryMetadata();
			entryMetadata.setTag(keyS.getBytes());
			entryMetadata.setAlgorithm("SHA1");

			emdOfList.add(entryMetadata);
		}

		for (int i = 0; i < MAX_KEYS; i++) {
			Entry entryPut = new Entry(toByteArray(keySList.get(i)),
					toByteArray(valueSList.get(i)), emdOfList.get(i));
			CallbackHandler<Entry> handler = buildSuccessOnlyCallbackHandler(new KineticTestHelpers.SuccessAsyncHandler<Entry>() {
				@Override
				public void onSuccess(CallbackResult<Entry> result) {
					putSignal.countDown();
				}
			});

			getClient().putAsync(entryPut, newVersion, handler);
		}

		waitForLatch(putSignal);

		final List<Entry> putReturnList = new ArrayList<Entry>(MAX_KEYS);
		final CountDownLatch putForcedSignal = new CountDownLatch(MAX_KEYS);

		byte[] forcedVersion = int32(1);
		emdOfList = new ArrayList<EntryMetadata>();
		byte[] tag = toByteArray("tag");
		for (int i = 0; i < MAX_KEYS; i++) {

			EntryMetadata entryMetadata = new EntryMetadata();
			entryMetadata.setVersion(forcedVersion);
			entryMetadata.setTag(tag);
			entryMetadata.setAlgorithm("SHA1");

			emdOfList.add(entryMetadata);
		}

		for (int i = 0; i < MAX_KEYS; i++) {
			Entry entryPut = new Entry(toByteArray(keySList.get(i)),
					toByteArray(valueSList.get(i)), emdOfList.get(i));
			CallbackHandler<Entry> handler = buildSuccessOnlyCallbackHandler(new KineticTestHelpers.SuccessAsyncHandler<Entry>() {
				@Override
				public void onSuccess(CallbackResult<Entry> result) {
					putReturnList.add(result.getResult());
					putForcedSignal.countDown();
				}
			});

			getClient().putForcedAsync(entryPut, option, handler);
		}

		waitForLatch(putForcedSignal);
		assertEquals(MAX_KEYS, putReturnList.size());

		for (int i = 0; i < MAX_KEYS; i++) {
			Entry getReturned = getClient().get(toByteArray(keySList.get(i)));

			assertTrue(keySList.contains(new String(getReturned.getKey())));
			assertTrue(valueSList.contains(new String(getReturned.getValue())));
			assertTrue(Arrays.equals(forcedVersion, getReturned
					.getEntryMetadata().getVersion()));
			assertTrue(Arrays.equals(tag, getReturned.getEntryMetadata()
					.getTag()));
		}

		logger.info(this.testEndInfo());
	}

	/**
	 * Test putForcedAsync with a serial of entries. Persist option is Sync. The
	 * test result should be successful and verify the result get is the same as
	 * put before
	 * <p>
	 * 
	 * @throws KineticException
	 *             if any internal error occurred.
	 * @throws InterruptedException
	 *             if thread is interrupted, or the specified waiting time
	 *             elapses.
	 */
	@Test
	public void testPutForcedAsyncWithPersistOption_Sync()
			throws UnsupportedEncodingException, KineticException,
			InterruptedException {
		PersistOption option = PersistOption.SYNC;
		byte[] newVersion = int32(0);

		final CountDownLatch putSignal = new CountDownLatch(MAX_KEYS);

		List<String> keySList = new ArrayList<String>();
		List<String> valueSList = new ArrayList<String>();
		List<EntryMetadata> emdOfList = new ArrayList<EntryMetadata>();

		for (int i = 0; i < MAX_KEYS; i++) {
			String keyS = kvGenerator.getNextKey();
			String valueS = kvGenerator.getValue(keyS);

			keySList.add(keyS);
			valueSList.add(valueS);

			EntryMetadata entryMetadata = new EntryMetadata();
			entryMetadata.setTag(keyS.getBytes());
			entryMetadata.setAlgorithm("SHA1");

			emdOfList.add(entryMetadata);
		}

		for (int i = 0; i < MAX_KEYS; i++) {
			Entry entryPut = new Entry(toByteArray(keySList.get(i)),
					toByteArray(valueSList.get(i)), emdOfList.get(i));
			CallbackHandler<Entry> handler = buildSuccessOnlyCallbackHandler(new KineticTestHelpers.SuccessAsyncHandler<Entry>() {
				@Override
				public void onSuccess(CallbackResult<Entry> result) {
					putSignal.countDown();
				}
			});

			getClient().putAsync(entryPut, newVersion, handler);
		}

		waitForLatch(putSignal);

		final List<Entry> putReturnList = new ArrayList<Entry>(MAX_KEYS);
		final CountDownLatch putForcedSignal = new CountDownLatch(MAX_KEYS);

		byte[] forcedVersion = int32(1);
		emdOfList = new ArrayList<EntryMetadata>();
		byte[] tag = toByteArray("tag");
		for (int i = 0; i < MAX_KEYS; i++) {

			EntryMetadata entryMetadata = new EntryMetadata();
			entryMetadata.setVersion(forcedVersion);
			entryMetadata.setTag(tag);
			entryMetadata.setAlgorithm("SHA1");

			emdOfList.add(entryMetadata);
		}

		for (int i = 0; i < MAX_KEYS; i++) {
			Entry entryPut = new Entry(toByteArray(keySList.get(i)),
					toByteArray(valueSList.get(i)), emdOfList.get(i));
			CallbackHandler<Entry> handler = buildSuccessOnlyCallbackHandler(new KineticTestHelpers.SuccessAsyncHandler<Entry>() {
				@Override
				public void onSuccess(CallbackResult<Entry> result) {
					putReturnList.add(result.getResult());
					putForcedSignal.countDown();
				}
			});

			getClient().putForcedAsync(entryPut, option, handler);
		}

		waitForLatch(putForcedSignal);
		assertEquals(MAX_KEYS, putReturnList.size());

		for (int i = 0; i < MAX_KEYS; i++) {
			Entry getReturned = getClient().get(toByteArray(keySList.get(i)));

			assertTrue(keySList.contains(new String(getReturned.getKey())));
			assertTrue(valueSList.contains(new String(getReturned.getValue())));
			assertTrue(Arrays.equals(forcedVersion, getReturned
					.getEntryMetadata().getVersion()));
			assertTrue(Arrays.equals(tag, getReturned.getEntryMetadata()
					.getTag()));
		}

		logger.info(this.testEndInfo());
	}

	/**
	 * Test putForcedAsync with a serial of entries. Persist option is Flush.
	 * The test result should be successful and verify the result get is the
	 * same as put before
	 * <p>
	 * 
	 * @throws KineticException
	 *             if any internal error occurred.
	 * @throws InterruptedException
	 *             if thread is interrupted, or the specified waiting time
	 *             elapses.
	 */
	@Test
	public void testPutForcedAsyncWithPersistOption_Flush()
			throws UnsupportedEncodingException, KineticException,
			InterruptedException {
		PersistOption option = PersistOption.FLUSH;
		byte[] newVersion = int32(0);

		final CountDownLatch putSignal = new CountDownLatch(MAX_KEYS);

		List<String> keySList = new ArrayList<String>();
		List<String> valueSList = new ArrayList<String>();
		List<EntryMetadata> emdOfList = new ArrayList<EntryMetadata>();

		for (int i = 0; i < MAX_KEYS; i++) {
			String keyS = kvGenerator.getNextKey();
			String valueS = kvGenerator.getValue(keyS);

			keySList.add(keyS);
			valueSList.add(valueS);

			EntryMetadata entryMetadata = new EntryMetadata();
			entryMetadata.setTag(keyS.getBytes());
			entryMetadata.setAlgorithm("SHA1");

			emdOfList.add(entryMetadata);
		}

		for (int i = 0; i < MAX_KEYS; i++) {
			Entry entryPut = new Entry(toByteArray(keySList.get(i)),
					toByteArray(valueSList.get(i)), emdOfList.get(i));
			CallbackHandler<Entry> handler = buildSuccessOnlyCallbackHandler(new KineticTestHelpers.SuccessAsyncHandler<Entry>() {
				@Override
				public void onSuccess(CallbackResult<Entry> result) {
					putSignal.countDown();
				}
			});

			getClient().putAsync(entryPut, newVersion, handler);
		}

		waitForLatch(putSignal);

		final List<Entry> putReturnList = new ArrayList<Entry>(MAX_KEYS);
		final CountDownLatch putForcedSignal = new CountDownLatch(MAX_KEYS);

		byte[] forcedVersion = int32(1);
		emdOfList = new ArrayList<EntryMetadata>();
		byte[] tag = toByteArray("tag");
		for (int i = 0; i < MAX_KEYS; i++) {

			EntryMetadata entryMetadata = new EntryMetadata();
			entryMetadata.setVersion(forcedVersion);
			entryMetadata.setTag(tag);
			entryMetadata.setAlgorithm("SHA1");

			emdOfList.add(entryMetadata);
		}

		for (int i = 0; i < MAX_KEYS; i++) {
			Entry entryPut = new Entry(toByteArray(keySList.get(i)),
					toByteArray(valueSList.get(i)), emdOfList.get(i));
			CallbackHandler<Entry> handler = buildSuccessOnlyCallbackHandler(new KineticTestHelpers.SuccessAsyncHandler<Entry>() {
				@Override
				public void onSuccess(CallbackResult<Entry> result) {
					putReturnList.add(result.getResult());
					putForcedSignal.countDown();
				}
			});

			getClient().putForcedAsync(entryPut, option, handler);
		}

		waitForLatch(putForcedSignal);
		assertEquals(MAX_KEYS, putReturnList.size());

		for (int i = 0; i < MAX_KEYS; i++) {
			Entry getReturned = getClient().get(toByteArray(keySList.get(i)));

			assertTrue(keySList.contains(new String(getReturned.getKey())));
			assertTrue(valueSList.contains(new String(getReturned.getValue())));
			assertTrue(Arrays.equals(forcedVersion, getReturned
					.getEntryMetadata().getVersion()));
			assertTrue(Arrays.equals(tag, getReturned.getEntryMetadata()
					.getTag()));
		}

		logger.info(this.testEndInfo());
	}

	/**
	 * Test delete API with a serial of entries. Persist option is Async. The
	 * entries have already existed in simulator/drive. The test result should
	 * be true. Try to get key to verify the results is null after delete.
	 * <p>
	 * 
	 * @throws KineticException
	 *             if any internal error occurred.
	 */
	@Test
	public void testDeleteWithPersitOption_Async() throws KineticException {
		PersistOption option = PersistOption.ASYNC;

		Long start = System.nanoTime();

		for (int i = 0; i < MAX_KEYS; i++) {
			byte[] key = toByteArray(KEY_PREFIX + i);
			byte[] value = ByteBuffer.allocate(8).putLong(start + i).array();
			EntryMetadata entryMetadata = new EntryMetadata();
			Entry versionedPut = new Entry(key, value, entryMetadata);

			Entry versionedGet = getClient().put(versionedPut, int32(i));
			assertTrue(getClient().delete(versionedGet, option));
		}

		for (int i = 0; i < MAX_KEYS; i++) {
			assertKeyNotFound(getClient(), toByteArray(KEY_PREFIX + i));
		}

		logger.info(this.testEndInfo());
	}

	/**
	 * Test delete API with a serial of entries. Persist option is Sync. The
	 * entries have already existed in simulator/drive. The test result should
	 * be true. Try to get key to verify the results is null after delete.
	 * <p>
	 * 
	 * @throws KineticException
	 *             if any internal error occurred.
	 */
	@Test
	public void testDeleteWithPersitOption_Sync() throws KineticException {
		PersistOption option = PersistOption.SYNC;

		Long start = System.nanoTime();

		for (int i = 0; i < MAX_KEYS; i++) {
			byte[] key = toByteArray(KEY_PREFIX + i);
			byte[] value = ByteBuffer.allocate(8).putLong(start + i).array();
			EntryMetadata entryMetadata = new EntryMetadata();
			Entry versionedPut = new Entry(key, value, entryMetadata);

			Entry versionedGet = getClient().put(versionedPut, int32(i));
			assertTrue(getClient().delete(versionedGet, option));
		}

		for (int i = 0; i < MAX_KEYS; i++) {
			assertKeyNotFound(getClient(), toByteArray(KEY_PREFIX + i));
		}

		logger.info(this.testEndInfo());
	}

	/**
	 * Test delete API with a serial of entries. Persist option is Flush. The
	 * entries have already existed in simulator/drive. The test result should
	 * be true. Try to get key to verify the results is null after delete.
	 * <p>
	 * 
	 * @throws KineticException
	 *             if any internal error occurred.
	 */
	@Test
	public void testDeleteWithPersitOption_Flush() throws KineticException {
		PersistOption option = PersistOption.FLUSH;

		Long start = System.nanoTime();

		for (int i = 0; i < MAX_KEYS; i++) {
			byte[] key = toByteArray(KEY_PREFIX + i);
			byte[] value = ByteBuffer.allocate(8).putLong(start + i).array();
			EntryMetadata entryMetadata = new EntryMetadata();
			Entry versionedPut = new Entry(key, value, entryMetadata);

			Entry versionedGet = getClient().put(versionedPut, int32(i));
			assertTrue(getClient().delete(versionedGet, option));
		}

		for (int i = 0; i < MAX_KEYS; i++) {
			assertKeyNotFound(getClient(), toByteArray(KEY_PREFIX + i));
		}

		logger.info(this.testEndInfo());
	}

	/**
	 * Test deleteAsync API with a serial of entries. Persist option is Async.
	 * The entries have already existed in simulator/drive. The test result
	 * should be true. Try to get key to verify the results is null after
	 * delete.
	 * <p>
	 * 
	 * @throws KineticException
	 *             if any internal error occurred.
	 * @throws InterruptedException
	 *             if thread is interrupted, or the specified waiting time
	 *             elapses.
	 */
	@Test
	public void testDeleteAsyncWithPersistOption_Async()
			throws UnsupportedEncodingException, KineticException,
			InterruptedException {
		PersistOption option = PersistOption.ASYNC;

		byte[] newVersion = int32(0);
		final List<Boolean> deleteReturnList = new ArrayList<Boolean>(MAX_KEYS);
		final List<Entry> getReturnList = new ArrayList<Entry>(MAX_KEYS);

		final CountDownLatch putSignal = new CountDownLatch(MAX_KEYS);
		final CountDownLatch getSignal = new CountDownLatch(MAX_KEYS);
		final CountDownLatch deleteSignal = new CountDownLatch(MAX_KEYS);

		List<String> keySList = new ArrayList<String>();
		List<String> valueSList = new ArrayList<String>();
		EntryMetadata entryMetadata = new EntryMetadata();
		for (int i = 0; i < MAX_KEYS; i++) {
			String keyS = kvGenerator.getNextKey();
			String valueS = kvGenerator.getValue(keyS);

			keySList.add(keyS);
			valueSList.add(valueS);
		}

		for (int i = 0; i < MAX_KEYS; i++) {
			Entry entryPut = new Entry(toByteArray(keySList.get(i)),
					toByteArray(valueSList.get(i)), entryMetadata);
			CallbackHandler<Entry> handler = buildSuccessOnlyCallbackHandler(new SuccessAsyncHandler<Entry>() {
				@Override
				public void onSuccess(CallbackResult<Entry> result) {
					putSignal.countDown();
				}
			});

			getClient().putAsync(entryPut, newVersion, handler);
		}
		waitForLatch(putSignal);

		for (int i = 0; i < MAX_KEYS; i++) {
			CallbackHandler<Boolean> handler = buildSuccessOnlyCallbackHandler(new KineticTestHelpers.SuccessAsyncHandler<Boolean>() {
				@Override
				public void onSuccess(CallbackResult<Boolean> result) {
					deleteReturnList.add(result.getResult());
					deleteSignal.countDown();
				}
			});

			byte[] key = toByteArray(keySList.get(i));
			byte[] value = toByteArray(valueSList.get(i));
			EntryMetadata entryMetadataDelete = new EntryMetadata();
			entryMetadataDelete.setVersion(newVersion);
			Entry deleteEntry = new Entry(key, value, entryMetadataDelete);

			getClient().deleteAsync(deleteEntry, option, handler);
		}
		waitForLatch(deleteSignal);

		assertEquals(MAX_KEYS, deleteReturnList.size());
		for (int i = 0; i < MAX_KEYS; i++) {
			assertTrue(deleteReturnList.get(i));
		}

		// verify the delete result
		for (int i = 0; i < MAX_KEYS; i++) {
			CallbackHandler<Entry> handler = buildSuccessOnlyCallbackHandler(new SuccessAsyncHandler<Entry>() {
				@Override
				public void onSuccess(CallbackResult<Entry> result) {
					getReturnList.add(result.getResult());
					getSignal.countDown();
				}
			});

			getClient().getAsync(toByteArray(keySList.get(i)), handler);
		}
		waitForLatch(getSignal);

		for (int i = 0; i < MAX_KEYS; i++) {
			assertNull(getReturnList.get(i));
		}

		logger.info(this.testEndInfo());
	}

	/**
	 * Test deleteAsync API with a serial of entries. Persist option is Sync.
	 * The entries have already existed in simulator/drive. The test result
	 * should be true. Try to get key to verify the results is null after
	 * delete.
	 * <p>
	 * 
	 * @throws KineticException
	 *             if any internal error occurred.
	 * @throws InterruptedException
	 *             if thread is interrupted, or the specified waiting time
	 *             elapses.
	 */
	@Test
	public void testDeleteAsyncWithPersistOption_Sync()
			throws UnsupportedEncodingException, KineticException,
			InterruptedException {

		PersistOption option = PersistOption.SYNC;

		byte[] newVersion = int32(0);
		final List<Boolean> deleteReturnList = new ArrayList<Boolean>(MAX_KEYS);
		final List<Entry> getReturnList = new ArrayList<Entry>(MAX_KEYS);

		final CountDownLatch putSignal = new CountDownLatch(MAX_KEYS);
		final CountDownLatch getSignal = new CountDownLatch(MAX_KEYS);
		final CountDownLatch deleteSignal = new CountDownLatch(MAX_KEYS);

		List<String> keySList = new ArrayList<String>();
		List<String> valueSList = new ArrayList<String>();
		EntryMetadata entryMetadata = new EntryMetadata();
		for (int i = 0; i < MAX_KEYS; i++) {
			String keyS = kvGenerator.getNextKey();
			String valueS = kvGenerator.getValue(keyS);

			keySList.add(keyS);
			valueSList.add(valueS);
		}

		for (int i = 0; i < MAX_KEYS; i++) {
			Entry entryPut = new Entry(toByteArray(keySList.get(i)),
					toByteArray(valueSList.get(i)), entryMetadata);
			CallbackHandler<Entry> handler = buildSuccessOnlyCallbackHandler(new SuccessAsyncHandler<Entry>() {
				@Override
				public void onSuccess(CallbackResult<Entry> result) {
					putSignal.countDown();
				}
			});

			getClient().putAsync(entryPut, newVersion, handler);
		}
		waitForLatch(putSignal);

		for (int i = 0; i < MAX_KEYS; i++) {
			CallbackHandler<Boolean> handler = buildSuccessOnlyCallbackHandler(new KineticTestHelpers.SuccessAsyncHandler<Boolean>() {
				@Override
				public void onSuccess(CallbackResult<Boolean> result) {
					deleteReturnList.add(result.getResult());
					deleteSignal.countDown();
				}
			});

			byte[] key = toByteArray(keySList.get(i));
			byte[] value = toByteArray(valueSList.get(i));
			EntryMetadata entryMetadataDelete = new EntryMetadata();
			entryMetadataDelete.setVersion(newVersion);
			Entry deleteEntry = new Entry(key, value, entryMetadataDelete);

			getClient().deleteAsync(deleteEntry, option, handler);
		}
		waitForLatch(deleteSignal);

		assertEquals(MAX_KEYS, deleteReturnList.size());
		for (int i = 0; i < MAX_KEYS; i++) {
			assertTrue(deleteReturnList.get(i));
		}

		// verify the delete result
		for (int i = 0; i < MAX_KEYS; i++) {
			CallbackHandler<Entry> handler = buildSuccessOnlyCallbackHandler(new SuccessAsyncHandler<Entry>() {
				@Override
				public void onSuccess(CallbackResult<Entry> result) {
					getReturnList.add(result.getResult());
					getSignal.countDown();
				}
			});

			getClient().getAsync(toByteArray(keySList.get(i)), handler);
		}
		waitForLatch(getSignal);

		for (int i = 0; i < MAX_KEYS; i++) {
			assertNull(getReturnList.get(i));
		}

		logger.info(this.testEndInfo());
	}

	/**
	 * Test deleteAsync API with a serial of entries. Persist option is Flush.
	 * The entries have already existed in simulator/drive. The test result
	 * should be true. Try to get key to verify the results is null after
	 * delete.
	 * <p>
	 * 
	 * @throws KineticException
	 *             if any internal error occurred.
	 * @throws InterruptedException
	 *             if thread is interrupted, or the specified waiting time
	 *             elapses.
	 */
	@Test
	public void testDeleteAsyncWithPersistOption_Flush()
			throws UnsupportedEncodingException, KineticException,
			InterruptedException {
		PersistOption option = PersistOption.FLUSH;

		byte[] newVersion = int32(0);
		final List<Boolean> deleteReturnList = new ArrayList<Boolean>(MAX_KEYS);
		final List<Entry> getReturnList = new ArrayList<Entry>(MAX_KEYS);

		final CountDownLatch putSignal = new CountDownLatch(MAX_KEYS);
		final CountDownLatch getSignal = new CountDownLatch(MAX_KEYS);
		final CountDownLatch deleteSignal = new CountDownLatch(MAX_KEYS);

		List<String> keySList = new ArrayList<String>();
		List<String> valueSList = new ArrayList<String>();
		EntryMetadata entryMetadata = new EntryMetadata();
		for (int i = 0; i < MAX_KEYS; i++) {
			String keyS = kvGenerator.getNextKey();
			String valueS = kvGenerator.getValue(keyS);

			keySList.add(keyS);
			valueSList.add(valueS);
		}

		for (int i = 0; i < MAX_KEYS; i++) {
			Entry entryPut = new Entry(toByteArray(keySList.get(i)),
					toByteArray(valueSList.get(i)), entryMetadata);
			CallbackHandler<Entry> handler = buildSuccessOnlyCallbackHandler(new SuccessAsyncHandler<Entry>() {
				@Override
				public void onSuccess(CallbackResult<Entry> result) {
					putSignal.countDown();
				}
			});

			getClient().putAsync(entryPut, newVersion, handler);
		}
		waitForLatch(putSignal);

		for (int i = 0; i < MAX_KEYS; i++) {
			CallbackHandler<Boolean> handler = buildSuccessOnlyCallbackHandler(new KineticTestHelpers.SuccessAsyncHandler<Boolean>() {
				@Override
				public void onSuccess(CallbackResult<Boolean> result) {
					deleteReturnList.add(result.getResult());
					deleteSignal.countDown();
				}
			});

			byte[] key = toByteArray(keySList.get(i));
			byte[] value = toByteArray(valueSList.get(i));
			EntryMetadata entryMetadataDelete = new EntryMetadata();
			entryMetadataDelete.setVersion(newVersion);
			Entry deleteEntry = new Entry(key, value, entryMetadataDelete);

			getClient().deleteAsync(deleteEntry, option, handler);
		}
		waitForLatch(deleteSignal);

		assertEquals(MAX_KEYS, deleteReturnList.size());
		for (int i = 0; i < MAX_KEYS; i++) {
			assertTrue(deleteReturnList.get(i));
		}

		// verify the delete result
		for (int i = 0; i < MAX_KEYS; i++) {
			CallbackHandler<Entry> handler = buildSuccessOnlyCallbackHandler(new SuccessAsyncHandler<Entry>() {
				@Override
				public void onSuccess(CallbackResult<Entry> result) {
					getReturnList.add(result.getResult());
					getSignal.countDown();
				}
			});

			getClient().getAsync(toByteArray(keySList.get(i)), handler);
		}
		waitForLatch(getSignal);

		for (int i = 0; i < MAX_KEYS; i++) {
			assertNull(getReturnList.get(i));
		}

		logger.info(this.testEndInfo());
	}

	/**
	 * Test deleteForced API result with a serial entries. Persist option is
	 * Async. The entries have already existed in simulator/drive. The test
	 * result should be true. Verify get the key is null after deleteForced.
	 * <p>
	 * 
	 * @throws KineticException
	 *             if any internal error occurred.
	 */
	@Test
	public void testDeleteForcedWithPersitOption_Async()
			throws KineticException {
		PersistOption option = PersistOption.ASYNC;
		Long start = System.nanoTime();

		for (int i = 0; i < MAX_KEYS; i++) {
			byte[] key = toByteArray(KEY_PREFIX + i);
			byte[] value = ByteBuffer.allocate(8).putLong(start + i).array();
			EntryMetadata entryMetadata = new EntryMetadata();
			Entry versionedPut = new Entry(key, value, entryMetadata);

			getClient().put(versionedPut, int32(i));

			assertTrue(getClient().deleteForced(key, option));
			assertKeyNotFound(getClient(), key);
		}

		logger.info(this.testEndInfo());
	}

	/**
	 * Test deleteForced API result with a serial entries. Persist option is
	 * Sync. The entries have already existed in simulator/drive. The test
	 * result should be true. Verify get the key is null after deleteForced.
	 * <p>
	 * 
	 * @throws KineticException
	 *             if any internal error occurred.
	 */
	@Test
	public void testDeleteForcedWithPersitOption_Sync() throws KineticException {
		PersistOption option = PersistOption.SYNC;
		Long start = System.nanoTime();

		for (int i = 0; i < MAX_KEYS; i++) {
			byte[] key = toByteArray(KEY_PREFIX + i);
			byte[] value = ByteBuffer.allocate(8).putLong(start + i).array();
			EntryMetadata entryMetadata = new EntryMetadata();
			Entry versionedPut = new Entry(key, value, entryMetadata);

			getClient().put(versionedPut, int32(i));

			assertTrue(getClient().deleteForced(key, option));
			assertKeyNotFound(getClient(), key);
		}

		logger.info(this.testEndInfo());
	}

	/**
	 * Test deleteForced API result with a serial entries. Persist option is
	 * Flush. The entries have already existed in simulator/drive. The test
	 * result should be true. Verify get the key is null after deleteForced.
	 * <p>
	 * 
	 * @throws KineticException
	 *             if any internal error occurred.
	 */
	@Test
	public void testDeleteForcedWithPersitOption_Flush()
			throws KineticException {
		PersistOption option = PersistOption.FLUSH;
		Long start = System.nanoTime();

		for (int i = 0; i < MAX_KEYS; i++) {
			byte[] key = toByteArray(KEY_PREFIX + i);
			byte[] value = ByteBuffer.allocate(8).putLong(start + i).array();
			EntryMetadata entryMetadata = new EntryMetadata();
			Entry versionedPut = new Entry(key, value, entryMetadata);

			getClient().put(versionedPut, int32(i));

			assertTrue(getClient().deleteForced(key, option));
			assertKeyNotFound(getClient(), key);
		}

		logger.info(this.testEndInfo());
	}

	/**
	 * Test deleteForcedAsync with a serial of entries. Persist option is Async.
	 * The entries have existed in simulator/drive.The test result should be
	 * successful and verify the result get is null.
	 * <p>
	 * 
	 * @throws KineticException
	 *             if any internal error occurred.
	 * @throws InterruptedException
	 *             if thread is interrupted, or the specified waiting time
	 *             elapses.
	 */
	@Test
	public void testDeleteForcedAsyncWithPersistOption_Async()
			throws UnsupportedEncodingException, KineticException,
			InterruptedException {
		PersistOption option = PersistOption.ASYNC;
		byte[] newVersion = int32(0);

		final CountDownLatch putSignal = new CountDownLatch(MAX_KEYS);

		List<String> keySList = new ArrayList<String>();
		List<String> valueSList = new ArrayList<String>();
		List<EntryMetadata> emdOfList = new ArrayList<EntryMetadata>();

		for (int i = 0; i < MAX_KEYS; i++) {
			String keyS = kvGenerator.getNextKey();
			String valueS = kvGenerator.getValue(keyS);

			keySList.add(keyS);
			valueSList.add(valueS);

			EntryMetadata entryMetadata = new EntryMetadata();
			entryMetadata.setTag(keyS.getBytes());
			entryMetadata.setAlgorithm("SHA1");

			emdOfList.add(entryMetadata);
		}

		for (int i = 0; i < MAX_KEYS; i++) {
			Entry entryPut = new Entry(toByteArray(keySList.get(i)),
					toByteArray(valueSList.get(i)), emdOfList.get(i));
			CallbackHandler<Entry> handler = buildSuccessOnlyCallbackHandler(new KineticTestHelpers.SuccessAsyncHandler<Entry>() {
				@Override
				public void onSuccess(CallbackResult<Entry> result) {
					putSignal.countDown();
				}
			});

			getClient().putAsync(entryPut, newVersion, handler);
		}

		waitForLatch(putSignal);

		final List<Boolean> deleteReturnList = new ArrayList<Boolean>(MAX_KEYS);
		final CountDownLatch deleteForcedSignal = new CountDownLatch(MAX_KEYS);

		for (int i = 0; i < MAX_KEYS; i++) {
			byte[] key = toByteArray(keySList.get(i));

			CallbackHandler<Boolean> handler = buildSuccessOnlyCallbackHandler(new KineticTestHelpers.SuccessAsyncHandler<Boolean>() {
				@Override
				public void onSuccess(CallbackResult<Boolean> result) {
					deleteReturnList.add(result.getResult());
					deleteForcedSignal.countDown();
				}
			});

			getClient().deleteForcedAsync(key, option, handler);
		}

		waitForLatch(deleteForcedSignal);
		assertEquals(MAX_KEYS, deleteReturnList.size());

		for (int i = 0; i < MAX_KEYS; i++) {
			assertTrue(deleteReturnList.get(i));

			Entry getReturned = getClient().get(toByteArray(keySList.get(i)));

			assertNull(getReturned);
		}

		logger.info(this.testEndInfo());
	}

	/**
	 * Test deleteForcedAsync with a serial of entries. Persist option is Sync.
	 * The entries have existed in simulator/drive.The test result should be
	 * successful and verify the result get is null.
	 * <p>
	 * 
	 * @throws KineticException
	 *             if any internal error occurred.
	 * @throws InterruptedException
	 *             if thread is interrupted, or the specified waiting time
	 *             elapses.
	 */
	@Test
	public void testDeleteForcedAsyncWithPersistOption_Sync()
			throws UnsupportedEncodingException, KineticException,
			InterruptedException {
		PersistOption option = PersistOption.SYNC;
		byte[] newVersion = int32(0);

		final CountDownLatch putSignal = new CountDownLatch(MAX_KEYS);

		List<String> keySList = new ArrayList<String>();
		List<String> valueSList = new ArrayList<String>();
		List<EntryMetadata> emdOfList = new ArrayList<EntryMetadata>();

		for (int i = 0; i < MAX_KEYS; i++) {
			String keyS = kvGenerator.getNextKey();
			String valueS = kvGenerator.getValue(keyS);

			keySList.add(keyS);
			valueSList.add(valueS);

			EntryMetadata entryMetadata = new EntryMetadata();
			entryMetadata.setTag(keyS.getBytes());
			entryMetadata.setAlgorithm("SHA1");

			emdOfList.add(entryMetadata);
		}

		for (int i = 0; i < MAX_KEYS; i++) {
			Entry entryPut = new Entry(toByteArray(keySList.get(i)),
					toByteArray(valueSList.get(i)), emdOfList.get(i));
			CallbackHandler<Entry> handler = buildSuccessOnlyCallbackHandler(new KineticTestHelpers.SuccessAsyncHandler<Entry>() {
				@Override
				public void onSuccess(CallbackResult<Entry> result) {
					putSignal.countDown();
				}
			});

			getClient().putAsync(entryPut, newVersion, handler);
		}

		waitForLatch(putSignal);

		final List<Boolean> deleteReturnList = new ArrayList<Boolean>(MAX_KEYS);
		final CountDownLatch deleteForcedSignal = new CountDownLatch(MAX_KEYS);

		for (int i = 0; i < MAX_KEYS; i++) {
			byte[] key = toByteArray(keySList.get(i));

			CallbackHandler<Boolean> handler = buildSuccessOnlyCallbackHandler(new KineticTestHelpers.SuccessAsyncHandler<Boolean>() {
				@Override
				public void onSuccess(CallbackResult<Boolean> result) {
					deleteReturnList.add(result.getResult());
					deleteForcedSignal.countDown();
				}
			});

			getClient().deleteForcedAsync(key, option, handler);
		}

		waitForLatch(deleteForcedSignal);
		assertEquals(MAX_KEYS, deleteReturnList.size());

		for (int i = 0; i < MAX_KEYS; i++) {
			assertTrue(deleteReturnList.get(i));

			Entry getReturned = getClient().get(toByteArray(keySList.get(i)));

			assertNull(getReturned);
		}

		logger.info(this.testEndInfo());
	}

	/**
	 * Test deleteForcedAsync with a serial of entries. Persist option is Flush.
	 * The entries have existed in simulator/drive.The test result should be
	 * successful and verify the result get is null.
	 * <p>
	 * 
	 * @throws KineticException
	 *             if any internal error occurred.
	 * @throws InterruptedException
	 *             if thread is interrupted, or the specified waiting time
	 *             elapses.
	 */
	@Test
	public void testDeleteForcedAsyncWithPersistOption_Flush()
			throws UnsupportedEncodingException, KineticException,
			InterruptedException {
		PersistOption option = PersistOption.FLUSH;
		byte[] newVersion = int32(0);

		final CountDownLatch putSignal = new CountDownLatch(MAX_KEYS);

		List<String> keySList = new ArrayList<String>();
		List<String> valueSList = new ArrayList<String>();
		List<EntryMetadata> emdOfList = new ArrayList<EntryMetadata>();

		for (int i = 0; i < MAX_KEYS; i++) {
			String keyS = kvGenerator.getNextKey();
			String valueS = kvGenerator.getValue(keyS);

			keySList.add(keyS);
			valueSList.add(valueS);

			EntryMetadata entryMetadata = new EntryMetadata();
			entryMetadata.setTag(keyS.getBytes());
			entryMetadata.setAlgorithm("SHA1");

			emdOfList.add(entryMetadata);
		}

		for (int i = 0; i < MAX_KEYS; i++) {
			Entry entryPut = new Entry(toByteArray(keySList.get(i)),
					toByteArray(valueSList.get(i)), emdOfList.get(i));
			CallbackHandler<Entry> handler = buildSuccessOnlyCallbackHandler(new KineticTestHelpers.SuccessAsyncHandler<Entry>() {
				@Override
				public void onSuccess(CallbackResult<Entry> result) {
					putSignal.countDown();
				}
			});

			getClient().putAsync(entryPut, newVersion, handler);
		}

		waitForLatch(putSignal);

		final List<Boolean> deleteReturnList = new ArrayList<Boolean>(MAX_KEYS);
		final CountDownLatch deleteForcedSignal = new CountDownLatch(MAX_KEYS);

		for (int i = 0; i < MAX_KEYS; i++) {
			byte[] key = toByteArray(keySList.get(i));

			CallbackHandler<Boolean> handler = buildSuccessOnlyCallbackHandler(new KineticTestHelpers.SuccessAsyncHandler<Boolean>() {
				@Override
				public void onSuccess(CallbackResult<Boolean> result) {
					deleteReturnList.add(result.getResult());
					deleteForcedSignal.countDown();
				}
			});

			getClient().deleteForcedAsync(key, option, handler);
		}

		waitForLatch(deleteForcedSignal);
		assertEquals(MAX_KEYS, deleteReturnList.size());

		for (int i = 0; i < MAX_KEYS; i++) {
			assertTrue(deleteReturnList.get(i));

			Entry getReturned = getClient().get(toByteArray(keySList.get(i)));

			assertNull(getReturned);
		}

		logger.info(this.testEndInfo());
	}

	/**
	 * Test getKeyRangeReversed API with a serial of entries. The entries have
	 * already existed in simulator/drive. Both startKey and endKey are
	 * inclusive, expected size is smaller than all keys size in
	 * simulator/drive. The test result should be successful. Verify the order
	 * is correct.
	 * <p>
	 * 
	 * @throws KineticException
	 *             if any internal error occurred.
	 */
	@Test
	public void testGetKeyRangeReverse_ForStartKeyAndEndKeyInclusive_WithExpectSizeLessThanRealSize()
			throws KineticException {
		int startIndex = 0;
		int endIndex = 17;
		int expectSize = endIndex - startIndex + 1;
		List<Entry> vPutList = prepareKeysForGetRangeReversed();

		List<byte[]> keys = getClient().getKeyRangeReversed(
				vPutList.get(startIndex).getKey(), true,
				vPutList.get(endIndex).getKey(), true, expectSize);
		int pos = 0;
		for (byte[] key : keys) {
			assertArrayEquals(vPutList.get(endIndex - pos).getKey(), key);
			pos++;
		}
		assertEquals(expectSize, pos);

		logger.info(this.testEndInfo());
	}

	/**
	 * Test getKeyRangeReversed API with a serial of entries. The entries have
	 * already existed in simulator/drive. Both startKey and endKey are
	 * exclusive, expected size is smaller than all keys size in
	 * simulator/drive. The test result should be successful. Verify the order
	 * is correct.
	 * <p>
	 * 
	 * @throws KineticException
	 *             if any internal error occurred.
	 */
	@Test
	public void testGetKeyRangeReverse_ForStartKeyAndEndKeyExclusive_WithExpectSizeLessThanRealSize()
			throws KineticException {
		int startIndex = 0;
		int endIndex = 17;
		int expectSize = endIndex - startIndex + 1;
		List<Entry> vPutList = prepareKeysForGetRangeReversed();

		List<byte[]> keys = getClient().getKeyRangeReversed(
				vPutList.get(startIndex).getKey(), false,
				vPutList.get(endIndex).getKey(), false, expectSize);
		int pos = 0;
		for (byte[] key : keys) {
			assertArrayEquals(vPutList.get(endIndex - pos - 1).getKey(), key);
			pos++;
		}
		assertEquals(expectSize - 2, pos);

		logger.info(this.testEndInfo());
	}

	/**
	 * Test getKeyRangeReversed API with a serial of entries. The entries have
	 * already existed in simulator/drive. StartKey is inclusive and endKey is
	 * exclusive, expected size is smaller than all keys size in
	 * simulator/drive. The test result should be successful. Verify the order
	 * is correct.
	 * <p>
	 * 
	 * @throws KineticException
	 *             if any internal error occurred.
	 */
	@Test
	public void testGetKeyRangeReverse_ForStartKeyInclusiveAndEndKeyExclusive_WithExpectSizeLessThanRealSize()
			throws KineticException {
		int startIndex = 0;
		int endIndex = 17;
		int expectSize = endIndex - startIndex + 1;
		List<Entry> vPutList = prepareKeysForGetRangeReversed();

		List<byte[]> keys = getClient().getKeyRangeReversed(
				vPutList.get(startIndex).getKey(), true,
				vPutList.get(endIndex).getKey(), false, expectSize);
		int pos = 0;
		for (byte[] key : keys) {
			assertArrayEquals(vPutList.get(endIndex - pos - 1).getKey(), key);
			pos++;
		}
		assertEquals(expectSize - 1, pos);

		logger.info(this.testEndInfo());
	}

	/**
	 * Test getKeyRangeReversed API with a serial of entries. The entries have
	 * already existed in simulator/drive. StartKey is exclusive and endKey is
	 * inclusive, expected size is smaller than all keys size in
	 * simulator/drive. The test result should be successful. Verify the order
	 * is correct.
	 * <p>
	 * 
	 * @throws KineticException
	 *             if any internal error occurred.
	 */
	@Test
	public void testGetKeyRangeReverse_ForStartKeyExclusiveAndEndKeyInclusive_WithExpectSizeLessThanRealSize()
			throws KineticException {
		int startIndex = 0;
		int endIndex = 17;
		int expectSize = endIndex - startIndex + 1;
		List<Entry> vPutList = prepareKeysForGetRangeReversed();

		List<byte[]> keys = getClient().getKeyRangeReversed(
				vPutList.get(startIndex).getKey(), false,
				vPutList.get(endIndex).getKey(), true, expectSize);
		int pos = 0;
		for (byte[] key : keys) {
			assertArrayEquals(vPutList.get(endIndex - pos).getKey(), key);
			pos++;
		}
		assertEquals(expectSize - 1, pos);

		logger.info(this.testEndInfo());
	}

	/**
	 * Test getKeyRangeReversed API with a serial of entries. The entries have
	 * already existed in simulator/drive. Both startKey and endKey are
	 * exclusive, expected size is smaller than start key to end key size. The
	 * test result should be successful. Verify the order is correct.
	 * <p>
	 * 
	 * @throws KineticException
	 *             if any internal error occurred.
	 */
	@Test
	public void testGetKeyRangeReverse_ForStartKeyAndEndKeyInclusive_WithExpectSizeLessThanStartToEndKeySize()
			throws KineticException {
		int startIndex = 0;
		int endIndex = 17;
		int expectSize = endIndex - startIndex - 9;
		List<Entry> vPutList = prepareKeysForGetRangeReversed();

		List<byte[]> keys = getClient().getKeyRangeReversed(
				vPutList.get(startIndex).getKey(), true,
				vPutList.get(endIndex).getKey(), true, expectSize);
		int pos = 0;
		for (byte[] key : keys) {
			assertArrayEquals(vPutList.get(endIndex - pos).getKey(), key);
			pos++;
		}
		assertEquals(expectSize, pos);

		logger.info(this.testEndInfo());
	}

	/**
	 * Test getKeyRangeReversed API with a serial of entries. The entries have
	 * already existed in simulator/drive. StartKey is inclusive and endKey is
	 * exclusive, expected size is smaller than start key to end key size. The
	 * test result should be successful. Verify the order is correct.
	 * <p>
	 * 
	 * @throws KineticException
	 *             if any internal error occurred.
	 */
	@Test
	public void testGetKeyRangeReverse_ForStartKeyInclusiveEndKeyExclusive_WithExpectSizeLessThanStartToEndKeySize()
			throws KineticException {
		int startIndex = 0;
		int endIndex = 17;
		int expectSize = endIndex - startIndex - 9;
		List<Entry> vPutList = prepareKeysForGetRangeReversed();

		List<byte[]> keys = getClient().getKeyRangeReversed(
				vPutList.get(startIndex).getKey(), true,
				vPutList.get(endIndex).getKey(), false, expectSize);
		int pos = 0;
		for (byte[] key : keys) {
			assertArrayEquals(vPutList.get(endIndex - pos - 1).getKey(), key);
			pos++;
		}
		assertEquals(expectSize, pos);

		logger.info(this.testEndInfo());
	}

	/**
	 * Test getKeyRangeReversed API with a serial of entries. The entries have
	 * already existed in simulator/drive. StartKey is exclusive and endKey is
	 * inclusive, expected size is smaller than start key to end key size. The
	 * test result should be successful. Verify the order is correct.
	 * <p>
	 * 
	 * @throws KineticException
	 *             if any internal error occurred.
	 */
	@Test
	public void testGetKeyRangeReverse_ForStartKeyExclusiveEndKeyInclusive_WithExpectSizeLessThanStartToEndKeySize()
			throws KineticException {
		int startIndex = 0;
		int endIndex = 17;
		int expectSize = endIndex - startIndex - 9;
		List<Entry> vPutList = prepareKeysForGetRangeReversed();

		List<byte[]> keys = getClient().getKeyRangeReversed(
				vPutList.get(startIndex).getKey(), false,
				vPutList.get(endIndex).getKey(), true, expectSize);
		int pos = 0;
		for (byte[] key : keys) {
			assertArrayEquals(vPutList.get(endIndex - pos).getKey(), key);
			pos++;
		}
		assertEquals(expectSize, pos);

		logger.info(this.testEndInfo());
	}

	/**
	 * Test getKeyRangeReversed API with a serial of entries. The entries have
	 * already existed in simulator/drive. Both startKey and endKey are
	 * inclusive, expected size is smaller than start key to end key size. The
	 * test result should be successful. Verify the order is correct.
	 * <p>
	 * 
	 * @throws KineticException
	 *             if any internal error occurred.
	 */
	@Test
	public void testGetKeyRangeReverse_ForStartKeyInclusiveEndKeyInclusive_WithExpectSizeLessThanStartToEndKeySize()
			throws KineticException {
		int startIndex = 0;
		int endIndex = 17;
		int expectSize = endIndex - startIndex - 9;
		List<Entry> vPutList = prepareKeysForGetRangeReversed();

		List<byte[]> keys = getClient().getKeyRangeReversed(
				vPutList.get(startIndex).getKey(), false,
				vPutList.get(endIndex).getKey(), false, expectSize);
		int pos = 0;
		for (byte[] key : keys) {
			assertArrayEquals(vPutList.get(endIndex - pos - 1).getKey(), key);
			pos++;
		}
		assertEquals(expectSize, pos);

		logger.info(this.testEndInfo());
	}

	/**
	 * Test getKeyRangeReversed API with a serial of entries. The entries have
	 * already existed in simulator/drive. Both startKey and endKey are
	 * inclusive, expected size is bigger than start key to end key size. The
	 * test result should be successful. Verify the order is correct.
	 * <p>
	 * 
	 * @throws KineticException
	 *             if any internal error occurred.
	 */
	@Test
	public void testGetKeyRangeReverse_ForStartKeyAndEndKeyInclusive_WithExpectSizeBiggerThanStartToEndKeySize()
			throws KineticException {
		int startIndex = 0;
		int endIndex = 17;
		int expectSize = endIndex - startIndex + 5;
		List<Entry> vPutList = prepareKeysForGetRangeReversed();

		List<byte[]> keys = getClient().getKeyRangeReversed(
				vPutList.get(startIndex).getKey(), true,
				vPutList.get(endIndex).getKey(), true, expectSize);
		int pos = 0;
		for (byte[] key : keys) {
			assertArrayEquals(vPutList.get(endIndex - pos).getKey(), key);
			pos++;
		}
		assertEquals(endIndex - startIndex + 1, pos);

		logger.info(this.testEndInfo());
	}

	/**
	 * Test getKeyRangeReversed API with a serial of entries. The entries have
	 * already existed in simulator/drive. StartKey is inclusive and endKey is
	 * exclusive, expected size is bigger than start key to end key size. The
	 * test result should be successful. Verify the order is correct.
	 * <p>
	 * 
	 * @throws KineticException
	 *             if any internal error occurred.
	 */
	@Test
	public void testGetKeyRangeReverse_ForStartKeyInclusiveEndKeyExclusive_WithExpectSizeBiggerThanStartToEndKeySize()
			throws KineticException {
		int startIndex = 0;
		int endIndex = 17;
		int expectSize = endIndex - startIndex + 5;
		List<Entry> vPutList = prepareKeysForGetRangeReversed();

		List<byte[]> keys = getClient().getKeyRangeReversed(
				vPutList.get(startIndex).getKey(), true,
				vPutList.get(endIndex).getKey(), false, expectSize);
		int pos = 0;
		for (byte[] key : keys) {
			assertArrayEquals(vPutList.get(endIndex - pos - 1).getKey(), key);
			pos++;
		}
		assertEquals(endIndex - startIndex, pos);

		logger.info(this.testEndInfo());
	}

	/**
	 * Test getKeyRangeReversed API with a serial of entries. The entries have
	 * already existed in simulator/drive. StartKey is exclusive and endKey is
	 * inclusive, expected size is bigger than start key to end key size. The
	 * test result should be successful. Verify the order is correct.
	 * <p>
	 * 
	 * @throws KineticException
	 *             if any internal error occurred.
	 */
	@Test
	public void testGetKeyRangeReverse_ForStartKeyExclusiveEndKeyInclusive_WithExpectSizeBiggerThanStartToEndKeySize()
			throws KineticException {
		int startIndex = 0;
		int endIndex = 17;
		int expectSize = endIndex - startIndex + 5;
		List<Entry> vPutList = prepareKeysForGetRangeReversed();

		List<byte[]> keys = getClient().getKeyRangeReversed(
				vPutList.get(startIndex).getKey(), false,
				vPutList.get(endIndex).getKey(), true, expectSize);
		int pos = 0;
		for (byte[] key : keys) {
			assertArrayEquals(vPutList.get(endIndex - pos).getKey(), key);
			pos++;
		}
		assertEquals(endIndex - startIndex, pos);

		logger.info(this.testEndInfo());
	}

	/**
	 * Test getKeyRangeReversed API with a serial of entries. The entries have
	 * already existed in simulator/drive. Both startKey and endKey are
	 * exclusive, expected size is bigger than start key to end key size. The
	 * test result should be successful. Verify the order is correct.
	 * <p>
	 * 
	 * @throws KineticException
	 *             if any internal error occurred.
	 */
	@Test
	public void testGetKeyRangeReverse_ForStartKeyExclusiveEndKeyExclusive_WithExpectSizeBiggerThanStartToEndKeySize()
			throws KineticException {
		int startIndex = 0;
		int endIndex = 17;
		int expectSize = endIndex - startIndex + 5;
		List<Entry> vPutList = prepareKeysForGetRangeReversed();

		List<byte[]> keys = getClient().getKeyRangeReversed(
				vPutList.get(startIndex).getKey(), false,
				vPutList.get(endIndex).getKey(), false, expectSize);
		int pos = 0;
		for (byte[] key : keys) {
			assertArrayEquals(vPutList.get(endIndex - pos - 1).getKey(), key);
			pos++;
		}
		assertEquals(endIndex - startIndex - 1, pos);

		logger.info(this.testEndInfo());
	}

	/**
	 * Test getKeyRangeReversed API with a serial of entries. The entries have
	 * already existed in simulator/drive. Both startKey and endKey are
	 * inclusive, expected size is equals all keys size in simulator/drive. The
	 * test result should be successful. Verify the order is correct.
	 * <p>
	 * 
	 * @throws KineticException
	 *             if any internal error occurred.
	 */
	@Test
	public void testGetKeyRangeReverse_ForStartKeyAndEndKeyInclusive_WithExpectSizeEqualsRealSize()
			throws KineticException {
		int startIndex = 0;
		int endIndex = 19;
		int expectSize = endIndex - startIndex + 1;
		List<Entry> vPutList = prepareKeysForGetRangeReversed();

		List<byte[]> keys = getClient().getKeyRangeReversed(
				vPutList.get(startIndex).getKey(), true,
				vPutList.get(endIndex).getKey(), true, expectSize);
		int pos = 0;
		for (byte[] key : keys) {
			assertArrayEquals(vPutList.get(endIndex - pos).getKey(), key);
			pos++;
		}
		assertEquals(expectSize, pos);

		logger.info(this.testEndInfo());
	}

	/**
	 * Test getKeyRangeReversed API with a serial of entries. The entries have
	 * already existed in simulator/drive. Both startKey and endKey are
	 * exclusive, expected size is equals all keys size in simulator/drive. The
	 * test result should be successful. Verify the order is correct.
	 * <p>
	 * 
	 * @throws KineticException
	 *             if any internal error occurred.
	 */
	@Test
	public void testGetKeyRangeReverse_ForStartKeyAndEndKeyExclusive_WithExpectSizeEqualsRealSize()
			throws KineticException {
		int startIndex = 0;
		int endIndex = 19;
		int expectSize = endIndex - startIndex + 1;
		List<Entry> vPutList = prepareKeysForGetRangeReversed();

		List<byte[]> keys = getClient().getKeyRangeReversed(
				vPutList.get(startIndex).getKey(), false,
				vPutList.get(endIndex).getKey(), false, expectSize);
		int pos = 0;
		for (byte[] key : keys) {
			assertArrayEquals(vPutList.get(endIndex - pos - 1).getKey(), key);
			pos++;
		}
		assertEquals(expectSize - 2, pos);

		logger.info(this.testEndInfo());
	}

	/**
	 * Test getKeyRangeReversed API with a serial of entries. The entries have
	 * already existed in simulator/drive. StartKey is inclusive and endKey is
	 * exclusive, expected size is equals all keys size in simulator/drive. The
	 * test result should be successful. Verify the order is correct.
	 * <p>
	 * 
	 * @throws KineticException
	 *             if any internal error occurred.
	 */
	@Test
	public void testGetKeyRangeReverse_ForStartKeyInclusiveAndEndKeyExclusive_WithExpectSizeEqualsRealSize()
			throws KineticException {
		int startIndex = 0;
		int endIndex = 19;
		int expectSize = endIndex - startIndex + 1;
		List<Entry> vPutList = prepareKeysForGetRangeReversed();

		List<byte[]> keys = getClient().getKeyRangeReversed(
				vPutList.get(startIndex).getKey(), true,
				vPutList.get(endIndex).getKey(), false, expectSize);
		int pos = 0;
		for (byte[] key : keys) {
			assertArrayEquals(vPutList.get(endIndex - pos - 1).getKey(), key);
			pos++;
		}
		assertEquals(expectSize - 1, pos);

		logger.info(this.testEndInfo());
	}

	/**
	 * Test getKeyRangeReversed API with a serial of entries. The entries have
	 * already existed in simulator/drive. StartKey is exclusive and endKey is
	 * inclusive, expected size is equals all keys size in simulator/drive. The
	 * test result should be successful. Verify the order is correct.
	 * <p>
	 * 
	 * @throws KineticException
	 *             if any internal error occurred.
	 */
	@Test
	public void testGetKeyRangeReverse_ForStartKeyExclusiveAndEndKeyInclusive_WithExpectSizeEqualsRealSize()
			throws KineticException {
		int startIndex = 0;
		int endIndex = 19;
		int expectSize = endIndex - startIndex + 1;
		List<Entry> vPutList = prepareKeysForGetRangeReversed();

		List<byte[]> keys = getClient().getKeyRangeReversed(
				vPutList.get(startIndex).getKey(), false,
				vPutList.get(endIndex).getKey(), true, expectSize);
		int pos = 0;
		for (byte[] key : keys) {
			assertArrayEquals(vPutList.get(endIndex - pos).getKey(), key);
			pos++;
		}
		assertEquals(expectSize - 1, pos);

		logger.info(this.testEndInfo());
	}

	/**
	 * Test getKeyRangeReversed API with a serial of entries. The entries have
	 * already existed in simulator/drive. Both startKey and endKey are
	 * inclusive, expected size is bigger than all keys size in simulator/drive.
	 * The test result should be successful. Verify the order is correct.
	 * <p>
	 * 
	 * @throws KineticException
	 *             if any internal error occurred.
	 */
	@Test
	public void testGetKeyRangeReverse_ForStartKeyAndEndKeyInclusive_WithExpectSizeBiggerThanRealSize()
			throws KineticException {
		int startIndex = 0;
		int endIndex = 19;
		int expectSize = endIndex - startIndex + 20;
		List<Entry> vPutList = prepareKeysForGetRangeReversed();

		List<byte[]> keys = getClient().getKeyRangeReversed(
				vPutList.get(startIndex).getKey(), true,
				vPutList.get(endIndex).getKey(), true, expectSize);
		int pos = 0;
		for (byte[] key : keys) {
			assertArrayEquals(vPutList.get(endIndex - pos).getKey(), key);
			pos++;
		}
		assertEquals(endIndex - startIndex + 1, pos);

		logger.info(this.testEndInfo());
	}

	/**
	 * Test getKeyRangeReversed API with a serial of entries. The entries have
	 * already existed in simulator/drive. Both startKey and endKey are
	 * exclusive, expected size is bigger than all keys size in simulator/drive.
	 * The test result should be successful. Verify the order is correct.
	 * <p>
	 * 
	 * @throws KineticException
	 *             if any internal error occurred.
	 */
	@Test
	public void testGetKeyRangeReverse_ForStartKeyAndEndKeyExclusive_WithExpectSizeBiggerThanRealSize()
			throws KineticException {
		int startIndex = 0;
		int endIndex = 19;
		int expectSize = endIndex - startIndex + 20;
		List<Entry> vPutList = prepareKeysForGetRangeReversed();

		List<byte[]> keys = getClient().getKeyRangeReversed(
				vPutList.get(startIndex).getKey(), false,
				vPutList.get(endIndex).getKey(), false, expectSize);
		int pos = 0;
		for (byte[] key : keys) {
			assertArrayEquals(vPutList.get(endIndex - pos - 1).getKey(), key);
			pos++;
		}
		assertEquals(endIndex - startIndex - 1, pos);

		logger.info(this.testEndInfo());
	}

	/**
	 * Test getKeyRangeReversed API with a serial of entries. The entries have
	 * already existed in simulator/drive. StartKey is inclusive and endKey is
	 * exclusive, expected size is bigger than all keys size in simulator/drive.
	 * The test result should be successful. Verify the order is correct.
	 * <p>
	 * 
	 * @throws KineticException
	 *             if any internal error occurred.
	 */
	@Test
	public void testGetKeyRangeReverse_ForStartKeyInclusiveAndEndKeyExclusive_WithExpectSizeBiggerThanRealSize()
			throws KineticException {
		int startIndex = 0;
		int endIndex = 19;
		int expectSize = endIndex - startIndex + 20;
		List<Entry> vPutList = prepareKeysForGetRangeReversed();

		List<byte[]> keys = getClient().getKeyRangeReversed(
				vPutList.get(startIndex).getKey(), true,
				vPutList.get(endIndex).getKey(), false, expectSize);
		int pos = 0;
		for (byte[] key : keys) {
			assertArrayEquals(vPutList.get(endIndex - pos - 1).getKey(), key);
			pos++;
		}
		assertEquals(endIndex - startIndex, pos);

		logger.info(this.testEndInfo());
	}

	/**
	 * Test getKeyRangeReversed API with a serial of entries. The entries have
	 * already existed in simulator/drive. StartKey is exclusive and endKey is
	 * inclusive, expected size is bigger than all keys size in simulator/drive.
	 * The test result should be successful. Verify the order is correct.
	 * <p>
	 * 
	 * @throws KineticException
	 *             if any internal error occurred.
	 */
	@Test
	public void testGetKeyRangeWithReverse_ForStartKeyExclusiveAndEndKeyInclusive_WithExpectSizeBiggerThanRealSize()
			throws KineticException {
		int startIndex = 0;
		int endIndex = 19;
		int expectSize = endIndex - startIndex + 20;
		List<Entry> vPutList = prepareKeysForGetRangeReversed();

		List<byte[]> keys = getClient().getKeyRangeReversed(
				vPutList.get(startIndex).getKey(), false,
				vPutList.get(endIndex).getKey(), true, expectSize);
		int pos = 0;
		for (byte[] key : keys) {
			assertArrayEquals(vPutList.get(endIndex - pos).getKey(), key);
			pos++;
		}
		assertEquals(endIndex - startIndex, pos);

		logger.info(this.testEndInfo());
	}

	/**
	 * Test getKeyRangeReversedAsync API with a serial of entries. The entries
	 * have already existed in simulator/drive. Both startKey and endKey are
	 * inclusive. The test result should be successful. Verify the order is
	 * correct.
	 * <p>
	 * 
	 * @throws KineticException
	 *             if any internal error occurred.
	 * @throws InterruptedException
	 *             if thread is interrupted, or the specified waiting time
	 *             elapses.
	 */
	@Test
	public void testGetKeyRangeReversedAsync_ReturnsCorrectResults_ForStartEndKeyInclusive()
			throws KineticException, InterruptedException {
		List<byte[]> keys = Arrays.asList(toByteArray("00"), toByteArray("01"),
				toByteArray("02"), toByteArray("03"), toByteArray("04"),
				toByteArray("05"), toByteArray("06"), toByteArray("07"),
				toByteArray("08"), toByteArray("09"), toByteArray("10"),
				toByteArray("11"), toByteArray("12"), toByteArray("13"),
				toByteArray("14"));

		byte[] newVersion = int32(0);
		final List<Entry> putList = new ArrayList<Entry>();
		final List<byte[]> keyRangeList = new ArrayList<byte[]>();
		final CountDownLatch putSignal = new CountDownLatch(keys.size());
		final CountDownLatch getKeyRangeSignal = new CountDownLatch(1);

		for (byte[] key : keys) {
			CallbackHandler<Entry> handler = buildSuccessOnlyCallbackHandler(new SuccessAsyncHandler<Entry>() {
				@Override
				public void onSuccess(CallbackResult<Entry> result) {
					putList.add(result.getResult());
					putSignal.countDown();
				}
			});

			getClient().putAsync(new Entry(key, key), newVersion, handler);
		}

		waitForLatch(putSignal);
		assertEquals(keys.size(), putList.size());

		CallbackHandler<List<byte[]>> handler = buildSuccessOnlyCallbackHandler(new SuccessAsyncHandler<List<byte[]>>() {
			@Override
			public void onSuccess(CallbackResult<List<byte[]>> result) {
				for (byte[] key : result.getResult()) {
					keyRangeList.add(key);
				}
				getKeyRangeSignal.countDown();
			}
		});

		getClient().getKeyRangeReversedAsync(keys.get(0), true,
				keys.get(keys.size() - 1), true, keys.size(), handler);

		waitForLatch(getKeyRangeSignal);

		int returnKeysSize = keyRangeList.size();

		assertEquals(keys.size(), returnKeysSize);

		for (int i = 0; i < returnKeysSize; i++) {
			assertArrayEquals(keys.get(returnKeysSize - i - 1),
					keyRangeList.get(i));
		}

		logger.info(this.testEndInfo());

	}

	/**
	 * Test getKeyRangeReversedAsync API with a serial of entries. The entries
	 * have already existed in simulator/drive. Both startKey and endKey are
	 * exclusive. The test result should be successful. Verify the order is
	 * correct.
	 * <p>
	 * 
	 * @throws KineticException
	 *             if any internal error occurred.
	 * @throws InterruptedException
	 *             if thread is interrupted, or the specified waiting time
	 *             elapses.
	 */
	@Test
	public void testGetKeyRangeReversedAsync_ReturnsCorrectResults_ForStartEndKeyExclusive()
			throws KineticException, InterruptedException {
		List<byte[]> keys = Arrays.asList(toByteArray("00"), toByteArray("01"),
				toByteArray("02"), toByteArray("03"), toByteArray("04"),
				toByteArray("05"), toByteArray("06"), toByteArray("07"),
				toByteArray("08"), toByteArray("09"), toByteArray("10"),
				toByteArray("11"), toByteArray("12"), toByteArray("13"),
				toByteArray("14"));

		byte[] newVersion = int32(0);
		final List<Entry> putList = new ArrayList<Entry>();
		final List<byte[]> keyRangeList = new ArrayList<byte[]>();
		final CountDownLatch putSignal = new CountDownLatch(keys.size());
		final CountDownLatch getKeyRangeSignal = new CountDownLatch(1);

		for (byte[] key : keys) {
			CallbackHandler<Entry> handler = buildSuccessOnlyCallbackHandler(new SuccessAsyncHandler<Entry>() {
				@Override
				public void onSuccess(CallbackResult<Entry> result) {
					putList.add(result.getResult());
					putSignal.countDown();
				}
			});

			getClient().putAsync(new Entry(key, key), newVersion, handler);
		}

		waitForLatch(putSignal);
		assertEquals(keys.size(), putList.size());

		CallbackHandler<List<byte[]>> handler = buildSuccessOnlyCallbackHandler(new SuccessAsyncHandler<List<byte[]>>() {
			@Override
			public void onSuccess(CallbackResult<List<byte[]>> result) {
				for (byte[] key : result.getResult()) {
					keyRangeList.add(key);
				}
				getKeyRangeSignal.countDown();
			}
		});

		getClient().getKeyRangeReversedAsync(keys.get(0), false,
				keys.get(keys.size() - 1), false, keys.size(), handler);

		waitForLatch(getKeyRangeSignal);

		int returnKeysSize = keyRangeList.size();

		assertEquals(keys.size() - 2, returnKeysSize);

		for (int i = 0; i < returnKeysSize; i++) {
			assertArrayEquals(keys.get(keys.size() - i - 2),
					keyRangeList.get(i));
		}

		logger.info(this.testEndInfo());
	}

	/**
	 * Test getKeyRangeReversedAsync API with a serial of entries. The entries
	 * have already existed in simulator/drive. StartKey is inclusive and endKey
	 * is exclusive. The test result should be successful. Verify the order is
	 * correct.
	 * <p>
	 * 
	 * @throws KineticException
	 *             if any internal error occurred.
	 * @throws InterruptedException
	 *             if thread is interrupted, or the specified waiting time
	 *             elapses.
	 */
	@Test
	public void testGetKeyRangeReversedAsync_ReturnsCorrectResults_ForStartInclusiveEndExclusive()
			throws KineticException, InterruptedException {
		List<byte[]> keys = Arrays.asList(toByteArray("00"), toByteArray("01"),
				toByteArray("02"), toByteArray("03"), toByteArray("04"),
				toByteArray("05"), toByteArray("06"), toByteArray("07"),
				toByteArray("08"), toByteArray("09"), toByteArray("10"),
				toByteArray("11"), toByteArray("12"), toByteArray("13"),
				toByteArray("14"));

		byte[] newVersion = int32(0);
		final List<Entry> putList = new ArrayList<Entry>();
		final List<byte[]> keyRangeList = new ArrayList<byte[]>();
		final CountDownLatch putSignal = new CountDownLatch(keys.size());
		final CountDownLatch getKeyRangeSignal = new CountDownLatch(1);

		for (byte[] key : keys) {
			CallbackHandler<Entry> handler = buildSuccessOnlyCallbackHandler(new SuccessAsyncHandler<Entry>() {
				@Override
				public void onSuccess(CallbackResult<Entry> result) {
					putList.add(result.getResult());
					putSignal.countDown();
				}
			});

			getClient().putAsync(new Entry(key, key), newVersion, handler);
		}

		waitForLatch(putSignal);
		assertEquals(keys.size(), putList.size());

		CallbackHandler<List<byte[]>> handler = buildSuccessOnlyCallbackHandler(new SuccessAsyncHandler<List<byte[]>>() {
			@Override
			public void onSuccess(CallbackResult<List<byte[]>> result) {
				for (byte[] key : result.getResult()) {
					keyRangeList.add(key);
				}
				getKeyRangeSignal.countDown();
			}
		});

		getClient().getKeyRangeReversedAsync(keys.get(0), true,
				keys.get(keys.size() - 1), false, keys.size(), handler);

		waitForLatch(getKeyRangeSignal);

		int returnKeysSize = keyRangeList.size();

		assertEquals(keys.size() - 1, returnKeysSize);

		for (int i = 0; i < returnKeysSize; i++) {
			assertArrayEquals(keys.get(keys.size() - i - 2),
					keyRangeList.get(i));
		}

		logger.info(this.testEndInfo());
	}

	/**
	 * Test getKeyRangeReversedAsync API with a serial of entries. The entries
	 * have already existed in simulator/drive. StartKey is exclusive and endKey
	 * is inclusive. The test result should be successful. Verify the order is
	 * correct.
	 * <p>
	 * 
	 * @throws KineticException
	 *             if any internal error occurred.
	 * @throws InterruptedException
	 *             if thread is interrupted, or the specified waiting time
	 *             elapses.
	 */
	@Test
	public void testGetKeyRangeReversedAsync_ReturnsCorrectResults_ForStartExclusiveEndInclusive()
			throws KineticException, InterruptedException {
		List<byte[]> keys = Arrays.asList(toByteArray("00"), toByteArray("01"),
				toByteArray("02"), toByteArray("03"), toByteArray("04"),
				toByteArray("05"), toByteArray("06"), toByteArray("07"),
				toByteArray("08"), toByteArray("09"), toByteArray("10"),
				toByteArray("11"), toByteArray("12"), toByteArray("13"),
				toByteArray("14"));

		byte[] newVersion = int32(0);
		final List<Entry> putList = new ArrayList<Entry>();
		final List<byte[]> keyRangeList = new ArrayList<byte[]>();
		final CountDownLatch putSignal = new CountDownLatch(keys.size());
		final CountDownLatch getKeyRangeSignal = new CountDownLatch(1);

		for (byte[] key : keys) {
			CallbackHandler<Entry> handler = buildSuccessOnlyCallbackHandler(new SuccessAsyncHandler<Entry>() {
				@Override
				public void onSuccess(CallbackResult<Entry> result) {
					putList.add(result.getResult());
					putSignal.countDown();
				}
			});

			getClient().putAsync(new Entry(key, key), newVersion, handler);
		}

		waitForLatch(putSignal);
		assertEquals(keys.size(), putList.size());

		CallbackHandler<List<byte[]>> handler = buildSuccessOnlyCallbackHandler(new SuccessAsyncHandler<List<byte[]>>() {
			@Override
			public void onSuccess(CallbackResult<List<byte[]>> result) {
				for (byte[] key : result.getResult()) {
					keyRangeList.add(key);
				}
				getKeyRangeSignal.countDown();
			}
		});

		getClient().getKeyRangeReversedAsync(keys.get(0), false,
				keys.get(keys.size() - 1), true, keys.size(), handler);

		waitForLatch(getKeyRangeSignal);

		int returnKeysSize = keyRangeList.size();

		assertEquals(keys.size() - 1, returnKeysSize);

		for (int i = 0; i < returnKeysSize; i++) {
			assertArrayEquals(keys.get(keys.size() - i - 1),
					keyRangeList.get(i));
		}

		logger.info(this.testEndInfo());
	}

	/**
	 * Prepare the keys for get key range reversed, put some keys to
	 * simulator/drive.
	 * <p>
	 * 
	 * @throws KineticException
	 *             if any internal error occurred.
	 */
	private List<Entry> prepareKeysForGetRangeReversed()
			throws KineticException {
		int keyCount = 20;

		KVGenerator kvGenerator = new KVGenerator();

		kvGenerator.reset();
		List<Entry> vPutList = new ArrayList<Entry>();
		for (int i = 0; i < keyCount; i++) {
			String key = kvGenerator.getNextKey();
			String value = kvGenerator.getValue(key);
			byte[] version = toByteArray("0");
			EntryMetadata entryMetadata = new EntryMetadata();
			Entry v = new Entry(toByteArray(key), toByteArray(value),
					entryMetadata);

			Entry vIn = getClient().put(v, version);
			vPutList.add(vIn);
		}

		return vPutList;
	}

}
