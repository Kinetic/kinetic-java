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

import java.io.IOException;
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
public class BatchOpHandler {

    private final static Logger logger = Logger.getLogger(BatchOpHandler.class
            .getName());

    private SimulatorEngine engine = null;
    private long cid = -1;

    boolean isEndBatch = false;

    private int queueDepth = 100;

    private BatchOperation<ByteString, KVValue> batch = null;

    private ArrayList<KineticMessage> list = new ArrayList<KineticMessage>();

    public BatchOpHandler(KineticMessage request, KineticMessage respond,
            SimulatorEngine engine) {

        // this batch op handler belongs to this connection
        this.cid = request.getCommand().getHeader().getConnectionID();
        // simulator engine
        this.engine = engine;
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

            if (request.getCommand().getHeader().getMessageType() == MessageType.END_BATCH) {
                // commit the batch
                isEndBatch = true;
                // do batch op.
                this.commitBatch();
            } else {
                // check if reached limit
                if (list.size() <= this.queueDepth) {
                    list.add(request);
                    logger.info("*** added message to batch queue ...");
                } else {

                    logger.warning("exceed max queue depth: " + this.queueDepth);

                    throw new RuntimeException("exceed max queue depth: "
                            + this.queueDepth);
                }
            }
        } catch (NotAttemptedException nae) {
            isEndBatch = true;
            throw nae;
        } catch (Exception e) {
            isEndBatch = true;
            throw new InvalidBatchException(e);
        } finally {

            if (isEndBatch) {
                // do lean up
                this.close();
            }
        }

        return isEndBatch;
    }

    @SuppressWarnings("unchecked")
    private synchronized void commitBatch() throws KVStoreException,
            IOException,
            NotAttemptedException {

        batch = engine.getStore().createBatchOperation();

        for (KineticMessage km : list) {

            ByteString key = km.getCommand().getBody().getKeyValue().getKey();

            logger.info("*** batch op entry., key = " + key);

            if (km.getCommand().getHeader().getMessageType() == MessageType.DELETE) {

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

            } else {
                ByteString valueByteString = null;

                if (km.getValue() != null) {
                    valueByteString = ByteString.copyFrom(km.getValue());
                } else {
                    // set value to empty if null
                    valueByteString = ByteString.EMPTY;
                }

                // KV in;
                KeyValue requestKeyValue = km.getCommand().getBody()
                        .getKeyValue();

                Algorithm al = null;
                if (requestKeyValue.hasAlgorithm()) {
                    al = requestKeyValue.getAlgorithm();
                }

                KVValue data = new KVValue(requestKeyValue.getKey(),
                        requestKeyValue.getNewVersion(),
                        requestKeyValue.getTag(), al, valueByteString);

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
        }

        // commit db batch
        batch.commit();
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
