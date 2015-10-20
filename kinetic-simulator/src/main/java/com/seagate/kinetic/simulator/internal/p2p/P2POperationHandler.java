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
package com.seagate.kinetic.simulator.internal.p2p;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import kinetic.client.Entry;
import kinetic.client.KineticClient;
import kinetic.client.KineticException;

import com.google.protobuf.ByteString;
import com.seagate.kinetic.common.lib.KineticMessage;
import com.seagate.kinetic.proto.Kinetic.Command;
import com.seagate.kinetic.proto.Kinetic.Command.MessageType;
import com.seagate.kinetic.proto.Kinetic.Command.P2POperation;
import com.seagate.kinetic.proto.Kinetic.Command.P2POperation.Operation;
import com.seagate.kinetic.proto.Kinetic.Command.Security.ACL;
import com.seagate.kinetic.proto.Kinetic.Command.Security.ACL.Permission;
import com.seagate.kinetic.proto.Kinetic.Command.Status.StatusCode;
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

    public static boolean checkPermission(KineticMessage request,
            KineticMessage respond, Map<Long, ACL> currentMap) {

        boolean hasPermission = false;

        Command.Builder commandBuilder = (Command.Builder) respond.getCommand();

        // set reply type
        commandBuilder.getHeaderBuilder().setMessageType(
                MessageType.PEER2PEERPUSH_RESPONSE);

        // set ack sequence
        commandBuilder.getHeaderBuilder().setAckSequence(
                request.getCommand().getHeader().getSequence());

        // check if has permission to set security
        if (currentMap == null) {
            hasPermission = true;
        } else {
            try {
                // check if client has permission
                Authorizer.checkPermission(currentMap, request.getMessage()
                        .getHmacAuth().getIdentity(), Permission.P2POP);

                hasPermission = true;
            } catch (KVSecurityException e) {
                commandBuilder.getStatusBuilder().setCode(
                        StatusCode.NOT_AUTHORIZED);
                commandBuilder.getStatusBuilder().setStatusMessage(
                        e.getMessage());
            }
        }

        return hasPermission;
    }

    public void push(Map<Long, ACL> aclmap,
            Store<ByteString, ByteString, KVValue> store,
            KineticMessage request, KineticMessage response) {

        // get client instance
        KineticClient client = this.getClient(request, response);

        try {
            Command.Builder commandBuilder = (Command.Builder) response
                    .getCommand();

            /**
             * perform p2p Ops if connected to peer. Otherwise,
             * REMOTE_CONNECTION_ERROR code was set in the status code and
             * return back to the client.
             */
            if (client != null) {

                // get p2p operation list
                P2POperation p2pOp = request.getCommand().getBody()
                        .getP2POperation();
                List<Operation> opList = p2pOp.getOperationList();

                // response operation builder
                P2POperation.Builder respP2POpBuilder = commandBuilder
                        .getBodyBuilder().getP2POperationBuilder();

                // set default value to true.
                // this will set to false when exception occurred
                respP2POpBuilder.setAllChildOperationsSucceeded(true);

                // loop through the list
                for (Operation operation : opList) {

                    // response op builder
                    Operation.Builder respOpBuilder = Operation
                            .newBuilder(operation);

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
                            // if there is a version specified in op, use
                            // versioned put
                            if (operation.hasVersion()) {

                                // set db version
                                entry.getEntryMetadata().setVersion(
                                        operation.getVersion().toByteArray());

                                // use store version as new version
                                // do versioned put
                                client.put(entry, kvvalue.getVersion()
                                        .toByteArray());
                            } else {
                                // do forced put
                                client.putForced(entry);
                            }
                        }

                        // set success status
                        respOpBuilder.getStatusBuilder().setCode(
                                StatusCode.SUCCESS);
                    } catch (KVStoreNotFound kvne) {

                        logger.warning("cannot find entry from the specified key in request message...");

                        // set overall status
                        respP2POpBuilder.setAllChildOperationsSucceeded(false);

                        /**
                         * The (command) response code is set to OK even if
                         * exception occurred. The application can examine each
                         * of the operation status in the p2p response.
                         */

                        // set individual status code
                        respOpBuilder.getStatusBuilder().setCode(
                                StatusCode.NOT_FOUND);

                        // set individual status message
                        respOpBuilder.getStatusBuilder().setStatusMessage(
                                "cannot find the specified key");

                    } catch (KineticException ke) {

                        /**
                         * errors occurred from remote peer
                         */

                        logger.warning(ke.getLocalizedMessage());

                        // set overall status
                        respP2POpBuilder.setAllChildOperationsSucceeded(false);

                        /**
                         * The (command) response code is set to OK even if
                         * exception occurred. The application can examine each
                         * of the operation status in the p2p response.
                         */

                        // set individual status code
                        respOpBuilder.getStatusBuilder().setCode(
                                ke.getResponseMessage().getCommand()
                                        .getStatus().getCode());

                        // set individual status message
                        String sm = ke.getResponseMessage().getCommand()
                                .getStatus().getStatusMessage();

                        if (sm != null) {
                            respOpBuilder.getStatusBuilder().setStatusMessage(
                                    sm);
                        }

                    } catch (Exception e) {

                        logger.log(Level.WARNING, e.getMessage(), e);

                        // set p2p overall status
                        respP2POpBuilder.setAllChildOperationsSucceeded(false);

                        // set individual status code
                        respOpBuilder.getStatusBuilder().setCode(
                                StatusCode.INTERNAL_ERROR);

                        // set individual status message
                        if (e.getMessage() != null) {
                            respOpBuilder.getStatusBuilder().setStatusMessage(
                                    e.getMessage());
                        }
                    } finally {
                        // add response operation
                        respP2POpBuilder.addOperation(respOpBuilder.build());
                    }
                }
            }

        } finally {
            this.close(client);
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
    private KineticClient getClient(KineticMessage request,
            KineticMessage response) {

        KineticClient client = null;

        Command.Builder commandBuilder = (Command.Builder) response
                .getCommand();

        try {
            client = this.pool.getKineticClient(request);
        } catch (Exception e) {

            // log message
            logger.log(Level.WARNING, e.getMessage(), e);

            // set status
            commandBuilder.getStatusBuilder().setCode(
                    StatusCode.REMOTE_CONNECTION_ERROR);

            // set status message
            if (e.getMessage() != null) {
                commandBuilder.getStatusBuilder().setStatusMessage(
                        e.getMessage());
            }
        }

        return client;
    }

    private void close(KineticClient client) {

        if (client == null) {
            return;
        }

        try {
            client.close();
        } catch (Exception e) {
            logger.warning(e.getMessage());
        }
    }

    /**
     * close connection pool.
     */
    public void close() {
        // close pool
        this.pool.close();
    }

}
