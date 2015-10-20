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
package com.seagate.kinetic.simulator.persist.leveldb;

import static org.fusesource.leveldbjni.JniDBFactory.factory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import kinetic.simulator.SimulatorConfiguration;

import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBIterator;
import org.iq80.leveldb.Options;
import org.iq80.leveldb.WriteBatch;
import org.iq80.leveldb.WriteOptions;

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
 * LevelDb store for Kinetic simulator.
 *
 * @author chiaming
 *
 */
public class LevelDbStore implements Store<ByteString, ByteString, KVValue> {

    private final static java.util.logging.Logger logger = Logger
            .getLogger(LevelDbStore.class.getName());

    // level db instance
    private DB db = null;

    // level db file
    private String dbFile = null;

    // simulator config
    private SimulatorConfiguration config = null;

    // sync write option
    private static final WriteOptions SYNC_WRITE_OPTION = new WriteOptions()
            .sync(true);

    // async write option
    private static final WriteOptions asyncWriteOption = new WriteOptions()
            .sync(false);
    
    private String persistFolder = null;

    // default no-arg constructor
    public LevelDbStore() {
        ;
    }

    @Override
    public void init(SimulatorConfiguration config) {

        this.config = config;

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

        // calculate persist folder
        persistFolder = kineticHome
                + File.separator
                + config.getProperty(SimulatorConfiguration.PERSIST_HOME,
                        "leveldb");

        File f = new File(persistFolder);

        logger.info("Database exists: " + f.exists() + ", persist folder ="
                + persistFolder);

        if (f.exists() == false) {
            boolean created = f.mkdir();
            logger.info("create persist folder: " + persistFolder
                    + ", created=" + created);
        }

        // db file
        dbFile = persistFolder + "/leveldb.ldb";

        // construct new options
        Options options = new Options();

        // kinetic comparator
        KineticComparator comparator = new KineticComparator();

        // set my own comparator
        options.comparator(comparator);

        // use 64m cache
        options.cacheSize(64 * 1048576);

        // create if not there
        options.createIfMissing(true);

        // options.blockSize(64 * 1024 * 1024);

        // options.maxOpenFiles(1000);

        options.verifyChecksums(true);

        try {

            // open db file
            db = factory.open(new File(dbFile), options);

            // init write option
            // this.syncWriteOption.sync(true);

        } catch (IOException e) {

            logger.log(Level.SEVERE, e.getMessage(), e);

            throw new RuntimeException(e);
        }

        logger.info("Level db created, db =" + dbFile);
    }

    @Override
    public synchronized void put(ByteString key, ByteString oldVersion,
            KVValue value, PersistOption pOption) throws KVStoreException {

        ByteString version = null;

        byte[] keyArray = key.toByteArray();

        byte[] data = null;

        data = db.get(keyArray);

        KVValue obj = null;

        if (data != null) {
            obj = new KVValue(data);

            version = obj.getVersion();
        }

        SimulatorEngine.logBytes("put, key", KvkOf(key).getKey());

        checkVersion(version, oldVersion);
        value.setKeyOf(key);

        // write options
        WriteOptions writeOptions = getWriteOption(pOption);
        // put with write options
        db.put(keyArray, value.toByteArray(), writeOptions);
    }

    @Override
    public synchronized void putForced(ByteString key, KVValue value,
            PersistOption pOption) throws KVStoreException {

        byte[] keyArray = key.toByteArray();

        value.setKeyOf(key);

        // write options
        WriteOptions writeOptions = getWriteOption(pOption);
        // logger.info ("****** put writing option: " + wOptions.sync());
        db.put(keyArray, value.toByteArray(), writeOptions);
    }

    @Override
    public synchronized void delete(ByteString key, ByteString oldVersion,
            PersistOption option) throws KVStoreException {

        ByteString prevVersion = getVersion(key);

        checkVersion(prevVersion, oldVersion);

        // write options
        WriteOptions writeOptions = getWriteOption(option);
        // delete with write options
        db.delete(key.toByteArray(), writeOptions);
    }

    @Override
    public synchronized void deleteForced(ByteString key, PersistOption option)
            throws KVStoreException {
        // forced delete

        // write options
        WriteOptions writeOptions = getWriteOption(option);
        // delete with write option
        db.delete(key.toByteArray(), writeOptions);
    }

    @Override
    public synchronized KVValue get(ByteString key) throws KVStoreException {

        byte[] keyArray = key.toByteArray();
        byte[] data = db.get(keyArray);

        if (data == null) {
            throw new KVStoreNotFound();
        }

        return new KVValue(data);
    }

    @Override
    public synchronized KVValue getPrevious(ByteString key)
            throws KVStoreException {
        // get iterator
        DBIterator dbit = db.iterator();

        // get byte[]
        byte[] kbytes = key.toByteArray();

        KVValue value = null;

        try {
            // move to closest key
            dbit.seek(kbytes);

            // if there is a key smaller
            if (dbit.hasPrev()) {
                // get entry
                Map.Entry<byte[], byte[]> entry = dbit.prev();

                // return value
                value = new KVValue(entry.getValue());

            } else {
                // go to the last key
                dbit.seekToLast();

                // check if there is an entry
                if (dbit.hasNext()) {
                    // get entry
                    Map.Entry<byte[], byte[]> entry = dbit.next();

                    // logger.info("key=" + new String(entry.getKey()));
                    // compare last
                    int cv = compare(entry.getKey(), kbytes);

                    if (cv < 0) {
                        // return value
                        value = new KVValue(entry.getValue());
                    } else {
                        throw new KVStoreNotFound();
                    }
                }
            }
        } catch (Exception e) {
            //
            throw new KVStoreNotFound();
        } finally {
            try {
                dbit.close();
            } catch (Exception e) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
        }

        if (value == null) {
            throw new KVStoreNotFound();
        }

        return value;
    }

    @Override
    public synchronized KVValue getNext(ByteString key) throws KVStoreException {
        DBIterator dbit = db.iterator();

        KVValue value = null;

        byte[] kbytes = key.toByteArray();

        try {

            dbit.seek(kbytes);

            if (dbit.hasNext()) {
                // get next element
                Map.Entry<byte[], byte[]> entry = dbit.next();

                if (compare(entry.getKey(), kbytes) == 0) {
                    // get next
                    entry = dbit.next();
                }

                value = new KVValue(entry.getValue());

            } else {
                throw new KVStoreNotFound();
            }
        } catch (NoSuchElementException ne) {
            throw new KVStoreNotFound();
        } finally {
            try {
                dbit.close();
            } catch (Exception e) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
        }

        return value;
    }

    @Override
    public synchronized SortedMap<?, ?> getRange(ByteString startKey,
            boolean startKeyInclusive, ByteString endKey,
            boolean endKeyInclusive, int max) throws KVStoreException {

        SortedMap<KVKey, KVValue> map = new TreeMap<KVKey, KVValue>();

        byte[] start = startKey.toByteArray();
        byte[] end = endKey.toByteArray();

        // Short-circuit when the start key comes after the end key.
        if (compare(start, end) > 0) {
            return map;
        }

        if ((compare(start, end) == 0)
                && ((startKeyInclusive && endKeyInclusive) == false)) {
            return map;
        }

        DBIterator cursor = null;

        try {

            cursor = db.iterator();

            cursor.seek(start);

            if (cursor.hasNext()) {
                Entry<byte[], byte[]> e = cursor.next();

                int cv = compare(e.getKey(), start);

                if (cv == 0) {
                    if (startKeyInclusive) {
                        map.put(new KVKey(e.getKey()),
                                new KVValue(e.getValue()));
                    }
                } else {
                    // check if should include
                    if (shouldInclude(e.getKey(), end, endKeyInclusive, false)) {
                        map.put(new KVKey(e.getKey()),
                                new KVValue(e.getValue()));
                    }
                }
            }

            while (cursor.hasNext() && map.size() < max) {

                Entry<byte[], byte[]> pair = cursor.next();

                // check should we put the pair in the map
                if (shouldInclude(pair.getKey(), end, endKeyInclusive, false)) {
                    map.put(new KVKey(pair.getKey()),
                            new KVValue(pair.getValue()));
                }

            }
        } catch (Exception e) {
            logger.log(Level.WARNING, e.getMessage(), e);

            // could get NoSuchElementException from getNext
            throw new KVStoreException(e.getMessage());
        } finally {
            try {
                cursor.close();
            } catch (Exception ex2) {
                logger.log(Level.WARNING, ex2.getMessage(), ex2);
            }
        }

        return map;

    }

    @Override
    public synchronized List<?> getRangeReversed(ByteString startKey,
            boolean startKeyInclusive, ByteString endKey,
            boolean endKeyInclusive, int max) throws KVStoreException {

        List<KVKey> listOfKVKey = new ArrayList<KVKey>();

        byte[] start = startKey.toByteArray();
        byte[] end = endKey.toByteArray();

        // Short-circuit when the start key comes after the end key.
        if (compare(start, end) > 0) {
            return listOfKVKey;
        }

        if ((compare(start, end) == 0)
                && ((startKeyInclusive && endKeyInclusive) == false)) {
            return listOfKVKey;
        }

        DBIterator cursor = null;

        try {

            cursor = db.iterator();

            cursor.seek(end);

            boolean endKeyExist = cursor.hasNext();

            Entry<byte[], byte[]> e = null;

            // if end key does not exist, seek to last and add the last key if
            // should include
            if (!endKeyExist) {
                cursor.seekToLast();

                if (cursor.hasNext()) {
                    e = cursor.next();
                    if (shouldInclude(e.getKey(), start, startKeyInclusive,
                            true)) {
                        listOfKVKey.add(new KVKey(e.getKey()));
                    }
                }
            } else // if end key exists, add end key if should include
            {
                if (endKeyInclusive) {
                    e = cursor.next();

                    int cv = compare(e.getKey(), end);

                    if (cv == 0) {
                        listOfKVKey.add(new KVKey(e.getKey()));
                    }
                    if (0 > cv) {
                        // check if should include
                        if (shouldInclude(e.getKey(), start, startKeyInclusive,
                                true)) {
                            listOfKVKey.add(new KVKey(e.getKey()));
                        }
                    }
                }
            }

            // if endKey exists, seek to endKey, or seek to last key
            if (endKeyExist) {
                cursor.seek(end);
            } else {
                cursor.seekToLast();
            }

            // move cursor to previous and add rest keys
            while (cursor.hasPrev() && listOfKVKey.size() < max) {

                Entry<byte[], byte[]> pair = cursor.prev();

                // check should we put the pair in the map
                if (shouldInclude(pair.getKey(), start, startKeyInclusive, true)) {
                    listOfKVKey.add(new KVKey(pair.getKey()));
                }
            }
        } catch (Exception e) {

            logger.log(Level.WARNING, e.getMessage(), e);

            // could get NoSuchElementException from getNext
            throw new KVStoreException(e.getMessage());
        } finally {
            try {
                cursor.close();
            } catch (Exception ex2) {
                logger.log(Level.WARNING, ex2.getMessage(), ex2);
            }
        }

        return listOfKVKey;
    }

    @Override
    public synchronized void close() {

        try {
            this.db.close();

            logger.info("leveldb closed ...");
        } catch (IOException e) {

            logger.log(Level.WARNING, e.getMessage(), e);
        }
    }

    @Override
    public void reset() throws KVStoreException {

        this.close();

        // delete db file
        File ldb = new File(this.dbFile);
        // boolean deleted = deleteDirectory(ldb);
        Options options = new Options();

        try {
            factory.destroy(ldb, options);
        } catch (IOException e) {
            throw new KVStoreException(e.getMessage());
        }

        logger.info("leveldb removed, path=" + this.dbFile);

        // re open
        this.init(config);

    }

    /**
     * delete the specified directory.
     *
     * @param directory
     *            to be deleted
     *
     * @return true if directory is deleted. Otherwise, delete false.
     */
    public static boolean deleteDirectory(File directory) {

        // cannot be null
        if (directory == null) {
            throw new NullPointerException("file cannot be null");
        }

        // check if the directory exists
        if (!directory.exists() || !directory.isDirectory()) {
            return false;
        }

        // get list of files in the dir, cannot be null since it is a dir.
        String[] fileNames = directory.list();

        for (String fineName : fileNames) {

            // get the file handle
            File file = new File(directory, fineName);

            if (file.isDirectory()) {
                // recursive delete dir
                deleteDirectory(file);
            } else {
                // delete file
                file.delete();
            }
        }

        // delete specified dir
        return directory.delete();
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

    private static boolean shouldInclude(byte[] k1, byte[] compareKey,
            boolean inclusive, boolean reverse) {
        int cv = compare(k1, compareKey);

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

    private static WriteOptions getWriteOption(PersistOption pOption) {

        // write option
        WriteOptions wOptions = null;

        switch (pOption) {
        case SYNC:
        case FLUSH:
            wOptions = SYNC_WRITE_OPTION;
            break;
        case ASYNC:
            wOptions = asyncWriteOption;
            break;
        default:
            wOptions = SYNC_WRITE_OPTION;
        }

        return wOptions;
    }

    @Override
    public BatchOperation<ByteString, KVValue> createBatchOperation()
            throws KVStoreException {
        return new LdbBatchOperation(db);
    }

    @Override
    public void flush() throws KVStoreException {
        try {
            doFlush();
        } catch (Exception e) {
            KVStoreException kvse = new KVStoreException(e.getMessage());
            throw kvse;
        }
    }

    public synchronized void doFlush() throws IOException {

        // make a key so that no key in DB matches it
        byte[] key = new byte[4 * 1024 + 1];

        // fill with 0
        Arrays.fill(key, (byte) 0);

        // get value
        byte[] data = db.get(key);

        WriteBatch batch = db.createWriteBatch();

        try {

            if (data == null) {
                /**
                 * no entry for key. perform no op
                 */
                batch.put(key, key);
                batch.delete(key);
            } else {
                /**
                 * entry found, put back after delete
                 */
                batch.delete(key);
                batch.put(key, data);
            }

            /**
             * do batch operation with sync option.
             */
            db.write(batch, SYNC_WRITE_OPTION);

            logger.info("data flushed to db ....");
        } finally {
            // close the batch
            batch.close();
        }
    }


    @Override
    public void compactRange(ByteString startKey, ByteString endKey)
            throws KVStoreException {

        try {

            // start key
            byte[] begin = null;
            // end key
            byte[] end = null;

            if (startKey != null && startKey.isEmpty() == false) {
                begin = startKey.toByteArray();
            }

            if (endKey != null && endKey.isEmpty() == false) {
                end = endKey.toByteArray();
            }

            this.db.compactRange(begin, end);

            logger.info("Media optimization finished");

        } catch (Exception e) {
            throw new KVStoreException(e.getMessage());
        }
    }

	@Override
    public String getPersistStorePath() throws KVStoreException {
        return this.persistFolder;
    }

}
