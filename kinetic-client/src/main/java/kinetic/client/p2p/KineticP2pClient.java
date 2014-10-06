/**
 * Copyright (C) 2014 Seagate Technology.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
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
