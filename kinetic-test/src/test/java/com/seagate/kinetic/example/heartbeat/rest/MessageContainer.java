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
