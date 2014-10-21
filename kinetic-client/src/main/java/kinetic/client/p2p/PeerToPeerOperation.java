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

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * Peer to peer operation request/response specification API.
 * <p>
 * A Kinetic peer to peer client specifies its peer with
 * <code>{@link #setPeer(Peer)}</code> and its operations with
 * <code>{@link #addOperation(Operation)}</code> methods.
 * <p>
 * The current connected host is the communication party (peer one) with the
 * peer (peer two) specified with {@link #setPeer(Peer)} API.
 * <p>
 * The overall response status <code>{@link #getStatus()}</code> is set by the
 * Kinetic service based on the result of all operations.
 * <p>
 * Each individual operation status is set in the corresponding
 * <code>Operation</code> added with the
 * <code>{@link #addOperation(Operation)}</code> method.
 * 
 * @see Operation
 * @see Peer
 * 
 * @author chiaming
 * 
 */
public class PeerToPeerOperation {

	// peer info
	private Peer peer = null;

	// operation list
	private final List<Operation> opList = new ArrayList<Operation>();

	// overall status, set by the simulator in response
	private boolean status = true;

	// overall message
	private String errorMessage = null;

	/**
	 * Set peer for the peer to peer operation.
	 * 
	 * @param peer
	 *            peer host info.
	 */
	public void setPeer(Peer peer) {
		this.peer = peer;
	}

	/**
	 * Get peer host info.
	 * 
	 * @return peer host info.
	 */
	public Peer getPeer() {
		return this.peer;
	}

	/**
	 * Add an operation to the operation list.
	 * 
	 * @param op
	 *            operation to be added to the operation list.
	 */
	public void addOperation(Operation op) {
		this.opList.add(op);
	}

	/**
	 * Get operation list for this P2P operation.
	 * 
	 * @return operation list for this P2P operation.
	 */
	public List<Operation> getOperationList() {
		return this.opList;
	}

	/**
	 * Set overall status for the P2P operation. This is set by the Kinetic
	 * service.
	 * 
	 * @param status
	 *            overall status for the P2P operation.
	 */
	public void setStatus(boolean status) {
		this.status = status;
	}

	/**
	 * Get overall status for the P2P operation.
	 * <p>
	 * Each operation's status can be obtained with
	 * {@link Operation#getStatus()} API.
	 * <p>
	 * 
	 * @return true if all operations are succeeded. Otherwise, return false.
	 */
	public boolean getStatus() {
		return this.status;
	}

	/**
	 * Set overall error message. Set by the Kinetic service.
	 * 
	 * @param msg
	 *            the error message, if any.
	 */
	public void setErrorMessage(String msg) {
		this.errorMessage = msg;
	}

	/**
	 * Get error message for this P2P operation.
	 * 
	 * @return error message for this P2P operation. Return null if there is no
	 *         error occurred.
	 */
	public String getErrorMessage() {
		return this.errorMessage;
	}
}
