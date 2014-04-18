package com.seagate.kinetic.simulator.client.internal;
//// Do NOT modify or remove this copyright and confidentiality notice!
////
//// Copyright (c) 2001 - $Date: 2012/06/27 $ Seagate Technology, LLC.
////
//// The code contained herein is CONFIDENTIAL to Seagate Technology, LLC.
//// Portions are also trade secret. Any use, duplication, derivation, distribution
//// or disclosure of this code, for any reason, not expressly authorized is
//// prohibited. All other rights are expressly reserved by Seagate Technology, LLC.
//
//package com.seagate.kinetic.simulator_only.client.internal;
//
//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertFalse;
//import static org.junit.Assert.assertTrue;
//import static org.junit.Assert.fail;
//
//import java.io.IOException;
//import java.nio.ByteBuffer;
//import java.nio.ByteOrder;
//import java.util.List;
//import java.util.logging.Logger;
//
//import kinetic.client.KineticException;
//
//import org.junit.Before;
//import org.junit.Test;
//
//import com.google.protobuf.ByteString;
//import com.seagate.kinetic.IntegrationTestCase;
//import com.seagate.kinetic.client.internal.ClientProxy;
//import com.seagate.kinetic.client.internal.ClientProxy.KeyRange;
//import com.seagate.kinetic.client.internal.ClientProxy.LCException;
//import com.seagate.kinetic.client.internal.ClientProxy.NotFound;
//import com.seagate.kinetic.client.internal.ClientProxy.VersionException;
//import com.seagate.kinetic.client.lib.ClientLogger;
//import com.seagate.kinetic.common.lib.Hmac;
//import com.seagate.kinetic.common.lib.KVValue;
//
//public class KineticTest extends IntegrationTestCase {
//
//	private final static Logger LOG = ClientLogger.get();
//
//	private ClientProxy c;
//
//	@Before
//	public void setUp() throws Exception {
//		c = new ClientProxy(getClientConfig());
//	}
//
//	@Test
//	public void noop() throws LCException {
//		c.noOp();
//	}
//
//	private void assertNotFound(String key) throws LCException {
//		assertNotFound(ByteString.copyFromUtf8(key));
//	}
//
//	private void assertNotFound(ByteString key) throws LCException {
//		try {
//			c.get(key); // should throw "NotFound".
//			fail("should not get here");
//		} catch (NotFound e) {
//			// This is expected
//		}
//	}
//
//	boolean delete(ByteString... keys) throws LCException {
//		boolean reply = true;
//		for (ByteString key : keys) {
//			ByteString version = null;
//			try {
//				version = c.getVersion(key);
//			} catch (NotFound e) {
//			}
//			reply &= c.delete(key, version);
//		}
//		return reply;
//	}
//
//	boolean delete(String... keys) throws LCException {
//		ByteString[] bsk = new ByteString[keys.length];
//		for (int i = 0; i < keys.length; i++) {
//			bsk[i] = bs(keys[i]);
//		}
//		return delete(bsk);
//	}
//
//	void deleteTrue(ByteString... keys) throws LCException {
//		assertTrue(delete(keys));
//	}
//
//	void deleteTrue(String... keys) throws LCException {
//		assertTrue(delete(keys));
//	}
//
//	@Test
//	public void simple() throws LCException {
//
//		String key = "hello";
//		String data = "there";
//		String v;
//
//		delete(key); // this may return true or false, but throws nothing
//
//		assertNotFound(key); // should throw "NotFound".
//
//		c.put(key, data); // Normal put
//		v = c.get(key); // get the data
//		assertEquals("Returned data", v, data);
//
//		data = "not there";
//
//		c.put(key, data); // Normal put of existing key. overwriting...
//		v = c.get(key);
//		assertEquals(key, v, data);
//
//		deleteTrue(key);
//
//		assertNotFound(key); // should throw "NotFound".
//	}
//
//	@Test
//	public void delete() throws LCException {
//		String foo = "foo";
//		c.delete(foo);
//		assertNotFound(foo);
//		c.put(foo, foo);
//		assertTrue(c.delete(foo));
//		assertNotFound(foo);
//		assertFalse(c.delete(foo));
//	}
//
//	byte[] zzz = { (byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 255,
//			(byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 255,
//			(byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 255,
//			(byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 255,
//			(byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 255,
//			(byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 255,
//			(byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 255 };
//
//	@Test
//	public void getNextSimple() throws LCException {
//		String k = "k";
//		String k1 = "k1";
//		String k2 = "k2";
//
//		delete(k, k1, k2);
//
//		c.put(k1, k1);
//		assertEquals("Get Next1", c.getNext(k), k1);
//		c.put(k2, k2);
//		assertEquals("Get Next2", c.getNext(k1), k2);
//		assertEquals("Get Next5", c.getNext(""), k1);
//
//		try {
//			c.getNext(ByteString.copyFrom(zzz)); // should throw "NotFound".
//			fail("should not get here");
//		} catch (NotFound e) {
//			// This is expected
//		}
//
//		deleteTrue(k1, k2);
//	}
//
//	@Test
//	public void getNext() throws LCException {
//		String k = "k";
//		String k1 = "k1";
//		String k2 = "k2";
//		String k3 = "k3";
//		String k4 = "k4";
//		String m = "m";
//
//		delete(k, k1, k2, k3, k4, m);
//
//		c.put(k2, k2);
//		c.put(k4, k4);
//		assertEquals("Get Next", c.getNext(k2), k4);
//		c.put(k1, k1);
//		c.put(m, m);
//		c.put(k3, k3);
//
//		assertEquals("Get Next1", c.getNext(k), k1);
//		assertEquals("Get Next2", c.getNext(k1), k2);
//		assertEquals("Get Next3", c.getNext(k2), k3);
//		assertEquals("Get Next4", c.getNext(k3), k4);
//		assertEquals("Get Next5", c.getNext(k4), m);
//		assertEquals("Get Next5", c.getNext(k1), k2);
//		assertEquals("Get Next5", c.getNext(""), k1);
//
//		deleteTrue(k1, k2, k3, k4, m);
//		assertFalse(c.delete(""));
//
//	}
//
//	static private ByteString int32(int x) {
//		return ByteString.copyFrom(ByteBuffer.allocate(4)
//				.order(ByteOrder.BIG_ENDIAN).putInt(x).array());
//	}
//
//	@Test
//	public void int32test() {
//		assertTrue(4 == int32(4).size());
//	}
//
//	ByteString bs(String s) {
//		return ByteString.copyFromUtf8(s);
//	}
//
//	@Test
//	public void simpleGetVersion() throws LCException {
//		ByteString key = bs("key1");
//		delete(key);
//		KVValue data = new KVValue(key, int32(42), null, null, key);
//		c.put(data, null);
//		ByteString v = c.getVersion(key);
//		assertTrue(int32(42).equals(v));
//		c.delete(key, int32(42));
//		assertNotFound(key);
//
//		try {
//			c.getVersion(key);
//			fail("should throw a NotFound");
//		} catch (NotFound e) {
//		}
//	}
//
//	private void assertKVEquals(KVValue a, KVValue b) {
//		assertTrue(a.getKeyOf().equals(b.getKeyOf()));
//		assertTrue(a.getData().equals(b.getData()));
//		assertTrue(a.getVersion().equals(b.getVersion()));
//		assertTrue(a.getTag().equals(b.getTag()));
//	}
//
//	private void print(KVValue v) {
//		System.out.println("KeyOf:   "
//				+ Hmac.toString(v.getKeyOf().toByteArray()));
//		System.out.println("Data:    "
//				+ Hmac.toString(v.getData().toByteArray()));
//		System.out.println("Version: "
//				+ Hmac.toString(v.getVersion().toByteArray()));
//		System.out.println("Tag:     "
//				+ Hmac.toString(v.getTag().toByteArray()));
//	}
//
//	@Test
//	public void fullGetNext() throws LCException {
//		ByteString k1 = bs("k1");
//		ByteString k10 = bs("k10");
//		ByteString k11 = bs("k11");
//
//		delete(k1, k10, k11);
//
//		KVValue d1 = new KVValue(k1, int32(1), int32(101), null, k1);
//		KVValue d10 = new KVValue(k10, int32(10), int32(110), null, k10);
//		KVValue d11 = new KVValue(k11, int32(11), int32(111), null, k11);
//
//		c.put(d11, null);
//		c.put(d10, null);
//		c.put(d1, null);
//
//		assertKVEquals(c.getVersionedNext(bs("a")), d1);
//		assertKVEquals(c.getVersionedNext(k10), d11);
//		assertKVEquals(c.getVersionedNext(k1), d10);
//
//		deleteTrue(k1, k10, k11);
//	}
//
//	@Test
//	public void simpleDeleteVersion() throws LCException {
//		ByteString key = bs("key1");
//		delete(key);
//
//		KVValue data = new KVValue(key, int32(42), null, null, key);
//		c.put(data, null);
//
//		try {
//			c.delete(key, null);
//			fail("should throw a VersionException");
//		} catch (VersionException e) {
//		}
//
//		try {
//			c.delete(key, int32(41));
//			fail("should throw a VersionException");
//		} catch (VersionException e) {
//		}
//
//		delete(key);
//	}
//
//	@Test
//	public void simplePutVersion() throws LCException {
//		ByteString key = bs("key1");
//		delete(key);
//
//		KVValue data = new KVValue(key, int32(42), null, null, key);
//
//		try {
//			c.put(data, int32(42));
//			fail("should throw a VersionException");
//		} catch (VersionException e) {
//		}
//
//		c.put(data, null);
//
//		c.put(data, int32(42));
//
//		try {
//			c.put(data, null);
//			fail("should throw a VersionException");
//		} catch (VersionException e) {
//		}
//
//		try {
//			c.put(data, int32(32));
//			fail("should throw a VersionException");
//		} catch (VersionException e) {
//		}
//
//		try {
//			c.delete(key, int32(41));
//			fail("should throw a VersionException");
//		} catch (VersionException e) {
//		}
//		deleteTrue(key);
//	}
//
//	@Test
//	public void dataPersistsAcrossServerRestarts() throws LCException,
//			IOException, InterruptedException, KineticException {
//		try {
//			c.get("long_lived_key");
//			fail("Should have thrown");
//		} catch (NotFound e) {
//		}
//
//		c.put("long_lived_key", "long_lived_value");
//
//		c.close();
//		restartServer();
//		c = new ClientProxy(getClientConfig());
//
//		assertEquals("long_lived_value", c.get("long_lived_key"));
//	}
//
//	// ------------------------------------------------------------------------------
//	// The following set of tests will fail on a clean database but
//	// will run properly on subsequent runs.
//
//	@Test
//	public void persistentData0() throws LCException {
//		ByteString k = bs("zzPersistent");
//		KVValue dataout = new KVValue(k, int32(42), null, null, k);
//
//		try {
//			// It should exist.
//			KVValue datain = c.getVersioned(k);
//			assertEquals(k, datain.getKeyOf());
//			assertEquals(dataout.getVersion(), datain.getVersion());
//			assertEquals(dataout.getTag(), datain.getTag());
//			assertEquals(dataout.getData(), datain.getData());
//
//			// We are going to try to overwrite with new key
//			// will fail.
//			try {
//				c.put(dataout, int32(43));
//				fail("should throw a VersionException");
//			} catch (VersionException e) {
//			}
//
//			c.put(dataout, int32(42));
//			try {
//				c.put(dataout, null);
//				fail("should throw a VersionException");
//			} catch (VersionException e) {
//			}
//
//			// Check to make sure this is the only record in the database.
//			try {
//				c.getNext(k);
//				fail("should throw a NotFound Exception");
//			} catch (NotFound e) {
//			}
//
//			try {
//				c.getPrevious(k);
//				fail("should throw a NotFound Exception");
//			} catch (NotFound e) {
//			}
//
//		} catch (NotFound e) {
//			c.put(dataout, null);
//
//			LOG.warning("record not found in the database. Was this a new DB? If so, just run again and this error should go away... If not, there is a persistance problem");
//
//			// fail("record not found in the database. Was this a new DB? If so, just run again and this error should go away... If not, there is a persistance problem");
//		}
//	}
//
//	@Test
//	public void getRangeTest() throws LCException {
//		String k = "k";
//		String k1 = "k1";
//		String k2 = "k2";
//
//		delete(k, k1, k2);
//
//		c.put(k, k);
//		c.put(k1, k1);
//		c.put(k2, k2);
//
//		KeyRange r;
//		List<ByteString> x;
//
//		r = c.new KeyRange(bs(k1), true, bs(k2), false, 100, false);
//		x = c.getKeyRange(r);
//		assertEquals(1, x.size());
//		assertEquals(x.get(0), bs(k1));
//
//		r = c.new KeyRange(bs(k), true, bs(k2), true, 100, false);
//		x = c.getKeyRange(r);
//		assertEquals(3, x.size());
//		assertEquals(x.get(0), bs(k));
//		assertEquals(x.get(1), bs(k1));
//		assertEquals(x.get(2), bs(k2));
//
//		r = c.new KeyRange(bs("i"), true, bs("l"), true, 100, false);
//		x = c.getKeyRange(r);
//		assertEquals(3, x.size());
//		assertEquals(x.get(0), bs(k));
//		assertEquals(x.get(1), bs(k1));
//		assertEquals(x.get(2), bs(k2));
//
//		deleteTrue(k, k1, k2);
//	}
//
//	@Test
//	public void getReturnsValueInsertedWithPut() throws LCException {
//		String key = "thisisthekey";
//		String value = "thisisthevalue";
//		c.put(key, value);
//
//		try {
//			assertEquals(value, c.get(key));
//		} finally {
//			// This is needed to clean up the DB for other tests until the
//			// setup/teardown methods insure a consistent
//			// clean state for the DB
//			c.delete(key);
//		}
//	}
//
//	@Test
//	public void getAndPutLargeMessage() throws LCException {
//		// Test that we can successfully put and get a large key-value pair
//		final int length = 32 * 1024;
//		byte largeKey[] = new byte[length];
//		byte largeValue[] = new byte[length];
//
//		for (int i = 0; i < length; i++) {
//			largeKey[i] = (byte) i;
//			largeValue[i] = (byte) i;
//		}
//
//		String key = new String(largeKey);
//		String value = new String(largeValue);
//
//		c.put(key, value);
//
//		try {
//			assertEquals(value, c.get(key));
//		} finally {
//			c.delete(key);
//		}
//	}
// }
