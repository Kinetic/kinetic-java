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
package com.seagate.kinetic.client.internal.p2p;

import java.util.List;
import java.util.logging.Logger;

import kinetic.client.ClientConfiguration;
import kinetic.client.KineticException;
import kinetic.client.p2p.KineticP2pClient;
import kinetic.client.p2p.Operation;
import kinetic.client.p2p.PeerToPeerOperation;

import com.google.protobuf.ByteString;
import com.seagate.kinetic.client.internal.DefaultKineticClient;
import com.seagate.kinetic.client.internal.MessageFactory;
import com.seagate.kinetic.client.lib.ClientLogger;
import com.seagate.kinetic.common.lib.KineticMessage;
import com.seagate.kinetic.proto.Kinetic.Command;
import com.seagate.kinetic.proto.Kinetic.Command.MessageType;
import com.seagate.kinetic.proto.Kinetic.Command.P2POperation;
import com.seagate.kinetic.proto.Kinetic.Command.Status.StatusCode;

/**
 *
 * Kinetic peer to peer client implementation.
 *
 * @author chiaming
 *
 */
public class DefaultKineticP2pClient extends DefaultKineticClient implements
        KineticP2pClient {

    private final static Logger LOG = ClientLogger.get();

    public DefaultKineticP2pClient(ClientConfiguration config)
            throws KineticException {
        super(config);
    }

    /**
     * P2P push operation raw API.
     *
     * @param request
     *            peer to peer push request message.
     *
     * @return peer to peer push response message.
     *
     * @throws KineticException
     *             if any internal error occurred.
     */
    public KineticMessage PeerToPeerPush(KineticMessage request)
            throws KineticException {
        KineticMessage response = null;

        response = request(request);

        return response;
    }

    /**
     * Peer to peer push application API (prototype).
     *
     * @param p2pOperation
     *            object that hold p2p operation information.
     * @return the same instance of p2pOperation as the specified parameter with
     *         status set by the simulator/drive.
     *
     * @throws KineticException
     *             any internal error occurred.
     */
    @Override
    public PeerToPeerOperation PeerToPeerPush(PeerToPeerOperation p2pOperation)
            throws KineticException {

        KineticMessage km = MessageFactory.createKineticMessageWithBuilder();

        // create request message.
        Command.Builder commandBuilder = (Command.Builder) km.getCommand();

        // set request type
        commandBuilder.getHeaderBuilder().setMessageType(
                MessageType.PEER2PEERPUSH);

        // p2p builder
        P2POperation.Builder p2pBuilder = commandBuilder.getBodyBuilder()
                .getP2POperationBuilder();

        // set peer hots/port/tls
        p2pBuilder.getPeerBuilder().setHostname(
                p2pOperation.getPeer().getHost());
        p2pBuilder.getPeerBuilder().setPort(p2pOperation.getPeer().getPort());
        p2pBuilder.getPeerBuilder().setTls(p2pOperation.getPeer().getUseTls());

        // set operation list
        List<Operation> operationList = p2pOperation.getOperationList();

        for (Operation op : operationList) {

            // operation builder
            Command.P2POperation.Operation.Builder operationBuilder = Command.P2POperation.Operation
                    .newBuilder();

            // set force flag
            operationBuilder.setForce(op.getForced());

            // set key
            operationBuilder.setKey(ByteString.copyFrom(op.getKey()));

            // set new key
            if (op.getNewKey() != null) {
                operationBuilder.setNewKey(ByteString.copyFrom(op.getNewKey()));
            }

            // set version
            if (op.getVersion() != null) {
                operationBuilder
                        .setVersion(ByteString.copyFrom(op.getVersion()));
            }

            // add operation to list
            p2pBuilder.addOperation(operationBuilder.build());
        }

        // do request
        KineticMessage response = PeerToPeerPush(km);

        // get p2p op response
        P2POperation peerToPeerOperationResponse = response.getCommand()
                .getBody().getP2POperation();

        StatusCode respScode = response.getCommand().getStatus().getCode();

        /**
         * throws KineticException if overall status failed.
         */
        if (respScode != StatusCode.SUCCESS) {

            // get status message
            String msg = response.getCommand().getStatus().getStatusMessage();

            // get exception message from response
            String emsg = (msg == null) ? "Internal error for P2P ops" : msg;

            // construct and throw exception
            KineticException ke = new KineticException(emsg);

            // set request message
            ke.setRequestMessage(km);

            // set response message
            ke.setResponseMessage(response);

            // log warning message
            LOG.warning("p2p op failed, status code: " + respScode + ", msg: "
                    + emsg);

            throw ke;
        }

        // set overall status and message
        // p2pOperation
        // .setStatus(response.getCommand().getStatus().getCode() ==
        // StatusCode.SUCCESS);

        // set overall status
        p2pOperation.setStatus(peerToPeerOperationResponse
                .getAllChildOperationsSucceeded());

        p2pOperation.setErrorMessage(response.getCommand().getStatus()
                .getStatusMessage());

        // set individual operation status and message
        for (int i = 0; i < operationList.size(); i++) {

            // status code
            StatusCode sc = peerToPeerOperationResponse.getOperation(i)
                    .getStatus().getCode();

            // set status
            operationList.get(i).setStatus(sc == StatusCode.SUCCESS);

            // set status code
            operationList.get(i).setStatusCode(sc);

            // set status message
            operationList.get(i).setErrorMessage(
                    peerToPeerOperationResponse.getOperation(i).getStatus()
                            .getStatusMessage());
        }

        // return p2p operation response
        return p2pOperation;
    }
}
