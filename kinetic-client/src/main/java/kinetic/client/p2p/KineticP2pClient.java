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
package kinetic.client.p2p;

import kinetic.client.KineticException;
import kinetic.client.advanced.AdvancedKineticClient;

/**
 * 
 * Kinetic peer to peer operation client interface.
 * 
 * @author chiaming
 * 
 */
public interface KineticP2pClient extends AdvancedKineticClient {

    /**
     * Perform Peer to Peer push operation.
     * <p>
     * Unless the p2p operation cannot be completed for all the operations, the
     * method does not throw an KineticException. Instead, the overall status is
     * set and can be obtained with {@link PeerToPeerOperation#getStatus()} API.
     * <p>
     * Applications check the overall status and if it is set to false, the
     * individual status can be obtained from
     * {@link PeerToPeerOperation#getOperationList()}.
     * 
     * @param p2pOperation
     *            specification to perform peer to peer operation.
     * 
     * @return <code>PeerToPeerOperation</code> that contains status for each
     *         P2P operation.
     * 
     * @throws KineticException
     *             if any internal error occurred.
     * 
     * @see PeerToPeerOperation
     */
    public PeerToPeerOperation PeerToPeerPush(PeerToPeerOperation p2pOperation)
            throws KineticException;

}
