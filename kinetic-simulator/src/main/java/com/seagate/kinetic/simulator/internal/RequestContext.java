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

import java.security.Key;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.protobuf.ByteString;
import com.seagate.kinetic.common.lib.Hmac;
import com.seagate.kinetic.common.lib.KineticMessage;
import com.seagate.kinetic.proto.Kinetic.Command;
import com.seagate.kinetic.proto.Kinetic.Command.MessageType;
import com.seagate.kinetic.proto.Kinetic.Command.PinOperation.PinOpType;
import com.seagate.kinetic.proto.Kinetic.Command.Status.StatusCode;
import com.seagate.kinetic.proto.Kinetic.Message;
import com.seagate.kinetic.proto.Kinetic.Message.AuthType;
import com.seagate.kinetic.simulator.internal.handler.ServiceException;
import com.seagate.kinetic.simulator.lib.HeaderOp;

/**
 * Request context is a container that encapsulate each request information to
 * process the request command.
 * 
 * @author chiaming
 *
 */
public class RequestContext {
    
    private final static Logger logger = Logger.getLogger(RequestContext.class
            .getName());

    // simulator engine
    private SimulatorEngine engine = null;

    // response message
    private KineticMessage request = null;
    
    // create response message
    private KineticMessage response = null;;
    
    // response command builder
    private Command.Builder commandBuilder = null;;

    // response message builder
    private Message.Builder messageBuilder = null;

    // user identity for this message
    private long userId = -1;

    // get user key
    private Key key = null;;

    private MessageType mtype = null;;

    /**
     * The constructor.
     * 
     * @param engine
     *            simulator engine.
     * 
     * @param request
     *            request message
     */
    public RequestContext(SimulatorEngine engine, KineticMessage request) {
        this.engine = engine;
        this.request = request;

        this.init();
    }

    /**
     * Get request message for this context.
     * 
     * @return request message for this context
     */
    public KineticMessage getRequestMessage() {
        return this.request;
    }

    /**
     * Get response message for this context.
     * 
     * @return response message for this context.
     */
    public KineticMessage getResponseMessage() {
        return this.response;
    }

    /**
     * Get message type for this request.
     * 
     * @return message type for this request
     */
    public MessageType getMessageType() {
        return this.mtype;
    }

    /**
     * Get user Id for this request message.
     * 
     * @return user Id for this request.
     */
    public long getUserId() {
        return this.userId;
    }

    /**
     * Get user key for this request message.
     * 
     * @return user key for this request message.
     */
    public Key getUserKey() {
        return this.key;
    }

    /**
     * Get response command builder for this context.
     * 
     * @return response command builder for this context.
     */
    public Command.Builder getCommandBuilder() {
        return this.commandBuilder;
    }

    /**
     * Get response message builder for this context.
     * 
     * @return response message builder for this context.
     */
    public Message.Builder getMessgeBuilder() {
        return this.messageBuilder;
    }
    
    /**
     * initialize context for this request message.
     * 
     */
    private void init() {

        // create response message
        response = createKineticMessageWithBuilder();

        // get response command builder
        commandBuilder = (Command.Builder) response.getCommand();

        // get response message builder
        messageBuilder = (Message.Builder) response.getMessage();
        
        // get user id for this request
        userId = request.getMessage().getHmacAuth().getIdentity();
        
        // get key for this request
        key = this.engine.getHmacKeyMap().get(Long.valueOf(userId));

        // get message type for this request
        mtype = request.getCommand().getHeader().getMessageType();

        if (request.getIsBatchMessage()) {
            response.setIsBatchMessage(true);
        }
    }
    
    /**
     * Create an internal message with empty builder message.
     *
     * @return an internal message with empty builder message
     */
    public static KineticMessage createKineticMessageWithBuilder() {

        // new instance of internal message
        KineticMessage kineticMessage = new KineticMessage();

        // new builder message
        Message.Builder message = Message.newBuilder();

        // set to im
        kineticMessage.setMessage(message);

        // set hmac auth type
        message.setAuthType(AuthType.HMACAUTH);

        // create command builder
        Command.Builder commandBuilder = Command.newBuilder();

        // set command
        kineticMessage.setCommand(commandBuilder);

        return kineticMessage;
    }

    /**
     * Pre process the request message.
     * 
     * @throws Exception
     *             if any internal error occurred.
     */
    public void preProcessRequest() throws Exception {

        HeaderOp.checkHeader(this.request, this.response, key,
 this.engine);

        checkDeviceLocked();
    }

    /**
     * check if the device is locked.
     * 
     * @param kmreq
     * @param kmresp
     * @throws DeviceLockedException
     */
    private void checkDeviceLocked()
            throws DeviceLockedException {

        if (this.engine.getDeviceLocked() == false) {
            return;
        }

        PinOpType pinOpType = request.getCommand().getBody().getPinOp()
                .getPinOpType();

        if (pinOpType != PinOpType.UNLOCK_PINOP
                && pinOpType != PinOpType.LOCK_PINOP) {

            // set device locked status code
            commandBuilder.getStatusBuilder().setCode(StatusCode.DEVICE_LOCKED);

            // set status message
            commandBuilder.getStatusBuilder().setStatusMessage(
                    "Device is locked");

            throw new DeviceLockedException();
        }

    }

    /**
     * Process the request message within the current context.
     * 
     * @throws ServiceException
     *             if any internal error occurred.
     */
    public void processRequest() throws ServiceException {

        // dispatch to handler to process the request
        this.engine.getCommandManager().getHandler(this.mtype)
                .processRequest(request, response);
    }

    /**
     * Post process the request message.
     */
    public void postProcessRequest() {
        this.finalizeResponseMessage();
    }

    /**
     * Finalize the response message.
     * 
     */
    private void finalizeResponseMessage() {

        try {
            // get command byte stirng
            ByteString commandByteString = commandBuilder.build()
                    .toByteString();

            // get command byte[]
            byte[] commandByte = commandByteString.toByteArray();

            // require Hmac calculation ?
            if (request.getMessage().getAuthType() == AuthType.HMACAUTH) {

                // calculate hmac
                ByteString hmac = Hmac.calc(commandByte, key);

                // set identity
                messageBuilder.getHmacAuthBuilder().setIdentity(userId);

                // set hmac
                messageBuilder.getHmacAuthBuilder().setHmac(hmac);
            }

            // set command bytes
            messageBuilder.setCommandBytes(commandByteString);
        } catch (Exception e) {
            logger.log(Level.WARNING, e.getMessage(), e);
        }
    }

}
