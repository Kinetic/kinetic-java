package com.seagate.kinetic.randomkv;

import static com.seagate.kinetic.KineticTestHelpers.buildSuccessOnlyCallbackHandler;
import static com.seagate.kinetic.KineticTestHelpers.toByteArray;
import static com.seagate.kinetic.KineticTestHelpers.waitForLatch;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

import kinetic.client.CallbackHandler;
import kinetic.client.CallbackResult;
import kinetic.client.Entry;
import kinetic.client.EntryMetadata;
import kinetic.client.KineticException;
import kinetic.client.advanced.PersistOption;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.seagate.kinetic.IntegrationTestCase;
import com.seagate.kinetic.KineticTestHelpers.SuccessAsyncHandler;
import com.seagate.kinetic.RandomKVGenerator;

/**
 * 
 * This class test random valid key/value/version size for put/get/delete. The
 * random number can be set to reproduceable or not.
 * <p>
 * How to run this test case<br>
 * ======================================================== <br>
 * 1. cd kinetic-java/bin <br>
 * 2. ./runRandomKVTests.sh -host <driveIP> <br>
 * More options, please run "./runRandomKVTests.sh -help". <br>
 * <p>
 * <p>
 * How to see the test result <br>
 * ======================================================== <br>
 * After test running over, copy absolute url of
 * "kinetic-java/bin/test-output/index.html" to your browser. <br>
 * <p>
 * <p>
 * Test case explanation. <br>
 * ======================================================== <br>
 * <p>
 * PUT_RANDOM_TEST_01 <br>
 * PUT with random key size/value and random value size/value, fixed new
 * version(size/value). <br>
 * Loop for 10 times to test PUT operation with async client, each loop includes
 * 1000 PUTs, GETs all keys back and check each vaule got back is correct.
 * <p>
 * PUT_RANDOM_TEST_02 <br>
 * PUT with random key(size/value), random valu(size/value) and random new
 * version(size/value). <br>
 * Loop for 10 times to test PUT operation with async client, every PUT with a
 * random "new version". Each loop includes 1000 PUTs. GETs all keys back and
 * then check each "vaule" got back is correct, each "dbVersion" got back is
 * correct.
 * <p>
 * PUT_RANDOM_TEST_03 <br>
 * PUT with random key(size/value), random value(size/value), random new
 * version(size/value), random db version(size/value). <br>
 * Loop for 10 times to test PUT operation with async client, each PUT with a
 * random "dbVersion", a random "new version". Each loop includes 1000 PUTs.
 * GETs all keys back and then check each "vaule" got back is correct, each
 * "dbVersion" got back is correct.
 * <p>
 * PUT_FORCE_RANDOM_TEST_01 <br>
 * PUT forced with random key (size/value) and random value (size/value), fixed
 * newVersion. <br>
 * Loop for 10 times to test PUT forced operation with async client, each loop
 * includes 1000 forced PUTs, GETs all keys back and check each vaule got back
 * is correct.
 * <p>
 * PUT_FORCE_RANDOM_TEST_02 <br>
 * PUT forced with random key(size/value) which doesn't exist in DB), random
 * valu(size/value) and random db version(size/value). <br>
 * Loop for 10 times to test PUT forced operation with async client, each PUT
 * forced with a random "db version". Each loop includes 1000 PUTs. Check each
 * "vaule" in PUT returned is correct, each "dbVersion" in PUT returned is
 * correct.
 * <p>
 * PUT_FORCE_RANDOM_TEST_03 <br>
 * PUT forced with random key(size/value) which has already existed in DB),
 * random valu(size/value) and random db version(size/value). <br>
 * Loop for 10 times to test PUT operation with async client, each PUT with a
 * random "new version". Then PUT forced for same key with random dbVersion.
 * Each loop includes 1000 PUTs. GETs all keys back and then check each "vaule"
 * got back is correct, each "dbVersion" got back is correct.
 * <p>
 * DELETE_RANDOM_TEST_01 <br>
 * DELETE random keys which has already existed in DB. <br>
 * Loop for 10 times to test DELETE operation with async client, each loop
 * includes 1000 PUTs, GETs all keys back and check each vaule got back is
 * correct. Then randomly DELETE 100 keys which exist in DB, GET all keys back
 * and make sure the deteled keys are gone.
 * <p>
 * DELETE_RANDOM_TEST_02 <br>
 * DELETE random keys which doesn't exist in DB. <br>
 * Loop for 10 times to test DELETE operation with async client, each loop
 * includes 1000 PUTs, GETs all keys back and check each vaule got back is
 * correct. Then randomly DELETE 100 keys which doesn't exist in DB, GET all
 * keys back and make sure all 1000 keys still exist in DB.
 * <p>
 * DELETE_FORCE_RANDOM_TEST_01 <br>
 * DELETE forced random keys which has already existed in DB. <br>
 * Loop for 10 times to test DELETE forced operation with async client, each
 * loop includes 1000 PUTs, GETs all keys back and check each vaule got back is
 * correct. Then randomly DELETE forced 100 keys which exist in DB, GET all keys
 * back and make sure the deteled keys are gone.
 * <p>
 * DELETE_FORCE_RANDOM_TEST_02 <br>
 * DELETE forced random keys which doesn't exist in DB. <br>
 * Loop for 10 times to test DELETE forced operation with async client, each
 * loop includes 1000 PUTs, GETs all keys back and check each vaule got back is
 * correct. Then randomly DELETE forced 100 keys which doesn't exist in DB, GET
 * all keys back and make sure all 1000 keys still exist in DB.
 * <p>
 * GET_RANDOM_TEST_01 <br>
 * GET random keys after first PUT. <br>
 * Loop for 10 times to test PUT operation(keys are PUT first time) with async
 * client, each loop includes 1000 PUTs, GETs all keys back and check each vaule
 * got back is correct.
 * <p>
 * GET_RANDOM_TEST_02 <br>
 * GET random keys which has already existed in DB. <br>
 * Loop for 10 times to test PUT operation(keys are PUT second time) with async
 * client, each loop includes 1000 PUTs, GETs all keys back and check each vaule
 * got back is correct.
 * <p>
 * GET_RANDOM_TEST_03 <br>
 * GET random keys which doesn't exist in DB. <br>
 * Loop for 10 times to test PUT operation with async client, each loop includes
 * 1000 PUTs, GETs 100 random keys back and make sure GETs results are null.
 * 
 */
@Test(groups = { "random" })
public class RandomKVTest extends IntegrationTestCase {
	private static final String CHARSET_NAME = "UTF8";
	private final static String ALGORITHM = "SHA1";
	private final static int LIMIT_KEY_SIZE = 4000;
	private final static int LIMIT_VALUE_SIZE = 1048576;
	private final static int LIMIT_VERSION_SIZE = 2048;
	private final static int LOOP_TIMES = 10;
	private final static int OP_IN_LOOP = 1000;
	private final static int RANDOM_DELETE_KEY_NUMBER = 100;
	private final static int MAX_RECORDS = LOOP_TIMES * OP_IN_LOOP;
	private final static boolean REPRODUCEABLE = true;
	private RandomKVGenerator randomKVGeneratorForValue;
	private RandomKVGenerator randomKVGeneratorForVersion;

	@BeforeMethod
	public void setUp() throws Exception {
		randomKVGeneratorForValue = new RandomKVGenerator(REPRODUCEABLE,
				MAX_RECORDS, LIMIT_KEY_SIZE, LIMIT_VALUE_SIZE);
		randomKVGeneratorForVersion = new RandomKVGenerator(REPRODUCEABLE,
				MAX_RECORDS, LIMIT_KEY_SIZE, LIMIT_VERSION_SIZE);
	}

	@Test(dataProvider = "transportProtocolOptions")
	public void put_Random_Test_01(String client) {
		byte[] newVersion = toByteArray("0");
		for (int i = 0; i < LOOP_TIMES; i++) {
			final Map<String, Entry> putReturnMap = new HashMap<String, Entry>(
					OP_IN_LOOP);
			final CountDownLatch putSignal = new CountDownLatch(OP_IN_LOOP);

			Map<String, ObjectStoredForTest> objectForTestOfMap = new HashMap<String, ObjectStoredForTest>();

			for (int j = 0; j < OP_IN_LOOP; j++) {
				String key = getRandomKey();

				String value = "";
				try {
					value = getRandomValue(key);
				} catch (Exception e1) {
					Assert.fail(e1.getMessage());
				}

				EntryMetadata emd = new EntryMetadata();
				// add one line
				emd.setVersion("".getBytes());
				emd.setAlgorithm(ALGORITHM);
				String tag = "";
				try {
					tag = sha1(value);
				} catch (NoSuchAlgorithmException e1) {
					Assert.fail(e1.getMessage());
				}
				emd.setTag(toByteArray(tag));

				ObjectStoredForTest objectStoredForTest = new ObjectStoredForTest(
						value.length(), tag);
				if (!objectForTestOfMap.containsKey(key)) {
					objectForTestOfMap.put(key, objectStoredForTest);
				}

				Entry entry = new Entry(toByteArray(key), toByteArray(value),
						emd);

				CallbackHandler<Entry> handler = buildSuccessOnlyCallbackHandler(new SuccessAsyncHandler<Entry>() {
					@Override
					public void onSuccess(CallbackResult<Entry> result) {
						try {
							putReturnMap.put(new String(result.getResult()
									.getKey(), CHARSET_NAME), result
									.getResult());
						} catch (UnsupportedEncodingException e) {
							Assert.fail(e.getMessage());
						}
						putSignal.countDown();
					}
				});

				try {
					getClient(client).putAsync(entry, newVersion,
							PersistOption.ASYNC, handler);
				} catch (KineticException e) {
					Assert.fail(e.getMessage());
				}
			}

			try {
				waitForLatch(putSignal);
			} catch (InterruptedException e) {
				Assert.fail(e.getMessage());
			}
			assertEquals(objectForTestOfMap.size(), putReturnMap.size());
			for (String key : objectForTestOfMap.keySet()) {
				assertTrue(putReturnMap.keySet().contains(key));
				try {
					byte[] value = putReturnMap.get(key).getValue();
					assertArrayEquals(
							toByteArray(getFromKey(key,
									objectForTestOfMap.get(key).getValueSize())),
							value);

					assertEquals(objectForTestOfMap.get(key).getTag(),
							sha1(new String(value, CHARSET_NAME)));
				} catch (Exception e) {
					Assert.fail(e.getMessage());
				}
			}

			// clean data
			for (String key : objectForTestOfMap.keySet()) {
				try {
					getClient(client).deleteForced(toByteArray(key),
							PersistOption.ASYNC);
				} catch (KineticException e) {
					Assert.fail(e.getMessage());
				}
			}
		}
	}

	@Test(dataProvider = "transportProtocolOptions")
	public void put_Random_Test_02(String client) {

		for (int i = 0; i < LOOP_TIMES; i++) {
			final Map<String, Entry> putReturnMap = new HashMap<String, Entry>(
					OP_IN_LOOP);
			final CountDownLatch putSignal = new CountDownLatch(OP_IN_LOOP);

			Map<String, ObjectStoredForTest> objectForTestOfMap = new HashMap<String, ObjectStoredForTest>();

			for (int j = 0; j < OP_IN_LOOP; j++) {
				String key = getRandomKey();

				String value = "";
				String newVersion = "";
				try {
					value = getRandomValue(key);
					newVersion = getRandomVersion(key);
				} catch (Exception e1) {
					Assert.fail(e1.getMessage());
				}

				EntryMetadata emd = new EntryMetadata();
				emd.setAlgorithm(ALGORITHM);
				String tag = "";
				try {
					tag = sha1(value);
				} catch (NoSuchAlgorithmException e1) {
					Assert.fail(e1.getMessage());
				}
				emd.setTag(toByteArray(tag));
				emd.setAlgorithm(ALGORITHM);

				ObjectStoredForTest objectStoredForTest = new ObjectStoredForTest(
						value.length(), newVersion.length(), tag);

				if (!objectForTestOfMap.containsKey(key)) {
					objectForTestOfMap.put(key, objectStoredForTest);
				}

				Entry entry = new Entry(toByteArray(key), toByteArray(value),
						emd);

				CallbackHandler<Entry> handler = buildSuccessOnlyCallbackHandler(new SuccessAsyncHandler<Entry>() {
					@Override
					public void onSuccess(CallbackResult<Entry> result) {
						try {
							putReturnMap.put(new String(result.getResult()
									.getKey(), CHARSET_NAME), result
									.getResult());
						} catch (UnsupportedEncodingException e) {
							Assert.fail(e.getMessage());
						}
						putSignal.countDown();
					}
				});

				try {
					getClient(client).putAsync(entry, toByteArray(newVersion),
							PersistOption.ASYNC, handler);
				} catch (KineticException e) {
					Assert.fail(e.getMessage());
				}
			}

			try {
				waitForLatch(putSignal);
			} catch (InterruptedException e) {
				Assert.fail(e.getMessage());
			}
			assertEquals(objectForTestOfMap.size(), putReturnMap.size());
			for (String key : objectForTestOfMap.keySet()) {
				assertTrue(putReturnMap.keySet().contains(key));
				try {
					byte[] value = putReturnMap.get(key).getValue();
					assertArrayEquals(
							toByteArray(getFromKey(key,
									objectForTestOfMap.get(key).getValueSize())),
							value);

					assertArrayEquals(
							toByteArray(getFromKey(key,
									objectForTestOfMap.get(key)
											.getVersionSize())), putReturnMap
									.get(key).getEntryMetadata().getVersion());

					assertEquals(objectForTestOfMap.get(key).getTag(),
							sha1(new String(value, CHARSET_NAME)));

				} catch (Exception e) {
					Assert.fail(e.getMessage());
				}
			}

			// clean data
			for (String key : objectForTestOfMap.keySet()) {
				try {
					getClient(client).deleteForced(toByteArray(key),
							PersistOption.ASYNC);
				} catch (KineticException e) {
					Assert.fail(e.getMessage());
				}
			}
		}
	}

	@Test(dataProvider = "transportProtocolOptions")
	public void put_Random_Test_03(String client) {

		for (int i = 0; i < LOOP_TIMES; i++) {
			final Map<String, Entry> putReturnMap = new HashMap<String, Entry>(
					OP_IN_LOOP);
			final Map<String, Entry> putReturnMapSecond = new HashMap<String, Entry>(
					OP_IN_LOOP);
			final CountDownLatch putSignal = new CountDownLatch(OP_IN_LOOP);
			final CountDownLatch putSignalSecond = new CountDownLatch(
					OP_IN_LOOP);

			Map<String, ObjectStoredForTest> objectForTestOfMap = new HashMap<String, ObjectStoredForTest>();
			Map<String, ObjectStoredForTest> objectForTestOfMapSecond = new HashMap<String, ObjectStoredForTest>();

			for (int j = 0; j < OP_IN_LOOP; j++) {
				String key = getRandomKey();

				String value = "";
				String newVersion = "";
				String tag = "";
				try {
					value = getRandomValue(key);
					newVersion = getRandomVersion(key);
					tag = sha1(value);
				} catch (Exception e1) {
					Assert.fail(e1.getMessage());
				}

				EntryMetadata emd = new EntryMetadata();
				emd.setTag(toByteArray(tag));
				emd.setAlgorithm(ALGORITHM);

				ObjectStoredForTest objectStoredForTest = new ObjectStoredForTest(
						value.length(), newVersion.length(), tag);
				if (!objectForTestOfMap.containsKey(key)) {
					objectForTestOfMap.put(key, objectStoredForTest);
				}

				Entry entry = new Entry(toByteArray(key), toByteArray(value),
						emd);

				CallbackHandler<Entry> handler = buildSuccessOnlyCallbackHandler(new SuccessAsyncHandler<Entry>() {
					@Override
					public void onSuccess(CallbackResult<Entry> result) {
						try {
							putReturnMap.put(new String(result.getResult()
									.getKey(), CHARSET_NAME), result
									.getResult());
						} catch (UnsupportedEncodingException e) {
							Assert.fail(e.getMessage());
						}
						putSignal.countDown();
					}
				});

				try {
					getClient(client).putAsync(entry, toByteArray(newVersion),
							PersistOption.ASYNC, handler);
				} catch (KineticException e) {
					Assert.fail(e.getMessage());
				}
			}

			try {
				waitForLatch(putSignal);
			} catch (InterruptedException e) {
				Assert.fail(e.getMessage());
			}

			for (String key : objectForTestOfMap.keySet()) {

				String value = "";
				String newVersion = "";
				String tag = "";
				try {
					value = getRandomValue(key);
					newVersion = getRandomVersion(key);
					tag = sha1(value);
				} catch (Exception e1) {
					Assert.fail(e1.getMessage());
				}

				ObjectStoredForTest objectStoredForTest = new ObjectStoredForTest(
						value.length(), newVersion.length(), tag);
				objectForTestOfMapSecond.put(key, objectStoredForTest);

				Entry entry = new Entry(toByteArray(key), toByteArray(value));
				EntryMetadata emd = new EntryMetadata();
				emd.setTag(toByteArray(tag));
				emd.setAlgorithm(ALGORITHM);
				try {
					emd.setVersion(toByteArray(getFromKey(key,
							objectForTestOfMap.get(key).getVersionSize())));
				} catch (Exception e1) {
					Assert.fail(e1.getMessage());
				}

				entry.setEntryMetadata(emd);

				CallbackHandler<Entry> handler = buildSuccessOnlyCallbackHandler(new SuccessAsyncHandler<Entry>() {
					@Override
					public void onSuccess(CallbackResult<Entry> result) {
						try {
							putReturnMapSecond.put(new String(result
									.getResult().getKey(), CHARSET_NAME),
									result.getResult());
						} catch (UnsupportedEncodingException e) {
							Assert.fail(e.getMessage());
						}
						putSignalSecond.countDown();
					}
				});

				try {
					getClient(client).putAsync(entry, toByteArray(newVersion),
							PersistOption.ASYNC, handler);
				} catch (KineticException e) {
					Assert.fail(e.getMessage());
				}
			}

			try {
				waitForLatch(putSignalSecond);
			} catch (InterruptedException e) {
				Assert.fail(e.getMessage());
			}

			assertEquals(objectForTestOfMapSecond.size(),
					putReturnMapSecond.size());

			for (String key : objectForTestOfMapSecond.keySet()) {
				assertTrue(putReturnMapSecond.keySet().contains(key));
				try {
					byte[] value = putReturnMapSecond.get(key).getValue();
					assertArrayEquals(
							toByteArray(getFromKey(key,
									objectForTestOfMapSecond.get(key)
											.getValueSize())), value);

					assertArrayEquals(
							toByteArray(getFromKey(key,
									objectForTestOfMapSecond.get(key)
											.getVersionSize())),
							putReturnMapSecond.get(key).getEntryMetadata()
									.getVersion());
					assertEquals(objectForTestOfMapSecond.get(key).getTag(),
							sha1(new String(value, CHARSET_NAME)));

				} catch (Exception e) {
					Assert.fail(e.getMessage());
				}
			}

			// clean data
			for (String key : objectForTestOfMap.keySet()) {
				try {
					getClient(client).deleteForced(toByteArray(key),
							PersistOption.ASYNC);
				} catch (KineticException e) {
					Assert.fail(e.getMessage());
				}
			}
		}
	}

	@Test(dataProvider = "transportProtocolOptions")
	public void put_Force_Random_Test_01(String client) {
		for (int i = 0; i < LOOP_TIMES; i++) {
			final Map<String, Entry> putReturnMap = new HashMap<String, Entry>(
					OP_IN_LOOP);
			final CountDownLatch putSignal = new CountDownLatch(OP_IN_LOOP);

			Map<String, ObjectStoredForTest> objectStoredForTestMap = new HashMap<String, ObjectStoredForTest>();

			for (int j = 0; j < OP_IN_LOOP; j++) {
				String key = getRandomKey();

				String value = "";
				String tag = "";
				try {
					value = getRandomValue(key);
					tag = sha1(value);
				} catch (Exception e1) {
					Assert.fail(e1.getMessage());
				}

				ObjectStoredForTest objectStoredForTest = new ObjectStoredForTest(
						value.length(), tag);
				if (!objectStoredForTestMap.containsKey(key)) {
					objectStoredForTestMap.put(key, objectStoredForTest);
				}

				EntryMetadata emd = new EntryMetadata();
				emd.setAlgorithm(ALGORITHM);
				emd.setTag(toByteArray(tag));
				Entry entry = new Entry(toByteArray(key), toByteArray(value),
						emd);

				CallbackHandler<Entry> handler = buildSuccessOnlyCallbackHandler(new SuccessAsyncHandler<Entry>() {
					@Override
					public void onSuccess(CallbackResult<Entry> result) {
						try {
							putReturnMap.put(new String(result.getResult()
									.getKey(), CHARSET_NAME), result
									.getResult());
						} catch (UnsupportedEncodingException e) {
							Assert.fail(e.getMessage());
						}
						putSignal.countDown();
					}
				});

				try {
					getClient(client).putForcedAsync(entry,
							PersistOption.ASYNC, handler);
				} catch (KineticException e) {
					Assert.fail(e.getMessage());
				}
			}

			try {
				waitForLatch(putSignal);
			} catch (InterruptedException e) {
				Assert.fail(e.getMessage());
			}
			assertEquals(objectStoredForTestMap.size(), putReturnMap.size());
			for (String key : objectStoredForTestMap.keySet()) {
				assertTrue(putReturnMap.keySet().contains(key));
				try {
					byte[] value = putReturnMap.get(key).getValue();
					assertArrayEquals(
							toByteArray(getFromKey(key, objectStoredForTestMap
									.get(key).getValueSize())), value);

					assertEquals(objectStoredForTestMap.get(key).getTag(),
							sha1(new String(value, "UTF8")));

				} catch (Exception e) {
					Assert.fail(e.getMessage());
				}
			}

			// clean data
			for (String key : objectStoredForTestMap.keySet()) {
				try {
					getClient(client).deleteForced(toByteArray(key),
							PersistOption.ASYNC);
				} catch (KineticException e) {
					Assert.fail(e.getMessage());
				}
			}
		}
	}

	@Test(dataProvider = "transportProtocolOptions")
	public void put_Forced_Random_Test_02(String client) {

		for (int i = 0; i < LOOP_TIMES; i++) {
			final Map<String, Entry> putReturnMap = new HashMap<String, Entry>(
					OP_IN_LOOP);
			final CountDownLatch putSignal = new CountDownLatch(OP_IN_LOOP);

			Map<String, ObjectStoredForTest> objectStoredForTestMap = new HashMap<String, ObjectStoredForTest>();

			for (int j = 0; j < OP_IN_LOOP; j++) {
				String key = getRandomKey();

				String value = "";
				String dbVersion = "";
				String tag = "";
				try {
					value = getRandomValue(key);
					dbVersion = getRandomVersion(key);
					tag = sha1(value);
				} catch (Exception e1) {
					Assert.fail(e1.getMessage());
				}
				ObjectStoredForTest objectStoredForTest = new ObjectStoredForTest(
						value.length(), dbVersion.length(), tag);
				if (!objectStoredForTestMap.containsKey(key)) {
					objectStoredForTestMap.put(key, objectStoredForTest);
				}

				Entry entry = new Entry(toByteArray(key), toByteArray(value));
				EntryMetadata emd = new EntryMetadata();
				emd.setVersion(toByteArray(dbVersion));
				emd.setTag(toByteArray(tag));
				emd.setAlgorithm(ALGORITHM);
				entry.setEntryMetadata(emd);

				CallbackHandler<Entry> handler = buildSuccessOnlyCallbackHandler(new SuccessAsyncHandler<Entry>() {
					@Override
					public void onSuccess(CallbackResult<Entry> result) {
						try {
							putReturnMap.put(new String(result.getResult()
									.getKey(), CHARSET_NAME), result
									.getResult());
						} catch (UnsupportedEncodingException e) {
							Assert.fail(e.getMessage());
						}
						putSignal.countDown();
					}
				});

				try {
					getClient(client).putForcedAsync(entry,
							PersistOption.ASYNC, handler);
				} catch (KineticException e) {
					Assert.fail(e.getMessage());
				}
			}

			try {
				waitForLatch(putSignal);
			} catch (InterruptedException e) {
				Assert.fail(e.getMessage());
			}
			assertEquals(objectStoredForTestMap.size(), putReturnMap.size());
			for (String key : objectStoredForTestMap.keySet()) {
				assertTrue(putReturnMap.keySet().contains(key));
				try {
					byte[] value = putReturnMap.get(key).getValue();
					assertArrayEquals(
							toByteArray(getFromKey(key, objectStoredForTestMap
									.get(key).getValueSize())), value);

					assertEquals(objectStoredForTestMap.get(key).getTag(),
							sha1(new String(value, CHARSET_NAME)));

					assertArrayEquals(
							toByteArray(getFromKey(key, objectStoredForTestMap
									.get(key).getVersionSize())), putReturnMap
									.get(key).getEntryMetadata().getVersion());
				} catch (Exception e) {
					Assert.fail(e.getMessage());
				}
			}

			// clean data
			for (String key : objectStoredForTestMap.keySet()) {
				try {
					getClient(client).deleteForced(toByteArray(key),
							PersistOption.ASYNC);
				} catch (KineticException e) {
					Assert.fail(e.getMessage());
				}
			}
		}
	}

	@Test(dataProvider = "transportProtocolOptions")
	public void put_Forced_Random_Test_03(String client) {

		for (int i = 0; i < LOOP_TIMES; i++) {
			final Map<String, Entry> putReturnMap = new HashMap<String, Entry>(
					OP_IN_LOOP);
			final Map<String, Entry> putReturnMapSecond = new HashMap<String, Entry>(
					OP_IN_LOOP);
			final CountDownLatch putSignal = new CountDownLatch(OP_IN_LOOP);
			final CountDownLatch putSignalSecond = new CountDownLatch(
					OP_IN_LOOP);

			Map<String, ObjectStoredForTest> objectStoredForTestMap = new HashMap<String, ObjectStoredForTest>();
			Map<String, ObjectStoredForTest> objectStoredForTestMapSecond = new HashMap<String, ObjectStoredForTest>();

			for (int j = 0; j < OP_IN_LOOP; j++) {
				String key = getRandomKey();

				String value = "";
				String dbVersion = "";
				String tag = "";
				try {
					value = getRandomValue(key);
					dbVersion = getRandomVersion(key);
					tag = sha1(value);
				} catch (Exception e1) {
					Assert.fail(e1.getMessage());
				}

				ObjectStoredForTest objectStoredForTest = new ObjectStoredForTest(
						value.length(), dbVersion.length(), tag);
				if (!objectStoredForTestMap.containsKey(key)) {
					objectStoredForTestMap.put(key, objectStoredForTest);
				}

				Entry entry = new Entry(toByteArray(key), toByteArray(value));
				EntryMetadata emd = new EntryMetadata();
				emd.setVersion(toByteArray(dbVersion));
				emd.setAlgorithm(ALGORITHM);
				emd.setTag(toByteArray(tag));
				entry.setEntryMetadata(emd);

				CallbackHandler<Entry> handler = buildSuccessOnlyCallbackHandler(new SuccessAsyncHandler<Entry>() {
					@Override
					public void onSuccess(CallbackResult<Entry> result) {
						try {
							putReturnMap.put(new String(result.getResult()
									.getKey(), CHARSET_NAME), result
									.getResult());
						} catch (UnsupportedEncodingException e) {
							Assert.fail(e.getMessage());
						}
						putSignal.countDown();
					}
				});

				try {
					getClient(client).putForcedAsync(entry,
							PersistOption.ASYNC, handler);
				} catch (KineticException e) {
					Assert.fail(e.getMessage());
				}
			}

			try {
				waitForLatch(putSignal);
			} catch (InterruptedException e) {
				Assert.fail(e.getMessage());
			}

			for (String key : objectStoredForTestMap.keySet()) {

				String value = "";
				String dbVersion = "";
				String tag = "";
				try {
					value = getRandomValue(key);
					dbVersion = getRandomVersion(key);
					tag = sha1(value);
				} catch (Exception e1) {
					Assert.fail(e1.getMessage());
				}

				ObjectStoredForTest objectStoredForTest = new ObjectStoredForTest(
						value.length(), dbVersion.length(), tag);
				objectStoredForTestMapSecond.put(key, objectStoredForTest);

				Entry entry = new Entry(toByteArray(key), toByteArray(value));
				EntryMetadata emd = new EntryMetadata();
				emd.setAlgorithm(ALGORITHM);
				emd.setTag(toByteArray(tag));
				try {
					emd.setVersion(toByteArray(dbVersion));
				} catch (Exception e1) {
					Assert.fail(e1.getMessage());
				}

				entry.setEntryMetadata(emd);

				CallbackHandler<Entry> handler = buildSuccessOnlyCallbackHandler(new SuccessAsyncHandler<Entry>() {
					@Override
					public void onSuccess(CallbackResult<Entry> result) {
						try {
							putReturnMapSecond.put(new String(result
									.getResult().getKey(), CHARSET_NAME),
									result.getResult());
						} catch (UnsupportedEncodingException e) {
							Assert.fail(e.getMessage());
						}
						putSignalSecond.countDown();
					}
				});

				try {
					getClient(client).putForcedAsync(entry,
							PersistOption.ASYNC, handler);
				} catch (KineticException e) {
					Assert.fail(e.getMessage());
				}
			}

			try {
				waitForLatch(putSignalSecond);
			} catch (InterruptedException e) {
				Assert.fail(e.getMessage());
			}
			assertEquals(objectStoredForTestMapSecond.size(),
					putReturnMap.size());
			for (String key : objectStoredForTestMap.keySet()) {
				assertTrue(putReturnMap.keySet().contains(key));
				try {
					byte[] value = putReturnMapSecond.get(key).getValue();
					assertArrayEquals(
							toByteArray(getFromKey(key,
									objectStoredForTestMapSecond.get(key)
											.getValueSize())), value);

					assertEquals(
							objectStoredForTestMapSecond.get(key).getTag(),
							sha1(new String(value, CHARSET_NAME)));

					assertArrayEquals(
							toByteArray(getFromKey(key,
									objectStoredForTestMapSecond.get(key)
											.getVersionSize())),
							putReturnMapSecond.get(key).getEntryMetadata()
									.getVersion());
				} catch (Exception e) {
					Assert.fail(e.getMessage());
				}
			}

			// clean data
			for (String key : objectStoredForTestMap.keySet()) {
				try {
					getClient(client).deleteForced(toByteArray(key),
							PersistOption.ASYNC);
				} catch (KineticException e) {
					Assert.fail(e.getMessage());
				}
			}
		}
	}

	@Test(dataProvider = "transportProtocolOptions")
	public void delete_Random_Test_01(String client) {
		byte[] newVersion = toByteArray("0");
		for (int i = 0; i < LOOP_TIMES; i++) {
			final Map<String, Entry> putReturnMap = new HashMap<String, Entry>(
					OP_IN_LOOP);
			final Map<String, Entry> getReturnMap = new HashMap<String, Entry>(
					OP_IN_LOOP);
			final List<Entry> getDeleteEntryOfList = new ArrayList<Entry>();
			final CountDownLatch putSignal = new CountDownLatch(OP_IN_LOOP);
			final CountDownLatch getSignal = new CountDownLatch(OP_IN_LOOP
					- RANDOM_DELETE_KEY_NUMBER);
			final CountDownLatch deleteSignal = new CountDownLatch(
					RANDOM_DELETE_KEY_NUMBER);

			Map<String, ObjectStoredForTest> objectStoredForTestMap = new HashMap<String, ObjectStoredForTest>();

			for (int j = 0; j < OP_IN_LOOP; j++) {
				String key = getRandomKey();

				String value = "";
				String tag = "";
				try {
					value = getRandomValue(key);
					tag = sha1(value);
				} catch (Exception e1) {
					Assert.fail(e1.getMessage());
				}

				ObjectStoredForTest objectStoredForTest = new ObjectStoredForTest(
						value.length(), tag);
				objectStoredForTestMap.put(key, objectStoredForTest);

				EntryMetadata emd = new EntryMetadata();
				emd.setTag(toByteArray(tag));
				emd.setAlgorithm(ALGORITHM);
				Entry entry = new Entry(toByteArray(key), toByteArray(value),
						emd);

				CallbackHandler<Entry> handler = buildSuccessOnlyCallbackHandler(new SuccessAsyncHandler<Entry>() {
					@Override
					public void onSuccess(CallbackResult<Entry> result) {
						try {
							putReturnMap.put(new String(result.getResult()
									.getKey(), CHARSET_NAME), result
									.getResult());
						} catch (UnsupportedEncodingException e) {
							Assert.fail(e.getMessage());
						}
						putSignal.countDown();
					}
				});

				try {
					getClient(client).putAsync(entry, newVersion,
							PersistOption.ASYNC, handler);
				} catch (KineticException e) {
					Assert.fail(e.getMessage());
				}
			}

			try {
				waitForLatch(putSignal);
			} catch (InterruptedException e) {
				Assert.fail(e.getMessage());
			}
			assertEquals(objectStoredForTestMap.size(), putReturnMap.size());
			for (String key : objectStoredForTestMap.keySet()) {
				assertTrue(putReturnMap.keySet().contains(key));
				try {
					assertArrayEquals(
							toByteArray(getFromKey(key, objectStoredForTestMap
									.get(key).getValueSize())), putReturnMap
									.get(key).getValue());

					assertEquals(objectStoredForTestMap.get(key).getTag(),
							sha1(new String(putReturnMap.get(key).getValue(),
									CHARSET_NAME)));
				} catch (Exception e) {
					Assert.fail(e.getMessage());
				}
			}
			// random delete 100 keys
			List<String> deleteKeysOfList = new ArrayList<String>();
			for (int deleteIndex = 0; deleteIndex < RANDOM_DELETE_KEY_NUMBER; deleteIndex++) {
				Random random = new Random();
				int deleteLocation = random.nextInt(RANDOM_DELETE_KEY_NUMBER);
				int count = 0;
				for (String randomKey : objectStoredForTestMap.keySet()) {
					if (count == deleteLocation) {
						deleteKeysOfList.add(randomKey);
						Entry deleteEntry = new Entry();
						deleteEntry.setKey(toByteArray(randomKey));
						EntryMetadata eed = new EntryMetadata();
						eed.setVersion(newVersion);
						deleteEntry.setEntryMetadata(eed);

						try {
							getClient(client).delete(deleteEntry,
									PersistOption.ASYNC);
						} catch (KineticException e) {
							Assert.fail(e.getMessage());
						}
					}
					count++;
				}
			}

			// get back all keys
			for (String key : objectStoredForTestMap.keySet()) {
				if (!deleteKeysOfList.contains(key)) {
					CallbackHandler<Entry> handler = buildSuccessOnlyCallbackHandler(new SuccessAsyncHandler<Entry>() {
						@Override
						public void onSuccess(CallbackResult<Entry> result) {
							try {
								getReturnMap.put(new String(result.getResult()
										.getKey(), CHARSET_NAME), result
										.getResult());
							} catch (UnsupportedEncodingException e) {
								Assert.fail(e.getMessage());
							}
							getSignal.countDown();
						}
					});

					try {
						getClient(client).getAsync(toByteArray(key), handler);
					} catch (KineticException e) {
						Assert.fail(e.getMessage());
					}
				}
			}

			try {
				waitForLatch(getSignal);
			} catch (InterruptedException e) {
				Assert.fail(e.getMessage());
			}

			// check each field
			for (String key : objectStoredForTestMap.keySet()) {
				if (!deleteKeysOfList.contains(key)) {

					try {
						assertArrayEquals(
								toByteArray(getFromKey(key,
										objectStoredForTestMap.get(key)
												.getValueSize())), getReturnMap
										.get(key).getValue());

						assertEquals(objectStoredForTestMap.get(key).getTag(),
								sha1(new String(putReturnMap.get(key)
										.getValue(), CHARSET_NAME)));
					} catch (Exception e) {
						Assert.fail(e.getMessage());
					}
				}
			}

			// get delete key
			for (String key : deleteKeysOfList) {
				CallbackHandler<Entry> handler = buildSuccessOnlyCallbackHandler(new SuccessAsyncHandler<Entry>() {
					@Override
					public void onSuccess(CallbackResult<Entry> result) {
						getDeleteEntryOfList.add(result.getResult());

						deleteSignal.countDown();
					}
				});

				try {
					getClient(client).getAsync(toByteArray(key), handler);
				} catch (KineticException e) {
					Assert.fail(e.getMessage());
				}
			}

			try {
				waitForLatch(deleteSignal);
			} catch (InterruptedException e) {
				Assert.fail(e.getMessage());
			}

			// check back
			for (int index = 0; i < getDeleteEntryOfList.size(); i++) {
				assertNull(getDeleteEntryOfList.get(index));
			}

			// clean data
			for (String key : objectStoredForTestMap.keySet()) {
				try {
					getClient(client).deleteForced(toByteArray(key),
							PersistOption.ASYNC);
				} catch (KineticException e) {
					Assert.fail(e.getMessage());
				}
			}
		}
	}

	@Test(dataProvider = "transportProtocolOptions")
	public void delete_Random_Test_02(String client) {
		byte[] newVersion = toByteArray("0");
		for (int i = 0; i < LOOP_TIMES; i++) {
			final Map<String, Entry> putReturnMap = new HashMap<String, Entry>(
					OP_IN_LOOP);
			final Map<String, Entry> getReturnMap = new HashMap<String, Entry>(
					OP_IN_LOOP);
			final List<Entry> getDeleteEntryOfList = new ArrayList<Entry>();
			final CountDownLatch putSignal = new CountDownLatch(OP_IN_LOOP);
			final CountDownLatch getSignal = new CountDownLatch(OP_IN_LOOP);
			final CountDownLatch deleteSignal = new CountDownLatch(
					RANDOM_DELETE_KEY_NUMBER);

			Map<String, ObjectStoredForTest> objectStoredForTestMap = new HashMap<String, ObjectStoredForTest>();

			for (int j = 0; j < OP_IN_LOOP; j++) {
				String key = getRandomKey();

				String value = "";
				String tag = "";
				try {
					value = getRandomValue(key);
					tag = sha1(value);
				} catch (Exception e1) {
					Assert.fail(e1.getMessage());
				}

				ObjectStoredForTest objectStoredForTest = new ObjectStoredForTest(
						value.length(), tag);
				objectStoredForTestMap.put(key, objectStoredForTest);

				EntryMetadata emd = new EntryMetadata();
				emd.setTag(toByteArray(tag));
				emd.setAlgorithm(ALGORITHM);

				Entry entry = new Entry(toByteArray(key), toByteArray(value),
						emd);

				CallbackHandler<Entry> handler = buildSuccessOnlyCallbackHandler(new SuccessAsyncHandler<Entry>() {
					@Override
					public void onSuccess(CallbackResult<Entry> result) {
						try {
							putReturnMap.put(new String(result.getResult()
									.getKey(), CHARSET_NAME), result
									.getResult());
						} catch (UnsupportedEncodingException e) {
							Assert.fail(e.getMessage());
						}
						putSignal.countDown();
					}
				});

				try {
					getClient(client).putAsync(entry, newVersion,
							PersistOption.ASYNC, handler);
				} catch (KineticException e) {
					Assert.fail(e.getMessage());
				}
			}

			try {
				waitForLatch(putSignal);
			} catch (InterruptedException e) {
				Assert.fail(e.getMessage());
			}
			assertEquals(objectStoredForTestMap.size(), putReturnMap.size());
			for (String key : objectStoredForTestMap.keySet()) {
				assertTrue(putReturnMap.keySet().contains(key));
				try {
					assertArrayEquals(
							toByteArray(getFromKey(key, objectStoredForTestMap
									.get(key).getValueSize())), putReturnMap
									.get(key).getValue());

					assertEquals(objectStoredForTestMap.get(key).getTag(),
							sha1(new String(putReturnMap.get(key).getValue(),
									CHARSET_NAME)));

				} catch (Exception e) {
					Assert.fail(e.getMessage());
				}
			}
			// random delete 100 keys(not existed in DB)
			List<String> deleteKeysOfList = new ArrayList<String>();
			for (int deleteIndex = 0; deleteIndex < RANDOM_DELETE_KEY_NUMBER; deleteIndex++) {

				Entry deleteEntry = new Entry();
				String deleteKey = getRandomNonExistingKey();
				deleteEntry.setKey(toByteArray(deleteKey));

				deleteKeysOfList.add(deleteKey);

				try {
					getClient(client).delete(deleteEntry, PersistOption.ASYNC);
				} catch (KineticException e) {
					Assert.fail(e.getMessage());
				}

			}

			// get back all keys
			for (String key : objectStoredForTestMap.keySet()) {
				CallbackHandler<Entry> handler = buildSuccessOnlyCallbackHandler(new SuccessAsyncHandler<Entry>() {
					@Override
					public void onSuccess(CallbackResult<Entry> result) {
						try {
							getReturnMap.put(new String(result.getResult()
									.getKey(), CHARSET_NAME), result
									.getResult());

						} catch (UnsupportedEncodingException e) {
							Assert.fail(e.getMessage());
						}
						getSignal.countDown();
					}
				});

				try {
					getClient(client).getAsync(toByteArray(key), handler);
				} catch (KineticException e) {
					Assert.fail(e.getMessage());
				}
			}

			try {
				waitForLatch(getSignal);
			} catch (InterruptedException e) {
				Assert.fail(e.getMessage());
			}

			// check each field
			assertEquals(objectStoredForTestMap.size(), getReturnMap.size());
			for (String key : objectStoredForTestMap.keySet()) {

				try {
					assertArrayEquals(
							toByteArray(getFromKey(key, objectStoredForTestMap
									.get(key).getValueSize())), getReturnMap
									.get(key).getValue());

					assertEquals(objectStoredForTestMap.get(key).getTag(),
							sha1(new String(getReturnMap.get(key).getValue(),
									CHARSET_NAME)));
				} catch (Exception e) {
					Assert.fail(e.getMessage());
				}
			}

			// get delete key
			for (String key : deleteKeysOfList) {
				CallbackHandler<Entry> handler = buildSuccessOnlyCallbackHandler(new SuccessAsyncHandler<Entry>() {
					@Override
					public void onSuccess(CallbackResult<Entry> result) {
						getDeleteEntryOfList.add(result.getResult());

						deleteSignal.countDown();
					}
				});

				try {
					getClient(client).getAsync(toByteArray(key), handler);
				} catch (KineticException e) {
					Assert.fail(e.getMessage());
				}
			}

			try {
				waitForLatch(deleteSignal);
			} catch (InterruptedException e) {
				Assert.fail(e.getMessage());
			}

			// check back
			for (int index = 0; i < getDeleteEntryOfList.size(); i++) {
				assertNull(getDeleteEntryOfList.get(index));
			}

			// clean data
			for (String key : objectStoredForTestMap.keySet()) {
				try {
					getClient(client).deleteForced(toByteArray(key),
							PersistOption.ASYNC);
				} catch (KineticException e) {
					Assert.fail(e.getMessage());
				}
			}
		}
	}

	@Test(dataProvider = "transportProtocolOptions")
	public void delete_Forced_Random_Test_01(String client) {
		byte[] newVersion = toByteArray("0");
		for (int i = 0; i < LOOP_TIMES; i++) {
			final Map<String, Entry> putReturnMap = new HashMap<String, Entry>(
					OP_IN_LOOP);
			final Map<String, Entry> getReturnMap = new HashMap<String, Entry>(
					OP_IN_LOOP);
			final List<Entry> getDeleteEntryOfList = new ArrayList<Entry>();
			final CountDownLatch putSignal = new CountDownLatch(OP_IN_LOOP);
			final CountDownLatch getSignal = new CountDownLatch(OP_IN_LOOP
					- RANDOM_DELETE_KEY_NUMBER);
			final CountDownLatch deleteSignal = new CountDownLatch(
					RANDOM_DELETE_KEY_NUMBER);

			Map<String, ObjectStoredForTest> objectStoredForTestMap = new HashMap<String, ObjectStoredForTest>();

			for (int j = 0; j < OP_IN_LOOP; j++) {
				String key = getRandomKey();

				String value = "";
				String tag = "";
				try {
					value = getRandomValue(key);
					tag = sha1(value);
				} catch (Exception e1) {
					Assert.fail(e1.getMessage());
				}

				ObjectStoredForTest objectStoredForTest = new ObjectStoredForTest(
						value.length(), tag);
				objectStoredForTestMap.put(key, objectStoredForTest);

				Entry entry = new Entry(toByteArray(key), toByteArray(value));

				CallbackHandler<Entry> handler = buildSuccessOnlyCallbackHandler(new SuccessAsyncHandler<Entry>() {
					@Override
					public void onSuccess(CallbackResult<Entry> result) {
						try {
							putReturnMap.put(new String(result.getResult()
									.getKey(), CHARSET_NAME), result
									.getResult());
						} catch (UnsupportedEncodingException e) {
							Assert.fail(e.getMessage());
						}
						putSignal.countDown();
					}
				});

				try {
					getClient(client).putAsync(entry, newVersion,
							PersistOption.ASYNC, handler);
				} catch (KineticException e) {
					Assert.fail(e.getMessage());
				}
			}

			try {
				waitForLatch(putSignal);
			} catch (InterruptedException e) {
				Assert.fail(e.getMessage());
			}
			assertEquals(objectStoredForTestMap.size(), putReturnMap.size());
			for (String key : objectStoredForTestMap.keySet()) {
				assertTrue(putReturnMap.keySet().contains(key));
				try {
					assertArrayEquals(
							toByteArray(getFromKey(key, objectStoredForTestMap
									.get(key).getValueSize())), putReturnMap
									.get(key).getValue());

					assertEquals(objectStoredForTestMap.get(key).getTag(),
							sha1(new String(putReturnMap.get(key).getValue(),
									CHARSET_NAME)));

				} catch (Exception e) {
					Assert.fail(e.getMessage());
				}
			}
			// random delete 100 keys
			List<String> deleteKeysOfList = new ArrayList<String>();
			for (int deleteIndex = 0; deleteIndex < RANDOM_DELETE_KEY_NUMBER; deleteIndex++) {
				Random random = new Random();
				int deleteLocation = random.nextInt(RANDOM_DELETE_KEY_NUMBER);
				int count = 0;
				for (String randomKey : objectStoredForTestMap.keySet()) {
					if (count == deleteLocation) {
						deleteKeysOfList.add(randomKey);
						Entry deleteEntry = new Entry();
						deleteEntry.setKey(toByteArray(randomKey));
						EntryMetadata eed = new EntryMetadata();
						eed.setVersion(newVersion);
						deleteEntry.setEntryMetadata(eed);

						try {
							getClient(client)
									.deleteForced(toByteArray(randomKey),
											PersistOption.ASYNC);
						} catch (KineticException e) {
							Assert.fail(e.getMessage());
						}
					}
					count++;
				}
			}

			// get back all keys
			for (String key : objectStoredForTestMap.keySet()) {
				if (!deleteKeysOfList.contains(key)) {
					CallbackHandler<Entry> handler = buildSuccessOnlyCallbackHandler(new SuccessAsyncHandler<Entry>() {
						@Override
						public void onSuccess(CallbackResult<Entry> result) {
							try {
								getReturnMap.put(new String(result.getResult()
										.getKey(), CHARSET_NAME), result
										.getResult());
							} catch (UnsupportedEncodingException e) {
								Assert.fail(e.getMessage());
							}
							getSignal.countDown();
						}
					});

					try {
						getClient(client).getAsync(toByteArray(key), handler);
					} catch (KineticException e) {
						Assert.fail(e.getMessage());
					}
				}
			}

			try {
				waitForLatch(getSignal);
			} catch (InterruptedException e) {
				Assert.fail(e.getMessage());
			}

			// check each field
			for (String key : objectStoredForTestMap.keySet()) {
				if (!deleteKeysOfList.contains(key)) {

					try {
						assertArrayEquals(
								toByteArray(getFromKey(key,
										objectStoredForTestMap.get(key)
												.getValueSize())), getReturnMap
										.get(key).getValue());

						assertEquals(objectStoredForTestMap.get(key).getTag(),
								sha1(new String(putReturnMap.get(key)
										.getValue(), CHARSET_NAME)));
					} catch (Exception e) {
						Assert.fail(e.getMessage());
					}
				}
			}

			// get delete key
			for (String key : deleteKeysOfList) {
				CallbackHandler<Entry> handler = buildSuccessOnlyCallbackHandler(new SuccessAsyncHandler<Entry>() {
					@Override
					public void onSuccess(CallbackResult<Entry> result) {
						getDeleteEntryOfList.add(result.getResult());

						deleteSignal.countDown();
					}
				});

				try {
					getClient(client).getAsync(toByteArray(key), handler);
				} catch (KineticException e) {
					Assert.fail(e.getMessage());
				}
			}

			try {
				waitForLatch(deleteSignal);
			} catch (InterruptedException e) {
				Assert.fail(e.getMessage());
			}

			// check back
			for (int index = 0; i < getDeleteEntryOfList.size(); i++) {
				assertNull(getDeleteEntryOfList.get(index));
			}

			// clean data
			for (String key : objectStoredForTestMap.keySet()) {
				try {
					getClient(client).deleteForced(toByteArray(key),
							PersistOption.ASYNC);
				} catch (KineticException e) {
					Assert.fail(e.getMessage());
				}
			}
		}
	}

	@Test(dataProvider = "transportProtocolOptions")
	public void delete_Forced_Random_Test_02(String client) {
		byte[] newVersion = toByteArray("0");
		for (int i = 0; i < LOOP_TIMES; i++) {
			final Map<String, Entry> putReturnMap = new HashMap<String, Entry>(
					OP_IN_LOOP);
			final Map<String, Entry> getReturnMap = new HashMap<String, Entry>(
					OP_IN_LOOP);
			final List<Entry> getDeleteEntryOfList = new ArrayList<Entry>();
			final CountDownLatch putSignal = new CountDownLatch(OP_IN_LOOP);
			final CountDownLatch getSignal = new CountDownLatch(OP_IN_LOOP
					- RANDOM_DELETE_KEY_NUMBER);
			final CountDownLatch deleteSignal = new CountDownLatch(
					RANDOM_DELETE_KEY_NUMBER);

			Map<String, ObjectStoredForTest> objectStoredForTestMap = new HashMap<String, ObjectStoredForTest>();

			for (int j = 0; j < OP_IN_LOOP; j++) {
				String key = getRandomKey();

				String value = "";
				String tag = "";
				try {
					value = getRandomValue(key);
					tag = sha1(value);
				} catch (Exception e1) {
					Assert.fail(e1.getMessage());
				}

				ObjectStoredForTest objectStoredForTest = new ObjectStoredForTest(
						value.length(), tag);
				objectStoredForTestMap.put(key, objectStoredForTest);

				EntryMetadata emd = new EntryMetadata();
				emd.setAlgorithm(ALGORITHM);
				emd.setTag(toByteArray(tag));

				Entry entry = new Entry(toByteArray(key), toByteArray(value),
						emd);

				CallbackHandler<Entry> handler = buildSuccessOnlyCallbackHandler(new SuccessAsyncHandler<Entry>() {
					@Override
					public void onSuccess(CallbackResult<Entry> result) {
						try {
							putReturnMap.put(new String(result.getResult()
									.getKey(), CHARSET_NAME), result
									.getResult());
						} catch (UnsupportedEncodingException e) {
							Assert.fail(e.getMessage());
						}
						putSignal.countDown();
					}
				});

				try {
					getClient(client).putAsync(entry, newVersion,
							PersistOption.ASYNC, handler);
				} catch (KineticException e) {
					Assert.fail(e.getMessage());
				}
			}

			try {
				waitForLatch(putSignal);
			} catch (InterruptedException e) {
				Assert.fail(e.getMessage());
			}
			assertEquals(objectStoredForTestMap.size(), putReturnMap.size());
			for (String key : objectStoredForTestMap.keySet()) {
				assertTrue(putReturnMap.keySet().contains(key));
				try {
					assertArrayEquals(
							toByteArray(getFromKey(key, objectStoredForTestMap
									.get(key).getValueSize())), putReturnMap
									.get(key).getValue());

					assertEquals(objectStoredForTestMap.get(key).getTag(),
							sha1(new String(putReturnMap.get(key).getValue(),
									CHARSET_NAME)));
				} catch (Exception e) {
					Assert.fail(e.getMessage());
				}
			}
			// random delete 100 keys(not existed in DB)
			List<String> deleteKeysOfList = new ArrayList<String>();
			for (int deleteIndex = 0; deleteIndex < RANDOM_DELETE_KEY_NUMBER; deleteIndex++) {

				Entry deleteEntry = new Entry();
				String deleteKey = getRandomNonExistingKey();
				deleteEntry.setKey(toByteArray(deleteKey));

				deleteKeysOfList.add(deleteKey);

				try {
					getClient(client).deleteForced(toByteArray(deleteKey),
							PersistOption.ASYNC);
				} catch (KineticException e) {
					Assert.fail(e.getMessage());
				}

			}

			// get back all keys
			for (String key : objectStoredForTestMap.keySet()) {
				CallbackHandler<Entry> handler = buildSuccessOnlyCallbackHandler(new SuccessAsyncHandler<Entry>() {
					@Override
					public void onSuccess(CallbackResult<Entry> result) {
						try {
							getReturnMap.put(new String(result.getResult()
									.getKey(), CHARSET_NAME), result
									.getResult());
						} catch (UnsupportedEncodingException e) {
							Assert.fail(e.getMessage());
						}
						getSignal.countDown();
					}
				});

				try {
					getClient(client).getAsync(toByteArray(key), handler);
				} catch (KineticException e) {
					Assert.fail(e.getMessage());
				}
			}

			try {
				waitForLatch(getSignal);
			} catch (InterruptedException e) {
				Assert.fail(e.getMessage());
			}

			// check each field
			for (String key : objectStoredForTestMap.keySet()) {

				try {
					assertArrayEquals(
							toByteArray(getFromKey(key, objectStoredForTestMap
									.get(key).getValueSize())), getReturnMap
									.get(key).getValue());

					assertEquals(objectStoredForTestMap.get(key).getTag(),
							sha1(new String(putReturnMap.get(key).getValue(),
									CHARSET_NAME)));
				} catch (Exception e) {
					Assert.fail(e.getMessage());
				}
			}

			// get delete key
			for (String key : deleteKeysOfList) {
				CallbackHandler<Entry> handler = buildSuccessOnlyCallbackHandler(new SuccessAsyncHandler<Entry>() {
					@Override
					public void onSuccess(CallbackResult<Entry> result) {
						getDeleteEntryOfList.add(result.getResult());

						deleteSignal.countDown();
					}
				});

				try {
					getClient(client).getAsync(toByteArray(key), handler);
				} catch (KineticException e) {
					Assert.fail(e.getMessage());
				}
			}

			try {
				waitForLatch(deleteSignal);
			} catch (InterruptedException e) {
				Assert.fail(e.getMessage());
			}

			// check back
			for (int index = 0; i < getDeleteEntryOfList.size(); i++) {
				assertNull(getDeleteEntryOfList.get(index));
			}

			// clean data
			for (String key : objectStoredForTestMap.keySet()) {
				try {
					getClient(client).deleteForced(toByteArray(key),
							PersistOption.ASYNC);
				} catch (KineticException e) {
					Assert.fail(e.getMessage());
				}
			}
		}
	}

	@Test(dataProvider = "transportProtocolOptions")
	public void get_Random_Test_01(String client) {
		byte[] newVersion = toByteArray("0");
		for (int i = 0; i < LOOP_TIMES; i++) {
			final Map<String, Entry> putReturnMap = new HashMap<String, Entry>(
					OP_IN_LOOP);
			final Map<String, Entry> getReturnMap = new HashMap<String, Entry>(
					OP_IN_LOOP);
			final CountDownLatch putSignal = new CountDownLatch(OP_IN_LOOP);
			final CountDownLatch getSignal = new CountDownLatch(OP_IN_LOOP);

			Map<String, ObjectStoredForTest> objectForTestOfMap = new HashMap<String, ObjectStoredForTest>();

			for (int j = 0; j < OP_IN_LOOP; j++) {
				String key = getRandomKey();

				String value = "";
				try {
					value = getRandomValue(key);
				} catch (Exception e1) {
					Assert.fail(e1.getMessage());
				}

				EntryMetadata emd = new EntryMetadata();
				emd.setAlgorithm(ALGORITHM);
				String tag = "";
				try {
					tag = sha1(value);
				} catch (NoSuchAlgorithmException e1) {
					Assert.fail(e1.getMessage());
				}
				emd.setTag(toByteArray(tag));

				ObjectStoredForTest objectStoredForTest = new ObjectStoredForTest(
						value.length(), tag);
				if (!objectForTestOfMap.containsKey(key)) {
					objectForTestOfMap.put(key, objectStoredForTest);
				}

				Entry entry = new Entry(toByteArray(key), toByteArray(value),
						emd);

				CallbackHandler<Entry> handler = buildSuccessOnlyCallbackHandler(new SuccessAsyncHandler<Entry>() {
					@Override
					public void onSuccess(CallbackResult<Entry> result) {
						try {
							putReturnMap.put(new String(result.getResult()
									.getKey(), CHARSET_NAME), result
									.getResult());
						} catch (UnsupportedEncodingException e) {
							Assert.fail(e.getMessage());
						}
						putSignal.countDown();
					}
				});

				try {
					getClient(client).putAsync(entry, newVersion,
							PersistOption.ASYNC, handler);
				} catch (KineticException e) {
					Assert.fail(e.getMessage());
				}
			}

			try {
				waitForLatch(putSignal);
			} catch (InterruptedException e) {
				Assert.fail(e.getMessage());
			}
			assertEquals(objectForTestOfMap.size(), putReturnMap.size());
			for (String key : objectForTestOfMap.keySet()) {
				assertTrue(putReturnMap.keySet().contains(key));
				try {
					byte[] value = putReturnMap.get(key).getValue();
					assertArrayEquals(
							toByteArray(getFromKey(key,
									objectForTestOfMap.get(key).getValueSize())),
							value);

					assertEquals(objectForTestOfMap.get(key).getTag(),
							sha1(new String(value, CHARSET_NAME)));
				} catch (Exception e) {
					Assert.fail(e.getMessage());
				}
			}

			// get back all keys
			for (String key : objectForTestOfMap.keySet()) {
				CallbackHandler<Entry> handler = buildSuccessOnlyCallbackHandler(new SuccessAsyncHandler<Entry>() {
					@Override
					public void onSuccess(CallbackResult<Entry> result) {
						try {
							getReturnMap.put(new String(result.getResult()
									.getKey(), CHARSET_NAME), result
									.getResult());
						} catch (UnsupportedEncodingException e) {
							Assert.fail(e.getMessage());
						}
						getSignal.countDown();
					}
				});

				try {
					getClient(client).getAsync(toByteArray(key), handler);
				} catch (KineticException e) {
					Assert.fail(e.getMessage());
				}
			}

			try {
				waitForLatch(getSignal);
			} catch (InterruptedException e) {
				Assert.fail(e.getMessage());
			}

			// check each field
			for (String key : objectForTestOfMap.keySet()) {
				try {
					assertArrayEquals(
							toByteArray(getFromKey(key,
									objectForTestOfMap.get(key).getValueSize())),
							getReturnMap.get(key).getValue());

					assertEquals(objectForTestOfMap.get(key).getTag(),
							sha1(new String(getReturnMap.get(key).getValue(),
									CHARSET_NAME)));

					assertArrayEquals(newVersion, getReturnMap.get(key)
							.getEntryMetadata().getVersion());
				} catch (Exception e) {
					Assert.fail(e.getMessage());
				}
			}

			// clean data
			for (String key : objectForTestOfMap.keySet()) {
				try {
					getClient(client).deleteForced(toByteArray(key),
							PersistOption.ASYNC);
				} catch (KineticException e) {
					Assert.fail(e.getMessage());
				}
			}
		}
	}

	@Test(dataProvider = "transportProtocolOptions")
	public void get_Random_Test_02(String client) {

		for (int i = 0; i < LOOP_TIMES; i++) {
			final Map<String, Entry> putReturnMap = new HashMap<String, Entry>(
					OP_IN_LOOP);
			final Map<String, Entry> getReturnMap = new HashMap<String, Entry>(
					OP_IN_LOOP);
			final Map<String, Entry> putReturnSecondMap = new HashMap<String, Entry>(
					OP_IN_LOOP);
			final Map<String, Entry> getReturnSecondMap = new HashMap<String, Entry>(
					OP_IN_LOOP);
			final CountDownLatch putSignal = new CountDownLatch(OP_IN_LOOP);
			final CountDownLatch putSecondSignal = new CountDownLatch(
					OP_IN_LOOP);
			final CountDownLatch getSignal = new CountDownLatch(OP_IN_LOOP);
			final CountDownLatch getSecondSignal = new CountDownLatch(
					OP_IN_LOOP);

			Map<String, ObjectStoredForTest> objectStoredForTestMap = new HashMap<String, ObjectStoredForTest>();
			Map<String, ObjectStoredForTest> objectStoredForTestMapSecond = new HashMap<String, ObjectStoredForTest>();

			for (int j = 0; j < OP_IN_LOOP; j++) {
				String key = getRandomKey();

				String value = "";
				String newVersion = "";
				String tag = "";
				try {
					value = getRandomValue(key);
					newVersion = getRandomVersion(key);
					tag = sha1(value);
				} catch (Exception e1) {
					Assert.fail(e1.getMessage());
				}

				ObjectStoredForTest objectStoredForTest = new ObjectStoredForTest(
						value.length(), newVersion.length(), tag);
				objectStoredForTestMap.put(key, objectStoredForTest);

				EntryMetadata emd = new EntryMetadata();
				emd.setAlgorithm(ALGORITHM);
				emd.setTag(toByteArray(tag));
				Entry entry = new Entry(toByteArray(key), toByteArray(value),
						emd);

				CallbackHandler<Entry> handler = buildSuccessOnlyCallbackHandler(new SuccessAsyncHandler<Entry>() {
					@Override
					public void onSuccess(CallbackResult<Entry> result) {
						try {
							putReturnMap.put(new String(result.getResult()
									.getKey(), CHARSET_NAME), result
									.getResult());
						} catch (UnsupportedEncodingException e) {
							Assert.fail(e.getMessage());
						}
						putSignal.countDown();
					}
				});

				try {
					getClient(client).putAsync(entry, toByteArray(newVersion),
							PersistOption.ASYNC, handler);
				} catch (KineticException e) {
					Assert.fail(e.getMessage());
				}
			}

			try {
				waitForLatch(putSignal);
			} catch (InterruptedException e) {
				Assert.fail(e.getMessage());
			}

			// get back all keys
			for (String key : objectStoredForTestMap.keySet()) {
				CallbackHandler<Entry> handler = buildSuccessOnlyCallbackHandler(new SuccessAsyncHandler<Entry>() {
					@Override
					public void onSuccess(CallbackResult<Entry> result) {
						try {
							getReturnMap.put(new String(result.getResult()
									.getKey(), CHARSET_NAME), result
									.getResult());
						} catch (UnsupportedEncodingException e) {
							Assert.fail(e.getMessage());
						}
						getSignal.countDown();
					}
				});

				try {
					getClient(client).getAsync(toByteArray(key), handler);
				} catch (KineticException e) {
					Assert.fail(e.getMessage());
				}
			}

			try {
				waitForLatch(getSignal);
			} catch (InterruptedException e) {
				Assert.fail(e.getMessage());
			}

			// check each field
			for (String key : objectStoredForTestMap.keySet()) {
				try {
					assertArrayEquals(
							toByteArray(getFromKey(key, objectStoredForTestMap
									.get(key).getValueSize())), getReturnMap
									.get(key).getValue());

					assertEquals(objectStoredForTestMap.get(key).getTag(),
							sha1(new String(getReturnMap.get(key).getValue(),
									CHARSET_NAME)));

					assertArrayEquals(
							toByteArray(getFromKey(key, objectStoredForTestMap
									.get(key).getVersionSize())), getReturnMap
									.get(key).getEntryMetadata().getVersion());
				} catch (Exception e) {
					Assert.fail(e.getMessage());
				}
			}

			for (String key : objectStoredForTestMap.keySet()) {

				String value = "";
				String newVersion = "";
				String tag = "";
				try {
					value = getRandomValue(key);
					newVersion = getRandomVersion(key);
					tag = sha1(value);
				} catch (Exception e1) {
					Assert.fail(e1.getMessage());
				}

				ObjectStoredForTest objectStoredForTest = new ObjectStoredForTest(
						value.length(), newVersion.length(), tag);
				objectStoredForTestMapSecond.put(key, objectStoredForTest);

				Entry entry = new Entry(toByteArray(key), toByteArray(value));
				EntryMetadata emd = new EntryMetadata();
				try {
					emd.setVersion(toByteArray(getFromKey(key,
							objectStoredForTestMap.get(key).getVersionSize())));
				} catch (Exception e1) {
					Assert.fail(e1.getMessage());
				}

				entry.setEntryMetadata(emd);

				CallbackHandler<Entry> handler = buildSuccessOnlyCallbackHandler(new SuccessAsyncHandler<Entry>() {
					@Override
					public void onSuccess(CallbackResult<Entry> result) {
						try {
							putReturnSecondMap.put(new String(result
									.getResult().getKey(), CHARSET_NAME),
									result.getResult());
						} catch (UnsupportedEncodingException e) {
							Assert.fail(e.getMessage());
						}
						putSecondSignal.countDown();
					}
				});

				try {
					getClient(client).putAsync(entry, toByteArray(newVersion),
							PersistOption.ASYNC, handler);
				} catch (KineticException e) {
					Assert.fail(e.getMessage());
				}
			}

			try {
				waitForLatch(putSecondSignal);
			} catch (InterruptedException e) {
				Assert.fail(e.getMessage());
			}

			// get back all keys
			for (String key : objectStoredForTestMap.keySet()) {
				CallbackHandler<Entry> handler = buildSuccessOnlyCallbackHandler(new SuccessAsyncHandler<Entry>() {
					@Override
					public void onSuccess(CallbackResult<Entry> result) {
						try {
							getReturnSecondMap.put(new String(result
									.getResult().getKey(), CHARSET_NAME),
									result.getResult());
						} catch (UnsupportedEncodingException e) {
							Assert.fail(e.getMessage());
						}
						getSecondSignal.countDown();
					}
				});

				try {
					getClient(client).getAsync(toByteArray(key), handler);
				} catch (KineticException e) {
					Assert.fail(e.getMessage());
				}
			}

			try {
				waitForLatch(getSecondSignal);
			} catch (InterruptedException e) {
				Assert.fail(e.getMessage());
			}

			// check each field
			for (String key : objectStoredForTestMap.keySet()) {
				try {
					assertArrayEquals(
							toByteArray(getFromKey(key,
									objectStoredForTestMapSecond.get(key)
											.getValueSize())),
							getReturnSecondMap.get(key).getValue());

					assertEquals(
							objectStoredForTestMapSecond.get(key).getTag(),
							sha1(new String(getReturnSecondMap.get(key)
									.getValue(), CHARSET_NAME)));

					assertArrayEquals(
							toByteArray(getFromKey(key,
									objectStoredForTestMapSecond.get(key)
											.getVersionSize())),
							getReturnSecondMap.get(key).getEntryMetadata()
									.getVersion());
				} catch (Exception e) {
					Assert.fail(e.getMessage());
				}
			}

			// clean data
			for (String key : objectStoredForTestMap.keySet()) {
				try {
					getClient(client).deleteForced(toByteArray(key),
							PersistOption.ASYNC);
				} catch (KineticException e) {
					Assert.fail(e.getMessage());
				}
			}
		}
	}

	@Test(dataProvider = "transportProtocolOptions")
	public void get_Random_Test_03(String client) {

		for (int i = 0; i < LOOP_TIMES; i++) {
			final Map<String, Entry> putReturnMap = new HashMap<String, Entry>(
					OP_IN_LOOP);
			final Map<String, Entry> getReturnMap = new HashMap<String, Entry>(
					OP_IN_LOOP);
			final Map<String, Entry> putReturnSecondMap = new HashMap<String, Entry>(
					OP_IN_LOOP);
			final Map<String, Entry> getReturnSecondMap = new HashMap<String, Entry>(
					OP_IN_LOOP);
			final CountDownLatch putSignal = new CountDownLatch(OP_IN_LOOP);
			final CountDownLatch getSignal = new CountDownLatch(OP_IN_LOOP);
			final CountDownLatch putSecondSignal = new CountDownLatch(
					OP_IN_LOOP);
			final CountDownLatch getSecondSignal = new CountDownLatch(
					OP_IN_LOOP);

			Map<String, ObjectStoredForTest> objectStoredForTestMap = new HashMap<String, ObjectStoredForTest>();
			Map<String, ObjectStoredForTest> objectStoredForTestSecondMap = new HashMap<String, ObjectStoredForTest>();

			for (int j = 0; j < OP_IN_LOOP; j++) {
				String key = getRandomKey();

				String value = "";
				String newVersion = "";
				String tag = "";
				try {
					value = getRandomValue(key);
					newVersion = getRandomVersion(key);
					tag = sha1(value);
				} catch (Exception e1) {
					Assert.fail(e1.getMessage());
				}

				ObjectStoredForTest objectStoredForTest = new ObjectStoredForTest(
						value.length(), newVersion.length(), tag);
				objectStoredForTestMap.put(key, objectStoredForTest);

				EntryMetadata emd = new EntryMetadata();
				emd.setAlgorithm(ALGORITHM);
				emd.setTag(toByteArray(tag));
				Entry entry = new Entry(toByteArray(key), toByteArray(value),
						emd);

				CallbackHandler<Entry> handler = buildSuccessOnlyCallbackHandler(new SuccessAsyncHandler<Entry>() {
					@Override
					public void onSuccess(CallbackResult<Entry> result) {
						try {
							putReturnMap.put(new String(result.getResult()
									.getKey(), CHARSET_NAME), result
									.getResult());
						} catch (UnsupportedEncodingException e) {
							Assert.fail(e.getMessage());
						}
						putSignal.countDown();
					}
				});

				try {
					getClient(client).putAsync(entry, toByteArray(newVersion),
							PersistOption.ASYNC, handler);
				} catch (KineticException e) {
					Assert.fail(e.getMessage());
				}
			}

			// get back all keys
			for (String key : objectStoredForTestMap.keySet()) {
				CallbackHandler<Entry> handler = buildSuccessOnlyCallbackHandler(new SuccessAsyncHandler<Entry>() {
					@Override
					public void onSuccess(CallbackResult<Entry> result) {
						try {
							getReturnMap.put(new String(result.getResult()
									.getKey(), CHARSET_NAME), result
									.getResult());
						} catch (UnsupportedEncodingException e) {
							Assert.fail(e.getMessage());
						}
						getSignal.countDown();
					}
				});

				try {
					getClient(client).getAsync(toByteArray(key), handler);
				} catch (KineticException e) {
					Assert.fail(e.getMessage());
				}
			}

			try {
				waitForLatch(getSignal);
			} catch (InterruptedException e) {
				Assert.fail(e.getMessage());
			}

			// check each field
			for (String key : objectStoredForTestMap.keySet()) {
				try {
					assertArrayEquals(
							toByteArray(getFromKey(key, objectStoredForTestMap
									.get(key).getValueSize())), getReturnMap
									.get(key).getValue());

					assertEquals(objectStoredForTestMap.get(key).getTag(),
							sha1(new String(getReturnMap.get(key).getValue(),
									CHARSET_NAME)));

					assertArrayEquals(
							toByteArray(getFromKey(key, objectStoredForTestMap
									.get(key).getVersionSize())), getReturnMap
									.get(key).getEntryMetadata().getVersion());
				} catch (Exception e) {
					Assert.fail(e.getMessage());
				}
			}

			for (String key : objectStoredForTestMap.keySet()) {

				String value = "";
				String newVersion = "";
				String tag = "";
				try {
					value = getRandomValue(key);
					newVersion = getRandomVersion(key);
					tag = sha1(value);
				} catch (Exception e1) {
					Assert.fail(e1.getMessage());
				}

				ObjectStoredForTest objectStoredForTest = new ObjectStoredForTest(
						value.length(), newVersion.length(), tag);
				objectStoredForTestSecondMap.put(key, objectStoredForTest);

				Entry entry = new Entry(toByteArray(key), toByteArray(value));
				EntryMetadata emd = new EntryMetadata();
				try {
					emd.setVersion(toByteArray(getFromKey(key,
							objectStoredForTestMap.get(key).getVersionSize())));
				} catch (Exception e1) {
					Assert.fail(e1.getMessage());
				}

				entry.setEntryMetadata(emd);

				CallbackHandler<Entry> handler = buildSuccessOnlyCallbackHandler(new SuccessAsyncHandler<Entry>() {
					@Override
					public void onSuccess(CallbackResult<Entry> result) {
						try {
							putReturnSecondMap.put(new String(result
									.getResult().getKey(), CHARSET_NAME),
									result.getResult());
						} catch (UnsupportedEncodingException e) {
							Assert.fail(e.getMessage());
						}
						putSecondSignal.countDown();
					}
				});

				try {
					getClient(client).putAsync(entry, toByteArray(newVersion),
							PersistOption.ASYNC, handler);
				} catch (KineticException e) {
					Assert.fail(e.getMessage());
				}
			}

			try {
				waitForLatch(putSecondSignal);
			} catch (InterruptedException e) {
				Assert.fail(e.getMessage());
			}

			// get back all keys
			for (String key : objectStoredForTestMap.keySet()) {
				CallbackHandler<Entry> handler = buildSuccessOnlyCallbackHandler(new SuccessAsyncHandler<Entry>() {
					@Override
					public void onSuccess(CallbackResult<Entry> result) {
						try {
							getReturnSecondMap.put(new String(result
									.getResult().getKey(), CHARSET_NAME),
									result.getResult());
						} catch (UnsupportedEncodingException e) {
							Assert.fail(e.getMessage());
						}
						getSecondSignal.countDown();
					}
				});

				try {
					getClient(client).getAsync(toByteArray(key), handler);
				} catch (KineticException e) {
					Assert.fail(e.getMessage());
				}
			}

			try {
				waitForLatch(getSecondSignal);
			} catch (InterruptedException e) {
				Assert.fail(e.getMessage());
			}

			// check each field
			for (String key : objectStoredForTestMap.keySet()) {
				try {
					assertArrayEquals(
							toByteArray(getFromKey(key,
									objectStoredForTestSecondMap.get(key)
											.getValueSize())),
							getReturnSecondMap.get(key).getValue());

					assertEquals(
							objectStoredForTestSecondMap.get(key).getTag(),
							sha1(new String(getReturnSecondMap.get(key)
									.getValue(), CHARSET_NAME)));

					assertArrayEquals(
							toByteArray(getFromKey(key,
									objectStoredForTestSecondMap.get(key)
											.getVersionSize())),
							getReturnSecondMap.get(key).getEntryMetadata()
									.getVersion());
				} catch (Exception e) {
					Assert.fail(e.getMessage());
				}
			}

			// clean data
			for (String key : objectStoredForTestMap.keySet()) {
				try {
					getClient(client).deleteForced(toByteArray(key),
							PersistOption.ASYNC);
				} catch (KineticException e) {
					Assert.fail(e.getMessage());
				}
			}
		}
	}

	private String getRandomKey() {
		randomKVGeneratorForVersion.nextKey(0);
		return randomKVGeneratorForValue.nextKey(0);
	}

	private String getRandomNonExistingKey() {
		Random random = new Random();
		int length = random.nextInt(LIMIT_KEY_SIZE + 1);
		String randomKey = getNoExistedKey(length);

		return randomKey;
	}

	private String getRandomVersion(String key) throws Exception {
		return randomKVGeneratorForVersion.getValue(key,
				getReproducibleVersionSize(key));
	}

	private String getRandomValue(String key) throws Exception {
		return randomKVGeneratorForValue.getValue(key,
				getReproducibleValueSize(key));
	}

	private String getFromKey(String key, int size) throws Exception {
		return randomKVGeneratorForValue.getValue(key, size);
	}

	private String getNoExistedKey(int length) {
		String val = "";
		Random random = new Random();
		for (int i = 0; i < length; i++) {
			val += String.valueOf(random.nextInt(10));
		}
		return val;
	}

	private String sha1(String input) throws NoSuchAlgorithmException {
		MessageDigest mDigest = MessageDigest.getInstance("SHA1");
		byte[] result = mDigest.digest(input.getBytes());
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < result.length; i++) {
			sb.append(Integer.toString((result[i] & 0xff) + 0x100, 16)
					.substring(1));
		}

		return sb.toString();
	}

	private int getReproducibleValueSize(String key) {
		return bernstein(key) % LIMIT_VALUE_SIZE;
	}

	private int getReproducibleVersionSize(String key) {
		return bernstein(key) % LIMIT_VERSION_SIZE;
	}

	private int bernstein(String key) {
		int hash = 0;
		int i;
		for (i = 0; i < key.length(); ++i)
			hash = 33 * hash + key.charAt(i);

		if (hash < 0) {
			hash = hash * -1;
		} else if (hash == 0) {
			hash = 1;
		}

		return hash;
	}
}

class ObjectStoredForTest {
	private int valueSize;
	private int versionSize;
	private String tag;

	public ObjectStoredForTest(int valueSize, String tag) {
		this.valueSize = valueSize;
		this.tag = tag;
	}

	public ObjectStoredForTest(int valueSize, int versionSize, String tag) {
		this.valueSize = valueSize;
		this.versionSize = versionSize;
		this.tag = tag;
	}

	public int getValueSize() {
		return valueSize;
	}

	public void setValueSize(int valueSize) {
		this.valueSize = valueSize;
	}

	public int getVersionSize() {
		return versionSize;
	}

	public void setVersionSize(int versionSize) {
		this.versionSize = versionSize;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}
}
