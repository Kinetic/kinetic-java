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
package com.seagate.kinetic.simulator.persist.memory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import kinetic.simulator.SimulatorConfiguration;

import com.google.protobuf.ByteString;
import com.seagate.kinetic.simulator.internal.KVStoreException;
import com.seagate.kinetic.simulator.internal.KVStoreNotFound;
import com.seagate.kinetic.simulator.internal.KVStoreVersionMismatch;
import com.seagate.kinetic.simulator.persist.BatchOperation;
import com.seagate.kinetic.simulator.persist.KVKey;
import com.seagate.kinetic.simulator.persist.KVValue;
import com.seagate.kinetic.simulator.persist.PersistOption;
import com.seagate.kinetic.simulator.persist.Store;

/**
 *
 * Memory store implementation for the Kinetic simulator.
 * <p>
 * All entries are stored in memory. When a Kinetic server is closed, the memory
 * tree is saved to the disk. When a kinetic server is crashed, all data updated
 * to memory store between start server to crash is lost.
 *
 * @author chiaming
 *
 */
public class MemoryStore implements Store<ByteString, ByteString, KVValue> {

    private final static java.util.logging.Logger logger = Logger
            .getLogger(MemoryStore.class.getName());

    // memory tree map
    private TreeMap<byte[], byte[]> sortedMap = null;

    // file to store the tree
    private String dbFile = null;

    // server config
    private SimulatorConfiguration config = null;
    
    // persist folder
    private String persistFolder = null;

    /**
     * default constructor
     */
    public MemoryStore() {
        ;
    }

    @Override
    public synchronized void put(ByteString key, ByteString oldVersion,
            KVValue value, PersistOption pOption) throws KVStoreException {

        ByteString version = null;

        byte[] keyArray = key.toByteArray();

        KVValue obj = null;
        byte[] valueInStore = this.sortedMap.get(keyArray);

        if (valueInStore != null) {
            obj = new KVValue(valueInStore);
            version = obj.getVersion();
        }

        checkVersion(version, oldVersion);

        value.setKeyOf(key);

        this.sortedMap.put(keyArray, value.toByteArray());
    }

    @Override
    public synchronized void putForced(ByteString key, KVValue value,
            PersistOption pOption) throws KVStoreException {

        try {

            value.setKeyOf(key);

            this.sortedMap.put(key.toByteArray(), value.toByteArray());
        } catch (Exception e) {
            throw new KVStoreException("DB internal exception");
        }

    }

    @Override
    public synchronized void delete(ByteString key, ByteString oldVersion,
            PersistOption pOption) throws KVStoreException {

        ByteString prevVersion = getVersion(key);

        checkVersion(prevVersion, oldVersion);

        sortedMap.remove(key.toByteArray());
    }

    @Override
    public synchronized void deleteForced(ByteString key, PersistOption pOption)
            throws KVStoreException {

        try {
            sortedMap.remove(key.toByteArray());
        } catch (Exception e) {
            throw new KVStoreException("DB internal exception");
        }
    }

    @Override
    public synchronized KVValue get(ByteString key) throws KVStoreException {

        byte[] object = this.sortedMap.get(key.toByteArray());

        if (object == null)
            throw new KVStoreNotFound();

        return new KVValue(object);
    }

    @Override
    public synchronized KVValue getPrevious(ByteString key)
            throws KVStoreException {

        byte[] key1 = sortedMap.lowerKey(key.toByteArray());

        if (key1 == null)
            throw new KVStoreNotFound();

        return new KVValue(sortedMap.get(key1));
    }

    @Override
    public synchronized KVValue getNext(ByteString key) throws KVStoreException {

        // logger.info("getNext key=" + key.toStringUtf8());

        byte[] key1 = sortedMap.higherKey(key.toByteArray());

        if (key1 == null) {
            throw new KVStoreNotFound();
        }

        return new KVValue(sortedMap.get(key1));
    }

    @Override
    public synchronized SortedMap<?, ?> getRange(ByteString startKey,
            boolean startKeyInclusive, ByteString endKey,
            boolean endKeyInclusive, int n) throws KVStoreException {

        SortedMap<byte[], byte[]> bmap;

        if (endKey.size() == 0) {
            bmap = sortedMap.tailMap(startKey.toByteArray(), startKeyInclusive);
        } else {
            bmap = sortedMap.subMap(startKey.toByteArray(), startKeyInclusive,
                    endKey.toByteArray(), endKeyInclusive);
        }

        // logger.fine("Number of entries: " + bmap.size() + " requesting " +
        // n);

        // convert type
        SortedMap<KVKey, KVValue> kvmap = new TreeMap<KVKey, KVValue>();

        for (Entry<byte[], byte[]> e : bmap.entrySet()) {
            if (n-- > 0) {
                kvmap.put(new KVKey(e.getKey()), new KVValue(e.getValue()));
            } else {
                // return kvmap;
                break;
            }
        }

        return kvmap;
    }

    @Override
    public synchronized List<?> getRangeReversed(ByteString startKey,
            boolean startKeyInclusive, ByteString endKey,
            boolean endKeyInclusive, int n) throws KVStoreException {

        SortedMap<byte[], byte[]> bmap;

        if (endKey.size() == 0) {
            bmap = sortedMap.tailMap(startKey.toByteArray(), startKeyInclusive);
        } else {
            bmap = sortedMap.subMap(startKey.toByteArray(), startKeyInclusive,
                    endKey.toByteArray(), endKeyInclusive);
        }

        // logger.fine("Number of entries: " + bmap.size() + " requesting " +
        // n);

        List<KVKey> kvKeyOfList = new ArrayList<KVKey>();
        int i = 0, j = 0;
        int size = bmap.size() > n ? n : bmap.size();
        byte[][] keys = new byte[size][];
        if (bmap.size() > n) {
            for (Entry<byte[], byte[]> e : bmap.entrySet()) {
                if (i++ >= bmap.size() - n)
                    keys[j++] = e.getKey();
            }
        } else {
            for (Entry<byte[], byte[]> e : bmap.entrySet()) {
                keys[i++] = e.getKey();
            }
        }
        for (i = size - 1; i >= 0; i--) {
            kvKeyOfList.add(new KVKey(keys[i]));
        }

        return kvKeyOfList;

    }

    @Override
    public void close() {

        FileOutputStream fos = null;
        ObjectOutputStream oos = null;

        try {
            // get out put stream
            fos = new FileOutputStream(dbFile);
            oos = new ObjectOutputStream(fos);

            // write to file.
            oos.writeObject(this.sortedMap);
            oos.flush();
            fos.flush();

            logger.info("saved memory file, path=" + dbFile + ", entry count="
                    + this.sortedMap.size());

        } catch (Exception e) {

            logger.log(Level.WARNING, e.getMessage(), e);

        } finally {
            try {
                oos.close();
                fos.close();
            } catch (Exception e2) {
                ;
            }
        }

        logger.info("memory store closed ...");
    }

    @Override
    public void reset() throws KVStoreException {
        // clean data
        this.sortedMap.clear();

        // this will write the reset map to file.
        this.close();

        // reopen the file
        this.init(config);
    }

    private static int mySize(ByteString s) {
        if (s == null)
            return 0;
        return s.size();
    }

    public static void checkVersion(ByteString version, ByteString oldVersion)
            throws KVStoreVersionMismatch {

        // logger.fine("Compare len: " + mySize(version) + ", "
        // + mySize(oldVersion));

        if (mySize(version) != mySize(oldVersion))
            throw new KVStoreVersionMismatch("Length mismatch");
        if (mySize(version) == 0)
            return;
        if (!version.equals(oldVersion))
            throw new KVStoreVersionMismatch("Compare mismatch");
    }

    // private KVKey KvkOf(ByteString k) {
    // return new KVKey(k);
    // }

    ByteString getVersion(ByteString key) throws KVStoreException {
        KVValue obj = get(key);
        if (obj == null)
            throw new KVStoreNotFound();
        if (!obj.hasVersion())
            return ByteString.EMPTY;
        return obj.getVersion();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void init(SimulatorConfiguration config) {

        this.config = config;

        // default home folder
        String defaultHome = System.getProperty("user.home") + File.separator
                + "kinetic";

        // kinetic home
        String kineticHome = config.getProperty(
                SimulatorConfiguration.KINETIC_HOME, defaultHome);

        File lchome = new File(kineticHome);

        // make folder if not there
        if (lchome.exists() == false) {
            boolean created = lchome.mkdir();
            logger.info("create kinetic home folder: " + kineticHome
                    + ", created=" + created);
        }

        // persist home
        persistFolder = kineticHome
                + File.separator
                + config.getProperty(SimulatorConfiguration.PERSIST_HOME,
                        "memory");

        File f = new File(persistFolder);

        logger.info("Database file exists: " + f.exists() + ", name="
                + persistFolder);

        // create persist folder if not existed
        if (f.exists() == false) {
            boolean created = f.mkdir();
            logger.info("create persist folder: " + persistFolder
                    + ", created=" + created);
        }

        // db file
        dbFile = persistFolder + "/memStore.ser";

        FileInputStream fis = null;
        ObjectInputStream ois = null;

        try {
            // read memory tree
            fis = new FileInputStream(dbFile);
            ois = new ObjectInputStream(fis);
            this.sortedMap = (TreeMap<byte[], byte[]>) ois.readObject();

            logger.info("loaded memory file, path=" + dbFile + ", size="
                    + this.sortedMap.size());

        } catch (Exception e) {

            // logger.log(Level.WARNING, e.getMessage(), e);
            logger.fine("unable to load memory store from file, using a new store, path="
                    + dbFile);

            // start a new one if unable to read from one on disk
            this.sortedMap = new TreeMap<byte[], byte[]>(new KeyComparator());

        } finally {
            try {
                ois.close();
                fis.close();
            } catch (Exception e2) {
                ;
            }
        }

    }

    @Override
    public BatchOperation<ByteString, KVValue> createBatchOperation()
            throws KVStoreException {

        throw new java.lang.UnsupportedOperationException();
    }

    @Override
    public void flush() throws KVStoreException {
        // no op for mem store
        ;
    }

    @Override
    public void compactRange(ByteString startKey, ByteString endKey)
            throws KVStoreException {
        // no op
        ;
    }
    
    @Override
    public String getPersistStorePath() throws KVStoreException {
        return this.persistFolder;
    }

}
