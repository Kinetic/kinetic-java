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

import java.util.logging.Logger;

import kinetic.client.KineticException;

import com.google.protobuf.ByteString;
import com.seagate.kinetic.common.lib.KineticMessage;
import com.seagate.kinetic.proto.Kinetic.Command;
import com.seagate.kinetic.proto.Kinetic.Command.MessageType;
import com.seagate.kinetic.proto.Kinetic.Command.Security.ACL.Permission;
import com.seagate.kinetic.proto.Kinetic.Command.Status.StatusCode;

/**
 * 
 * Back ground operation handler prototype implementation
 * <p>
 * The current implementation responds to the request with a SUCCESS status if it passed
 * minimal verifications.
 * 
 * @author chiaming
 *
 */
public abstract class BackGroundOpHandler {
    
    private final static Logger logger = Logger.getLogger(BackGroundOpHandler.class
            .getName());
    
    public static void mediaScan(KineticMessage request,
            KineticMessage respond, SimulatorEngine engine)
            throws KVStoreException, KineticException {

        Command.Builder commandBuilder = (Command.Builder) respond.getCommand();

        // set reply type
        commandBuilder.getHeaderBuilder().setMessageType(
                MessageType.MEDIASCAN_RESPONSE);

        // set ack sequence
        commandBuilder.getHeaderBuilder().setAckSequence(
                request.getCommand().getHeader().getSequence());

        try {
            
            checkIsMessageValid (request);
            
            // check permission
            checkPermission (request, engine);  
            
            /**
             *  XXX 09/09/2014 chiaming:
             *  framework to start background operation
             *  the job should be stoppable by a higher priority received
             *  before/after the long running bg ops.
             *  
             *  The following statements are for testing purpose only
             */
            
            // set endkey in response
            commandBuilder
                    .getBodyBuilder()
                    .getRangeBuilder()
                    .setEndKey(
                            request.getCommand().getBody().getRange()
                                    .getEndKey());

        } catch (KVSecurityException se) {
            commandBuilder.getStatusBuilder()
                    .setCode(StatusCode.NOT_AUTHORIZED);
            commandBuilder.getStatusBuilder().setStatusMessage(se.getMessage());
            logger.warning("unauthorized media scan opeartion request");
        } catch (InvalidRequestException ire) {
            commandBuilder.getStatusBuilder().setCode(
                    StatusCode.INVALID_REQUEST);
            commandBuilder.getStatusBuilder()
                    .setStatusMessage(ire.getMessage());
        }
    }
    
    @SuppressWarnings("unchecked")
    public static void mediaOptimize(KineticMessage request,
            KineticMessage respond, SimulatorEngine engine)
            throws KVStoreException, KineticException {

        Command.Builder commandBuilder = (Command.Builder) respond.getCommand();

        // set reply type
        commandBuilder.getHeaderBuilder().setMessageType(
                MessageType.MEDIAOPTIMIZE_RESPONSE);

        // set ack sequence
        commandBuilder.getHeaderBuilder().setAckSequence(
                request.getCommand().getHeader().getSequence());

        try {
            
            checkIsMessageValid (request);
            
            // check permission
            checkPermission (request, engine);  
            
            /**
             *  XXX 09/11/2014 chiaming:
             *  framework to start media optimize op
             *  the job should be stoppable by a higher priority received
             *  before/after the long running bg ops.
             *  
             *  The following statements are for testing purpose only
             */
            
            // get start key
            ByteString startKey = request.getCommand().getBody().getRange()
                    .getStartKey();

            // get end key
            ByteString endKey = request.getCommand().getBody().getRange()
                    .getEndKey();

            // ask store to do media compaction
            engine.getStore().compactRange(startKey, endKey);

            // set endkey in response
            commandBuilder
                    .getBodyBuilder()
                    .getRangeBuilder()
                    .setEndKey(
                            request.getCommand().getBody().getRange()
                                    .getEndKey());

        } catch (KVSecurityException se) {
            commandBuilder.getStatusBuilder()
                    .setCode(StatusCode.NOT_AUTHORIZED);
            commandBuilder.getStatusBuilder().setStatusMessage(se.getMessage());
            logger.warning("unauthorized media optimize opeartion request");
        } catch (InvalidRequestException ire) {
            commandBuilder.getStatusBuilder().setCode(
                    StatusCode.INVALID_REQUEST);
            commandBuilder.getStatusBuilder()
                    .setStatusMessage(ire.getMessage());
        }
    }
    
    private static void checkIsMessageValid (KineticMessage request) throws InvalidRequestException {
        
        MessageType mtype = request.getCommand().getHeader().getMessageType();
        
        switch (mtype) {
        case MEDIASCAN:
        case MEDIAOPTIMIZE:
            // XXX: more request message validation here
            return;
        default:
            throw new InvalidRequestException ("not a valid back ground op type: " + mtype.name());
        }
        
    }
    
    private static void checkPermission (KineticMessage request,
            SimulatorEngine engine) throws KVSecurityException {
        
        // check if client has permission
        Authorizer.checkPermission(engine.getAclMap(), request.getMessage().getHmacAuth().getIdentity(), 
                Permission.RANGE);
    }

}
