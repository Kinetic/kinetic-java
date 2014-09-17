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

import com.seagate.kinetic.common.lib.KineticMessage;

/**
 * A stateful message is a Kinetic Message that contains a connection Id.
 * 
 * @author chiaming
 *
 */
public class StatefulMessage extends KineticMessage {
    
    // connection Id
    private long connectionId = -1;
    
    public StatefulMessage() {
        ;
    }
    
    /**
     * Construct a stateful message with the specified message.
     * 
     * @param km Kinetic Message based on which the stateful message is constructed.
     * 
     */
    public StatefulMessage(KineticMessage km) {
        //set message
        super.setMessage(km.getMessage());
        
        //set value
        super.setValue(km.getValue());
        
        //set command
        super.setCommand(km.getCommand());
    }
    
    /**
     * Set connection Id.
     * 
     * @param cid the connection Id to be set to this message.
     */
    public void setConnectionId (long cid) {
        this.connectionId = cid;
    }
    
    /**
     * Get connection Id of this message.
     * 
     * @return connection Id
     */
    public long getConnectionId() {
        return this.connectionId;
    }
}
