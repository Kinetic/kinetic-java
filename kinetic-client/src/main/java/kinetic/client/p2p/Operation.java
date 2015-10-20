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

import com.seagate.kinetic.proto.Kinetic.Command.Status.StatusCode;

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

    private StatusCode statusCode = StatusCode.SUCCESS;

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

    /**
     * Get status code for this operation.
     * 
     * @return status code for this operation
     */
    public StatusCode getStatusCode() {
        return this.statusCode;
    }

    /**
     * Set status code for this operation.
     * 
     * @param statusCode
     *            status code for this operation
     */
    public void setStatusCode(StatusCode statusCode) {
        this.statusCode = statusCode;
    }

}
