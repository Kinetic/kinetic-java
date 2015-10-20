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

import java.util.logging.Logger;

import com.seagate.kinetic.common.lib.KineticMessage;

/**
 * Container to hold connection id and its status of being set to the client.
 * 
 * @author chiaming
 */
public class ConnectionInfo {

    private final static Logger logger = Logger.getLogger(SimulatorEngine.class
            .getName());

    private long connectionId = -1;
    
    private boolean isConnectionIdSetToClient = false;
    
    // last received seq#
    private long lastSequenceReceived = Long.MIN_VALUE;

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

    /**
     * check and set received sequence number. The sequence number received must
     * be greater than previous in a connection.
     * <p>
     * The internal sequence# is set to the sequence received if the comparison
     * is true.
     * 
     * @param sequence
     *            the last sequence received
     * @return true if last sequence received is greater than previous.
     */
    public synchronized boolean checkAndSetLastReceivedSequence(
            KineticMessage request) {

        boolean flag = false;

        // request sequence
        long sequence = request.getCommand().getHeader().getSequence();

        /**
         * update last received if there is a new one and set return flag to
         * true.
         */
        if (sequence > lastSequenceReceived) {
            this.lastSequenceReceived = sequence;
            flag = true;
        } else {
            // mark this message as invalid
            request.setIsInvalidRequest(true);
            request.setErrorMessage("Invalid Sequence Id: " + sequence);

            logger.warning("invalid sequence Id: " + sequence
                    + ", lastSequenceReceived: " + lastSequenceReceived);
        }

        return flag;
    }
}
