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
package com.seagate.kinetic.simulator.io.provider.nio;

import io.netty.channel.ChannelHandlerContext;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import kinetic.simulator.SimulatorConfiguration;

import com.google.protobuf.ByteString;
import com.seagate.kinetic.common.lib.KineticMessage;
import com.seagate.kinetic.proto.Kinetic.Command;
import com.seagate.kinetic.proto.Kinetic.Command.MessageType;
import com.seagate.kinetic.proto.Kinetic.Command.Status.StatusCode;
import com.seagate.kinetic.proto.Kinetic.Message;
import com.seagate.kinetic.proto.Kinetic.Message.AuthType;
import com.seagate.kinetic.simulator.internal.InvalidBatchException;
import com.seagate.kinetic.simulator.internal.KVStoreException;

/**
 * Batch op Pre processor.
 * <p>
 * 
 * @author chiaming
 *
 */
public class NioBatchOpPreProcessor {

    private static final Logger logger = Logger
            .getLogger(NioBatchOpPreProcessor.class.getName());

    private static final String SEP = ".";

    // key = connId + "-" + batchId
    private static Map<String, BatchQueue> batchMap = new ConcurrentHashMap<String, BatchQueue>();

    public static boolean processMessage(NioMessageServiceHandler nioHandler,
            ChannelHandlerContext ctx, KineticMessage request)
            throws InterruptedException, InvalidBatchException,
            KVStoreException {

        MessageType mtype = getMessageType(request);

        // add to queue if batchQueue has started
        if (shouldAddToBatch(ctx, request, mtype)) {
            // the command was queued until END_BATCH is received
            return false;
        }

        switch (mtype) {
        case START_BATCH:
            request.setIsBatchMessage(true);
            createBatchQueue(ctx, request);
            break;
        case END_BATCH:
            request.setIsBatchMessage(true);
            processBatchQueue(nioHandler, ctx, request);
            break;
        case ABORT_BATCH:
            request.setIsBatchMessage(true);
            processBatchAbort(request);
            break;
        default:
            break;
        }

        return true;
    }

    private static MessageType getMessageType(KineticMessage request) {
        return request.getCommand().getHeader().getMessageType();
    }

    private static void createBatchQueue(
            ChannelHandlerContext ctx,
            KineticMessage request) {

        // check outstanding batches
        if (batchMap.size() == SimulatorConfiguration
                .getMaxOutstandingBatches()) {

            /**
             * Exceeded max outstanding batches, closing the connection.
             */
            String msg = "Exceed max outstanding batches., max allowed: "
                    + SimulatorConfiguration.getMaxOutstandingBatches()
                    + ", batch Id: "
                    + request.getCommand().getHeader().getBatchID();

            // send unsolicitated msg and close connection
            handleInvalidBatch(ctx, StatusCode.INVALID_REQUEST, msg);
        }

        String key = request.getCommand().getHeader().getConnectionID() + SEP
                + request.getCommand().getHeader().getBatchID();

        BatchQueue batchQueue = batchMap.get(key);

        if (batchQueue == null) {
            batchQueue = new BatchQueue(request);
            batchMap.put(key, batchQueue);
        } else {
            // batch already started
            throw new RuntimeException("batch already started");
        }

        logger.info("batch queue created for key: " + key);
    }


    private static boolean shouldAddToBatch(ChannelHandlerContext ctx,
 KineticMessage request, MessageType mtype)
            throws KVStoreException {

        boolean flag = false;

        boolean hasBatchId = request.getCommand().getHeader().hasBatchID();

        /**
         * not in a batch
         */
        if (hasBatchId == false) {
            return false;
        }

        String key = request.getCommand().getHeader().getConnectionID() + SEP
                + request.getCommand().getHeader().getBatchID();

        BatchQueue batchQueue = batchMap.get(key);

        if (batchQueue != null) {

            if (mtype == MessageType.PUT || mtype == MessageType.DELETE) {

                // check counts limits
                if (batchQueue.size() == SimulatorConfiguration
                        .getMaxCommandsPerBatch()) {

                    /**
                     * Exceed max commands per batch, closing the connection.
                     */
                    String msg = "Exceed max commands per batch., max allowed: "
                            + SimulatorConfiguration.getMaxCommandsPerBatch()
                            + ", batch Id: "
                            + request.getCommand().getHeader().getBatchID();

                    // send unsolicitated msg and close connection
                    handleInvalidBatch(ctx, StatusCode.INVALID_REQUEST, msg);
                }

                // is added to batch queue
                flag = true;

                // is a batch message
                request.setIsBatchMessage(true);

                // add to batch queue
                batchQueue.add(request);
            }
        } else {
            // there is a batch ID not known at this point
            // the only allowed message type is start message.
            if (mtype != MessageType.START_BATCH) {

                request.setIsInvalidBatchMessage(true);

                /**
                 * received unknown batch id. closing the connection to prevent
                 * further corruption.
                 */
                String msg = "Received unknown batch Id: "
                        + request.getCommand().getHeader().getBatchID();

                handleInvalidBatch(ctx, StatusCode.INVALID_REQUEST, msg);
            }
        }

        return flag;
    }

    /**
     * Send an unsolicitated message and close the connection.
     * 
     * @param ctx
     *            current channel context
     * @param sc
     *            status code set in the message
     * @param msg
     *            status message set in the message
     */
    private static void handleInvalidBatch(ChannelHandlerContext ctx,
            StatusCode sc,
            String msg) {

        // create message
        KineticMessage km = createUnsolicitedStatusMessage(
                StatusCode.INVALID_REQUEST, msg);

        // send to client
        ctx.writeAndFlush(km);

        // this will close the current connection
        throw new RuntimeException(msg);

    }

    private static synchronized void processBatchQueue(
            NioMessageServiceHandler ioHandler,
            ChannelHandlerContext ctx,
            KineticMessage km) throws InterruptedException,
            InvalidBatchException {

        String key = km.getCommand().getHeader().getConnectionID() + SEP
                + km.getCommand().getHeader().getBatchID();

        BatchQueue batchQueue = batchMap.get(key);

        if (batchQueue == null) {
            throw new RuntimeException("No batch Id found for key: " + key);
        }

        try {

            List<KineticMessage> mlist = batchQueue.getMessageList();

            int msize = mlist.size();

            if (msize > 0) {
                mlist.get(0).setIsFirstBatchMessage(true);
            }


            for (int index = 0; index < msize; index++) {
                // get request message
                KineticMessage request = mlist.get(index);

                // process message
                ioHandler.processRequest(ctx, request);
            }

        } finally {
            cleanup(key);

            /**
             * end batch is called in the end of message processing
             * (simulatorEngine).
             */
        }
    }

    private static void processBatchAbort(KineticMessage km) {

        String key = km.getCommand().getHeader().getConnectionID() + SEP
                + km.getCommand().getHeader().getBatchID();

        boolean isFound = batchMap.containsKey(key);

        if (isFound == false) {
            throw new RuntimeException("No batch Id found for key: " + key);
        }

        cleanup(key);

        logger.info("batch aborted ... key=" + key);
    }

    private static void cleanup(String key) {

        BatchQueue batchQueue = batchMap.remove(key);

        if (batchQueue == null) {
            logger.warning("No batch Id found, key=" + key);
        }
    }

    /**
     * Create an internal message with empty builder message.
     *
     * @return an internal message with empty builder message
     */
    private static KineticMessage createUnsolicitedStatusMessage(
            StatusCode sc, String sm) {

        // new instance of internal message
        KineticMessage kineticMessage = new KineticMessage();

        // new builder message
        Message.Builder message = Message.newBuilder();

        // set to im
        kineticMessage.setMessage(message);

        // set hmac auth type
        message.setAuthType(AuthType.UNSOLICITEDSTATUS);

        // create command builder
        Command.Builder commandBuilder = Command.newBuilder();

        commandBuilder.getStatusBuilder().setCode(sc);

        commandBuilder.getStatusBuilder().setStatusMessage(sm);

        // get command byte stirng
        ByteString commandByteString = commandBuilder.build().toByteString();

        message.setCommandBytes(commandByteString);

        // set command
        kineticMessage.setCommand(commandBuilder);

        return kineticMessage;
    }
    
    /**
     * remove batches from this connection.
     * 
     * @param km kinetic message.
     */
    public static void cleanUpConnection (long cid) {
        
        // connection id
        String keyPrefix = String.valueOf(cid);
        // batch keys
        Set <String> keys = batchMap.keySet();
        
        // remove matched connection ids
        for (String key: keys) {    
            if (key.startsWith(keyPrefix)) {
                batchMap.remove(key);
            }
        }
    }
}
