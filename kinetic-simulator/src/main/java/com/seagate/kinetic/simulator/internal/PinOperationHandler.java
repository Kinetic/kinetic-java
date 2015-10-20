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

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import kinetic.client.KineticException;

import com.google.protobuf.ByteString;
import com.seagate.kinetic.common.lib.KineticMessage;
import com.seagate.kinetic.proto.Kinetic.Command;
import com.seagate.kinetic.proto.Kinetic.Command.PinOperation.PinOpType;
import com.seagate.kinetic.proto.Kinetic.Command.MessageType;
import com.seagate.kinetic.proto.Kinetic.Command.Setup;
import com.seagate.kinetic.proto.Kinetic.Command.Status.StatusCode;
import com.seagate.kinetic.proto.Kinetic.Message.AuthType;
import com.seagate.kinetic.proto.Kinetic.Message;

/**
 * 
 * Pin operation handler.
 *
 */
public abstract class PinOperationHandler {
    
    private final static Logger logger = Logger.getLogger(PinOperationHandler.class
            .getName());

    public static void handleOperation(KineticMessage request,
            KineticMessage respond, SimulatorEngine engine)
            throws KVStoreException, KineticException {

        Message.Builder messageBuilder = (Message.Builder) respond.getMessage();
        // set pin auth
        messageBuilder.setAuthType(AuthType.PINAUTH);

        Command.Builder commandBuilder = (Command.Builder) respond.getCommand();

        // set reply type
        commandBuilder.getHeaderBuilder().setMessageType(
                MessageType.PINOP_RESPONSE);

        // set ack sequence
        commandBuilder.getHeaderBuilder().setAckSequence(
                request.getCommand().getHeader().getSequence());

        // request pin
        ByteString requestPin = request.getMessage().getPinAuth().getPin();

        // request pin op type
        PinOpType pinOpType = request.getCommand().getBody().getPinOp()
                .getPinOpType();

        try {

            // check if met TLS requirement
            checkSecureChannel(request);

            switch (pinOpType) {
            case LOCK_PINOP:
                
                // check if not empty
                checkRequestPin (requestPin);
                
                // check if has permission
                comparePin(requestPin, engine.getSecurityPin().getLockPin());

                // lock device
                engine.setDeviceLocked(true);
                
                logger.info("Device locked ...");

                break;
            case UNLOCK_PINOP:
                 
                // check if not empty
                checkRequestPin (requestPin);
                
                // check if has permission
                comparePin(requestPin, engine.getSecurityPin().getLockPin());
                
                // unlock device
                engine.setDeviceLocked(false);

                logger.info("Device unlocked ...");

                break;
            case ERASE_PINOP:
                // Both erase operations will return
                // the device to an as manufactured state removing all
                // user data and configuration settings.
                // Erase the device. This may be secure
                // or not. The implication is that it may be faster
                // than the secure operation.

                comparePin(requestPin, engine.getSecurityPin().getErasePin());
                
                // do erase
                doErase (engine);
                break;
            case SECURE_ERASE_PINOP:
                // Erase the device in a way that will
                // physical access and disassembly of the device
                // will not
                comparePin(requestPin, engine.getSecurityPin().getErasePin());
                
                // do erase
                doErase (engine);
                break;
            case INVALID_PINOP:
                throw new InvalidRequestException("Invalid Pin Op Type: "
                        + pinOpType);
            default:
                throw new InvalidRequestException("Invalid Pin Op Type: "
                        + pinOpType);

            }

        } catch (KVSecurityException se) {
            commandBuilder.getStatusBuilder()
                    .setCode(StatusCode.NOT_AUTHORIZED);
            commandBuilder.getStatusBuilder().setStatusMessage(se.getMessage());
            logger.warning("unauthorized pin opeartion request");
        } catch (InvalidRequestException ire) {
            commandBuilder.getStatusBuilder().setCode(
                    StatusCode.INVALID_REQUEST);
            commandBuilder.getStatusBuilder()
                    .setStatusMessage(ire.getMessage());
        }

    }
    
    /**
     * Perform secure erase pin operation.
     * 
     * @param engine
     * @throws KVStoreException
     * @throws KineticException
     */
    public static void doErase(SimulatorEngine engine) throws KVStoreException,
            KineticException {
        // reset store
        engine.getStore().reset();

        // reset setup
        resetSetup(engine);

        // reset security
        SecurityHandler.resetSecurity(engine);
    }
    
    private static void resetSetup(SimulatorEngine engine) {
        Setup.Builder sb = Setup.newBuilder();
        sb.setNewClusterVersion(0);
        try {
            SetupHandler.persistSetup(sb.build().toByteArray(), engine.getKineticHome());
            engine.setClusterVersion(0);
        } catch (IOException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
        }
    }
    
    /**
     * compare if request pin is equal to the device pin.
     * 
     * @param requestPin pin in the request message
     * @param devicePin device pin.
     * @return true if the same, otherwise return false
     */
    private static void comparePin (ByteString requestPin, ByteString devicePin) throws KVSecurityException {
        
        /**
         * if not set, simply returns.
         */
        if (devicePin == null || devicePin.isEmpty()) {
            return;
        }
        
        /**
         * compare if pins are equal.
         */
        if (devicePin.equals(requestPin) == false) {
            throw new KVSecurityException ("pin does not match., requestPin=" + requestPin);
        }
    }
    
    /**
     * Check if the request op is under TLS channel.
     * 
     * @param request
     * @throws InvalidRequestException
     */
    private static void checkSecureChannel(KineticMessage request)
            throws InvalidRequestException {

        boolean hasPermission = request.getIsSecureChannel();

        if (hasPermission == false) {
            throw new InvalidRequestException(
                    "TLS channel is required for Pin operation");
        }

    }
    
    /**
     * Check if the pin is not null or empty.
     * 
     * @param pin pin to be validated.
     * 
     * @throws InvalidRequestException if pin is null or empty.
     */
    private static void checkRequestPin (ByteString pin) throws InvalidRequestException {
        
        if (pin.isEmpty()) {
            throw new InvalidRequestException ("Pin cannot be empty");
        }
    }  
    
}
