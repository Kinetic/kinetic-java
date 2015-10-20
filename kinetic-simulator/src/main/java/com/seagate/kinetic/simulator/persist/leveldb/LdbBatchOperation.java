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

import java.io.IOException;
import java.util.logging.Logger;

import org.iq80.leveldb.DB;
import org.iq80.leveldb.WriteBatch;
import org.iq80.leveldb.WriteOptions;

import com.google.protobuf.ByteString;
import com.seagate.kinetic.simulator.persist.BatchOperation;
import com.seagate.kinetic.simulator.persist.KVValue;

public class LdbBatchOperation implements BatchOperation<ByteString, KVValue> {

    private final static Logger logger = Logger
            .getLogger(LdbBatchOperation.class.getName());

    private WriteBatch batch = null;

    private DB db = null;

    private volatile boolean isClosed = false;

    // sync write option
    public static final WriteOptions SYNC = new WriteOptions().sync(true);

    public LdbBatchOperation(DB db) {
        this.db = db;
        this.batch = db.createWriteBatch();

        logger.info("*** batch created ....");
    }

    @Override
    public synchronized void close() throws IOException {
        // close batch
        try {
            this.batch.close();
            logger.info("*** batch closed ....");
        } finally {
            this.isClosed = true;
        }
    }

    @Override
    public void put(ByteString key, KVValue value) {
        // put entry in batch
        this.batch.put(key.toByteArray(), value.toByteArray());
    }

    @Override
    public void delete(ByteString key) {
        // delte key in batch
        this.batch.delete(key.toByteArray());
    }

    @Override
    public synchronized void commit() {

        try {
            db.write(batch, SYNC);
        } finally {
            this.isClosed = true;
        }

        logger.info("*** batch committed ....");
    }

    @Override
    public boolean isClosed() {
        return this.isClosed;
    }

}
