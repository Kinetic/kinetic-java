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
package com.seagate.kinetic.common.lib;

import com.seagate.kinetic.proto.Kinetic.CommandOrBuilder;
import com.seagate.kinetic.proto.Kinetic.MessageOrBuilder;

/**
 * A data container that holds a Kinetic protocol buffer message and an optional
 * byte[] value.
 * <p>
 * Please note that instances of this class does not provide synchronization and
 * thus API user must provide synchronization facility for the concurrent
 * operations.
 * <p>
 *
 * @author chiaming
 *
 */
public class KineticMessage {

	// protocol buffer message
	private MessageOrBuilder message = null;

	// optional value
	private byte[] value = null;
	
	// command
	private CommandOrBuilder command = null;
	
	// set to true if traveling through TLS/SSL
	private volatile boolean isSecuredChannel = false;  

    // set to true if this is a batch message
    private volatile boolean isBatchMessage = false;

    // set to true if this is the first message in the batch
    private volatile boolean isFirstBatchMessage = false;

    // set to true if this message is marked as invalid batch
    private volatile boolean isInvalidBatchMessage = false;

    // set to true if request is not valid
    private volatile boolean isInValidRequest = false;

    // error message
    private String errorMsg = null;

	/**
	 * Set protocol buffer message.
	 *
	 * @param message
	 *            message to be set in this instance.
	 */
	public void setMessage(MessageOrBuilder message) {
		this.message = message;
	}

	/**
	 * Get protocol buffer message.
	 *
	 * @return protocol buffer message in this instance.
	 */
	public MessageOrBuilder getMessage() {
		return this.message;
	}

	/**
	 * Set value to this message instance.
	 *
	 * @param value
	 *            value to bes et into this message instance.
	 */
	public void setValue(byte[] value) {
		this.value = value;
	}

	/**
	 * Get value from this message instance.
	 *
	 * @return value from this message instance.
	 */
	public byte[] getValue() {
		return this.value;
	}
	
	/**
	 * set command for this message instance
	 */
	public void setCommand (CommandOrBuilder command) {
	    this.command = command;
	}
	
	/**
	 * get command of this instance.
	 * 
	 * @return command of this instance.
	 */
	public CommandOrBuilder getCommand() {
	    return this.command;
	}
	
	/**
	 * set if this message travels through SSL
	 * 
	 * @param flag true if TLS, otherwise set to false
	 */
	public void setIsSecureChannel(boolean flag) {
	    this.isSecuredChannel = flag;
	}
	
	/**
	 * Get if this message travels through SSL.
	 * 
	 * @return true if this message travels through SSL.
	 */
	public boolean getIsSecureChannel() {
	    return this.isSecuredChannel;
	}

    /**
     * Get if this message is a batch message.
     * 
     * @return true if this is a batch message
     */
    public boolean getIsBatchMessage() {
        return this.isBatchMessage;
    }

    /**
     * Set if this message is a batch message.
     * 
     * @param flag
     *            true if this is a batch message.
     */
    public void setIsBatchMessage(boolean flag) {
        this.isBatchMessage = flag;
    }

    /**
     * Get if this message is the first batch message.
     * 
     * @return true if this is a batch message
     */
    public boolean getIsFirstBatchMessage() {
        return this.isFirstBatchMessage;
    }

    /**
     * Set if this message is the first batch message.
     * 
     * @param flag
     *            true if this is a batch message.
     */
    public void setIsFirstBatchMessage(boolean flag) {
        this.isFirstBatchMessage = flag;
    }

    /**
     * Get if this message is not a valid batch message
     * 
     * @return true if this is NOT a valid batch message
     */
    public boolean getIsInvalidBatchMessage() {
        return this.isInvalidBatchMessage;
    }

    /**
     * Set if this message is an invalid batch message.
     * 
     * @param flag
     *            true if this is an invalid batch message.
     */
    public void setIsInvalidBatchMessage(boolean flag) {
        this.isInvalidBatchMessage = flag;
    }

    /**
     * Set if this message is an invalid kinetic message.
     * 
     * @param flag
     *            true if this is an invalid kinetic message.
     */
    public void setIsInvalidRequest(boolean flag) {
        this.isInValidRequest = flag;
    }

    /**
     * get if this message is an invalid kinetic message.
     * 
     * @return true if invalid message.
     */
    public boolean getIsInvalidRequest() {
        return this.isInValidRequest;
    }

    /**
     * set error message for this message.
     * 
     * @param msg
     *            the error message to be set.
     */
    public void setErrorMessage(String msg) {
        this.errorMsg = msg;
    }

    /**
     * Get error message for this message.
     * 
     * @return error message for this message.
     */
    public String getErrorMessage() {
        return this.errorMsg;
    }

}
