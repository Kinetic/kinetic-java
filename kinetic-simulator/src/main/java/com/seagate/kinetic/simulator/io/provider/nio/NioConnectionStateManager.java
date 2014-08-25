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

import java.util.logging.Logger;

import kinetic.simulator.SimulatorConfiguration;

import com.seagate.kinetic.common.lib.KineticMessage;
import com.seagate.kinetic.simulator.internal.ConnectionInfo;
import com.seagate.kinetic.simulator.internal.SimulatorEngine;

import io.netty.channel.ChannelHandlerContext;

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
