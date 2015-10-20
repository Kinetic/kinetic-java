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

import java.util.logging.Logger;

import kinetic.simulator.SimulatorConfiguration;

import com.seagate.kinetic.common.lib.KineticMessage;
import com.seagate.kinetic.simulator.internal.ConnectionInfo;
import com.seagate.kinetic.simulator.internal.SimulatorEngine;

/**
 * Nio connection manager utility.
 * 
 * @author chiaming
 *
 */
public class NioConnectionStateManager {
    
    private static final Logger logger = Logger
            .getLogger(NioConnectionStateManager.class.getName());

    /**
     * Get a stateful message from the specified channel handler context and message.
     * 
     * @param ctx channel handler context
     * @param request request message from client
     * 
     * @return A stateful message that contains a connection Id to be set in the response message.  
     *          Otherwise return null if connection Id has already set in a response message for this connection.
     *          
     * @throws RuntimeException if connection has already set by the simulator but received a connection Id 
     *         does not match. 
     */
    public static void checkIfConnectionIdSet(ChannelHandlerContext ctx,
            KineticMessage request) {

        // get connection info for this channel
        ConnectionInfo cinfo = SimulatorEngine.getConnectionInfo(ctx);
        
        // check sequence
        cinfo.checkAndSetLastReceivedSequence(request);

        if (cinfo.getConnectionId() != request.getCommand().getHeader().getConnectionID()) {
            
            logger.warning ("expect connection Id="
                    + cinfo.getConnectionId()
                    + ", received request message connection Id="
                    + request.getCommand().getHeader()
                            .getConnectionID());
            
            if (SimulatorConfiguration.getIsConnectionIdCheckEnforced()) {
                throw new RuntimeException("expect CID="
                        + cinfo.getConnectionId()
                        + " , but received CID="
                        + request.getCommand().getHeader()
                                .getConnectionID());
            }
        }
    }

}
