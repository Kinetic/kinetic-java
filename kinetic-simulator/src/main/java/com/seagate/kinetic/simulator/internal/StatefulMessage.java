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
