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
package com.seagate.kinetic.simulator.io.provider.nio;

import io.netty.channel.ChannelHandlerContext;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import com.seagate.kinetic.common.lib.KineticMessage;
import com.seagate.kinetic.proto.Kinetic.Command.MessageType;
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
            }
        }

        return flag;
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
}
