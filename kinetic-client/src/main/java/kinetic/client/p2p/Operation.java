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

/**
 * 
 * Kinetic peer to peer push operation specification.
 * <p>
 * The connected host service performs a PUT operation to the peer for the entry
 * found with the specified key.
 * 
 * @author chiaming
 * 
 */
public class Operation {

	// forced flag
	private boolean forced = false;

	// key for the entry
	private byte[] key = null;

	// new key for the pushed entry
	private byte[] newKey = null;

	// db version for the pushed entry
	private byte[] dbVersion = null;

	// operation status
	private boolean opStatus = false;

	// error message
	private String errorMessage = null;

	/**
	 * Set forced flag for the entry to be pushed.
	 * 
	 * @param forced
	 *            forced flag for the entry to be pushed.
	 */
	public void setForced(boolean forced) {
		this.forced = forced;
	}

	/**
	 * Get forced flag for PUT operation.
	 * 
	 * @return forced flag for the entry.
	 */
	public boolean getForced() {
		return this.forced;
	}

	/**
	 * set the key for the entry to be pushed.
	 * 
	 * @param key
	 *            the key for the entry to be pushed.
	 */
	public void setKey(byte[] key) {
		this.key = key;
	}

	/**
	 * 
	 * Get the key for the entry to be pushed.
	 * 
	 * @return the key for the entry to be pushed.
	 */
	public byte[] getKey() {
		return this.key;
	}

	/**
	 * Set db version for the entry to be pushed.
	 * 
	 * @param dbVersion
	 *            db version for the entry to be pushed.
	 */
	public void setVersion(byte[] dbVersion) {
		this.dbVersion = dbVersion;
	}

	/**
	 * 
	 * Get db version for the entry to be pushed.
	 * 
	 * @return db version for the entry to be pushed.
	 */
	public byte[] getVersion() {
		return this.dbVersion;
	}

	/**
	 * Set new key for the entry to be pushed.
	 * 
	 * @param newKey
	 *            new key for the entry to be pushed.
	 */
	public void setNewKey(byte[] newKey) {
		this.newKey = newKey;
	}

	/**
	 * Get new key for the entry to be pushed.
	 * 
	 * @return new key for the entry to be pushed.
	 */
	public byte[] getNewKey() {
		return this.newKey;
	}

	/**
	 * Get status for this operation.
	 * 
	 * @return status for this operation.
	 */
	public boolean getStatus() {
		return this.opStatus;
	}

	/**
	 * Set status for this operation. Set by the simulator/drive.
	 * 
	 * @param status
	 *            status for this operation.
	 */
	public void setStatus(boolean status) {
		this.opStatus = status;
	}

	/**
	 * Get error message for the operation. Set by simulator/drive.
	 * 
	 * @return error message for the operation.
	 */
	public String getErrorMessage() {
		return this.errorMessage;
	}

	/**
	 * Set error message for this operation. Set by simulator/drive.
	 * 
	 * @param errorMessage
	 *            error message for this operation.
	 */
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
}
