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

import java.util.List;
import java.util.SortedMap;
import java.util.logging.Logger;

import kinetic.simulator.SimulatorConfiguration;

import com.google.protobuf.ByteString;
import com.seagate.kinetic.simulator.internal.KVStoreException;
import com.seagate.kinetic.simulator.persist.BatchOperation;
import com.seagate.kinetic.simulator.persist.KVValue;
import com.seagate.kinetic.simulator.persist.PersistOption;
import com.seagate.kinetic.simulator.persist.Store;

/**
 * Implement Kinetic Store interface.
 *
 * XXX chiaming 12/24/2013: support PersistOption
 *
 * @author James Hughes
 * @author Chenchong(Emma) Li
 *
 */
public class BdbStore implements Store<ByteString, ByteString, KVValue> {
    private final static Logger logger = Logger.getLogger(BdbStore.class
            .getName());

    private SimulatorConfiguration config = new SimulatorConfiguration();
    private KVStore kvStore = null;

    public BdbStore() {
        ;
    }

    @Override
    public void close() {
        this.kvStore.close();
    }

    @Override
    public void init(SimulatorConfiguration config) {

        this.config = config;

        logger.info("begin a new DB");
        kvStore = new KVStore(config);
    }

    @Override
    public void put(ByteString key, ByteString oldVersion, KVValue value,
            PersistOption option) throws KVStoreException {
        this.kvStore.put(key, oldVersion, value);
    }

    @Override
    public void putForced(ByteString key, KVValue value, PersistOption option)
            throws KVStoreException {
        this.kvStore.putForced(key, value);
    }

    @Override
    public void delete(ByteString key, ByteString oldVersion,
            PersistOption option) throws KVStoreException {
        this.kvStore.delete(key, oldVersion);

    }

    @Override
    public void deleteForced(ByteString key, PersistOption option)
            throws KVStoreException {
        this.kvStore.deleteForced(key);
    }

    @Override
    public KVValue get(ByteString key) throws KVStoreException {
        return this.kvStore.get(key);
    }

    @Override
    public KVValue getPrevious(ByteString key) throws KVStoreException {
        return this.kvStore.getPrevious(key);
    }

    @Override
    public KVValue getNext(ByteString key) throws KVStoreException {
        return this.kvStore.getNext(key);
    }

    @Override
    public SortedMap<?, ?> getRange(ByteString k1, boolean i1, ByteString k2,
            boolean i2, int n) throws KVStoreException {
        return this.kvStore.getRange(k1, i1, k2, i2, n);
    }

    @Override
    public List<?> getRangeReversed(ByteString k1, boolean i1, ByteString k2,
            boolean i2, int n) throws KVStoreException {
        return this.kvStore.getRangeReversed(k1, i1, k2, i2, n);
    }

    @Override
    public void reset() throws KVStoreException {
        logger.info("erase db begin.");
        if (!this.erase(this.kvStore)) {
            throw new KVStoreException("reset store failed");
        } else {
            logger.info("init kvstore.");
            this.init(this.config);
        }
    }

    private boolean erase(KVStore store) {
        boolean erased = false;

        try {
            this.kvStore.removeDatabase();
            this.kvStore.closeEvn();
            erased = true;
            logger.info("erase db successfully.");
        } catch (Exception e) {
            logger.warning(e.getMessage());
        }

        return erased;
    }

    @Override
    public BatchOperation<ByteString, KVValue> createBatchOperation()
            throws KVStoreException {
        // TODO Auto-generated method stub
        throw new java.lang.UnsupportedOperationException();
    }

    @Override
    public void flush() throws KVStoreException {
        logger.warning("flush is not implemented for bdb");
    }

    @Override
    public void compactRange(ByteString startKey, ByteString endKey)
            throws KVStoreException {
        // TODO Auto-generated method stub
        logger.warning("method is not implemented for bdb");
    }
    
    @Override
    public String getPersistStorePath() throws KVStoreException {
        return this.kvStore.getPersistStorePath();
    }

}
