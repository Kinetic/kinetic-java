/**
 * 
 * Copyright (C) 2014 Seagate Technology.
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 */
package com.seagate.kinetic.simulator.internal;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.protobuf.ByteString;
import com.seagate.kinetic.common.lib.KineticMessage;
import com.seagate.kinetic.proto.Kinetic.Command.Algorithm;
import com.seagate.kinetic.proto.Kinetic.Command.KeyValue;
import com.seagate.kinetic.proto.Kinetic.Command.MessageType;
import com.seagate.kinetic.simulator.persist.BatchOperation;
import com.seagate.kinetic.simulator.persist.KVValue;

/**
 * Batch operation handler.
 * 
 * @author chiaming
 *
 */
public class BatchOperationHandler {

    private final static Logger logger = Logger.getLogger(BatchOperationHandler.class
            .getName());

    @SuppressWarnings("unused")
    private SimulatorEngine engine = null;

    private long cid = -1;

    boolean isEndBatch = false;

    private BatchOperation<ByteString, KVValue> batch = null;

    private ArrayList<KineticMessage> list = new ArrayList<KineticMessage>();

    @SuppressWarnings("unchecked")
    public BatchOperationHandler(KineticMessage request, KineticMessage respond,
 SimulatorEngine engine)
            throws InvalidBatchException {

        try {
            // start batch
            batch = engine.getStore().createBatchOperation();

            // this batch op handler belongs to this connection
            this.cid = request.getCommand().getHeader().getConnectionID();

            // simulator engine
            this.engine = engine;
        } catch (Exception e) {
            throw new InvalidBatchException(e);
        }
    }

    public synchronized boolean handleRequest(KineticMessage request,
            KineticMessage response)
            throws InvalidBatchException, NotAttemptedException {

        if (this.isEndBatch) {
            throw new InvalidBatchException("batch is not started");
        }

        try {

            // check if request is from the same client/connection
            if (request.getCommand().getHeader().getConnectionID() != this.cid) {
                throw new RuntimeException("DB is locked by: " + cid
                        + ", request cid: "
                        + request.getCommand().getHeader().getConnectionID());
            }

            MessageType mtype = request.getCommand().getHeader()
                    .getMessageType();

            if (mtype == MessageType.END_BATCH) {
                this.commitBatch();
            } else if (mtype == MessageType.PUT) {
                this.batchPut(request);
            } else if (mtype == MessageType.DELETE) {
                this.batchDelete(request);
            } else {
                throw new NotAttemptedException("invalid message type: "
                        + mtype);
            }
        } catch (NotAttemptedException nae) {
            close();
            throw nae;
        } catch (Exception e) {
            close();
            throw new InvalidBatchException(e);
        }

        return isEndBatch;
    }

    private void batchDelete(KineticMessage km) throws NotAttemptedException {

        ByteString key = km.getCommand().getBody().getKeyValue().getKey();

        if (km.getCommand().getBody().getKeyValue().getForce() == false) {

            boolean isVersionMatched = true;

            // XXX: check version
            // ByteString dbv =
            // km.getCommand().getBody().getKeyValue().getDbVersion();
            if (isVersionMatched == false) {
                throw new NotAttemptedException("version mismatch");
            }

        }

        batch.delete(key);
    }

    private void batchPut(KineticMessage km) throws NotAttemptedException {

        ByteString key = km.getCommand().getBody().getKeyValue().getKey();

        ByteString valueByteString = null;

        if (km.getValue() != null) {
            valueByteString = ByteString.copyFrom(km.getValue());
        } else {
            // set value to empty if null
            valueByteString = ByteString.EMPTY;
        }

        // KV in;
        KeyValue requestKeyValue = km.getCommand().getBody().getKeyValue();

        Algorithm al = null;
        if (requestKeyValue.hasAlgorithm()) {
            al = requestKeyValue.getAlgorithm();
        }

        KVValue data = new KVValue(requestKeyValue.getKey(),
                requestKeyValue.getNewVersion(), requestKeyValue.getTag(), al,
                valueByteString);

        // XXX: check version
        // ByteString dbv =
        // km.getCommand().getBody().getKeyValue().getDbVersion();
        boolean isVersionMatched = true;
        if (isVersionMatched == false) {
            throw new NotAttemptedException("version mismatch");
        }

        // XXX: check version
        batch.put(key, data);

        logger.info("*** batch op put entry., key = " + key);
    }

    private synchronized void commitBatch() {
        try {
            // commit db batch
            batch.commit();
        } finally {
            this.close();
        }
    }

    /**
     * close the current batch operation.
     */
    public void close() {

        try {

            if (batch != null) {
                batch.close();
            }

            // clear list
            list.clear();

            logger.info("*** batch op closed ");
        } catch (Exception e) {
            logger.log(Level.WARNING, e.getMessage(), e);
        } finally {
            this.isEndBatch = true;
        }
    }

    public synchronized boolean isClosed() {
        return this.isEndBatch;
    }
}
