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
import com.seagate.kinetic.proto.Kinetic.Command;
import com.seagate.kinetic.proto.Kinetic.Command.Algorithm;
import com.seagate.kinetic.proto.Kinetic.Command.Batch;
import com.seagate.kinetic.proto.Kinetic.Command.KeyValue;
import com.seagate.kinetic.proto.Kinetic.Command.MessageType;
import com.seagate.kinetic.proto.Kinetic.Command.Status.StatusCode;
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

    private long MAX_TIME_OUT = 30000;

    private SimulatorEngine engine = null;

    @SuppressWarnings("rawtypes")
    private Store store = null;

    private long cid = -1;

    private int batchId = -1;

    // boolean isClosed = false;

    private BatchOperation<ByteString, KVValue> batch = null;

    // sequence list
    private ArrayList<Long> sequenceList = new ArrayList<Long>();

    // saved exception
    private InvalidBatchException batchException = null;

    public BatchOperationHandler(SimulatorEngine engine) {

            // simulator engine
            this.engine = engine;

            // store
            this.store = engine.getStore();
    }

    @SuppressWarnings("unchecked")
    public synchronized void init(RequestContext context)
            throws InvalidBatchException, InvalidRequestException {

        if (this.batch != null) {
            this.waitForBatchToFinish();
        }

        // this batch op handler belongs to this connection
        this.cid = context.getRequestMessage().getCommand().getHeader()
                .getConnectionID();

        // batch Id
        this.batchId = context.getRequestMessage().getCommand().getHeader()
                .getBatchID();

        // start batch
        try {
            // create new batch instance
            batch = engine.getStore().createBatchOperation();

            // clear seq list
            sequenceList.clear();

            // clear exception
            this.batchException = null;
        } catch (KVStoreException e) {
            throw new InvalidBatchException(e);
        }
    }

    public synchronized void checkBatchMode(KineticMessage kmreq)
            throws InvalidRequestException {

        if (kmreq.getIsInvalidBatchMessage()) {
            throw new InvalidRequestException(
                    "Invalid batch Id found in message: "
                            + kmreq.getCommand().getHeader().getBatchID());
        }

        if (this.batch == null) {
            return;
        }

        if (kmreq.getCommand().getHeader().getBatchID() == this.batchId
                && kmreq.getCommand().getHeader().getConnectionID() == this.cid) {

            return;
        }

        this.waitForBatchToFinish();
    }

    private synchronized void waitForBatchToFinish() {

        long totalWaitTime = 0;
        long period = 3000;

        long start = System.currentTimeMillis();

        while (batch != null) {

            try {

                this.wait(period);

                if (batch == null) {
                    return;
                }

                totalWaitTime = (System.currentTimeMillis() - start);

                if (totalWaitTime >= MAX_TIME_OUT) {
                    throw new RuntimeException(
                            "Timeout waiting for batch mode to finish");
                } else {
                    logger.warning("waiting for batch mode to finish., total wait time ="
                            + totalWaitTime);
                }
            } catch (InterruptedException e) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
        }
    }

    public synchronized void handleRequest(RequestContext context)
            throws InvalidBatchException,
            NotAttemptedException, KVStoreException {

        MessageType mtype = context.getMessageType();

        /**
         * messages will be queued or dequeued. no process until end batch is
         * received.
         */
        if (mtype == MessageType.START_BATCH
                || mtype == MessageType.ABORT_BATCH) {
            return;
        }

        try {

            /**
             * start batch if message is tagged as first batch message
             */
            if (context.getRequestMessage().getIsFirstBatchMessage()) {
                this.init(context);
            }

            /**
             * put op sequence to queue
             */
            if (mtype == MessageType.PUT || mtype == MessageType.DELETE) {
                this.addSequenceList(context);
            }

            // check if this is a valid batch message
            checkBatch(context);

            if (mtype == MessageType.END_BATCH) {
                this.commitBatch(context);
            } else if (mtype == MessageType.PUT) {
                this.batchPut(context.getRequestMessage());
            } else if (mtype == MessageType.DELETE) {
                this.batchDelete(context.getRequestMessage());
            } else {
                throw new NotAttemptedException("invalid message type: "
                        + mtype);
            }
        } catch (NotAttemptedException nae) {

            logger.log(Level.WARNING, nae.getMessage(), nae);

            // set status code and message
            context.getCommandBuilder().getStatusBuilder()
                    .setCode(StatusCode.NOT_ATTEMPTED);
            context.getCommandBuilder().getStatusBuilder()
                    .setStatusMessage(nae.getMessage());

            this.saveFailedRequestContext(context);

            close();

            throw nae;
        } catch (KVStoreVersionMismatch vmismatch) {

            logger.log(Level.WARNING, vmismatch.getMessage(), vmismatch);

            // set status code and message
            context.getCommandBuilder().getStatusBuilder()
                    .setCode(StatusCode.VERSION_MISMATCH);
            context.getCommandBuilder().getStatusBuilder()
                    .setStatusMessage(vmismatch.getMessage());

            this.saveFailedRequestContext(context);

            close();

            throw vmismatch;
        } catch (KVStoreException kvse) {

            logger.log(Level.WARNING, kvse.getMessage(), kvse);

            context.getCommandBuilder().getStatusBuilder()
                    .setCode(StatusCode.INTERNAL_ERROR);
            context.getCommandBuilder().getStatusBuilder()
                    .setStatusMessage(kvse.getMessage());

            this.saveFailedRequestContext(context);

            close();

            throw kvse;
        } catch (Exception e) {

            logger.log(Level.WARNING, e.getMessage(), e);

            context.getCommandBuilder().getStatusBuilder()
                    .setCode(StatusCode.INVALID_BATCH);
            context.getCommandBuilder().getStatusBuilder()
                    .setStatusMessage(e.getMessage());

            this.saveFailedRequestContext(context);

            close();
        }
    }

    private void saveFailedRequestContext(RequestContext context) {
        if (batchException == null) {
            this.batchException = new InvalidBatchException();
            this.batchException.setFailedRequestContext(context);
        }
    }

    private void addSequenceList(RequestContext context) {
        this.sequenceList.add(context.getRequestMessage().getCommand().getHeader()
                .getSequence());
    }

    private void checkBatch(RequestContext context)
            throws InvalidBatchException, NotAttemptedException {

        if (this.batch == null) {

            String msg = "batch is not started or has ended";

            if (context.getMessageType() == MessageType.END_BATCH) {

                // batch had failed earlier
                if (this.batchException != null) {

                    // get saved failed status code and message
                    StatusCode code = this.batchException
                            .getFailedRequestContext().getResponseMessage()
                            .getCommand().getStatus().getCode();

                    String smessage = this.batchException
                            .getFailedRequestContext().getResponseMessage()
                            .getCommand().getStatus().getStatusMessage();

                    // set failed status code
                    context.getCommandBuilder().getStatusBuilder()
                            .setCode(code);
                    // set status message
                    context.getCommandBuilder().getStatusBuilder()
                            .setStatusMessage(smessage);

                    // get failed request message sequence
                    long failedSeq = this.batchException
                            .getFailedRequestContext().getRequestMessage()
                            .getCommand().getHeader().getSequence();

                    // set failed request sequence
                    context.getCommandBuilder().getBodyBuilder()
                            .getBatchBuilder().setFailedSequence(failedSeq);

                    /**
                     * get batch construct for END_BATCH_RESPONSE message
                     */
                    Batch.Builder bb = context.getCommandBuilder()
                            .getBodyBuilder().getBatchBuilder();

                    /**
                     * add sequence list to batch construct
                     */
                    for (Long sequence : this.sequenceList) {
                        bb.addSequence(sequence.longValue());
                    }

                    // propagate the exception
                    throw new InvalidBatchException(smessage);
                } else {
                    throw new InvalidBatchException(msg);
                }
            } else {
                throw new NotAttemptedException(msg);
            }
        }

        // check if request is from the same batch
        if (context.getRequestMessage().getCommand().getHeader()
                .getConnectionID() != this.cid
                || context.getRequestMessage().getCommand().getHeader()
                        .getBatchID() != this.batchId) {

            throw new RuntimeException("DB is locked by: "
                    + cid
                    + "-"
                    + batchId
                    + ", request id: "
                    + context.getRequestMessage().getCommand().getHeader()
                            .getConnectionID()
                    + context.getRequestMessage().getCommand().getHeader()
                            .getBatchID());
        }

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
    }

    private synchronized void commitBatch(RequestContext context) {

        try {

            /**
             * db commit batch
             */
            batch.commit();

            /**
             * add sequence sequenceList to end batch response message
             */
            Command.Builder cb = context.getCommandBuilder();

            /**
             * add sequence list to batch construct
             */
            for (Long sequence : sequenceList) {
                cb.getBodyBuilder().getBatchBuilder()
                        .addSequence(sequence.longValue());
            }

        } finally {
            this.close();
        }
    }

    /**
     * close the current batch operation.
     */
    public synchronized void close() {

        if (this.batch == null) {
            return;
        }

        try {

            // close db batch
            batch.close();
        } catch (Exception e) {
            logger.log(Level.WARNING, e.getMessage(), e);
        } finally {
            batch = null;
            this.notifyAll();
        }
    }

    private static void compareVersion(ByteString storeDbVersion,
            ByteString requestDbVersion) throws KVStoreVersionMismatch {

        if (mySize(storeDbVersion) != mySize(requestDbVersion)) {
            throw new KVStoreVersionMismatch("Version mismatch");
        }

        if (mySize(storeDbVersion) == 0) {
            return;
        }

        if (!storeDbVersion.equals(requestDbVersion)) {
            throw new KVStoreVersionMismatch("Version mismatch");
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

        compareVersion(storeDbVersion, requestDbVersion);
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
        return (this.batch == null);
    }

    public long getConnectionId() {
        return this.cid;
    }

    public int getBatchId() {
        return this.batchId;
    }
}
