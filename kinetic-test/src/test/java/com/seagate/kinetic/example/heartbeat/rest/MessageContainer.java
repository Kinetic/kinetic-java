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
package com.seagate.kinetic.example.heartbeat.rest;

import com.seagate.kinetic.heartbeat.HeartbeatMessage;

/**
 * 
 * Container to hold heartbeat message and the timestamp put in the heartbeat table.
 * 
 * @author chiaming
 *
 */
public class MessageContainer {
    
    private HeartbeatMessage hbm = null;
    
    private long timestamp = 0;

    public MessageContainer(HeartbeatMessage hbm, long timestamp) {
        this.hbm = hbm;
        this.timestamp = timestamp;
    }
    
    public HeartbeatMessage getHeartbeatMessage() {
        return this.hbm;
    }
    
    public long getTimestamp() {
        return this.timestamp;
    }
}
