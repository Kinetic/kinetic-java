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

/**
 * Container to hold connection id and its status of being set to the client.
 * 
 * @author chiaming
 */
public class ConnectionInfo {

    private long connectionId = -1;
    
    private boolean isConnectionIdSetToClient = false;
    
    /**
     * default constructor.
     */
    public ConnectionInfo() {
        // TODO Auto-generated constructor stub
    }
    
    /**
     * set connection Id.
     * 
     * @param cid the connection id association to its connection.
     */
    public synchronized void setConnectionId (long cid) {
        this.connectionId = cid;
    }
    
    /**
     * Get the connection Id.
     * 
     * @return connection Id.
     */
    public synchronized long getConnectionId() {
        return this.connectionId;
    }
    
    /**
     * Set if the connection Id has been set to the client.
     * 
     * @param flag true if the Id is set to the client.
     */
    public synchronized void setIsConnectionIdSetToClient (boolean flag) {
        this.isConnectionIdSetToClient = flag;
    }
    
    /**
     * Get if the connection Id is set to the client flag.
     * @return true if set to the client.  Otherwise, returns false.
     */
    public synchronized boolean getIsConnectionIdSetToClient() {
        return this.isConnectionIdSetToClient;
    }
}
