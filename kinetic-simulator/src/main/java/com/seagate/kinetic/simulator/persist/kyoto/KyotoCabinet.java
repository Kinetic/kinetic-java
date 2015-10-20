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
package com.seagate.kinetic.simulator.persist.kyoto;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import kinetic.simulator.SimulatorConfiguration;
import kyotocabinet.Cursor;
import kyotocabinet.DB;

import com.google.protobuf.ByteString;
import com.seagate.kinetic.simulator.internal.KVStoreException;
import com.seagate.kinetic.simulator.internal.KVStoreNotFound;
import com.seagate.kinetic.simulator.internal.KVStoreVersionMismatch;
import com.seagate.kinetic.simulator.internal.SimulatorEngine;
import com.seagate.kinetic.simulator.persist.BatchOperation;
import com.seagate.kinetic.simulator.persist.KVKey;
import com.seagate.kinetic.simulator.persist.KVValue;
import com.seagate.kinetic.simulator.persist.PersistOption;
import com.seagate.kinetic.simulator.persist.Store;

/**
 *
 * Kyoto Cabinet store implementation.
 *
 * Prototype quality.
 *
 * XXX chiaming 12/24/2013: support PersistOption
 *
 * @author chiaming
 *
 */
public class KyotoCabinet implements Store<ByteString, ByteString, KVValue> {

    private final static java.util.logging.Logger logger = Logger
            .getLogger(KyotoCabinet.class.getName());

    private DB db = null;
    
    private String persistFolder = null;

    public KyotoCabinet() {
        ;
    }

    @Override
    public void init(SimulatorConfiguration config) {

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
                + config.getProperty(SimulatorConfiguration.PERSIST_HOME,
                        "kyoto");

        File f = new File(persistFolder);

        logger.info("Database file exists: " + f.exists() + ", name="
                + persistFolder);

        if (f.exists() == false) {
            boolean created = f.mkdir();
            logger.info("create persist folder: " + persistFolder
                    + ", created=" + created);
        }

        // db file
        String dbFile = persistFolder + "/kyoto.kct";

        db = new DB();

        // open the database ( file tree database)
        if (!db.open(dbFile, DB.OWRITER | DB.OCREATE | DB.OAUTOTRAN)) {
            logger.info("open error: " + db.error() + ", db file=" + dbFile);
        } else {
            logger.info("opened kyoto cabinet db, file=" + dbFile);
        }
    }

    @Override
    public void put(ByteString key, ByteString oldVersion, KVValue value,
            PersistOption option) throws KVStoreException {

        ByteString version = null;

        byte[] keyArray = key.toByteArray();

        byte[] data = db.get(keyArray);

        KVValue obj = null;

        logger.finest("put versioned ...., data =" + data);

        if (data != null) {
            obj = new KVValue(data);

            version = obj.getVersion();
            // checkVersion(version, oldVersion);
        }

        SimulatorEngine.logBytes("put, key", KvkOf(key).getKey());

        checkVersion(version, oldVersion);
        value.setKeyOf(key);

        db.set(keyArray, value.toByteArray());
    }

    @Override
    public void putForced(ByteString key, KVValue value, PersistOption option)
            throws KVStoreException {

        byte[] keyArray = key.toByteArray();

        value.setKeyOf(key);

        db.set(keyArray, value.toByteArray());
    }

    @Override
    public void delete(ByteString key, ByteString oldVersion,
            PersistOption option) throws KVStoreException {

        ByteString prevVersion = getVersion(key);

        checkVersion(prevVersion, oldVersion);

        db.remove(key.toByteArray());

    }

    @Override
    public void deleteForced(ByteString key, PersistOption option)
            throws KVStoreException {

        db.remove(key.toByteArray());
    }

    @Override
    public KVValue get(ByteString key) throws KVStoreException {

        byte[] keyArray = key.toByteArray();
        byte[] data = db.get(keyArray);

        if (data == null) {
            throw new KVStoreNotFound();
        }

        return new KVValue(data);
    }

    @Override
    public KVValue getPrevious(ByteString key) throws KVStoreException {
        Cursor cursor = null;

        KVValue value = null;

        try {
            cursor = db.cursor();

            boolean exist = cursor.jump(key.toByteArray());

            exist = cursor.step_back();
            if (exist) {
                byte[] data = cursor.get_value(false);

                value = new KVValue(data);

                // logger.info("get previous has value ...");
            } else {
                throw new KVStoreNotFound();
            }
        } catch (KVStoreNotFound kvnf) {
            throw kvnf;
        } catch (Exception e) {
            logger.log(Level.WARNING, e.getMessage(), e);
            throw new KVStoreException(e.getMessage());
        } finally {
            cursor.disable();
        }

        return value;
    }

    @Override
    public KVValue getNext(ByteString key) throws KVStoreException {
        // TODO Auto-generated method stub
        Cursor cursor = null;

        KVValue value = null;

        byte[] bkey = key.toByteArray();

        try {

            cursor = db.cursor();

            // if (db.check(bkey) < 0) {
            // return this.scanFindNext(cursor, bkey);
            // }

            boolean exist = cursor.jump(bkey);

            exist = cursor.step();

            if (exist) {
                byte[] key2 = cursor.get_key(false);

                logger.finest("get next, key= " + key.toStringUtf8()
                        + ", store key2="
                        + ByteString.copyFrom(key2).toStringUtf8());

                byte[] data = cursor.get_value(false);

                logger.finest("get next store data ="
                        + ByteString.copyFrom(data).toStringUtf8());

                value = new KVValue(data);

            } else {
                throw new KVStoreNotFound();
            }
        } catch (KVStoreNotFound kvsnf) {
            throw kvsnf;
        } catch (Exception e) {
            logger.log(Level.WARNING, e.getMessage(), e);
            throw new KVStoreException(e.getMessage());
        } finally {
            cursor.disable();
        }

        return value;
    }

    @Override
    public SortedMap<?, ?> getRange(ByteString startKey,
            boolean startKeyInclusive, ByteString endKey,
            boolean endKeyInclusive, int max) throws KVStoreException {

        SortedMap<KVKey, KVValue> map = new TreeMap<KVKey, KVValue>();

        byte[] start = startKey.toByteArray();
        byte[] end = endKey.toByteArray();

        Cursor cursor = null;

        try {

            cursor = db.cursor();

            boolean exist = cursor.jump(start);

            if (startKeyInclusive == false) {
                exist = cursor.step();
            }

            // first key there?
            boolean more = exist;

            while (more) {

                byte[][] pair = cursor.get(true);

                if (pair != null) {

                    // check should we put the pair in the map
                    if (shouldInclude(pair[0], end, endKeyInclusive, false)) {
                        map.put(new KVKey(pair[0]), new KVValue(pair[1]));
                    } else {
                        // exit loop
                        more = false;
                    }
                } else {
                    more = false;
                }

                // check if reached max size
                if (map.size() == max) {
                    more = false;
                }
            }
        } catch (Exception e) {

            logger.log(Level.WARNING, e.getMessage(), e);

            throw new KVStoreException(e.getMessage());
        } finally {
            cursor.disable();
        }

        return map;
    }

    @Override
    public List<?> getRangeReversed(ByteString startKey,
            boolean startKeyInclusive, ByteString endKey,
            boolean endKeyInclusive, int maxReturned) throws KVStoreException {
        byte[] start = startKey.toByteArray();
        byte[] end = endKey.toByteArray();

        List<KVKey> listOfKVKey = new ArrayList<KVKey>();
        Cursor cursor = null;

        try {

            cursor = db.cursor();

            boolean exist = cursor.jump_back(end);

            if (endKeyInclusive == false) {
                exist = cursor.step_back();
            }

            // last key there?
            boolean more = exist;

            while (more) {

                byte[] key = cursor.get_key(false);

                if (key != null) {
                    // check should we put the pair in the map
                    if (shouldInclude(key, start, startKeyInclusive, true)) {
                        listOfKVKey.add(new KVKey(key));
                    } else {
                        // exit loop
                        more = false;
                    }
                } else {
                    more = false;
                }

                // check if reached max size
                if (listOfKVKey.size() >= maxReturned) {
                    if (listOfKVKey.size() > maxReturned) {
                        listOfKVKey.remove(listOfKVKey.size() - 1);
                    } else {
                        more = false;
                    }
                } else {
                    more = cursor.step_back();
                }
            }
        } catch (Exception e) {

            logger.log(Level.WARNING, e.getMessage(), e);

            throw new KVStoreException(e.getMessage());
        } finally {
            cursor.disable();
        }

        return listOfKVKey;

    }

    @Override
    public void close() {

        this.db.close();

        logger.info("Kyoto db close ...");
    }

    @Override
    public void reset() throws KVStoreException {

        this.db.clear();
    }

    private KVKey KvkOf(ByteString k) {
        return new KVKey(k);
    }

    private void checkVersion(ByteString version, ByteString oldVersion)
            throws KVStoreVersionMismatch {

        logger.finest("Compare len, version size= " + mySize(version)
                + ", ols version size=" + mySize(oldVersion));

        if (mySize(version) != mySize(oldVersion)) {
            throw new KVStoreVersionMismatch("Length mismatch");
        }

        if (mySize(version) == 0)
            return;

        if (!version.equals(oldVersion)) {
            throw new KVStoreVersionMismatch("Compare mismatch");
        }
    }

    private int mySize(ByteString s) {
        if (s == null)
            return 0;
        return s.size();
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

    private static boolean shouldInclude(byte[] k1, byte[] endKey,
            boolean inclusive, boolean reverse) {
        int cv = compare(k1, endKey);

        if (cv == 0) {
            if (inclusive) {
                return true;
            }
        }

        return reverse ? (cv > 0) : (cv < 0);
    }

    /**
     *
     * @param left
     * @param right
     * @return
     */
    public static int compare(byte[] left, byte[] right) {

        int l1 = left.length;
        int l2 = right.length;

        int len = Math.min(l1, l2);

        for (int i = 0; i < len; i++) {
            int a = (left[i] & 0xff);
            int b = (right[i] & 0xff);
            if (a != b) {
                return a - b;
            }
        }

        return left.length - right.length;
    }

    @Override
    public BatchOperation<ByteString, KVValue> createBatchOperation()
            throws KVStoreException {
        // TODO Auto-generated method stub
        throw new java.lang.UnsupportedOperationException();
    }

    @Override
    public void flush() throws KVStoreException {
        logger.warning("Flush is not yet implemented.");
    }

    @Override
    public void compactRange(ByteString startKey, ByteString endKey)
            throws KVStoreException {
        logger.warning("method is not yet implemented.");
    }
    
    @Override
    public String getPersistStorePath() throws KVStoreException {
        return this.persistFolder;
    }

}
