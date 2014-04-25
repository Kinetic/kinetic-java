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
package com.seagate.kinetic.simulator.internal.p2p;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import kinetic.client.Entry;
import kinetic.client.KineticClient;

import com.google.protobuf.ByteString;
import com.seagate.kinetic.proto.Kinetic.Message;
import com.seagate.kinetic.proto.Kinetic.Message.MessageType;
import com.seagate.kinetic.proto.Kinetic.Message.P2POperation;
import com.seagate.kinetic.proto.Kinetic.Message.P2POperation.Operation;
import com.seagate.kinetic.proto.Kinetic.Message.Security.ACL;
import com.seagate.kinetic.proto.Kinetic.Message.Security.ACL.Permission;
import com.seagate.kinetic.proto.Kinetic.Message.Status.StatusCode;
import com.seagate.kinetic.simulator.internal.Authorizer;
import com.seagate.kinetic.simulator.internal.KVSecurityException;
import com.seagate.kinetic.simulator.internal.KVStoreNotFound;
import com.seagate.kinetic.simulator.persist.KVValue;
import com.seagate.kinetic.simulator.persist.Store;

public class P2POperationHandler {

    private final static Logger logger = Logger
            .getLogger(P2POperationHandler.class.getName());

    private P2PConnectionPool pool = null;

    public P2POperationHandler() {
        pool = new P2PConnectionPool();
    }

    public static boolean checkPermission(Message request,
            Message.Builder respond, Map<Long, ACL> currentMap) {
        boolean hasPermission = false;

        // set reply type
        respond.getCommandBuilder().getHeaderBuilder()
        .setMessageType(MessageType.PEER2PEERPUSH_RESPONSE);

        // set ack sequence
        respond.getCommandBuilder().getHeaderBuilder()
        .setAckSequence(request.getCommand().getHeader().getSequence());

        // check if has permission to set security
        if (currentMap == null) {
            hasPermission = true;
        } else {
            try {
                // check if client has permission
                Authorizer.checkPermission(currentMap, request.getCommand()
                        .getHeader().getIdentity(), Permission.P2POP);

                hasPermission = true;
            } catch (KVSecurityException e) {
                respond.getCommandBuilder().getStatusBuilder()
                .setCode(StatusCode.NOT_AUTHORIZED);
                respond.getCommandBuilder().getStatusBuilder()
                .setStatusMessage(e.getMessage());
            }
        }

        return hasPermission;
    }

    public void push (Map<Long, ACL> aclmap,
            Store<ByteString, ByteString, KVValue> store, Message request,
            Message.Builder response) {

        // get client instance
        KineticClient client = this.getClient(request, response);

        /**
         * perform p2p Ops if connected to peer. Otherwise,
         * REMOTE_CONNECTION_ERROR code was set in the status code and return
         * back to the client.
         */
        if (client != null) {

            // get p2p operation list
            P2POperation p2pOp = request.getCommand().getBody().getP2POperation();
            List<Operation> opList = p2pOp.getOperationList();

            // response operation builder
            P2POperation.Builder respP2POpBuilder = response.getCommandBuilder()
                    .getBodyBuilder()
                    .getP2POperationBuilder();

            // loop through the list
            for (Operation operation : opList) {

                // response op builder
                Operation.Builder respOpBuilder = Operation.newBuilder(operation);

                try {

                    // get entry from store
                    KVValue kvvalue = store.get(operation.getKey());

                    if (kvvalue == null) {
                        throw new KVStoreNotFound();
                    }

                    // construct entry to be pushed to peer
                    Entry entry = new Entry();

                    // set key
                    if (operation.hasNewKey()) {
                        // use new key
                        entry.setKey(operation.getNewKey().toByteArray());
                    } else {
                        // use the same key as stored
                        entry.setKey(kvvalue.getKeyOf().toByteArray());
                    }

                    // set value
                    entry.setValue(kvvalue.getData().toByteArray());

                    // set version, if any
                    if (kvvalue.hasVersion()) {
                        entry.getEntryMetadata().setVersion(
                                kvvalue.getVersion().toByteArray());
                    }

                    // set tag
                    if (kvvalue.hasTag()) {
                        entry.getEntryMetadata().setTag(
                                kvvalue.getTag().toByteArray());
                    }

                    if (operation.getForce()) {
                        // forced put ignore version
                        client.putForced(entry);
                    } else {
                        // if there is a version specified in op, use versioned put
                        if (operation.hasVersion()) {

                            // set db version
                            entry.getEntryMetadata().setVersion(
                                    operation.getVersion().toByteArray());

                            // use store version as new version
                            // do versioned put
                            client.put(entry, kvvalue.getVersion().toByteArray());
                        } else {
                            // do forced put
                            client.putForced(entry);
                        }
                    }

                    // set success status
                    respOpBuilder.getStatusBuilder().setCode(StatusCode.SUCCESS);
                } catch (KVStoreNotFound kvne) {

                    logger.warning("cannot find entry from the specified key in request message...");

                    respOpBuilder.getStatusBuilder().setCode(StatusCode.NOT_FOUND);

                    respOpBuilder.getStatusBuilder().setDetailedMessage(
                            ByteString
                            .copyFromUtf8("cannot find the specified key"));

                } catch (Exception e) {

                    logger.log(Level.WARNING, e.getMessage(), e);

                    respOpBuilder.getStatusBuilder().setCode(
                            StatusCode.INTERNAL_ERROR);

                    if (e.getMessage() != null) {
                        respOpBuilder.getStatusBuilder().setDetailedMessage(
                                ByteString.copyFromUtf8(e.getMessage()));
                    }
                }

                // add response operation
                respP2POpBuilder.addOperation(respOpBuilder.build());
            }
        }
    }

    /**
     * Get peer instance.
     *
     * @param request
     *            request message
     * @param response
     *            response message builder
     * @return client instance if created and cached. Return null if any error
     *         occurred.
     */
    private KineticClient getClient(Message request, Message.Builder response) {

        KineticClient client = null;

        try {
            client = this.pool.getKineticClient(request);
        } catch (Exception e) {

            // log message
            logger.log(Level.WARNING, e.getMessage(), e);

            // set status
            response.getCommandBuilder().getStatusBuilder()
            .setCode(StatusCode.REMOTE_CONNECTION_ERROR);

            // set status message
            if (e.getMessage() != null) {
                response.getCommandBuilder().getStatusBuilder()
                .setStatusMessage(e.getMessage());
            }
        }

        return client;
    }

    /**
     * close connection pool.
     */
    public void close() {
        // close pool
        this.pool.close();
    }

}
