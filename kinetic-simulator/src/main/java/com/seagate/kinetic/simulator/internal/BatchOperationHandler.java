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
import com.seagate.kinetic.simulator.persist.Store;

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

    @SuppressWarnings("rawtypes")
    private Store store = null;

    private long cid = -1;

    private int batchId = -1;

    boolean isEndBatch = false;

    private BatchOperation<ByteString, KVValue> batch = null;

    private ArrayList<KineticMessage> list = new ArrayList<KineticMessage>();

    @SuppressWarnings("unchecked")
    public BatchOperationHandler(KineticMessage request,
 SimulatorEngine engine)
            throws InvalidBatchException {

        try {
            // start batch
            batch = engine.getStore().createBatchOperation();

            // this batch op handler belongs to this connection
            this.cid = request.getCommand().getHeader().getConnectionID();

            this.batchId = request.getCommand().getHeader().getBatchID();

            // simulator engine
            this.engine = engine;

            // store
            this.store = engine.getStore();

        } catch (Exception e) {
            throw new InvalidBatchException(e);
        }
    }

    public synchronized boolean handleRequest(KineticMessage request,
            KineticMessage response)
            throws InvalidBatchException, NotAttemptedException {

        if (this.isEndBatch) {
            throw new InvalidBatchException("batch is not started or has ended");
        }

        try {

            // check if request is from the same batch
            if (request.getCommand().getHeader().getConnectionID() != this.cid
                    || request.getCommand().getHeader().getBatchID() != this.batchId) {

                throw new RuntimeException("DB is locked by: " + cid + "-"
                        + batchId + ", request id: "
                        + request.getCommand().getHeader().getConnectionID()
                        + request.getCommand().getHeader().getBatchID());
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
            logger.log(Level.WARNING, nae.getMessage(), nae);
            close();
            throw nae;
        } catch (KVStoreException kvse) {
            logger.log(Level.WARNING, kvse.getMessage(), kvse);
            close();
            throw new NotAttemptedException(kvse);
        } catch (Exception e) {
            logger.log(Level.WARNING, e.getMessage(), e);
            close();
            throw new InvalidBatchException(e);
        }

        return isEndBatch;
    }

    private void batchDelete(KineticMessage km) throws KVStoreException {



        // proto request KV
        KeyValue requestKeyValue = km.getCommand().getBody().getKeyValue();

        ByteString key = requestKeyValue.getKey();

        // check version if required
        if (requestKeyValue.getForce() == false) {
            checkVersion(requestKeyValue);
        }

        batch.delete(key);
    }

    private void batchPut(KineticMessage km) throws KVStoreException {

        ByteString key = km.getCommand().getBody().getKeyValue().getKey();

        ByteString valueByteString = null;

        if (km.getValue() != null) {
            valueByteString = ByteString.copyFrom(km.getValue());
        } else {
            // set value to empty if null
            valueByteString = ByteString.EMPTY;
        }

        // proto request KV
        KeyValue requestKeyValue = km.getCommand().getBody().getKeyValue();

        // check version if required
        if (requestKeyValue.getForce() == false) {
            checkVersion(requestKeyValue);
        }

        // construct store KV
        Algorithm al = null;
        if (requestKeyValue.hasAlgorithm()) {
            al = requestKeyValue.getAlgorithm();
        }

        KVValue data = new KVValue(requestKeyValue.getKey(),
                requestKeyValue.getNewVersion(), requestKeyValue.getTag(), al,
                valueByteString);

        // batch put
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

    private static void compareVersion(ByteString storeDbVersion,
            ByteString requestDbVersion) throws KVStoreVersionMismatch {

        if (mySize(storeDbVersion) != mySize(requestDbVersion)) {
            throw new KVStoreVersionMismatch("Length mismatch");
        }

        if (mySize(storeDbVersion) == 0) {
            return;
        }

        if (!storeDbVersion.equals(requestDbVersion)) {
            throw new KVStoreVersionMismatch("Compare mismatch");
        }
    }

    private static int mySize(ByteString s) {
        if (s == null)
            return 0;
        return s.size();
    }

    private void checkVersion(KeyValue requestKeyValue) throws KVStoreException {

        ByteString requestDbVersion = requestKeyValue.getDbVersion();

        ByteString key = requestKeyValue.getKey();

        ByteString storeDbVersion = this.getDbVersion(key);

        logger.info("*********comparing version., storeV=" + storeDbVersion
                + "requestV=" + requestDbVersion);

        compareVersion(storeDbVersion, requestDbVersion);

        logger.info("*********batch op version checked and passed ...");
    }

    @SuppressWarnings("unchecked")
    private ByteString getDbVersion(ByteString key) {

        KVValue storeKv = null;
        ByteString storeDbVersion = null;

        try {
            storeKv = (KVValue) store.get(key);
            storeDbVersion = storeKv.getVersion();
        } catch (Exception e) {
            ;
        }

        return storeDbVersion;
    }

    public synchronized boolean isClosed() {
        return this.isEndBatch;
    }

    public long getConnectionId() {
        return this.cid;
    }

    public int getBatchId() {
        return this.batchId;
    }
}
