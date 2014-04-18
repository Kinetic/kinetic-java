/** Do NOT modify or remove this copyright and confidentiality notice!
 *
 * Copyright (c) 2001 - $Date: 2012/06/27 $ Seagate Technology, LLC.
 *
 * The code contained herein is CONFIDENTIAL to Seagate Technology, LLC.
 * Portions are also trade secret. Any use, duplication, derivation, distribution
 * or disclosure of this code, for any reason, not expressly authorized is
 * prohibited. All other rights are expressly reserved by Seagate Technology, LLC.
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
	 * 
	 * @param p2pOperation
	 *            specification to perform peer to peer operation.
	 * 
	 * @return <code>PeerToPeerOperation</code> that contains status for each
	 *         P2P operation.
	 * 
	 * @throws KineticException
	 *             if any internal error occurred.
	 */
	public PeerToPeerOperation PeerToPeerPush(PeerToPeerOperation p2pOperation)
			throws KineticException;

}
