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

import com.seagate.kinetic.common.lib.KineticMessage;
import com.seagate.kinetic.proto.Kinetic.Command;
import com.seagate.kinetic.proto.Kinetic.Command.BackgroundOperation;
import com.seagate.kinetic.proto.Kinetic.Command.BackgroundOperation.BackOpType;

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
    
    private final static Logger logger = Logger.getLogger(BackgroundOperation.class
            .getName());

    public static void handleOperation(KineticMessage request,
            KineticMessage respond, SimulatorEngine engine)
            throws KVStoreException, KineticException {

        Command.Builder commandBuilder = (Command.Builder) respond.getCommand();

        // set reply type
        commandBuilder.getHeaderBuilder().setMessageType(
                MessageType.BACKOP_RESPONSE);

        // set ack sequence
        commandBuilder.getHeaderBuilder().setAckSequence(
                request.getCommand().getHeader().getSequence());

        try {
            
            //check if message is valid
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
            
            // set response back op type
            commandBuilder
                    .getBodyBuilder()
                    .getBackgroundOperationBuilder()
                    .setBackOpType(
                            request.getCommand().getBody()
                                    .getBackgroundOperation().getBackOpType());
            
            // set endkey in response
            commandBuilder
                    .getBodyBuilder()
                    .getBackgroundOperationBuilder()
                    .getRangeBuilder()
                    .setEndKey(
                            request.getCommand().getBody()
                                    .getBackgroundOperation().getRange()
                                    .getEndKey());

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
    
    private static void checkIsMessageValid (KineticMessage request) throws InvalidRequestException {
        
        BackgroundOperation bgo = request.getCommand().getBody().getBackgroundOperation();
        BackOpType boType = bgo.getBackOpType();
        
        switch (boType) {
        case MEDIASCAN:
        case MEDIAOPTIMIZE:
            // XXX: more request message validation here
            return;
        default:
            throw new InvalidRequestException ("not a valid back ground op type: " + boType.name());
        }
        
    }
    
    private static void checkPermission (KineticMessage request,
            SimulatorEngine engine) throws KVSecurityException {
        
        // check if client has permission
        Authorizer.checkPermission(engine.getAclMap(), request.getMessage().getHmacAuth().getIdentity(), 
                Permission.RANGE);
    }

}
