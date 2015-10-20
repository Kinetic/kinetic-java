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

package com.seagate.kinetic.simulator.persist.bdb;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Logger;

import kinetic.simulator.SimulatorConfiguration;

import com.google.protobuf.ByteString;
import com.seagate.kinetic.common.lib.Hmac;
import com.seagate.kinetic.simulator.internal.KVStoreException;
import com.seagate.kinetic.simulator.internal.KVStoreNotFound;
import com.seagate.kinetic.simulator.internal.KVStoreVersionMismatch;
import com.seagate.kinetic.simulator.internal.SimulatorEngine;
import com.seagate.kinetic.simulator.persist.KVKey;
import com.seagate.kinetic.simulator.persist.KVValue;
import com.sleepycat.bind.EntryBinding;
import com.sleepycat.collections.StoredSortedMap;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.Durability;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.Transaction;

public final class KVStore {
	private final static java.util.logging.Logger logger = Logger
			.getLogger(KVStore.class.getName());

	private SimulatorConfiguration config = null;

	// private KVStore me = new KVStore();

	private final EnvironmentConfig envConfig = new EnvironmentConfig();
	private Environment myDbEnvironment = null;
	private Database myDatabase = null;
	private final DatabaseConfig dbConfig = new DatabaseConfig();
	private String kineticDbName = null;
	
	private String persistFolder = null;

	static DatabaseEntry dbe(String s) {
		ByteString bs = ByteString.copyFromUtf8(s);
		DatabaseEntry dbe = new DatabaseEntry();
		dbe.setData(bs.toByteArray());
		return dbe;
	}

	static String toString(DatabaseEntry dbe) {
		if (dbe.getData() == null)
			return new String("");
		return new String(dbe.getData());
	}

	private StoredSortedMap<KVKey, KVValue> v = null;

	public KVStore(SimulatorConfiguration config) {
		this.config = config;
		this.init();
	}

	private void init() {

		String defaultHome = System.getProperty("user.home") + File.separator
				+ "kinetic";

		String kineticHome = config.getProperty(
				SimulatorConfiguration.KINETIC_HOME, defaultHome);

		File lchome = new File(kineticHome);

		if (lchome.exists() == false) {
			boolean created = lchome.mkdir();
			logger.info("create kinetic home folder: " + kineticHome
					+ ", created=" + created);
		}

		persistFolder = kineticHome
				+ File.separator
				+ config.getProperty(SimulatorConfiguration.PERSIST_HOME, "bdb");

		File f = new File(persistFolder);
		logger.info("Database file exists: " + f.exists() + ", name="
				+ persistFolder);

		if (f.exists() == false) {
			boolean created = f.mkdir();
			logger.info("create persist folder: " + persistFolder
					+ ", created=" + created);
		}

		// allow fresh.
		envConfig.setAllowCreate(true);

		// conservative.
		envConfig.setDurability(Durability.COMMIT_SYNC);

		// Persistent (needed to make the data stay around)
		envConfig.setTransactional(true);
		// envConfig.setLockTimeout(10, java.util.concurrent.TimeUnit.SECONDS);
		myDbEnvironment = new Environment(new File(persistFolder), envConfig);

		// allow fresh.
		dbConfig.setAllowCreate(true);

		// Persistent (needed to make the data stay around)
		dbConfig.setTransactional(true);

		kineticDbName = "kineticDatabase";

		myDatabase = myDbEnvironment
				.openDatabase(null, kineticDbName, dbConfig);

		v = new StoredSortedMap<KVKey, KVValue>(myDatabase, new KeyBinding(),
				new ValueBinding(), true);
	}

	// TODO should I put this in a destructor?!?!
	public void close() {
		logger.fine("Temporary == " + dbConfig.getTemporary());
		myDatabase.close();
		logger.fine("database closed1");
		logger.fine("database closed2");
		myDbEnvironment.close();
		logger.fine("database closed3");
	}

	public void closeEvn() {
		myDbEnvironment.close();
		logger.fine("myDbEnvironment closed");
	}

	public synchronized void removeDatabase() throws InterruptedException {
		myDatabase.close();
		Transaction txn = myDbEnvironment.beginTransaction(null, null);
		myDbEnvironment.removeDatabase(txn, kineticDbName);
		txn.commit();
	}

	private KVKey KvkOf(ByteString k) {
		return new KVKey(k);
	}

	static class KeyBinding implements EntryBinding<KVKey> {

		@Override
		public KVKey entryToObject(DatabaseEntry arg0) {
			return new KVKey(arg0.getData());
		}

		@Override
		public void objectToEntry(KVKey arg0, DatabaseEntry arg1) {
			arg1.setData(arg0.getKey());
		}
	}

	static class ValueBinding implements EntryBinding<KVValue> {

		ValueBinding() {
		}

		@Override
		public KVValue entryToObject(DatabaseEntry dbe) {
			return new KVValue(dbe.getData());
		}

		@Override
		public void objectToEntry(KVValue kvv, DatabaseEntry dbe) {
			dbe.setData(kvv.toByteArray());
		}

	}

	public synchronized KVValue get(ByteString key) throws KVStoreNotFound {
		KVValue object = v.get(KvkOf(key));
		if (object == null)
			throw new KVStoreNotFound();
		return object;
	}

	// returns the version if it is in the db. Null otherwise.
	ByteString getVersion(ByteString key) throws KVStoreException {
		KVValue obj = get(key);
		if (obj == null)
			throw new KVStoreNotFound();
		if (!obj.hasVersion())
			return ByteString.EMPTY;
		return obj.getVersion();
	}

	public synchronized KVValue getNext(ByteString key) throws KVStoreException {
		SortedMap<KVKey, KVValue> m = v.tailMap(KvkOf(key), false);
		KVKey key1 = m.firstKey();
		if (key1 == null)
			throw new KVStoreNotFound();
		return v.get(key1);
	}

	public synchronized SortedMap<KVKey, KVValue> getRange(ByteString k1,
			boolean i1, ByteString k2, boolean i2, int n)
			throws KVStoreException {
		logger.fine("Key 1: " + Hmac.toString(k1) + "Key 2: "
				+ Hmac.toString(k2));

		SortedMap<KVKey, KVValue> m;
		if (k2.size() == 0)
			m = v.tailMap(KvkOf(k1), i1);
		else
			m = v.subMap(KvkOf(k1), i1, KvkOf(k2), i2);
		logger.fine("Number of entries: " + m.size() + " requesting " + n);

		// if it is more entries than we need, copy only the first n.
		if (m.size() > n) {
			SortedMap<KVKey, KVValue> m1 = new TreeMap<KVKey, KVValue>();
			for (Entry<KVKey, KVValue> e : m.entrySet()) {
				if (n-- > 0)
					m1.put(e.getKey(), e.getValue());
				else
					return m1;
			}
		}
		return m;
	}

	public synchronized List<KVKey> getRangeReversed(ByteString k1, boolean i1,
			ByteString k2, boolean i2, int n) throws KVStoreException {
		logger.fine("Key 1: " + Hmac.toString(k1) + "Key 2: "
				+ Hmac.toString(k2));

		SortedMap<KVKey, KVValue> m;
		if (k2.size() == 0)
			m = v.tailMap(KvkOf(k1), i1);
		else
			m = v.subMap(KvkOf(k1), i1, KvkOf(k2), i2);
		logger.fine("Number of entries: " + m.size() + " requesting " + n);

		// if it is more entries than we need, copy only the first n.

		List<KVKey> kvKeyOfList = new ArrayList<KVKey>();
		int i = 0, j = 0;
		int size = m.size() > n ? n : m.size();
		KVKey[] keys = new KVKey[size];
		if (m.size() > n) {

			for (Entry<KVKey, KVValue> e : m.entrySet()) {
				if (i++ >= m.size() - n)
					keys[j++] = e.getKey();
			}
		} else {
			for (Entry<KVKey, KVValue> e : m.entrySet()) {
				keys[i++] = e.getKey();
			}
		}
		for (i = size - 1; i >= 0; i--) {
			kvKeyOfList.add(keys[i]);
		}

		return kvKeyOfList;

	}

	public synchronized KVValue getPrevious(ByteString key)
			throws KVStoreException {
		SortedMap<KVKey, KVValue> m = v.headMap(KvkOf(key), false);
		KVKey key1 = m.lastKey();
		if (key1 == null)
			throw new KVStoreNotFound();
		return v.get(key1);
	}

	// This adds/replaces an entry. throw if there is a version mismatch.
	public synchronized void put(ByteString key, ByteString oldVersion,
			KVValue value) throws KVStoreVersionMismatch {

		ByteString version = null;
		KVValue obj = v.get(KvkOf(key));
		if (obj != null) {// exists
			version = obj.getVersion();
		} else {
			logger.fine("Key does not exist");
		}
		checkVersion(version, oldVersion);
		SimulatorEngine.logBytes("put, key", KvkOf(key).getKey());
		value.setKeyOf(key);
		v.put(KvkOf(key), value);
	}

	public synchronized void putForced(ByteString key, KVValue value)
			throws KVStoreException {
		try {
			SimulatorEngine.logBytes("put force, key", KvkOf(key).getKey());
			value.setKeyOf(key);
			v.put(KvkOf(key), value);
		} catch (Exception e) {
			throw new KVStoreException("DB internal exception");
		}
	}

	private int mySize(ByteString s) {
		if (s == null)
			return 0;
		return s.size();
	}

	private void checkVersion(ByteString version, ByteString oldVersion)
			throws KVStoreVersionMismatch {
		logger.fine("Compare len: " + mySize(version) + ", "
				+ mySize(oldVersion));
		if (mySize(version) != mySize(oldVersion))
			throw new KVStoreVersionMismatch("Length mismatch");
		if (mySize(version) == 0)
			return;
		if (!version.equals(oldVersion))
			throw new KVStoreVersionMismatch("Compare mismatch");
	}

	// TODO need to test the versions.
	public synchronized void delete(ByteString key, ByteString oldVersion)
			throws KVStoreException {
		ByteString prevVersion = getVersion(key);
		checkVersion(prevVersion, oldVersion);
		v.remove(KvkOf(key));
	}

	public synchronized void deleteForced(ByteString key)
			throws KVStoreException {
		try {
			v.remove(KvkOf(key));
		} catch (Exception e) {
			throw new KVStoreException("DB internal exception");
		}
	}
	
    /**
     * Get persist store path.
     * 
     * @return persist store path.
     * @throws KVStoreException
     *             if any internal error occurred.
     */
    public String getPersistStorePath() throws KVStoreException {
        return this.persistFolder;
    }

}
