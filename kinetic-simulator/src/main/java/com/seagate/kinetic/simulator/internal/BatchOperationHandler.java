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
import com.seagate.kinetic.proto.Kinetic.Command.Security.ACL.Permission;
import com.seagate.kinetic.proto.Kinetic.Command.Status.StatusCode;
import com.seagate.kinetic.simulator.persist.BatchOperation;
import com.seagate.kinetic.simulator.persist.KVOp;
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

    //
    // private Map<ByteString, ByteString> map = new
    // ConcurrentHashMap<ByteString, ByteString>();

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

            // clear version map
            // map.clear();

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
            throws InvalidBatchException, NotAttemptedException,
            KVStoreException, InvalidRequestException, KVSecurityException {

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
        } catch (InvalidRequestException ire) {

            logger.log(Level.WARNING, ire.getMessage(), ire);

            // set status code and message
            context.getCommandBuilder().getStatusBuilder()
                    .setCode(StatusCode.INVALID_REQUEST);
            context.getCommandBuilder().getStatusBuilder()
                    .setStatusMessage(ire.getMessage());

            this.saveFailedRequestContext(context);

            close();

            throw ire;
        } catch (KVSecurityException kse) {

            logger.log(Level.WARNING, kse.getMessage(), kse);

            // set status code and message
            context.getCommandBuilder().getStatusBuilder()
                    .setCode(StatusCode.NOT_AUTHORIZED);
            context.getCommandBuilder().getStatusBuilder()
                    .setStatusMessage(kse.getMessage());

            this.saveFailedRequestContext(context);

            close();

            throw kse;
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
        } catch (KVStoreNotFound kvsne) {

            logger.log(Level.WARNING, StatusCode.NOT_FOUND.toString(), kvsne);

            // set status code
            context.getCommandBuilder().getStatusBuilder()
                    .setCode(StatusCode.NOT_FOUND);

            // set status message
            context.getCommandBuilder().getStatusBuilder()
                        .setStatusMessage("key not found");

            this.saveFailedRequestContext(context);

            close();

            throw kvsne;
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

    private void batchDelete(KineticMessage km) throws KVStoreException,
            InvalidRequestException, KVSecurityException {

        KVOp.checkWrite(engine.getAclMap(), km, Permission.DELETE);

        // proto request KV
        KeyValue requestKeyValue = km.getCommand().getBody().getKeyValue();

        ByteString key = requestKeyValue.getKey();

        // check version if required
        if (requestKeyValue.getForce() == false) {
            checkVersion(km);
        }

        // batch delete entry
        batch.delete(key);
    }

    private void batchPut(KineticMessage km) throws KVStoreException,
            InvalidRequestException, KVSecurityException {

        KVOp.checkWrite(engine.getAclMap(), km, Permission.WRITE);

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
            batchPutCheckVersion(km);
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

    private void batchPutCheckVersion(KineticMessage km)
            throws KVStoreException {

        KeyValue requestKeyValue = km.getCommand().getBody().getKeyValue();

        ByteString requestDbVersion = requestKeyValue.getDbVersion();

        ByteString key = requestKeyValue.getKey();

        ByteString storeDbVersion = null;

        try {
            storeDbVersion = this.getDbVersion(key);
        } catch (KVStoreException kvne) {

            /**
             * check if new entry
             */
            if (kvne instanceof KVStoreNotFound) {

                if (requestDbVersion == null
                        || requestDbVersion == ByteString.EMPTY) {
                    /**
                     * new entry to put in store.
                     */
                    return;
                }
            }

            // db error, re-throw exception
            throw kvne;
        }

        // compare request version with store version
        compareVersion(storeDbVersion, requestDbVersion);
    }

    private void checkVersion(KineticMessage km) throws KVStoreException {

        KeyValue requestKeyValue = km.getCommand().getBody().getKeyValue();

        ByteString requestDbVersion = requestKeyValue.getDbVersion();

        ByteString key = requestKeyValue.getKey();

        ByteString storeDbVersion = this.getDbVersion(key);

        // compare version with store
        compareVersion(storeDbVersion, requestDbVersion);
    }

    @SuppressWarnings("unchecked")
    private ByteString getDbVersion(ByteString key) throws KVStoreException {

        KVValue storeKv = null;
        ByteString storeDbVersion = null;

        storeKv = (KVValue) store.get(key);
        storeDbVersion = storeKv.getVersion();

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
